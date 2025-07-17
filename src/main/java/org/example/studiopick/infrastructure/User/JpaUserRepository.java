package org.example.studiopick.infrastructure.User;

import org.example.studiopick.domain.common.enums.SocialProvider;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
  // 일반 로그인 및 중복검사
  Optional<User> findByEmail(String email);
  Optional<User> findByPhone(String phone);
  Optional<User> findByNickname(String nickname);
  boolean existsByEmail(String email);
  boolean existsByPhone(String phone);
  boolean existsByNickname(String nickname);

  // 닉네임 중복 검사 (특정 사용자 제외)
  boolean existsByNicknameAndIdNot(String nickname, Long id);

  // 소셜 로그인용
  Optional<User> findBySocialAccountsProviderAndSocialAccountsSocialId(SocialProvider provider, String socialId);

  // 페이징 조회용
  Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
  Page<User> findByRoleOrderByCreatedAtDesc(UserRole role, Pageable pageable);
  Page<User> findByStatusOrderByCreatedAtDesc(UserStatus status, Pageable pageable);
  Page<User> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);

  // 통계용
  long countByRole(UserRole role);
  long countByStatus(UserStatus status);

  // 복합 조건 검색용 (AdminUserService에서 사용)
  Page<User> findByRoleAndStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
      UserRole role, UserStatus status, String name, Pageable pageable);

  Page<User> findByRoleAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
      UserRole role, String name, Pageable pageable);

  Page<User> findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
      UserStatus status, String name, Pageable pageable);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
