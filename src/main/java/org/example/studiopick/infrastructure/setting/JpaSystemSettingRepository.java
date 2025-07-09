package org.example.studiopick.infrastructure.setting;

import org.example.studiopick.domain.setting.SystemSetting;
import org.example.studiopick.domain.setting.SystemSettingRepository;
import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaSystemSettingRepository extends JpaRepository<SystemSetting, Long>, SystemSettingRepository {

}