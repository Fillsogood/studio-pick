package org.example.studiopick.domain.setting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;

@Entity
@Table(name = "\"SystemSetting\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemSetting extends BaseEntity {

  @Column(name = "setting_key", nullable = false, unique = true, length = 100)
  private String settingKey;

  @Column(name = "setting_value", nullable = false, length = 500)
  private String settingValue;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "category", nullable = false, length = 50)
  private String category;

  @Column(name = "data_type", nullable = false, length = 20)
  private String dataType;

  @Column(name = "is_editable", nullable = false)
  private Boolean isEditable = true;

  @Column(name = "default_value", length = 500)
  private String defaultValue;

  @Builder
  public SystemSetting(String settingKey, String settingValue, String description,
                       String category, String dataType, Boolean isEditable, String defaultValue) {
    this.settingKey = settingKey;
    this.settingValue = settingValue;
    this.description = description;
    this.category = category;
    this.dataType = dataType;
    this.isEditable = isEditable != null ? isEditable : true;
    this.defaultValue = defaultValue;
  }

  public void updateValue(String newValue) {
    if (!this.isEditable) {
      throw new IllegalStateException("수정할 수 없는 설정입니다: " + this.settingKey);
    }
    this.settingValue = newValue;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public boolean isEditable() {
    return this.isEditable;
  }

  public String getValueOrDefault() {
    return this.settingValue != null ? this.settingValue : this.defaultValue;
  }
}