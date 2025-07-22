package org.example.studiopick.infrastructure.payment;

import org.example.studiopick.application.admin.dto.sales.AdminPaymentMethodData;
import org.example.studiopick.application.admin.dto.sales.AdminStudioSalesData;
import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long>{

  /**
   * 결제 키로 결제 정보 조회
   */
  Optional<Payment> findByPaymentKey(String paymentKey);

  /**
   * 예약 ID로 결제 정보 조회
   */
  Optional<Payment> findByReservationId(Long reservationId);

  /**
   * 주문 ID로 결제 정보 조회
   */
  Optional<Payment> findByOrderId(String orderId);

  // ===== 매출 통계 관련 메서드들 =====

  /**
   * 특정 상태의 전체 매출 조회
   */
  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
  BigDecimal getTotalSalesByStatus(@Param("status") PaymentStatus status);

  /**
   * 기간별 및 상태별 매출 조회
   */
  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
         "WHERE p.paidAt BETWEEN :startDate AND :endDate AND p.status = :status")
  BigDecimal getSalesByDateRangeAndStatus(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("status") PaymentStatus status);

  /**
   * 특정 상태의 결제 건수 조회
   */
  long countByStatus(PaymentStatus status);

  /**
   * 기간별 및 상태별 결제 건수 조회
   */
  @Query("SELECT COUNT(p) FROM Payment p " +
         "WHERE p.paidAt BETWEEN :startDate AND :endDate AND p.status = :status")
  long countByDateRangeAndStatus(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("status") PaymentStatus status);

  /**
   * 스튜디오별 매출 데이터 조회
   */
  @Query("SELECT new org.example.studiopick.application.admin.dto.sales.AdminStudioSalesData(" +
         "s.id, s.name, COALESCE(SUM(p.amount), 0), COUNT(p.id)) " +
         "FROM Payment p " +
         "JOIN p.reservation r " +
         "JOIN r.studio s " +
         "WHERE p.paidAt BETWEEN :startDate AND :endDate AND p.status = :status " +
         "GROUP BY s.id, s.name " +
         "ORDER BY SUM(p.amount) DESC")
  List<AdminStudioSalesData> getStudioSalesData(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("status") PaymentStatus status,
      Pageable pageable);

  /**
   * 기간별 스튜디오 수 조회 (페이징용)
   */
  @Query("SELECT COUNT(DISTINCT s.id) FROM Payment p " +
         "JOIN p.reservation r " +
         "JOIN r.studio s " +
         "WHERE p.paidAt BETWEEN :startDate AND :endDate")
  long countDistinctStudiosByDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * 결제 방법별 통계 조회
   */
  @Query("SELECT new org.example.studiopick.application.admin.dto.sales.AdminPaymentMethodData(" +
         "p.method, COALESCE(SUM(p.amount), 0), COUNT(p.id)) " +
         "FROM Payment p " +
         "WHERE p.paidAt BETWEEN :startDate AND :endDate AND p.status = :status " +
         "GROUP BY p.method")
  List<AdminPaymentMethodData> getPaymentMethodStats(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("status") PaymentStatus status);

  /**
   * 기간별 결제 내역 조회 (모든 조건)
   */
  Page<Payment> findByPaidAtBetweenOrderByPaidAtDesc(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * 기간별 및 결제방법별 결제 내역 조회
   */
  Page<Payment> findByPaidAtBetweenAndMethodOrderByPaidAtDesc(
      LocalDateTime startDate, LocalDateTime endDate, PaymentMethod method, Pageable pageable);

  /**
   * 기간별 및 상태별 결제 내역 조회
   */
  Page<Payment> findByPaidAtBetweenAndStatusOrderByPaidAtDesc(
      LocalDateTime startDate, LocalDateTime endDate, PaymentStatus status, Pageable pageable);

  /**
   * 기간별, 결제방법별, 상태별 결제 내역 조회
   */
  Page<Payment> findByPaidAtBetweenAndMethodAndStatusOrderByPaidAtDesc(
      LocalDateTime startDate, LocalDateTime endDate, 
      PaymentMethod method, PaymentStatus status, Pageable pageable);

  // ===== 사용자별 결제 내역 조회 =====
  
  /**
   * 사용자별 결제 내역 조회
   */
  @Query("SELECT p FROM Payment p JOIN p.reservation r WHERE r.user.id = :userId ORDER BY p.paidAt DESC")
  Page<Payment> findByUserIdOrderByPaidAtDesc(@Param("userId") Long userId, Pageable pageable);
  
  /**
   * 사용자별 + 상태별 결제 내역 조회
   */
  @Query("SELECT p FROM Payment p JOIN p.reservation r WHERE r.user.id = :userId AND p.status = :status ORDER BY p.paidAt DESC")
  Page<Payment> findByUserIdAndStatusOrderByPaidAtDesc(@Param("userId") Long userId, @Param("status") PaymentStatus status, Pageable pageable);
  
  /**
   * 사용자별 + 기간별 결제 내역 조회
   */
  @Query("SELECT p FROM Payment p JOIN p.reservation r WHERE r.user.id = :userId AND p.paidAt BETWEEN :startDate AND :endDate ORDER BY p.paidAt DESC")
  Page<Payment> findByUserIdAndPaidAtBetweenOrderByPaidAtDesc(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
  
  /**
   * 사용자별 + 기간별 + 상태별 결제 내역 조회
   */
  @Query("SELECT p FROM Payment p JOIN p.reservation r WHERE r.user.id = :userId AND p.paidAt BETWEEN :startDate AND :endDate AND p.status = :status ORDER BY p.paidAt DESC")
  Page<Payment> findByUserIdAndPaidAtBetweenAndStatusOrderByPaidAtDesc(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") PaymentStatus status, Pageable pageable);

  /**
   * 주어진 워크샵 ID 리스트에 대해,
   * 결제 완료된 금액(amount) 합계를
   * workshopId → sum(amount) 로 반환
   */
  @Query("""
    SELECT p.reservation.workshop.id AS workshopId,
           COALESCE(SUM(p.amount), 0)       AS sum
    FROM Payment p
    WHERE p.reservation.workshop.id IN :ids
      AND p.status = :paidStatus
    GROUP BY p.reservation.workshop.id
    """)
  List<Object[]> sumPaidAmountByWorkshopIds(
          @Param("ids") List<Long> workshopIds,
          @Param("paidStatus") PaymentStatus paidStatus
  );

}