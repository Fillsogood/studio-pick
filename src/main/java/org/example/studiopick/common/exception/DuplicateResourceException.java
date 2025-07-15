package org.example.studiopick.common.exception;

import org.example.studiopick.common.enums.ErrorCode;

public class DuplicateResourceException extends BusinessLogicException {
    public DuplicateResourceException(String resource) {
        super(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 " + resource + "입니다");
    }
    
    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
