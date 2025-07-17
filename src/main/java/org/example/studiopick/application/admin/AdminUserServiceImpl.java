package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.studio.AdminPaginationResponse;
import org.example.studiopick.application.admin.dto.user.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final JpaUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PaginationValidator paginationValidator;
  private final SystemSettingUtils settingUtils;

  /**
   * 사용자 계정 목록 조회 (페이징, 필터링)
   */
  public AdminUserListResponse getUserAccounts(int page, Integer size, String role, String status, String keyword) {
    // 입력값 검증
    int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);
    paginationValidator.validatePaginationParameters(page, pageSize);
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<User> usersPage;

    // 필터링 조건에 따른 조회
    if (role != null && status != null && keyword != null && !keyword.trim().isEmpty()) {
      UserRole userRole = parseUserRole(role);
      UserStatus userStatus = parseUserStatus(status);
      usersPage = userRepository.findByRoleAndStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
          userRole, userStatus, keyword.trim(), pageable);
    } else if (role != null && keyword != null && !keyword.trim().isEmpty()) {
      UserRole userRole = parseUserRole(role);
      usersPage = userRepository.findByRoleAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
          userRole, keyword.trim(), pageable);
    } else if (status != null && keyword != null && !keyword.trim().isEmpty()) {
      UserStatus userStatus = parseUserStatus(status);
      usersPage = userRepository.findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
          userStatus, keyword.trim(), pageable);
    } else if (role != null) {
      UserRole userRole = parseUserRole(role);
      usersPage = userRepository.findByRoleOrderByCreatedAtDesc(userRole, pageable);
    } else if (status != null) {
      UserStatus userStatus = parseUserStatus(status);
      usersPage = userRepository.findByStatusOrderByCreatedAtDesc(userStatus, pageable);
    } else if (keyword != null && !keyword.trim().isEmpty()) {
      usersPage = userRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(
          keyword.trim(), pageable);
    } else {
      usersPage = userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    List<AdminUserResponse> users = usersPage.getContent()
        .stream()
        .map(this::toAdminUserResponse)
        .toList();

    return new AdminUserListResponse(
        users,
        new AdminPaginationResponse(page, usersPage.getTotalElements(), usersPage.getTotalPages())
    );
  }

  /**
   * 사용자 계정 상세 조회
   */
  public AdminUserDetailResponse getUserAccount(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    return toAdminUserDetailResponse(user);
  }

  /**
   * 사용자 계정 생성
   */
  @Transactional
  public AdminUserCreateResponse createUserAccount(AdminUserCreateCommand command) {
    // 이메일 중복 확인
    if (userRepository.existsByEmail(command.email())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 휴대폰 중복 확인
    if (userRepository.existsByPhone(command.phone())) {
      throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
    }

    // 닉네임 중복 확인
    if (userRepository.existsByNickname(command.nickname())) {
      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
    }

    try {
      // 사용자 계정 생성
      User user = User.builder()
          .email(command.email())
          .password(passwordEncoder.encode(command.password()))
          .name(command.name())
          .phone(command.phone())
          .nickname(command.nickname())
          .role(parseUserRole(command.role()))
          .status(UserStatus.ACTIVE)
          .isStudioOwner(parseUserRole(command.role()) == UserRole.STUDIO_OWNER)
          .build();

      User savedUser = userRepository.save(user);

      log.info("사용자 계정 생성 완료: userId={}, email={}", savedUser.getId(), command.email());

      return new AdminUserCreateResponse(
          savedUser.getId(),
          savedUser.getName(),
          savedUser.getEmail(),
          savedUser.getRole().getValue(),
          savedUser.getStatus().getValue(),
          savedUser.getCreatedAt()
      );

    } catch (Exception e) {
      log.error("사용자 계정 생성 실패: email={}, error={}", command.email(), e.getMessage());
      throw new RuntimeException("사용자 계정 생성에 실패했습니다.", e);
    }
  }

  /**
   * 사용자 계정 수정
   */
  @Transactional
  public AdminUserUpdateResponse updateUserAccount(Long userId, AdminUserUpdateCommand command) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    try {
      // 사용자 정보 업데이트
      if (command.name() != null && !command.name().trim().isEmpty()) {
        user.updateName(command.name());
      }

      if (command.phone() != null && !command.phone().trim().isEmpty()) {
        // 휴대폰 중복 확인 (본인 제외)
        if (!user.getPhone().equals(command.phone()) &&
            userRepository.existsByPhone(command.phone())) {
          throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
        }
        user.updatePhone(command.phone());
      }

      if (command.nickname() != null && !command.nickname().trim().isEmpty()) {
        // 닉네임 중복 확인 (본인 제외)
        if (!user.getNickname().equals(command.nickname()) &&
            userRepository.existsByNickname(command.nickname())) {
          throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        user.updateProfile(user.getName(), user.getPhone(), command.nickname());
      }

      // 비밀번호 변경 (선택적)
      if (command.newPassword() != null && !command.newPassword().trim().isEmpty()) {
        user.updatePassword(passwordEncoder.encode(command.newPassword()));
      }

      userRepository.save(user);

      log.info("사용자 계정 수정 완료: userId={}", userId);

      return new AdminUserUpdateResponse(
          user.getId(),
          user.getName(),
          user.getEmail(),
          user.getUpdatedAt()
      );

    } catch (Exception e) {
      log.error("사용자 계정 수정 실패: userId={}, error={}", userId, e.getMessage());
      throw e;
    }
  }

  /**
   * 사용자 계정 상태 변경 (활성화/비활성화/잠금)
   */
  @Transactional
  public AdminUserStatusResponse changeUserStatus(Long userId, AdminUserStatusCommand command) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    UserStatus oldStatus = user.getStatus();
    UserStatus newStatus = parseUserStatus(command.status());

    try {
      // 사용자 상태 변경
      switch (newStatus) {
        case ACTIVE -> user.activate();
        case INACTIVE -> user.deactivate();
        case LOCKED -> user.changeStatus(UserStatus.LOCKED);
      }

      userRepository.save(user);

      log.info("사용자 상태 변경 완료: userId={}, {} -> {}, reason={}",
          userId, oldStatus, newStatus, command.reason());

      return new AdminUserStatusResponse(
          user.getId(),
          user.getName(),
          oldStatus.getValue(),
          newStatus.getValue(),
          command.reason(),
          LocalDateTime.now()
      );

    } catch (Exception e) {
      log.error("사용자 상태 변경 실패: userId={}, error={}", userId, e.getMessage());
      throw new RuntimeException("사용자 상태 변경에 실패했습니다.", e);
    }
  }

  /**
   * 사용자 역할 변경 (USER ↔ STUDIO_OWNER)
   */
  @Transactional
  public AdminUserRoleResponse changeUserRole(Long userId, AdminUserRoleCommand command) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 관리자 계정은 역할 변경 불가
    if (user.getRole() == UserRole.ADMIN) {
      throw new IllegalArgumentException("관리자 계정의 역할은 변경할 수 없습니다.");
    }

    UserRole oldRole = user.getRole();
    UserRole newRole = parseUserRole(command.role());

    try {
      // 역할 변경
      user.changeRole(newRole);

      userRepository.save(user);

      log.info("사용자 역할 변경 완료: userId={}, {} -> {}, reason={}",
          userId, oldRole, newRole, command.reason());

      return new AdminUserRoleResponse(
          user.getId(),
          user.getName(),
          oldRole.getValue(),
          newRole.getValue(),
          command.reason(),
          LocalDateTime.now()
      );

    } catch (Exception e) {
      log.error("사용자 역할 변경 실패: userId={}, error={}", userId, e.getMessage());
      throw new RuntimeException("사용자 역할 변경에 실패했습니다.", e);
    }
  }

  /**
   * 사용자 계정 삭제 (소프트 삭제)
   */
  @Transactional
  public void deleteUserAccount(Long userId, String reason) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 관리자 계정은 삭제 불가
    if (user.getRole() == UserRole.ADMIN) {
      throw new IllegalArgumentException("관리자 계정은 삭제할 수 없습니다.");
    }

    try {
      // 사용자 비활성화 (소프트 삭제)
      user.changeStatus(UserStatus.LOCKED);

      userRepository.save(user);

      log.info("사용자 계정 삭제 완료: userId={}, reason={}", userId, reason);

    } catch (Exception e) {
      log.error("사용자 계정 삭제 실패: userId={}, error={}", userId, e.getMessage());
      throw new RuntimeException("사용자 계정 삭제에 실패했습니다.", e);
    }
  }

  /**
   * 사용자 통계 조회
   */
  public AdminUserStatsResponse getUserStats() {
    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
    long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);
    long lockedUsers = userRepository.countByStatus(UserStatus.LOCKED);

    long regularUsers = userRepository.countByRole(UserRole.USER);
    long studioOwners = userRepository.countByRole(UserRole.STUDIO_OWNER);
    long admins = userRepository.countByRole(UserRole.ADMIN);

    return new AdminUserStatsResponse(
        totalUsers,
        activeUsers,
        inactiveUsers,
        lockedUsers,
        regularUsers,
        studioOwners,
        admins
    );
  }

  // Private helper methods

  private UserRole parseUserRole(String role) {
    try {
      return UserRole.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("잘못된 사용자 역할입니다: " + role);
    }
  }

  private UserStatus parseUserStatus(String status) {
    try {
      return UserStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("잘못된 사용자 상태입니다: " + status);
    }
  }

  private AdminUserResponse toAdminUserResponse(User user) {
    return new AdminUserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getNickname(),
        user.getRole().getValue(),
        user.getStatus().getValue(),
        user.getEmailVerified(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }

  private AdminUserDetailResponse toAdminUserDetailResponse(User user) {
    return new AdminUserDetailResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getNickname(),
        user.getRole().getValue(),
        user.getStatus().getValue(),
        user.getEmailVerified(),
        user.getIsStudioOwner(),
        user.getLoginFailCount(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }
}