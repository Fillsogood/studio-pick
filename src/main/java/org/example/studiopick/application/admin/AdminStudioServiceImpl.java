package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.studio.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
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
public class AdminStudioServiceImpl implements AdminStudioService {
  private final JpaStudioRepository jpaStudioRepository;
  private final JpaUserRepository jpaUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final PaginationValidator paginationValidator;
  private final SystemSettingUtils settingUtils;

  /**
   * 스튜디오 계정 목록 조회 (페이징, 필터링)
   */
  public AdminStudioListResponse getStudioAccounts(int page, Integer size, String status, String keyword) {
    // 입력값 검증
    int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);
    paginationValidator.validatePaginationParameters(page, pageSize);
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Studio> studiosPage;

    // 필터링 조건에 따른 조회
    if (status != null && keyword != null && !keyword.trim().isEmpty()) {
      StudioStatus studioStatus = parseStudioStatus(status);
      studiosPage = jpaStudioRepository.findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
          studioStatus, keyword.trim(), pageable);
    } else if (status != null) {
      StudioStatus studioStatus = parseStudioStatus(status);
      studiosPage = jpaStudioRepository.findByStatusOrderByCreatedAtDesc(studioStatus, pageable);
    } else if (keyword != null && !keyword.trim().isEmpty()) {
      studiosPage = jpaStudioRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(
          keyword.trim(), pageable);
    } else {
      studiosPage = jpaStudioRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    List<AdminStudioResponse> studios = studiosPage.getContent()
        .stream()
        .map(this::toAdminStudioResponse)
        .toList();

    return new AdminStudioListResponse(
        studios,
        new AdminPaginationResponse(page, studiosPage.getTotalElements(), studiosPage.getTotalPages())
    );
  }

  /**
   * 스튜디오 계정 상세 조회
   */
  public AdminStudioDetailResponse getStudioAccount(Long studioId) {
    Studio studio = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    return toAdminStudioDetailResponse(studio);
  }

  /**
   * 스튜디오 계정 생성
   */
  @Transactional
  public AdminStudioCreateResponse createStudioAccount(AdminStudioCreateCommand command) {
    // 이메일 중복 확인
    if (jpaUserRepository.existsByEmail(command.email())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 스튜디오명 중복 확인
    if (jpaStudioRepository.existsByName(command.studioName())) {
      throw new IllegalArgumentException("이미 사용 중인 스튜디오명입니다.");
    }

    try {
      // 1. 사용자 계정 생성 (스튜디오 소유자)
      User studioOwner = User.builder()
          .email(command.email())
          .password(passwordEncoder.encode(command.password()))
          .name(command.ownerName())
          .phone(command.phone())
          .nickname(command.ownerName() + "_studio") // 임시 닉네임
          .role(UserRole.STUDIO_OWNER)
          .status(UserStatus.ACTIVE)
          .isStudioOwner(true)
          .build();

      User savedUser = jpaUserRepository.save(studioOwner);

      // 2. 스튜디오 생성
      Studio studio = Studio.builder()
          .name(command.studioName())
          .description(command.description())
          .location(command.address())
          .phone(command.studioPhone())
          .hourlyBaseRate(command.hourlyBaseRate())
          .perPersonRate(command.perPersonRate())
          .owner(savedUser)
          .status(StudioStatus.PENDING) // 관리자 승인 대기
          .build();

      Studio savedStudio = jpaStudioRepository.save(studio);

      log.info("스튜디오 계정 생성 완료: studioId={}, email={}", savedStudio.getId(), command.email());

      return new AdminStudioCreateResponse(
          savedStudio.getId(),
          savedStudio.getName(),
          savedUser.getEmail(),
          savedStudio.getStatus().getValue(),
          savedStudio.getCreatedAt()
      );

    } catch (Exception e) {
      log.error("스튜디오 계정 생성 실패: email={}, error={}", command.email(), e.getMessage());
      throw new RuntimeException("스튜디오 계정 생성에 실패했습니다.", e);
    }
  }

  /**
   * 스튜디오 계정 수정
   */
  @Transactional
  public AdminStudioUpdateResponse updateStudioAccount(Long studioId, AdminStudioUpdateCommand command) {
    Studio studio = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    User owner = studio.getOwner();

    try {
      // 스튜디오 정보 업데이트
      if (command.studioName() != null && !command.studioName().trim().isEmpty()) {
        // 스튜디오명 중복 확인 (본인 제외)
        if (!studio.getName().equals(command.studioName()) &&
            jpaStudioRepository.existsByName(command.studioName())) {
          throw new IllegalArgumentException("이미 사용 중인 스튜디오명입니다.");
        }
        studio.updateName(command.studioName());
      }

      if (command.description() != null) {
        studio.updateDescription(command.description());
      }

      if (command.address() != null && !command.address().trim().isEmpty()) {
        studio.updateLocation(command.address());
      }

      if (command.studioPhone() != null && !command.studioPhone().trim().isEmpty()) {
        studio.updatePhone(command.studioPhone());
      }

      if (command.hourlyBaseRate() != null) {
        studio.updateHourlyBaseRate(command.hourlyBaseRate());
      }

      if (command.perPersonRate() != null) {
        studio.updatePerPersonRate(command.perPersonRate());
      }

      // 소유자 정보 업데이트
      if (command.ownerName() != null && !command.ownerName().trim().isEmpty()) {
        owner.updateName(command.ownerName());
      }

      if (command.phone() != null && !command.phone().trim().isEmpty()) {
        owner.updatePhone(command.phone());
      }

      // 비밀번호 변경 (선택적)
      if (command.newPassword() != null && !command.newPassword().trim().isEmpty()) {
        owner.updatePassword(passwordEncoder.encode(command.newPassword()));
      }

      jpaStudioRepository.save(studio);
      jpaUserRepository.save(owner);

      log.info("스튜디오 계정 수정 완료: studioId={}", studioId);

      return new AdminStudioUpdateResponse(
          studio.getId(),
          studio.getName(),
          owner.getName(),
          studio.getUpdatedAt()
      );

    } catch (Exception e) {
      log.error("스튜디오 계정 수정 실패: studioId={}, error={}", studioId, e.getMessage());
      throw e;
    }
  }

  /**
   * 스튜디오 계정 상태 변경 (승인/거부/정지/활성화)
   */
  @Transactional
  public AdminStudioStatusResponse changeStudioStatus(Long studioId, AdminStudioStatusCommand command) {
    Studio studio = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    StudioStatus oldStatus = studio.getStatus();
    StudioStatus newStatus = parseStudioStatus(command.status());
    User owner = studio.getOwner(); // ✅ owner 변수 선언 추가

    try {
      // 스튜디오 상태 변경
      studio.updateStatus(newStatus);

      // 운영 타입별 사용자 권한 처리
      String grantedPermissions = processUserPermissionsByOperationType(owner, studio, newStatus);

      jpaStudioRepository.save(studio);
      jpaUserRepository.save(owner);

      log.info("스튜디오 상태 변경 완료: studioId={}, type={}, {} -> {}, reason={}",
          studioId, studio.getOperationType(), oldStatus, newStatus, command.reason());

      return new AdminStudioStatusResponse(
          studio.getId(),
          studio.getName(),
          oldStatus.getValue(),
          newStatus.getValue(),
          command.reason(),
          LocalDateTime.now()
      );

    } catch (Exception e) {
      log.error("스튜디오 상태 변경 실패: studioId={}, error={}", studioId, e.getMessage());
      throw new RuntimeException("스튜디오 상태 변경에 실패했습니다.", e);
    }
  }

  /**
   * 스튜디오 계정 삭제 (소프트 삭제)
   */
  @Transactional
  public void deleteStudioAccount(Long studioId, String reason) {
    Studio studio = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    try {
      // 스튜디오 비활성화
      studio.updateStatus(StudioStatus.SUSPENDED);

      // 소유자 계정도 비활성화
      User owner = studio.getOwner();
      owner.changeStatus(UserStatus.LOCKED);

      jpaStudioRepository.save(studio);
      jpaUserRepository.save(owner);

      log.info("스튜디오 계정 삭제 완료: studioId={}, reason={}", studioId, reason);

    } catch (Exception e) {
      log.error("스튜디오 계정 삭제 실패: studioId={}, error={}", studioId, e.getMessage());
      throw new RuntimeException("스튜디오 계정 삭제에 실패했습니다.", e);
    }
  }

  /**
   * 스튜디오 통계 조회
   */
  public AdminStudioStatsResponse getStudioStats() {
    long totalStudios = jpaStudioRepository.count();
    long approvedStudios = jpaStudioRepository.countByStatus(StudioStatus.APPROVED);
    long pendingStudios = jpaStudioRepository.countByStatus(StudioStatus.PENDING);
    long rejectedStudios = jpaStudioRepository.countByStatus(StudioStatus.REJECTED);
    long suspendedStudios = jpaStudioRepository.countByStatus(StudioStatus.SUSPENDED);

    return new AdminStudioStatsResponse(
        totalStudios,
        approvedStudios,
        rejectedStudios,
        pendingStudios,
        suspendedStudios
    );
  }

  /**
   * 운영 타입별 사용자 권한 처리
   */
  private String processUserPermissionsByOperationType(User owner, Studio studio, StudioStatus newStatus) {
    String grantedPermissions = "";
    
    switch (newStatus) {
      case APPROVED -> {
        // 승인 시 운영 타입별 권한 부여
        owner.promoteToStudioOwner(); // 기본 STUDIO_OWNER 권한 부여
        
        switch (studio.getOperationType()) {
          case SPACE_RENTAL -> {
            grantedPermissions = "공간 대여 운영 권한 (예약 관리, 요금 설정, 운영시간 관리)";
            log.info("공간 대여 권한 부여: userId={}, studioId={}", owner.getId(), studio.getId());
          }
          case CLASS_WORKSHOP -> {
            grantedPermissions = "공방 체험 운영 권한 (클래스 개설, 예약 관리, 강사 활동)";
            log.info("공방 체험 권한 부여: userId={}, studioId={}", owner.getId(), studio.getId());
          }
        }
        owner.activate();
      }
      case SUSPENDED -> {
        owner.changeStatus(UserStatus.LOCKED);
        grantedPermissions = "권한 정지 (서비스 이용 불가)";
      }
      case REJECTED -> {
        // 거부 시에는 권한 변경하지 않음 (일반 사용자 유지)
        grantedPermissions = "없음 (신청 거부)";
      }
    }
    
    return grantedPermissions;
  }

  // Private helper methods

  private StudioStatus parseStudioStatus(String status) {
    try {
      return StudioStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("잘못된 스튜디오 상태입니다: " + status);
    }
  }

  private AdminStudioResponse toAdminStudioResponse(Studio studio) {
    return new AdminStudioResponse(
        studio.getId(),
        studio.getName(),
        studio.getOwner().getName(),
        studio.getOwner().getEmail(),
        studio.getPhone(),
        studio.getStatus().getValue(),
        studio.getCreatedAt(),
        studio.getUpdatedAt()
    );
  }

  private AdminStudioDetailResponse toAdminStudioDetailResponse(Studio studio) {
    User owner = studio.getOwner();

    return new AdminStudioDetailResponse(
        studio.getId(),
        studio.getName(),
        studio.getDescription(),
        studio.getLocation(), // address 대신 location 사용
        studio.getPhone(),
        studio.getHourlyBaseRate(),
        studio.getPerPersonRate(),
        studio.getStatus().getValue(),
        new AdminStudioOwnerInfo(
            owner.getId(),
            owner.getName(),
            owner.getEmail(),
            owner.getPhone(),
            owner.getStatus().getValue()
        ),
        studio.getCreatedAt(),
        studio.getUpdatedAt()
    );
  }
}
