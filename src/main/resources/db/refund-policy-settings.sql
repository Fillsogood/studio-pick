-- 환불 정책 관련 시스템 설정 추가
INSERT INTO "SystemSetting" (setting_key, setting_value, description, category, data_type, default_value) VALUES
('reservation.free.cancel.hours', '48', '무료 취소 가능 시간 (시간 단위)', 'reservation', 'INTEGER', '48'),
('reservation.early.cancel.fee.percent', '10', '조기 취소 수수료 비율 (%)', 'reservation', 'INTEGER', '10'),
('reservation.late.cancel.fee.percent', '30', '당일 취소 수수료 비율 (%)', 'reservation', 'INTEGER', '30'),
('reservation.refund.processing.days', '3', '환불 처리 소요일', 'reservation', 'INTEGER', '3'),
('reservation.auto.refund.enabled', 'true', '자동 환불 처리 여부', 'reservation', 'BOOLEAN', 'true');

-- 환불 정책 설명을 위한 설정
INSERT INTO "SystemSetting" (setting_key, setting_value, description, category, data_type, default_value) VALUES
('refund.policy.description', '48시간 전: 무료, 24시간 전: 10% 수수료, 24시간 이내: 30% 수수료', '환불 정책 설명', 'policy', 'STRING', '');
