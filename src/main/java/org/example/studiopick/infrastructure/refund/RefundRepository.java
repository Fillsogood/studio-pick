package org.example.studiopick.infrastructure.refund;

import org.example.studiopick.domain.common.enums.RefundStatus;
import org.example.studiopick.domain.refund.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 환불 내역 Repository
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    /**
     * 예약 ID로 환불 내역 조회
     */
    List<Refund> findByReservationIdOrderByCreatedAtDesc(Long reservationId);
    
    /**
     * 결제 ID로 환불 내역 조회
     */
    List<Refund> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
    
    /**
     * 사용자별 환불 내역 조회
     */
    @Query("SELECT r FROM Refund r " +
           "JOIN r.reservation res " +
           "WHERE res.user.id = :userId " +
           "ORDER BY r.createdAt DESC")
    List<Refund> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 특정 기간 환불 통계
     */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r " +
           "WHERE r.status = 'COMPLETED' " +
           "AND r.refundedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRefundAmountByPeriod(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * 특정 기간 환불 건수
     */
    @Query("SELECT COUNT(r) FROM Refund r " +
           "WHERE r.status = 'COMPLETED' " +
           "AND r.refundedAt BETWEEN :startDate AND :endDate")
    Long getRefundCountByPeriod(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * 환불 실패 건 조회
     */
    List<Refund> findByStatusOrderByCreatedAtDesc(org.example.studiopick.domain.common.enums.RefundStatus status);
    
    /**
     * 상태별 환불 건수 조회
     */
    long countByStatus(org.example.studiopick.domain.common.enums.RefundStatus status);
    
    /**
     * 토스 결제키로 환불 내역 조회
     */
    Optional<Refund> findByTossPaymentKey(String tossPaymentKey);
    
    /**
     * 일별 환불 통계 - Native Query 사용
     */
    @Query(value = "SELECT DATE(refunded_at) as refund_date, " +
                   "COUNT(*) as refund_count, " +
                   "COALESCE(SUM(refund_amount), 0) as total_amount " +
                   "FROM refund " +
                   "WHERE status = 'COMPLETED' " +
                   "AND DATE(refunded_at) BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(refunded_at) " +
                   "ORDER BY DATE(refunded_at) DESC", 
           nativeQuery = true)
    List<Object[]> getDailyRefundStats(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 날짜 범위별 환불 건수 조회
     */
    @Query("SELECT COUNT(r) FROM Refund r WHERE DATE(r.createdAt) BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 날짜 범위와 상태별 환불 건수 조회
     */
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status AND DATE(r.createdAt) BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") org.example.studiopick.domain.common.enums.RefundStatus status,
                                  @Param("startDate") String startDate, 
                                  @Param("endDate") String endDate);
    
    /**
     * 날짜 범위별 환불 금액 합계
     */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r " +
           "WHERE r.status = 'COMPLETED' AND DATE(r.refundedAt) BETWEEN :startDate AND :endDate")
    BigDecimal sumRefundAmountByDateRange(@Param("startDate") String startDate, 
                                         @Param("endDate") String endDate);
    
    /**
     * 검색 조건에 따른 환불 내역 조회 (페이징)
     */
    @Query("""
    SELECT r FROM Refund r 
    WHERE (:status IS NULL OR r.status = :status)
    ORDER BY r.createdAt DESC
""")
    Page<Refund> searchRefunds(
        @Param("status") RefundStatus status,
        Pageable pageable
    );


}
