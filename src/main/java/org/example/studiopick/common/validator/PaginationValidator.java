package org.example.studiopick.common.validator;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.exception.InvalidParameterException;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaginationValidator {


  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;
  private final SystemSettingUtils settingUtils;

  public void validatePaginationParameters(int page, int size) {
    if (page < 1) {
      throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
    }

    // 동적으로 최대 사이즈 조회
    int maxSize = settingUtils.getIntegerSetting("pagination.max.size", 100);

    if (size < 1 || size > maxSize) {
      throw new IllegalArgumentException("페이지 크기는 1 이상 " + maxSize + " 이하여야 합니다.");
    }
  }

  public int getDefaultPageSize() {
    return settingUtils.getIntegerSetting("pagination.default.size", 10);
  }

  public int getMaxPageSize() {
    return settingUtils.getIntegerSetting("pagination.max.size", 100);
  }

  public int validatePage(Integer page) {
    if (page == null || page < 1) {
      return DEFAULT_PAGE;
    }
    return page - 1;  // Spring Data JPA는 0부터 시작
  }

  public int validateSize(Integer size) {
    if (size == null || size < 1) {
      return DEFAULT_SIZE;
    }
    return Math.min(size, MAX_SIZE);
  }

}