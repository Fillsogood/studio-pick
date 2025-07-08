package org.example.studiopick.application.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.payment.dto.TossPaymentCancelRequest;
import org.example.studiopick.application.payment.dto.TossPaymentCancelResponse;
import org.example.studiopick.application.payment.dto.TossPaymentConfirmRequest;
import org.example.studiopick.application.payment.dto.TossPaymentResponse;
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
   * 토스페이먼츠 결제 승인 처리
   * @param paymentKey 결제 키
   * @param orderId 주문 ID
   * @param amount 결제 금액
   * @return 결제 승인 결과
   */
  public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {

    WebClient webClient = createWebClient();

    TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
        .orderId(orderId)
        .amount(amount)
        .build();

    try {
      return webClient.post()
          .uri("/v1/payments/confirm")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(TossPaymentResponse.class)
          .block();

    } catch (Exception e) {
      log.error("토스페이먼츠 결제 승인 실패: {}", e.getMessage());
      throw new RuntimeException("결제 승인 처리 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 토스페이먼츠 결제 취소 처리
   * @param paymentKey 결제 키
   * @param cancelReason 취소 사유
   * @return 결제 취소 결과
   */
  public TossPaymentCancelResponse cancelPayment(String paymentKey, String cancelReason) {

    WebClient webClient = createWebClient();

    TossPaymentCancelRequest request = TossPaymentCancelRequest.builder()
        .cancelReason(cancelReason)
        .build();

    try {
      return webClient.post()
          .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(TossPaymentCancelResponse.class)
          .block();

    } catch (Exception e) {
      log.error("토스페이먼츠 결제 취소 실패: {}", e.getMessage());
      throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 토스페이먼츠 결제 정보 조회
   * @param paymentKey 결제 키
   * @return 결제 정보
   */
  public TossPaymentResponse getPayment(String paymentKey) {

    WebClient webClient = createWebClient();

    try {
      return webClient.get()
          .uri("/v1/payments/{paymentKey}", paymentKey)
          .retrieve()
          .bodyToMono(TossPaymentResponse.class)
          .block();

    } catch (Exception e) {
      log.error("토스페이먼츠 결제 조회 실패: {}", e.getMessage());
      throw new RuntimeException("결제 정보 조회 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 토스페이먼츠 API 호출용 WebClient 생성
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
}

