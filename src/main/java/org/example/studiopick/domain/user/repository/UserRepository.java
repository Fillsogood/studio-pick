package org.example.studiopick.domain.user.repository;

import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  Optional<User> findByPhone(String phone);
  Optional<User> findByNickname(String nickname);

  boolean existsByEmail(String email);
}
