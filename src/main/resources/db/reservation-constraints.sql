-- =========================================
-- Studio-Pick 예약 시스템 데이터베이스 제약조건
-- =========================================

-- 1. 예약 시간 중복 방지를 위한 부분 유니크 제약조건
-- 활성 예약(PENDING, CONFIRMED)에 대해서만 중복 방지
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_reservation_studio_date_time_active'
    ) THEN
        ALTER TABLE "Reservation" 
        ADD CONSTRAINT uk_reservation_studio_date_time_active 
        UNIQUE (studio_id, reservation_date, start_time, end_time)
        WHERE status IN ('PENDING', 'CONFIRMED');
    END IF;
END $$;

-- 2. 예약 시간 유효성 체크 제약조건
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_reservation_time_valid'
    ) THEN
        ALTER TABLE "reservation"
        ADD CONSTRAINT chk_reservation_time_valid 
        CHECK (start_time < end_time);
    END IF;
END $$;

-- 3. 인원 수 유효성 체크 제약조건
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_people_count_valid'
    ) THEN
        ALTER TABLE "reservation"
        ADD CONSTRAINT chk_people_count_valid 
        CHECK (people_count > 0 AND people_count <= 50);
    END IF;
END $$;

-- 4. 총 금액 양수 체크 제약조건
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_total_amount_positive'
    ) THEN
        ALTER TABLE "reservation"
        ADD CONSTRAINT chk_total_amount_positive 
        CHECK (total_amount > 0);
    END IF;
END $$;

-- 5. 예약 날짜 미래 날짜 체크 제약조건
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_reservation_date_future'
    ) THEN
        ALTER TABLE "reservation"
        ADD CONSTRAINT chk_reservation_date_future 
        CHECK (reservation_date >= CURRENT_DATE);
    END IF;
END $$;

-- =========================================
-- 성능 최적화를 위한 인덱스
-- =========================================

-- 1. 예약 조회 성능 개선을 위한 인덱스
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reservation_studio_date 
ON "reservation" (studio_id, reservation_date);

-- 2. 사용자별 예약 조회 인덱스
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reservation_user_date 
ON "reservation" (user_id, reservation_date DESC);

-- 3. 상태별 예약 조회 인덱스
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reservation_status_date 
ON "reservation" (status, reservation_date);

-- 4. 예약 시간대 검색을 위한 복합 인덱스
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reservation_studio_date_time 
ON "reservation" (studio_id, reservation_date, start_time, end_time);

-- 5. 관리자 검색을 위한 복합 인덱스
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reservation_search 
ON "reservation" (status, reservation_date, created_at);

-- =========================================
-- 통계 및 분석을 위한 뷰 생성
-- =========================================

-- 예약 통계 뷰
CREATE OR REPLACE VIEW v_reservation_stats AS
SELECT 
    COUNT(*) as total_reservations,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
    COUNT(CASE WHEN status = 'CONFIRMED' THEN 1 END) as confirmed_count,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_count,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
    COUNT(CASE WHEN status = 'REFUNDED' THEN 1 END) as refunded_count,
    COUNT(CASE WHEN reservation_date = CURRENT_DATE THEN 1 END) as today_count,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_amount
FROM "reservation";

-- 스튜디오별 예약 통계 뷰
CREATE OR REPLACE VIEW v_studio_reservation_stats AS
SELECT 
    s.id as studio_id,
    s.name as studio_name,
    COUNT(r.id) as total_reservations,
    COUNT(CASE WHEN r.status = 'CONFIRMED' THEN 1 END) as confirmed_count,
    SUM(r.total_amount) as total_revenue,
    AVG(r.total_amount) as avg_amount,
    MAX(r.reservation_date) as last_reservation_date
FROM "studio" s
LEFT JOIN "reservation" r ON s.id = r.studio_id
GROUP BY s.id, s.name;

-- =========================================
-- 데이터 정합성 검증 함수
-- =========================================

-- 예약 중복 검증 함수
CREATE OR REPLACE FUNCTION check_reservation_overlap(
    p_studio_id BIGINT,
    p_reservation_date DATE,
    p_start_time TIME,
    p_end_time TIME,
    p_exclude_id BIGINT DEFAULT NULL
) RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM "reservation"
        WHERE studio_id = p_studio_id
          AND reservation_date = p_reservation_date
          AND status IN ('PENDING', 'CONFIRMED')
          AND (p_exclude_id IS NULL OR id != p_exclude_id)
          AND (start_time < p_end_time AND end_time > p_start_time)
    );
END;
$$ LANGUAGE plpgsql;

-- 예약 가능 시간 조회 함수
CREATE OR REPLACE FUNCTION get_available_times(
    p_studio_id BIGINT,
    p_reservation_date DATE,
    p_interval_hours INTEGER DEFAULT 1
) RETURNS TABLE(available_time TIME) AS $$
DECLARE
    operating_start TIME := '09:00:00';
    operating_end TIME := '18:00:00';
    current_time TIME;
BEGIN
    current_time := operating_start;
    
    WHILE current_time < operating_end LOOP
        IF NOT check_reservation_overlap(
            p_studio_id, 
            p_reservation_date, 
            current_time, 
            current_time + (p_interval_hours || ' hours')::INTERVAL
        ) THEN
            available_time := current_time;
            RETURN NEXT;
        END IF;
        
        current_time := current_time + (p_interval_hours || ' hours')::INTERVAL;
    END LOOP;
    
    RETURN;
END;
$$ LANGUAGE plpgsql;

-- =========================================
-- 트리거 함수 (로그 및 감사)
-- =========================================

-- 예약 상태 변경 로그 테이블
CREATE TABLE IF NOT EXISTS "ReservationStatusLog" (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES "reservation"(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES "user"(id),
    change_reason TEXT,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 예약 상태 변경 트리거 함수
CREATE OR REPLACE FUNCTION log_reservation_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO "ReservationStatusLog" (
            reservation_id, old_status, new_status, changed_at
        ) VALUES (
            NEW.id, OLD.status, NEW.status, CURRENT_TIMESTAMP
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성
DROP TRIGGER IF EXISTS tr_reservation_status_change ON "reservation";
CREATE TRIGGER tr_reservation_status_change
    AFTER UPDATE ON "reservation"
    FOR EACH ROW
    EXECUTE FUNCTION log_reservation_status_change();

-- =========================================
-- 정리 및 최적화 작업
-- =========================================

-- 통계 정보 업데이트
ANALYZE "reservation";
ANALYZE "studio";
ANALYZE "user";

COMMIT;
