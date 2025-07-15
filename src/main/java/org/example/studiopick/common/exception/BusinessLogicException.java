package org.example.studiopick.common.exception;

import lombok.Getter;
import org.example.studiopick.common.enums.ErrorCode;

@Getter
public class BusinessLogicException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessLogicException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessLogicException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
