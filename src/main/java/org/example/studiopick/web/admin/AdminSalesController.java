package org.example.studiopick.web.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminSalesServiceImpl;
import org.example.studiopick.application.admin.dto.sales.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSalesController {
  private final SystemSettingUtils settingUtils;
  private final AdminSalesServiceImpl adminSalesServiceImpl;

  /**
   * 전체 매출 통계 조회
   * GET /api/admin/sales/stats
   */
  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<AdminSalesStatsResponse>> getSalesStats() {
    log.info("전체 매출 통계 조회 요청");

    AdminSalesStatsResponse response = adminSalesServiceImpl.getSalesStats();

    ApiResponse<AdminSalesStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "매출 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 기간별 매출 트렌드 분석
   * GET /api/admin/sales/trend?startDate=2025-01-01&endDate=2025-01-31&period=daily
   */
  @GetMapping("/trend")
  public ResponseEntity<ApiResponse<AdminSalesTrendResponse>> getSalesTrend(
      @RequestParam String startDate,
      @RequestParam String endDate,
      @RequestParam(defaultValue = "daily") String period
  ) {
    log.info("매출 트렌드 분석 요청: startDate={}, endDate={}, period={}",
        startDate, endDate, period);

    AdminSalesTrendResponse response = adminSalesServiceImpl.getSalesTrend(startDate, endDate, period);

    ApiResponse<AdminSalesTrendResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "매출 트렌드를 분석했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오별 매출 분석
   * GET /api/admin/sales/studios?page=1&size=10&startDate=2025-01-01&endDate=2025-01-31
   */
  @GetMapping("/studios")
  public ResponseEntity<ApiResponse<AdminStudioSalesResponse>> getStudioSalesAnalysis(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate
  ) {
    log.info("스튜디오별 매출 분석 요청: page={}, size={}, startDate={}, endDate={}",
        page, size, startDate, endDate);

    int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);


    AdminStudioSalesResponse response = adminSalesServiceImpl.getStudioSalesAnalysis(
        page, pageSize, startDate, endDate);

    ApiResponse<AdminStudioSalesResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오별 매출을 분석했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 결제 방법별 통계
   * GET /api/admin/sales/payment-methods?startDate=2025-01-01&endDate=2025-01-31
   */
  @GetMapping("/payment-methods")
  public ResponseEntity<ApiResponse<AdminPaymentMethodStatsResponse>> getPaymentMethodStats(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate
  ) {
    log.info("결제 방법별 통계 요청: startDate={}, endDate={}", startDate, endDate);

    AdminPaymentMethodStatsResponse response = adminSalesServiceImpl.getPaymentMethodStats(
        startDate, endDate);

    ApiResponse<AdminPaymentMethodStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "결제 방법별 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 환불 통계 및 분석
   * GET /api/admin/sales/refunds?startDate=2025-01-01&endDate=2025-01-31
   */
  @GetMapping("/refunds")
  public ResponseEntity<ApiResponse<AdminRefundStatsResponse>> getRefundStats(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate
  ) {
    log.info("환불 통계 분석 요청: startDate={}, endDate={}", startDate, endDate);

    AdminRefundStatsResponse response = adminSalesServiceImpl.getRefundStats(startDate, endDate);

    ApiResponse<AdminRefundStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "환불 통계를 분석했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 매출 상세 내역 조회
   * GET /api/admin/sales/details?page=1&size=10&startDate=2025-01-01&endDate=2025-01-31&method=card&status=paid
   */
  @GetMapping("/details")
  public ResponseEntity<ApiResponse<AdminSalesDetailResponse>> getSalesDetails(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String status
  ) {
    log.info("매출 상세 내역 조회 요청: page={}, size={}, startDate={}, endDate={}, method={}, status={}",
        page, size, startDate, endDate, method, status);

    int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);

    AdminSalesDetailResponse response = adminSalesServiceImpl.getSalesDetails(
        page, pageSize, startDate, endDate, method, status);

    ApiResponse<AdminSalesDetailResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "매출 상세 내역을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 오늘 매출 현황 (빠른 접근용)
   * GET /api/admin/sales/today
   */
  @GetMapping("/today")
  public ResponseEntity<ApiResponse<AdminSalesTrendResponse>> getTodaySales() {
    log.info("오늘 매출 현황 조회 요청");

    String today = java.time.LocalDate.now().toString();
    AdminSalesTrendResponse response = adminSalesServiceImpl.getSalesTrend(today, today, "daily");

    ApiResponse<AdminSalesTrendResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "오늘 매출 현황을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 이번 달 매출 현황 (빠른 접근용)
   * GET /api/admin/sales/this-month
   */
  @GetMapping("/this-month")
  public ResponseEntity<ApiResponse<AdminSalesTrendResponse>> getThisMonthSales() {
    log.info("이번 달 매출 현황 조회 요청");

    java.time.LocalDate now = java.time.LocalDate.now();
    String startOfMonth = now.withDayOfMonth(1).toString();
    String endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString();

    AdminSalesTrendResponse response = adminSalesServiceImpl.getSalesTrend(
        startOfMonth, endOfMonth, "daily");

    ApiResponse<AdminSalesTrendResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "이번 달 매출 현황을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 이번 년도 월별 매출 트렌드 (빠른 접근용)
   * GET /api/admin/sales/this-year
   */
  @GetMapping("/this-year")
  public ResponseEntity<ApiResponse<AdminSalesTrendResponse>> getThisYearSales() {
    log.info("이번 년도 매출 트렌드 조회 요청");

    java.time.LocalDate now = java.time.LocalDate.now();
    String startOfYear = now.withDayOfYear(1).toString();
    String endOfYear = now.withDayOfYear(now.lengthOfYear()).toString();

    AdminSalesTrendResponse response = adminSalesServiceImpl.getSalesTrend(
        startOfYear, endOfYear, "monthly");

    ApiResponse<AdminSalesTrendResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "이번 년도 매출 트렌드를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 매출 요약 대시보드 (빠른 접근용)
   * GET /api/admin/sales/dashboard
   */
  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<AdminSalesDashboardResponse>> getSalesDashboard() {
    log.info("매출 대시보드 조회 요청");

    // 기본 통계
    AdminSalesStatsResponse stats = adminSalesServiceImpl.getSalesStats();

    // 최근 7일 트렌드
    java.time.LocalDate now = java.time.LocalDate.now();
    String weekAgo = now.minusDays(6).toString();
    String today = now.toString();
    AdminSalesTrendResponse weekTrend = adminSalesServiceImpl.getSalesTrend(weekAgo, today, "daily");

    // 결제 방법별 통계 (최근 30일)
    String monthAgo = now.minusDays(29).toString();
    AdminPaymentMethodStatsResponse methodStats = adminSalesServiceImpl.getPaymentMethodStats(monthAgo, today);

    AdminSalesDashboardResponse dashboard = new AdminSalesDashboardResponse(
        stats,
        weekTrend,
        methodStats
    );

    ApiResponse<AdminSalesDashboardResponse> apiResponse = new ApiResponse<>(
        true,
        dashboard,
        "매출 대시보드를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 대시보드용 응답 DTO (컨트롤러 내부 클래스)
   */
  public record AdminSalesDashboardResponse(
      AdminSalesStatsResponse stats,
      AdminSalesTrendResponse weekTrend,
      AdminPaymentMethodStatsResponse methodStats
  ) {}
}