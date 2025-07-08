package org.example.studiopick.domain.user.repository;

import org.example.studiopick.domain.common.enums.SocialProvider;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  // 일반 로그인 및 중복검사
  Optional<User> findByEmail(String email);
  Optional<User> findByPhone(String phone);
  Optional<User> findByNickname(String nickname);
  boolean existsByEmail(String email);
  boolean existsByPhone(String phone);

  // 소셜 로그인용
  Optional<User> findBySocialAccountsProviderAndSocialAccountsSocialId(SocialProvider provider, String socialId);
}
