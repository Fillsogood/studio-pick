-- ✅ 환불 내역 테이블 생성 스크립트
-- 모든 환불 이력을 추적하여 투명한 환불 관리 제공

CREATE TABLE IF NOT EXISTS refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '환불 ID',
    payment_id BIGINT NOT NULL COMMENT '결제 ID',
    reservation_id BIGINT NOT NULL COMMENT '예약 ID',
    
    -- 환불 금액 정보
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '실제 환불 금액',
    original_amount DECIMAL(10,2) NOT NULL COMMENT '원래 결제 금액',
    cancellation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '취소 수수료',
    
    -- 환불 상세 정보
    refund_reason VARCHAR(500) NOT NULL COMMENT '환불 사유',
    refund_policy VARCHAR(200) NOT NULL COMMENT '적용된 환불 정책',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '환불 상태',
    
    -- 토스페이먼츠 연동 정보
    toss_payment_key VARCHAR(200) COMMENT '토스페이먼츠 결제 키',
    toss_transaction_key VARCHAR(200) COMMENT '토스페이먼츠 거래 키',
    
    -- 처리 시간
    refunded_at TIMESTAMP NULL COMMENT '환불 완료 시간',
    failure_reason VARCHAR(1000) COMMENT '환불 실패 사유',
    
    -- 공통 필드 (BaseEntity)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    
    -- 외래키 제약조건
    FOREIGN KEY (payment_id) REFERENCES Payment(id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES Reservation(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_refund_payment_id (payment_id),
    INDEX idx_refund_reservation_id (reservation_id),
    INDEX idx_refund_status (status),
    INDEX idx_refund_created_at (created_at),
    INDEX idx_refund_toss_payment_key (toss_payment_key),
    
    -- 복합 인덱스 (자주 사용되는 쿼리 최적화)
    INDEX idx_refund_status_created_at (status, created_at),
    INDEX idx_refund_reservation_created_at (reservation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='환불 내역 테이블';

-- ✅ 환불 통계를 위한 뷰 생성
CREATE OR REPLACE VIEW v_refund_stats AS
SELECT 
    DATE(created_at) as refund_date,
    status,
    COUNT(*) as refund_count,
    SUM(refund_amount) as total_refund_amount,
    SUM(cancellation_fee) as total_cancellation_fee,
    AVG(refund_amount) as avg_refund_amount,
    
    -- 환불율 통계
    COUNT(CASE WHEN refund_amount = original_amount THEN 1 END) as full_refund_count,
    COUNT(CASE WHEN refund_amount > 0 AND refund_amount < original_amount THEN 1 END) as partial_refund_count,
    COUNT(CASE WHEN refund_amount = 0 THEN 1 END) as no_refund_count
FROM refund 
GROUP BY DATE(created_at), status
ORDER BY refund_date DESC, status;

-- ✅ 환불 정책별 통계 뷰
CREATE OR REPLACE VIEW v_refund_policy_stats AS
SELECT 
    refund_policy,
    COUNT(*) as usage_count,
    SUM(refund_amount) as total_refund_amount,
    AVG(refund_amount) as avg_refund_amount,
    AVG((refund_amount / original_amount) * 100) as avg_refund_rate
FROM refund 
WHERE status = 'COMPLETED'
GROUP BY refund_policy
ORDER BY usage_count DESC;

-- ✅ 환불 내역 조회 성능을 위한 추가 인덱스
-- 사용자별 환불 내역 조회 최적화
ALTER TABLE refund ADD INDEX idx_refund_user_lookup (reservation_id, created_at);

-- 일별 환불 통계 조회 최적화  
ALTER TABLE refund ADD INDEX idx_refund_daily_stats (created_at, status, refund_amount);
