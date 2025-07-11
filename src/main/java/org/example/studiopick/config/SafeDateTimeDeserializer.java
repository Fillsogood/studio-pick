package org.example.studiopick.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class SafeDateTimeDeserializer {

    /**
     * ✅ 타임존 정보가 있는 문자열을 OffsetDateTime으로 안전하게 변환
     */
    public static class SafeOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
        @Override
        public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getText();
            
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            try {
                // ✅ 타임존 정보가 포함된 ISO 8601 형식으로 파싱
                return OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                log.warn("OffsetDateTime 파싱 실패, 다른 형식으로 시도: {}", dateString);
                
                try {
                    // ✅ 타임존 정보가 없는 경우 LocalDateTime으로 파싱 후 시스템 기본 타임존 적용
                    LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return localDateTime.atOffset(java.time.ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
                } catch (DateTimeParseException e2) {
                    log.error("모든 날짜 형식 파싱 실패: {}", dateString, e2);
                    throw new RuntimeException("날짜 형식을 파싱할 수 없습니다: " + dateString, e2);
                }
            }
        }
    }

    /**
     * ✅ 타임존 정보를 제거하고 LocalDateTime으로 안전하게 변환
     */
    public static class SafeLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getText();
            
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            try {
                // ✅ 타임존 정보가 있는 경우 OffsetDateTime으로 파싱 후 LocalDateTime으로 변환
                if (dateString.contains("+") || dateString.contains("Z")) {
                    return OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .toLocalDateTime();
                } else {
                    // ✅ 타임존 정보가 없는 경우 직접 LocalDateTime으로 파싱
                    return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            } catch (DateTimeParseException e) {
                log.error("LocalDateTime 파싱 실패: {}", dateString, e);
                throw new RuntimeException("날짜 형식을 파싱할 수 없습니다: " + dateString, e);
            }
        }
    }
}
