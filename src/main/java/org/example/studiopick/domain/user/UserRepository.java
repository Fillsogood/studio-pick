package org.example.studiopick.domain.user;


import java.util.Optional;

public interface UserRepository {
  public Optional<User> findById(Long id);
}
