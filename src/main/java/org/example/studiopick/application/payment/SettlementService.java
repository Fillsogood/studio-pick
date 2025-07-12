package org.example.studiopick.application.payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.payment.dto.SettlementResponseDto;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.payment.Settlement;
import org.example.studiopick.domain.payment.SettlementRepository;
import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public List<SettlementResponseDto> getSettlementsByStudio(Long studioId) {
        return settlementRepository.findByStudioId(studioId).stream()
                .map(s -> SettlementResponseDto.builder()
                        .settlementId(s.getId())
                        .paymentId(s.getPayment().getId())
                        .totalAmount(s.getTotalAmount())
                        .platformFee(s.getPlatformFee())
                        .payoutAmount(s.getPayoutAmount())
                        .taxAmount(s.getTaxAmount())
                        .status(s.getSettlementStatus().name())
                        .settledAt(s.getSettledAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void withdrawSettlement(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

        if (settlement.isPaid() || settlement.isOnHold()) {
            throw new IllegalStateException("이미 출금되었거나 요청 중인 정산입니다.");
        }

        settlement.hold();
        settlementRepository.save(settlement);
    }

    @Transactional
    public void createSettlement(Payment payment) {
        var studio = payment.getReservation().getStudio();
        var commission = studio.getCommission();
        if (commission == null) throw new IllegalStateException("수수료 정보가 없습니다");

        BigDecimal totalAmount = payment.getAmount();
        BigDecimal commissionRate = commission.getCommissionRate();
        BigDecimal platformFee = totalAmount.multiply(commissionRate).divide(BigDecimal.valueOf(100));
        BigDecimal payoutAmount = totalAmount.subtract(platformFee);

        BigDecimal taxRate = BigDecimal.valueOf(3.3); // 세금율 3.3%
        BigDecimal taxAmount = payoutAmount.multiply(taxRate).divide(BigDecimal.valueOf(100));

        Settlement settlement = Settlement.builder()
                .studio(studio)
                .payment(payment)
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .payoutAmount(payoutAmount)
                .taxAmount(taxAmount)
                .settlementStatus(SettlementStatus.PENDING)
                .build();

        settlementRepository.save(settlement);
    }

    @Transactional
    public void approveSettlement(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

        if (!settlement.isOnHold()) {
            throw new IllegalStateException("HOLD 상태인 정산만 승인할 수 있습니다.");
        }

        settlement.markAsPaid(); // 상태: PAID, 시간: now()
    }
}
