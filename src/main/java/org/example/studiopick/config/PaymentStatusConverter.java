package org.example.studiopick.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.studiopick.domain.common.enums.PaymentStatus;

import java.util.Arrays;

@Converter(autoApply = false)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {
  @Override
  public String convertToDatabaseColumn(PaymentStatus status) {
    return status != null ? status.getValue() : null;
  }

  @Override
  public PaymentStatus convertToEntityAttribute(String dbData) {
    System.out.println("컨버터 변환 시도: " + dbData);
    if (dbData == null) return null;
    return Arrays.stream(PaymentStatus.values())
        .filter(s -> s.getValue().equals(dbData))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + dbData));
  }
}
