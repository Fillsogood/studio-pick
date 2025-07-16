package org.example.studiopick.application.payment;

import org.example.studiopick.application.payment.dto.SettlementResponseDto;
import org.example.studiopick.domain.payment.Payment;

import java.util.List;

/**
 * 정산 관리 서비스 인터페이스
 */
public interface SettlementService {

    /**
     * 스튜디오별 정산 내역 조회
     */
    List<SettlementResponseDto> getSettlementsByStudio(Long studioId);

    /**
     * 정산 출금 요청
     */
    void withdrawSettlement(Long settlementId);

    /**
     * 정산 생성
     */
    void createSettlement(Payment payment);

    /**
     * 정산 승인
     */
    void approveSettlement(Long settlementId);
}
