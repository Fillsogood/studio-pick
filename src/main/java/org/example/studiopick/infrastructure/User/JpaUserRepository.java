package org.example.studiopick.infrastructure.User;

import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
}
