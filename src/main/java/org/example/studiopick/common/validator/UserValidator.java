package org.example.studiopick.common.validator;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.exception.InvalidParameterException;
import org.example.studiopick.common.exception.UserNotFoundException;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

  private final UserRepository userRepository;

  public void validateUserId(Long userId) {
    if (userId == null || userId <= 0) {
      throw new InvalidParameterException("유효하지 않은 사용자 ID입니다.");
    }
  }

  public User findAndValidateUser(Long userId) {
    validateUserId(userId);  // 기존 검증 재사용
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }
}