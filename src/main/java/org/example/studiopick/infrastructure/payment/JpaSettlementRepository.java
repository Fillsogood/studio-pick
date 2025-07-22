package org.example.studiopick.infrastructure.payment;

import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.example.studiopick.domain.payment.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaSettlementRepository extends JpaRepository<Settlement, Long> {
    
    /**
     * 스튜디오 ID로 정산 내역 조회
     */
    List<Settlement> findByStudioId(Long studioId);
    
    /**
     * 워크샵 ID로 정산 내역 조회
     */
    List<Settlement> findByWorkshopId(Long workshopId);
    
    /**
     * 상태별 정산 내역 조회 (페이징)
     */
    Page<Settlement> findBySettlementStatus(SettlementStatus status, Pageable pageable);

    Optional<Settlement> findByPaymentId(Long paymentId);
    
    /**
     * 기간별 정산 내역 조회
     */
    @Query("SELECT s FROM Settlement s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Settlement> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 스튜디오 ID와 상태별 정산 내역 조회
     */
    List<Settlement> findByStudioIdAndSettlementStatus(Long studioId, SettlementStatus status);
    
    /**
     * 워크샵 ID와 상태별 정산 내역 조회
     */
    List<Settlement> findByWorkshopIdAndSettlementStatus(Long workshopId, SettlementStatus status);
    
    /**
     * 스튜디오 정산만 조회
     */
    @Query("SELECT s FROM Settlement s WHERE s.studio IS NOT NULL ORDER BY s.createdAt DESC")
    List<Settlement> findAllStudioSettlements();
    
    /**
     * 워크샵 정산만 조회
     */
    @Query("SELECT s FROM Settlement s WHERE s.workshop IS NOT NULL ORDER BY s.createdAt DESC")
    List<Settlement> findAllWorkshopSettlements();
    
    /**
     * 스튜디오 정산만 상태별 조회 (페이징)
     */
    @Query("SELECT s FROM Settlement s WHERE s.studio IS NOT NULL AND s.settlementStatus = :status ORDER BY s.createdAt DESC")
    Page<Settlement> findStudioSettlementsByStatus(@Param("status") SettlementStatus status, Pageable pageable);
    
    /**
     * 워크샵 정산만 상태별 조회 (페이징)
     */
    @Query("SELECT s FROM Settlement s WHERE s.workshop IS NOT NULL AND s.settlementStatus = :status ORDER BY s.createdAt DESC")
    Page<Settlement> findWorkshopSettlementsByStatus(@Param("status") SettlementStatus status, Pageable pageable);
}