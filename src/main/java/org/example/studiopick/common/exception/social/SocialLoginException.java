package org.example.studiopick.common.exception.social;

import lombok.Getter;
import org.example.studiopick.common.enums.ErrorCode;
import org.example.studiopick.common.exception.BusinessLogicException;

@Getter
public class SocialLoginException extends BusinessLogicException {
    private final String provider;
    
    public SocialLoginException(String provider, String message) {
        super(ErrorCode.SOCIAL_LOGIN_FAILED, message);
        this.provider = provider;
    }
    
    public SocialLoginException(String provider, ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.provider = provider;
    }
}
