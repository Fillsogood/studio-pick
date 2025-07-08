package org.example.studiopick.application.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.payment.dto.TossPaymentCancelRequest;
import org.example.studiopick.application.payment.dto.TossPaymentCancelResponse;
import org.example.studiopick.application.payment.dto.TossPaymentConfirmRequest;
import org.example.studiopick.application.payment.dto.TossPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentsService {
    private final WebClient.Builder webClientBuilder;

    @Value("${toss.payments.test-secret-key}")
    private String secretKey;

    @Value("${toss.payments.base-url:https://api.tosspayments.com}")
    private String baseUrl;

    /**
     * ✅ 수정된 토스페이먼츠 결제 승인 처리
     */
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {

        log.info("결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}",
            paymentKey, orderId, amount);

        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PaymentKey는 필수입니다.");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }

        WebClient webClient = createWebClient();

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
            .paymentKey(paymentKey)  // ✅ paymentKey 추가!
            .orderId(orderId)
            .amount(amount)
            .build();

        try {
            // ✅ 올바른 토스 API 엔드포인트 사용
            String requestUrl = "/v1/payments/confirm";
            log.info("토스페이먼츠 API 호출: {} - PaymentKey: {}", requestUrl, paymentKey);
            log.info("요청 데이터: {}", request);

            TossPaymentResponse response = webClient.post()
                .uri("/v1/payments/confirm")  // ✅ 올바른 엔드포인트
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    httpStatus -> httpStatus.is4xxClientError(),
                    clientResponse -> {
                        log.error("4xx 클라이언트 오류 발생: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                            .map(body -> {
                                log.error("응답 본문: {}", body);
                                return new RuntimeException("결제 승인 실패: " + clientResponse.statusCode() + " - " + body);
                            });
                    }
                )
                .onStatus(
                    httpStatus -> httpStatus.is5xxServerError(),
                    clientResponse -> {
                        log.error("5xx 서버 오류 발생: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                            .map(body -> new RuntimeException("토스페이먼츠 서버 오류: " + body));
                    }
                )
                .bodyToMono(TossPaymentResponse.class)
                .block();

            log.info("토스페이먼츠 결제 승인 성공: paymentKey={}", paymentKey);
            return response;

        } catch (Exception e) {
            log.error("결제 승인 예외 발생 - paymentKey={}, orderId={}, error={}",
                paymentKey, orderId, e.getMessage());

            if (e.getMessage().contains("400")) {
                throw new RuntimeException(
                    String.format("잘못된 결제 요청입니다. PaymentKey: %s, OrderId: %s, Amount: %s",
                        paymentKey, orderId, amount), e);
            } else if (e.getMessage().contains("404")) {
                throw new RuntimeException(
                    String.format("결제 정보를 찾을 수 없습니다. PaymentKey: %s", paymentKey), e);
            } else {
                throw new RuntimeException("결제 승인 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
    }

    /**
     * ✅ 토스페이먼츠 API 호출용 WebClient 생성 (개선)
     */
    private WebClient createWebClient() {
        String credentials = secretKey + ":";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    // ✅ 기존 메서드들은 그대로 유지
    public TossPaymentResponse getPayment(String paymentKey) {
        WebClient webClient = createWebClient();

        try {
            log.info("PaymentKey로 결제 조회: {}", paymentKey);

            return webClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .onStatus(
                    httpStatus -> httpStatus.is4xxClientError(),
                    clientResponse -> {
                        log.error("결제 조회 실패 - 4xx 오류: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                            .map(body -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + body));
                    }
                )
                .bodyToMono(TossPaymentResponse.class)
                .block();

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패: paymentKey={}, error={}", paymentKey, e.getMessage());
            throw new RuntimeException("결제 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public TossPaymentCancelResponse cancelPayment(String paymentKey, String cancelReason) {
        WebClient webClient = createWebClient();

        TossPaymentCancelRequest request = TossPaymentCancelRequest.builder()
            .cancelReason(cancelReason)
            .build();

        try {
            log.info("결제 취소 요청: paymentKey={}, reason={}", paymentKey, cancelReason);

            return webClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentCancelResponse.class)
                .block();

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패: paymentKey={}, error={}", paymentKey, e.getMessage());
            throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
