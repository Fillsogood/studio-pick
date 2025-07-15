package org.example.studiopick.common.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.common.dto.ErrorResponse;
import org.example.studiopick.common.enums.ErrorCode;
import org.example.studiopick.common.exception.social.SocialLoginException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 비즈니스 로직 예외
  @ExceptionHandler(BusinessLogicException.class)
  public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException e, HttpServletRequest request) {
    log.warn("Business logic error: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(e.getErrorCode().getCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.badRequest().body(response);
  }
  
  // 소셜 로그인 예외 (특별 처리)
  @ExceptionHandler(SocialLoginException.class)
  public ResponseEntity<ErrorResponse> handleSocialLoginException(SocialLoginException e, HttpServletRequest request) {
    log.error("Social login error - provider: {}, message: {}", e.getProvider(), e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(e.getErrorCode().getCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .provider(e.getProvider())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.badRequest().body(response);
  }
  
  // Spring Security 인증 예외
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
    log.warn("Authentication failed: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.AUTHENTICATION_FAILED.getCode())
            .message("이메일 또는 비밀번호가 올바르지 않습니다")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
  
  // JWT 관련 예외
  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException e, HttpServletRequest request) {
    log.warn("JWT error: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.INVALID_TOKEN.getCode())
            .message("토큰이 유효하지 않습니다")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
  
  // 네트워크 관련 예외
  @ExceptionHandler({ResourceAccessException.class, ConnectException.class})
  public ResponseEntity<ErrorResponse> handleNetworkException(Exception e, HttpServletRequest request) {
    log.error("Network error: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.NETWORK_ERROR.getCode())
            .message("네트워크 연결에 문제가 발생했습니다")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }
  
  // Validation 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
    String errorMessage = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
            
    log.warn("Validation error: {}", errorMessage);
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.INVALID_PARAMETER.getCode())
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.badRequest().body(response);
  }
  
  // 중복 이메일/닉네임 등 비즈니스 로직 에러 (하위 호환성)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
    log.warn("Illegal argument: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.INVALID_PARAMETER.getCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.badRequest().body(response);
  }
  
  // 잘못된 파라미터 (하위 호환성)
  @ExceptionHandler(InvalidParameterException.class)
  public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException e, HttpServletRequest request) {
    log.warn("Invalid parameter: {}", e.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.INVALID_PARAMETER.getCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.badRequest().body(response);
  }
  
  // 예상치 못한 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    
    ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message("서버 내부 오류가 발생했습니다")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
