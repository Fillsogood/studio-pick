package org.example.studiopick.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.payment.PaymentService;
import org.example.studiopick.application.payment.dto.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청 (예약과 함께 결제 정보 생성)
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<PaymentRequestResponse>> requestPayment(
            @RequestBody PaymentRequestCommand command
    ) {
        PaymentRequestResponse response = paymentService.requestPayment(command);
        
        ApiResponse<PaymentRequestResponse> apiResponse = new ApiResponse<>(
                true,
                response,
                "결제 요청이 생성되었습니다."
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * 결제 승인 (토스페이먼츠 결제 완료 후 호출)
     */
    @PostMapping("/confirm")
    public ApiResponse<PaymentConfirmResponse> confirmPayment(
            @RequestBody PaymentConfirmCommand command
    ) {
        PaymentConfirmResponse response = paymentService.confirmPayment(command);
        return new ApiResponse<>(true, response, "결제가 승인되었습니다.");
    }

    /**
     * 결제 취소 (예약 취소시 호출)
     */
    @PostMapping("/{paymentKey}/cancel")
    public ApiResponse<PaymentCancelResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelCommand command
    ) {
        PaymentCancelResponse response = paymentService.cancelPayment(paymentKey, command);
        return new ApiResponse<>(true, response, "결제가 취소되었습니다.");
    }

    /**
     * 결제 정보 조회
     */
    @GetMapping("/{paymentKey}")
    public ApiResponse<PaymentInfoResponse> getPaymentInfo(
            @PathVariable String paymentKey
    ) {
        PaymentInfoResponse response = paymentService.getPaymentInfo(paymentKey);
        return new ApiResponse<>(true, response, null);
    }

    /**
     * 예약별 결제 정보 조회
     */
    @GetMapping("/reservation/{reservationId}")
    public ApiResponse<PaymentInfoResponse> getPaymentByReservation(
            @PathVariable Long reservationId
    ) {
        PaymentInfoResponse response = paymentService.getPaymentByReservation(reservationId);
        return new ApiResponse<>(true, response, null);
    }
    
    /**
     * 내 결제 내역 조회
     */
    @GetMapping("/my")
    public ApiResponse<UserPaymentHistoryListResponse> getMyPaymentHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        UserPaymentHistoryListResponse response = paymentService.getUserPaymentHistory(
                userId, page, size, status, startDate, endDate);
        return new ApiResponse<>(true, response, null);
    }
}
