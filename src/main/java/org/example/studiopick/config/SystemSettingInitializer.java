package org.example.studiopick.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.infrastructure.setting.JpaSystemSettingRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "system.setting.validation.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SystemSettingInitializer {

  private final JpaSystemSettingRepository settingRepository;

  @Bean
  @Order(1)
  public ApplicationRunner validateCriticalSettings() {
    return args -> {
      List<String> criticalSettings = List.of(
          "platform.commission.rate",
          "reservation.cancel.hours",
          "pagination.default.size",
          "maintenance.mode"
      );

      for (String settingKey : criticalSettings) {
        if (!settingRepository.existsBySettingKey(settingKey)) {
          log.error("Critical system setting missing: {}", settingKey);
          throw new IllegalStateException("Required system setting not found: " + settingKey);
        }
      }

      log.info("All critical system settings validated successfully");
    };
  }
}