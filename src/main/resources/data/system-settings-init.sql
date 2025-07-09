-- src/main/resources/data/system-settings-init.sql
-- 시스템 설정 초기 데이터

-- 비즈니스 설정
INSERT INTO "SystemSetting" (setting_key, setting_value, description, category, data_type, is_editable, default_value, created_at, updated_at)
VALUES
-- 수수료 관련
('platform.commission.rate', '5.0', '플랫폼 수수료율(%)', 'BUSINESS', 'DECIMAL', true, '5.0', NOW(), NOW()),
('platform.refund.fee.rate', '3.0', '환불 수수료율(%)', 'BUSINESS', 'DECIMAL', true, '3.0', NOW(), NOW()),

-- 예약 관련
('reservation.cancel.hours', '24', '예약 취소 가능 시간(시간)', 'BUSINESS', 'INTEGER', true, '24', NOW(), NOW()),
('reservation.max.people', '20', '예약 최대 인원수', 'BUSINESS', 'INTEGER', true, '20', NOW(), NOW()),
('reservation.advance.days', '90', '최대 예약 가능 일수(일)', 'BUSINESS', 'INTEGER', true, '90', NOW(), NOW()),

-- 스튜디오 기본값
('studio.default.hourly.rate', '30000', '스튜디오 기본 시간당 요금(원)', 'BUSINESS', 'INTEGER', true, '30000', NOW(), NOW()),
('studio.default.per.person.rate', '5000', '스튜디오 기본 인당 추가요금(원)', 'BUSINESS', 'INTEGER', true, '5000', NOW(), NOW()),
('studio.default.max.people', '10', '스튜디오 기본 최대 수용인원', 'BUSINESS', 'INTEGER', true, '10', NOW(), NOW()),

-- 결제 관련
('payment.min.amount', '10000', '최소 결제 금액(원)', 'PAYMENT', 'INTEGER', true, '10000', NOW(), NOW()),
('payment.timeout.minutes', '30', '결제 대기 시간(분)', 'PAYMENT', 'INTEGER', true, '30', NOW(), NOW()),

-- 시스템 설정
('pagination.default.size', '10', '기본 페이지 사이즈', 'SYSTEM', 'INTEGER', true, '10', NOW(), NOW()),
('pagination.max.size', '100', '최대 페이지 사이즈', 'SYSTEM', 'INTEGER', true, '100', NOW(), NOW()),
('maintenance.mode', 'false', '점검 모드 여부', 'SYSTEM', 'BOOLEAN', true, 'false', NOW(), NOW()),
('signup.enabled', 'true', '신규 회원가입 허용 여부', 'SYSTEM', 'BOOLEAN', true, 'true', NOW(), NOW()),

-- 사용자 관련
('user.login.fail.max.count', '5', '로그인 실패 최대 횟수', 'SYSTEM', 'INTEGER', true, '5', NOW(), NOW()),
('user.password.min.length', '8', '비밀번호 최소 길이', 'SYSTEM', 'INTEGER', false, '8', NOW(), NOW()),
('user.password.max.length', '20', '비밀번호 최대 길이', 'SYSTEM', 'INTEGER', false, '20', NOW(), NOW()),

-- 공방 관련
('class.default.max.participants', '8', '클래스 기본 최대 참가자 수', 'BUSINESS', 'INTEGER', true, '8', NOW(), NOW()),
('class.default.supplies', '', '클래스 기본 준비물 목록(콤마 구분)', 'BUSINESS', 'STRING', true, '', NOW(), NOW());