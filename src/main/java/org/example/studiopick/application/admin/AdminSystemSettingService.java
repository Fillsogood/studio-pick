package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.setting.*;

import java.util.Map;

/**
 * 관리자 시스템 설정 관리 서비스 인터페이스
 */
public interface AdminSystemSettingService {

    /**
     * 전체 시스템 설정 조회
     */
    SystemSettingListResponse getAllSettings();

    /**
     * 특정 설정 조회
     */
    SystemSettingResponse getSetting(String settingKey);

    /**
     * 설정값 업데이트
     */
    SystemSettingResponse updateSetting(String settingKey, SystemSettingUpdateCommand command);

    /**
     * 새 설정 생성
     */
    SystemSettingResponse createSetting(SystemSettingCreateCommand command);

    /**
     * 설정 삭제
     */
    void deleteSetting(String settingKey);

}
