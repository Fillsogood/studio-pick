package org.example.studiopick.common.exception;

import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 사용자 없음
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
    ApiResponse<Void> response = new ApiResponse<>(false, null, e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  // 잘못된 파라미터
  @ExceptionHandler(InvalidParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidParameter(InvalidParameterException e) {
    ApiResponse<Void> response = new ApiResponse<>(false, null, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 중복 이메일/닉네임 등 비즈니스 로직 에러
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
    ApiResponse<Void> response = new ApiResponse<>(false, null, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 상태 변경 불가능 등 상태 관련 에러
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
    ApiResponse<Void> response = new ApiResponse<>(false, null, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // DTO @Valid 검증 실패 (비밀번호 형식 등)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("잘못된 입력입니다.");

    ApiResponse<Void> response = new ApiResponse<>(false, null, errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
