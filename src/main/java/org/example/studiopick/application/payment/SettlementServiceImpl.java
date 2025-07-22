package org.example.studiopick.application.payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.payment.dto.SettlementResponseDto;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.payment.Settlement;
import org.example.studiopick.infrastructure.payment.JpaSettlementRepository;
import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final JpaSettlementRepository settlementRepository;

    @Override
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

    @Override
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

    @Override
    @Transactional
    public void createSettlement(Payment payment) {
        var reservation = payment.getReservation();

        BigDecimal totalAmount = payment.getAmount();
        BigDecimal platformFee;
        BigDecimal payoutAmount;
        BigDecimal taxRate = BigDecimal.valueOf(3.3);
        BigDecimal taxAmount;

        Settlement settlement;

        if (reservation.getStudio() != null) {
            var studio = reservation.getStudio();
            var commission = studio.getCommission();
            if (commission == null) throw new IllegalStateException("스튜디오 수수료 정보가 없습니다.");

            BigDecimal commissionRate = commission.getCommissionRate();
            platformFee = totalAmount.multiply(commissionRate).divide(BigDecimal.valueOf(100));
            payoutAmount = totalAmount.subtract(platformFee);
            taxAmount = payoutAmount.multiply(taxRate).divide(BigDecimal.valueOf(100));

            settlement = Settlement.studioBuilder()
                .studio(studio)
                .payment(payment)
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .payoutAmount(payoutAmount)
                .taxAmount(taxAmount)
                .settlementStatus(SettlementStatus.PAID)
                .build();

        } else if (reservation.getWorkshop() != null) {
            var workshop = reservation.getWorkshop();
            var owner = workshop.getOwner();
            if (owner == null) throw new IllegalStateException("공방 소유자 정보가 없습니다.");

            // 공방 수수료율은 예시로 10%
            BigDecimal commissionRate = BigDecimal.valueOf(10);
            platformFee = totalAmount.multiply(commissionRate).divide(BigDecimal.valueOf(100));
            payoutAmount = totalAmount.subtract(platformFee);
            taxAmount = payoutAmount.multiply(taxRate).divide(BigDecimal.valueOf(100));

            settlement = Settlement.workshopBuilder()
                .workshop(workshop)
                .payment(payment)
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .payoutAmount(payoutAmount)
                .taxAmount(taxAmount)
                .settlementStatus(SettlementStatus.PAID)
                .build();

        } else {
            throw new IllegalStateException("예약에 스튜디오와 공방이 모두 없습니다.");
        }

        settlementRepository.save(settlement);
    }

    @Override
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
