package org.example.studiopick.infrastructure.setting;

import org.example.studiopick.domain.setting.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface JpaSystemSettingRepository extends JpaRepository<SystemSetting, Long> {
  Optional<SystemSetting> findBySettingKey(String settingKey);

  List<SystemSetting> findByCategory(String category);

  List<SystemSetting> findByIsEditableTrue();

  @Query("SELECT s FROM SystemSetting s WHERE s.category = :category ORDER BY s.settingKey ASC")
  List<SystemSetting> findByCategoryOrderBySettingKey(@Param("category") String category);

  @Query("SELECT s FROM SystemSetting s ORDER BY s.category ASC, s.settingKey ASC")
  List<SystemSetting> findAllOrderByCategoryAndKey();

  void deleteBySettingKey(String settingKey);

  boolean existsBySettingKey(String settingKey);

  long count();

}