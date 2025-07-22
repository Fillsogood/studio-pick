package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.sales.*;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.*;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSalesServiceImpl implements AdminSalesService {

  private final JpaPaymentRepository paymentRepository;
  private final PaginationValidator paginationValidator;
  private final JpaUserRepository userRepository;
  private final JpaStudioRepository studioRepository;
  private final JpaWorkShopRepository workshopRepository;

  /**
   * 전체 매출 통계 조회
   */
  @Override
  public AdminSalesStatsResponse getSalesStats() {
    // 전체 매출
    BigDecimal totalSales = paymentRepository.getTotalSalesByStatus(PaymentStatus.PAID);

    // 오늘 매출
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
    BigDecimal todaySales = paymentRepository.getSalesByDateRangeAndStatus(
        todayStart, todayEnd, PaymentStatus.PAID);

    // 이번 달 매출
    LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);
    BigDecimal thisMonthSales = paymentRepository.getSalesByDateRangeAndStatus(
        monthStart, monthEnd, PaymentStatus.PAID);

    // 이번 년도 매출
    LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
    LocalDateTime yearEnd = LocalDate.now().atTime(LocalTime.MAX);
    BigDecimal thisYearSales = paymentRepository.getSalesByDateRangeAndStatus(
        yearStart, yearEnd, PaymentStatus.PAID);

    // 환불 금액
    BigDecimal totalRefunds = paymentRepository.getTotalSalesByStatus(PaymentStatus.REFUNDED);

    // 결제 건수 통계
    long totalPayments = paymentRepository.countByStatus(PaymentStatus.PAID);
    long totalRefundCount = paymentRepository.countByStatus(PaymentStatus.REFUNDED);
    long totalStudios = studioRepository.countByStatus(StudioStatus.ACTIVE);
    long totalWorkShop = workshopRepository.countByStatus(WorkShopStatus.ACTIVE);
    long total = totalStudios + totalWorkShop;
    long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);


    return new AdminSalesStatsResponse(
        totalSales != null ? totalSales : BigDecimal.ZERO,
        todaySales != null ? todaySales : BigDecimal.ZERO,
        thisMonthSales != null ? thisMonthSales : BigDecimal.ZERO,
        thisYearSales != null ? thisYearSales : BigDecimal.ZERO,
        totalRefunds != null ? totalRefunds : BigDecimal.ZERO,
        totalPayments,
        totalRefundCount,
        total,
        activeUsers
    );
  }

  /**
   * 기간별 매출 트렌드 분석
   */
  @Override
  public AdminSalesTrendResponse getSalesTrend(String startDate, String endDate, String period) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

    List<AdminSalesTrendData> trendData = new ArrayList<>();

    switch (period.toLowerCase()) {
      case "daily" -> trendData = getDailySalesTrend(start, end);
      case "monthly" -> trendData = getMonthlySalesTrend(start, end);
      case "yearly" -> trendData = getYearlySalesTrend(start, end);
      default -> throw new IllegalArgumentException("지원하지 않는 기간 타입입니다: " + period);
    }

    // 기간별 총 매출
    BigDecimal totalSales = paymentRepository.getSalesByDateRangeAndStatus(
        startDateTime, endDateTime, PaymentStatus.PAID);

    return new AdminSalesTrendResponse(
        startDate,
        endDate,
        period,
        totalSales != null ? totalSales : BigDecimal.ZERO,
        trendData
    );
  }

  /**
   * 스튜디오별 매출 분석
   */
  @Override
  public AdminStudioSalesResponse getStudioSalesAnalysis(
      int page, int size, String startDate, String endDate) {

    paginationValidator.validatePaginationParameters(page, size);

    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

    Pageable pageable = PageRequest.of(page - 1, size);

    // 스튜디오별 매출 데이터 조회
    List<AdminStudioSalesData> studioSalesData = paymentRepository.getStudioSalesData(
        startDateTime, endDateTime, PaymentStatus.PAID, pageable);

    // 전체 스튜디오 수 (페이징용)
    long totalStudios = paymentRepository.countDistinctStudiosByDateRange(startDateTime, endDateTime);

    return new AdminStudioSalesResponse(
        start.toString(),
        end.toString(),
        studioSalesData,
        new AdminSalesPaginationResponse(page, totalStudios, (int) Math.ceil((double) totalStudios / size))
    );
  }

  /**
   * 결제 방법별 통계
   */
  @Override
  public AdminPaymentMethodStatsResponse getPaymentMethodStats(String startDate, String endDate) {
    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

    // 결제 방법별 통계 조회
    List<AdminPaymentMethodData> methodStats = paymentRepository.getPaymentMethodStats(
        startDateTime, endDateTime, PaymentStatus.PAID);

    // 전체 매출 (비율 계산용)
    BigDecimal totalSales = paymentRepository.getSalesByDateRangeAndStatus(
        startDateTime, endDateTime, PaymentStatus.PAID);

    return new AdminPaymentMethodStatsResponse(
        start.toString(),
        end.toString(),
        totalSales != null ? totalSales : BigDecimal.ZERO,
        methodStats
    );
  }

  /**
   * 환불 통계 및 분석
   */
  @Override
  public AdminRefundStatsResponse getRefundStats(String startDate, String endDate) {
    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

    // 환불 통계
    BigDecimal totalRefunds = paymentRepository.getSalesByDateRangeAndStatus(
        startDateTime, endDateTime, PaymentStatus.REFUNDED);

    long refundCount = paymentRepository.countByDateRangeAndStatus(
        startDateTime, endDateTime, PaymentStatus.REFUNDED);

    // 전체 매출 대비 환불률 계산
    BigDecimal totalSales = paymentRepository.getSalesByDateRangeAndStatus(
        startDateTime, endDateTime, PaymentStatus.PAID);

    double refundRate = 0.0;
    if (totalSales != null && totalSales.compareTo(BigDecimal.ZERO) > 0) {
      refundRate = totalRefunds.divide(totalSales, 4, BigDecimal.ROUND_HALF_UP)
          .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    // 일별 환불 트렌드
    List<AdminRefundTrendData> refundTrend = getDailyRefundTrend(start, end);

    return new AdminRefundStatsResponse(
        start.toString(),
        end.toString(),
        totalRefunds != null ? totalRefunds : BigDecimal.ZERO,
        refundCount,
        refundRate,
        refundTrend
    );
  }

  /**
   * 매출 상세 내역 조회
   */
  @Override
  public AdminSalesDetailResponse getSalesDetails(
      int page, int size, String startDate, String endDate, String method, String status) {

    paginationValidator.validatePaginationParameters(page, size);

    LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

    Pageable pageable = PageRequest.of(page - 1, size);

    PaymentMethod paymentMethod = method != null ? PaymentMethod.valueOf(method.toUpperCase()) : null;
    PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;

    // 조건에 따른 결제 내역 조회
    Page<Payment> paymentsPage = getFilteredPayments(
        startDateTime, endDateTime, paymentMethod, paymentStatus, pageable);

    List<AdminSalesDetailData> salesDetails = paymentsPage.getContent()
        .stream()
        .map(this::toAdminSalesDetailData)
        .toList();

    return new AdminSalesDetailResponse(
        start.toString(),
        end.toString(),
        salesDetails,
        new AdminSalesPaginationResponse(page, paymentsPage.getTotalElements(), paymentsPage.getTotalPages())
    );
  }

  // Private helper methods

  private List<AdminSalesTrendData> getDailySalesTrend(LocalDate start, LocalDate end) {
    List<AdminSalesTrendData> trendData = new ArrayList<>();
    LocalDate current = start;

    while (!current.isAfter(end)) {
      LocalDateTime dayStart = current.atStartOfDay();
      LocalDateTime dayEnd = current.atTime(LocalTime.MAX);

      BigDecimal dailySales = paymentRepository.getSalesByDateRangeAndStatus(
          dayStart, dayEnd, PaymentStatus.PAID);

      trendData.add(new AdminSalesTrendData(
          current.toString(),
          dailySales != null ? dailySales : BigDecimal.ZERO
      ));

      current = current.plusDays(1);
    }

    return trendData;
  }

  private List<AdminSalesTrendData> getMonthlySalesTrend(LocalDate start, LocalDate end) {
    List<AdminSalesTrendData> trendData = new ArrayList<>();
    LocalDate current = start.withDayOfMonth(1);

    while (!current.isAfter(end)) {
      LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
      LocalDateTime monthStart = current.atStartOfDay();
      LocalDateTime monthEndTime = monthEnd.atTime(LocalTime.MAX);

      BigDecimal monthlySales = paymentRepository.getSalesByDateRangeAndStatus(
          monthStart, monthEndTime, PaymentStatus.PAID);

      trendData.add(new AdminSalesTrendData(
          current.format(DateTimeFormatter.ofPattern("yyyy-MM")),
          monthlySales != null ? monthlySales : BigDecimal.ZERO
      ));

      current = current.plusMonths(1);
    }

    return trendData;
  }

  private List<AdminSalesTrendData> getYearlySalesTrend(LocalDate start, LocalDate end) {
    List<AdminSalesTrendData> trendData = new ArrayList<>();
    LocalDate current = start.withDayOfYear(1);

    while (!current.isAfter(end)) {
      LocalDate yearEnd = current.withDayOfYear(current.lengthOfYear());
      LocalDateTime yearStart = current.atStartOfDay();
      LocalDateTime yearEndTime = yearEnd.atTime(LocalTime.MAX);

      BigDecimal yearlySales = paymentRepository.getSalesByDateRangeAndStatus(
          yearStart, yearEndTime, PaymentStatus.PAID);

      trendData.add(new AdminSalesTrendData(
          String.valueOf(current.getYear()),
          yearlySales != null ? yearlySales : BigDecimal.ZERO
      ));

      current = current.plusYears(1);
    }

    return trendData;
  }

  private List<AdminRefundTrendData> getDailyRefundTrend(LocalDate start, LocalDate end) {
    List<AdminRefundTrendData> trendData = new ArrayList<>();
    LocalDate current = start;

    while (!current.isAfter(end)) {
      LocalDateTime dayStart = current.atStartOfDay();
      LocalDateTime dayEnd = current.atTime(LocalTime.MAX);

      BigDecimal dailyRefunds = paymentRepository.getSalesByDateRangeAndStatus(
          dayStart, dayEnd, PaymentStatus.REFUNDED);

      long refundCount = paymentRepository.countByDateRangeAndStatus(
          dayStart, dayEnd, PaymentStatus.REFUNDED);

      trendData.add(new AdminRefundTrendData(
          current.toString(),
          dailyRefunds != null ? dailyRefunds : BigDecimal.ZERO,
          refundCount
      ));

      current = current.plusDays(1);
    }

    return trendData;
  }

  private Page<Payment> getFilteredPayments(
      LocalDateTime startDateTime, LocalDateTime endDateTime,
      PaymentMethod method, PaymentStatus status, Pageable pageable) {

    if (method != null && status != null) {
      return paymentRepository.findByPaidAtBetweenAndMethodAndStatusOrderByPaidAtDesc(
          startDateTime, endDateTime, method, status, pageable);
    } else if (method != null) {
      return paymentRepository.findByPaidAtBetweenAndMethodOrderByPaidAtDesc(
          startDateTime, endDateTime, method, pageable);
    } else if (status != null) {
      return paymentRepository.findByPaidAtBetweenAndStatusOrderByPaidAtDesc(
          startDateTime, endDateTime, status, pageable);
    } else {
      return paymentRepository.findByPaidAtBetweenOrderByPaidAtDesc(
          startDateTime, endDateTime, pageable);
    }
  }

  private AdminSalesDetailData toAdminSalesDetailData(Payment payment) {
    return new AdminSalesDetailData(
        payment.getId(),
        payment.getOrderId(),
        payment.getReservation().getUser().getName(),
        payment.getReservation().getStudio().getName(),
        payment.getAmount(),
        payment.getMethod() != null ? payment.getMethod().getValue() : null,
        payment.getStatus().getValue(),
        payment.getPaidAt(),
        payment.getCreatedAt()
    );
  }
}
