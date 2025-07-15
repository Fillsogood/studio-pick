package org.example.studiopick.common.exception.auth;

import org.example.studiopick.common.enums.ErrorCode;
import org.example.studiopick.common.exception.BusinessLogicException;

public class TokenException extends BusinessLogicException {
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public TokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
