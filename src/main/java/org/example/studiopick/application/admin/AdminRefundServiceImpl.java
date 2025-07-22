package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.admin.dto.refund.RefundDTOs;
import org.example.studiopick.domain.common.enums.RefundStatus;
import org.example.studiopick.domain.refund.Refund;
import org.example.studiopick.infrastructure.refund.RefundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRefundServiceImpl implements AdminRefundService {

  private final RefundRepository refundRepository;

  @Override
  public RefundDTOs.AdminRefundListResponse getRefunds(int page, int size, String status, String startDate, String endDate) {
    // 페이지는 0부터 시작하므로 page-1 적용
    PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    
    // 상태 변환 (null 체크 및 예외 처리 추가)
    RefundStatus refundStatus = null;
    if (status != null && !status.trim().isEmpty()) {
      try {
        refundStatus = RefundStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("유효하지 않은 환불 상태입니다: " + status);
      }
    }

    // 검색 수행
    Page<Refund> refunds = refundRepository.searchRefunds(refundStatus, pageable);


    // DTO 변환
    List<RefundDTOs.AdminRefundListResponse.RefundSummary> summaries = refunds.stream()
        .map(r -> new RefundDTOs.AdminRefundListResponse.RefundSummary(
            r.getId(),
            r.getReservation().getId(),
            r.getPayment().getId(),
            r.getReservation().getUser().getName(), // 예약에서 사용자 정보 가져오기
            r.getRefundAmount(),
            r.getStatus(),
            r.getCreatedAt()
        )).collect(Collectors.toList());

    // 페이지네이션 정보 생성
    RefundDTOs.RefundPagination pagination = new RefundDTOs.RefundPagination(
        page, // currentPage
        refunds.getTotalElements(), // totalElements
        refunds.getTotalPages() // totalPages
    );

    return new RefundDTOs.AdminRefundListResponse(summaries, pagination);
  }

  @Override
  public RefundDTOs.AdminRefundDetailResponse getRefundDetail(Long refundId) {
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("환불 내역을 찾을 수 없습니다."));

    return new RefundDTOs.AdminRefundDetailResponse(
        refund.getId(),
        refund.getReservation().getId(),
        refund.getUser().getName(),
        refund.getUser().getEmail(),
        refund.getPayment().getId(),
        refund.getRefundAmount(),
        refund.getOriginalAmount(),
        refund.getCancellationFee(),
        refund.getRefundReason(),
        refund.getRefundPolicy(),
        refund.getStatus(),
        refund.getPayment().getPaymentKey(),
        refund.getPayment().getTransactionKey(),
        refund.getRefundedAt(),
        refund.getCreatedAt(),
        refund.getFailureReason()
    );
  }

  @Override
  @Transactional
  public RefundDTOs.AdminRefundProcessResponse processRefund(Long refundId, RefundDTOs.AdminRefundProcessCommand command) {
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("환불 내역을 찾을 수 없습니다."));

    // 상태 검증
    if (refund.getStatus() != RefundStatus.PENDING && refund.getStatus() != RefundStatus.PROCESSING) {
      throw new IllegalStateException("처리할 수 없는 상태의 환불 요청입니다: " + refund.getStatus());
    }

    // 액션에 따른 처리
    if ("APPROVE".equals(command.action())) {
      refund.markAsCompleted(null); // transactionKey는 실제 결제 시스템에서 얻어야 함
    } else if ("REJECT".equals(command.action())) {
      refund.markAsFailed(command.reason());
    } else {
      throw new IllegalArgumentException("유효하지 않은 액션입니다: " + command.action());
    }
    
    refundRepository.save(refund);

    return new RefundDTOs.AdminRefundProcessResponse(
        refund.getId(),
        refund.getStatus(),
        refund.getRefundedAt() != null ? refund.getRefundedAt() : refund.getUpdatedAt()
    );
  }

  @Override
  public RefundDTOs.AdminRefundStatsResponse getRefundStats(String startDate, String endDate) {
    // 날짜 범위 검증
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("시작일과 마지막일을 모두 입력해야 합니다.");
    }
    
    // 통계 데이터 조회
    long total = refundRepository.countByDateRange(startDate, endDate);
    long completed = refundRepository.countByStatusAndDateRange(RefundStatus.COMPLETED, startDate, endDate);
    long failed = refundRepository.countByStatusAndDateRange(RefundStatus.FAILED, startDate, endDate);
    long pending = refundRepository.countByStatusAndDateRange(RefundStatus.PENDING, startDate, endDate);
    long processing = refundRepository.countByStatusAndDateRange(RefundStatus.PROCESSING, startDate, endDate);

    // 환불 총 금액
    BigDecimal totalRefundAmount = refundRepository.sumRefundAmountByDateRange(startDate, endDate);
    
    // 성공률 계산
    double successRate = total > 0 ? (double) completed / total * 100 : 0.0;

    return new RefundDTOs.AdminRefundStatsResponse(
        total,
        completed,
        failed,
        pending,
        totalRefundAmount != null ? totalRefundAmount : BigDecimal.ZERO
    );
  }

  @Override
  public long getPendingRefundCount() {
    return refundRepository.countByStatus(RefundStatus.PENDING);
  }

  @Override
  @Transactional
  public RefundDTOs.AdminBulkRefundResponse processBulkRefunds(RefundDTOs.AdminBulkRefundCommand command) {
    // 입력 검증
    if (command.refundIds() == null || command.refundIds().isEmpty()) {
      throw new IllegalArgumentException("처리할 환불 ID 목록이 비어있습니다.");
    }
    
    int success = 0;
    int failed = 0;
    StringBuilder failureMessages = new StringBuilder();
    
    for (Long refundId : command.refundIds()) {
      try {
        processRefund(refundId, new RefundDTOs.AdminRefundProcessCommand(command.action(), command.reason()));
        success++;
      } catch (Exception e) {
        failed++;
        failureMessages.append("ID ").append(refundId).append(": ").append(e.getMessage()).append("; ");
      }
    }
    
    return new RefundDTOs.AdminBulkRefundResponse(
        success, 
        failed
    );
  }


  @Override
  @Transactional
  public void cancelRefund(Long refundId, String reason) {
    // 입력 검증
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("취소 사유를 입력해야 합니다.");
    }
    
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("환불 내역을 찾을 수 없습니다."));
    
    // 상태 검증 (완료된 환불은 취소할 수 없음)
    if (refund.getStatus() == RefundStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 환불은 취소할 수 없습니다.");
    }
    
    if (refund.getStatus() == RefundStatus.FAILED) {
      throw new IllegalStateException("이미 실패 처리된 환불입니다.");
    }
    
    // 환불 실패 처리 (관리자 취소)
    refund.markAsFailed("관리자 취소: " + reason);
    refundRepository.save(refund);
  }
}