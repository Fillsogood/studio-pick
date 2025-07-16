package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.setting.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.setting.SystemSetting;
import org.example.studiopick.infrastructure.setting.JpaSystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSystemSettingServiceImpl implements AdminSystemSettingService {

  private final JpaSystemSettingRepository settingRepository;
  private final SystemSettingUtils settingUtils;

  /**
   * 전체 시스템 설정 조회
   */
  public SystemSettingListResponse getAllSettings() {
    List<SystemSetting> settings = settingRepository.findAllOrderByCategoryAndKey();

    List<SystemSettingResponse> settingResponses = settings.stream()
        .map(this::toSystemSettingResponse)
        .toList();

    return new SystemSettingListResponse(
        settingResponses,
        "ALL",
        settings.size()
    );
  }

  /**
   * 카테고리별 시스템 설정 조회
   */
  public SystemSettingListResponse getSettingsByCategory(String category) {
    List<SystemSetting> settings = settingRepository.findByCategoryOrderBySettingKey(category);

    List<SystemSettingResponse> settingResponses = settings.stream()
        .map(this::toSystemSettingResponse)
        .toList();

    return new SystemSettingListResponse(
        settingResponses,
        category,
        settings.size()
    );
  }

  /**
   * 특정 설정 조회
   */
  public SystemSettingResponse getSetting(String settingKey) {
    SystemSetting setting = settingRepository.findBySettingKey(settingKey)
        .orElseThrow(() -> new IllegalArgumentException("설정을 찾을 수 없습니다: " + settingKey));

    return toSystemSettingResponse(setting);
  }

  /**
   * 설정값 업데이트
   */
  @Transactional
  public SystemSettingResponse updateSetting(String settingKey, SystemSettingUpdateCommand command) {
    SystemSetting setting = settingRepository.findBySettingKey(settingKey)
        .orElseThrow(() -> new IllegalArgumentException("설정을 찾을 수 없습니다: " + settingKey));

    if (!setting.isEditable()) {
      throw new IllegalStateException("수정할 수 없는 설정입니다: " + settingKey);
    }

    // 데이터 타입 검증
    validateSettingValue(command.settingValue(), setting.getDataType());

    setting.updateValue(command.settingValue());
    if (command.description() != null) {
      setting.updateDescription(command.description());
    }

    SystemSetting savedSetting = settingRepository.save(setting);

    // 캐시 무효화
    settingUtils.evictSettingCache(settingKey);

    log.info("시스템 설정 업데이트 완료: {} = {}", settingKey, command.settingValue());

    return toSystemSettingResponse(savedSetting);
  }

  /**
   * 새 설정 생성
   */
  @Transactional
  public SystemSettingResponse createSetting(SystemSettingCreateCommand command) {
    if (settingRepository.existsBySettingKey(command.settingKey())) {
      throw new IllegalArgumentException("이미 존재하는 설정입니다: " + command.settingKey());
    }

    // 데이터 타입 검증
    validateSettingValue(command.settingValue(), command.dataType());

    SystemSetting setting = SystemSetting.builder()
        .settingKey(command.settingKey())
        .settingValue(command.settingValue())
        .description(command.description())
        .category(command.category())
        .dataType(command.dataType())
        .isEditable(command.isEditable() != null ? command.isEditable() : true)
        .defaultValue(command.defaultValue())
        .build();

    SystemSetting savedSetting = settingRepository.save(setting);

    log.info("새 시스템 설정 생성 완료: {} = {}", command.settingKey(), command.settingValue());

    return toSystemSettingResponse(savedSetting);
  }

  /**
   * 설정 삭제
   */
  @Transactional
  public void deleteSetting(String settingKey) {
    SystemSetting setting = settingRepository.findBySettingKey(settingKey)
        .orElseThrow(() -> new IllegalArgumentException("설정을 찾을 수 없습니다: " + settingKey));

    if (!setting.isEditable()) {
      throw new IllegalStateException("삭제할 수 없는 설정입니다: " + settingKey);
    }

    settingRepository.deleteBySettingKey(settingKey);

    // 캐시 무효화
    settingUtils.evictSettingCache(settingKey);

    log.info("시스템 설정 삭제 완료: {}", settingKey);
  }

  /**
   * 카테고리별 통계
   */
  public Map<String, Long> getSettingsStatsByCategory() {
    List<SystemSetting> allSettings = settingRepository.findAll();

    return allSettings.stream()
        .collect(Collectors.groupingBy(
            SystemSetting::getCategory,
            Collectors.counting()
        ));
  }

  // Private helper methods

  private void validateSettingValue(String value, String dataType) {
    try {
      switch (dataType) {
        case "INTEGER" -> Integer.parseInt(value);
        case "DECIMAL" -> new java.math.BigDecimal(value);
        case "BOOLEAN" -> {
          if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value) &&
              !"1".equals(value) && !"0".equals(value)) {
            throw new IllegalArgumentException("Boolean 값은 true/false 또는 1/0 이어야 합니다");
          }
        }
        // STRING은 별도 검증 없음
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("데이터 타입에 맞지 않는 값입니다: " + dataType);
    }
  }

  private SystemSettingResponse toSystemSettingResponse(SystemSetting setting) {
    return new SystemSettingResponse(
        setting.getSettingKey(),
        setting.getSettingValue(),
        setting.getDescription(),
        setting.getCategory(),
        setting.getDataType(),
        setting.isEditable(),
        setting.getDefaultValue(),
        setting.getCreatedAt(),
        setting.getUpdatedAt()
    );
  }
}