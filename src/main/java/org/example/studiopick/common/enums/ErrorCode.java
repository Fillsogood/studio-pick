package org.example.studiopick.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 인증 관련
    AUTHENTICATION_FAILED("AUTH001", "인증에 실패했습니다"),
    INVALID_TOKEN("AUTH002", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED("AUTH003", "토큰이 만료되었습니다"),
    UNAUTHORIZED_ACCESS("AUTH004", "접근 권한이 없습니다"),
    
    // 사용자 관련
    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("USER002", "이미 사용 중인 이메일입니다"),
    DUPLICATE_PHONE("USER003", "이미 사용 중인 휴대폰 번호입니다"),
    DUPLICATE_NICKNAME("USER004", "이미 사용 중인 닉네임입니다"),
    INVALID_PASSWORD("USER005", "비밀번호가 일치하지 않습니다"),
    
    // 소셜 로그인 관련
    SOCIAL_LOGIN_FAILED("SOCIAL001", "소셜 로그인에 실패했습니다"),
    OAUTH_TOKEN_REQUEST_FAILED("SOCIAL002", "OAuth 토큰 요청에 실패했습니다"),
    OAUTH_USER_INFO_REQUEST_FAILED("SOCIAL003", "사용자 정보 조회에 실패했습니다"),
    UNSUPPORTED_SOCIAL_PROVIDER("SOCIAL004", "지원하지 않는 소셜 로그인 제공자입니다"),
    
    // 시스템 관련
    INTERNAL_SERVER_ERROR("SYS001", "서버 내부 오류가 발생했습니다"),
    EXTERNAL_API_ERROR("SYS002", "외부 API 호출 중 오류가 발생했습니다"),
    NETWORK_ERROR("SYS003", "네트워크 오류가 발생했습니다"),
    
    // 공통
    INVALID_PARAMETER("COMMON001", "잘못된 파라미터입니다"),
    DUPLICATE_RESOURCE("COMMON002", "중복된 리소스입니다"),
    RESOURCE_NOT_FOUND("COMMON003", "리소스를 찾을 수 없습니다");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
