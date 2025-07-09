package org.example.studiopick.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.domain.setting.SystemSetting;
import org.example.studiopick.infrastructure.setting.JpaSystemSettingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemSettingUtils {

  private final JpaSystemSettingRepository settingRepository;

  @Cacheable(value = "systemSettings", key = "#settingKey")
  public String getStringSetting(String settingKey, String defaultValue) {
    return settingRepository.findBySettingKey(settingKey)
        .map(SystemSetting::getValueOrDefault)
        .orElse(defaultValue);
  }

  @Cacheable(value = "systemSettings", key = "#settingKey")
  public Integer getIntegerSetting(String settingKey, Integer defaultValue) {
    try {
      return settingRepository.findBySettingKey(settingKey)
          .map(SystemSetting::getValueOrDefault)
          .map(Integer::parseInt)
          .orElse(defaultValue);
    } catch (NumberFormatException e) {
      log.warn("Failed to parse integer setting: {} = {}", settingKey,
          settingRepository.findBySettingKey(settingKey)
              .map(SystemSetting::getValueOrDefault).orElse("null"));
      return defaultValue;
    }
  }

  @Cacheable(value = "systemSettings", key = "#settingKey")
  public BigDecimal getDecimalSetting(String settingKey, BigDecimal defaultValue) {
    try {
      return settingRepository.findBySettingKey(settingKey)
          .map(SystemSetting::getValueOrDefault)
          .map(BigDecimal::new)
          .orElse(defaultValue);
    } catch (NumberFormatException e) {
      log.warn("Failed to parse decimal setting: {} = {}", settingKey,
          settingRepository.findBySettingKey(settingKey)
              .map(SystemSetting::getValueOrDefault).orElse("null"));
      return defaultValue;
    }
  }

  @Cacheable(value = "systemSettings", key = "#settingKey")
  public Boolean getBooleanSetting(String settingKey, Boolean defaultValue) {
    return settingRepository.findBySettingKey(settingKey)
        .map(SystemSetting::getValueOrDefault)
        .map(value -> "true".equalsIgnoreCase(value) || "1".equals(value))
        .orElse(defaultValue);
  }

  // 캐시 무효화를 위한 메서드
  @org.springframework.cache.annotation.CacheEvict(value = "systemSettings", key = "#settingKey")
  public void evictSettingCache(String settingKey) {
    log.debug("Setting cache evicted for key: {}", settingKey);
  }

  @org.springframework.cache.annotation.CacheEvict(value = "systemSettings", allEntries = true)
  public void evictAllSettingCache() {
    log.debug("All setting cache evicted");
  }
}