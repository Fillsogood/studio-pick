package org.example.studiopick.common.exception.auth;

import org.example.studiopick.common.enums.ErrorCode;
import org.example.studiopick.common.exception.BusinessLogicException;

public class AuthenticationFailedException extends BusinessLogicException {
    public AuthenticationFailedException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }
    
    public AuthenticationFailedException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }
}
