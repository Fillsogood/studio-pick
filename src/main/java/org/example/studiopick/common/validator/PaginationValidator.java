package org.example.studiopick.common.validator;

import org.example.studiopick.common.exception.InvalidParameterException;
import org.springframework.stereotype.Component;

@Component
public class PaginationValidator {

  public void validatePaginationParameters(int page, int size) {
    if (page <= 0) {
      throw new InvalidParameterException("페이지 번호는 1 이상이어야 합니다.");
    }
    if (size <= 0 || size > 100) {
      throw new InvalidParameterException("페이지 크기는 1-100 사이여야 합니다.");
    }
  }
}