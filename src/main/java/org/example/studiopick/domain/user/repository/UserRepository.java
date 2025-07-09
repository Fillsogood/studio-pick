package org.example.studiopick.domain.user.repository;

import org.example.studiopick.domain.common.enums.SocialProvider;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository {

  // 일반 로그인 및 중복검사
  Optional<User> findByEmail(String email);
  Optional<User> findByPhone(String phone);
  Optional<User> findByNickname(String nickname);
  boolean existsByEmail(String email);
  boolean existsByPhone(String phone);

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

  // 추가 중복 확인
  boolean existsByNickname(String nickname);
}
