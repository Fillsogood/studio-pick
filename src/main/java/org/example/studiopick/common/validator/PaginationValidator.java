package org.example.studiopick.common.validator;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.exception.InvalidParameterException;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaginationValidator {

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
}