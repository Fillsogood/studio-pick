package org.example.studiopick.common.exception;

import org.example.studiopick.common.enums.ErrorCode;

public class UserNotFoundException extends BusinessLogicException {
  public UserNotFoundException(String message) {
    super(ErrorCode.USER_NOT_FOUND, message);
  }

  public UserNotFoundException(Long userId) {
    super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: " + userId);
  }
}