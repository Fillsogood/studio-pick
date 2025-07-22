package org.example.studiopick.application.workshop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.review.service.ReviewService;
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.workshop.WorkShopImage;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopImageRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkShopServiceImpl implements WorkShopService {

  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final JpaUserRepository userRepository;
  private final SystemSettingUtils settingUtils;
  private final JpaWorkShopImageRepository workShopImageRepository;
  private final S3Uploader s3Uploader;
  private final ReviewService reviewService;
  private final JpaReservationRepository jpaReservationRepository;
  private final JpaPaymentRepository paymentRepository;

  @Override
  public WorkShopListResponse getWorkShopList(String status, String date) {
    WorkShopStatus workshopStatus = WorkShopStatus.valueOf(status.toUpperCase());
    List<WorkShop> workshops;
    if (date == null || date.isBlank() || date.equalsIgnoreCase("undefined")) {
      workshops = jpaWorkShopRepository.findByStatus(workshopStatus);
    } else {
      try {
        LocalDate parsedDate = LocalDate.parse(date);
        workshops = jpaWorkShopRepository.findByStatusAndDate(workshopStatus, parsedDate);
      } catch (Exception e) {
        log.error("날짜 파싱 실패: {}", date, e);
        throw new IllegalArgumentException("유효하지 않은 날짜 형식입니다.");
      }
    }

    var result = workshops.stream()
            .map(c -> {
              Double rating = reviewService.getAverageRatingByWorkshopId(c.getId());
              return new org.example.studiopick.application.workshop.dto.WorkShopListDto(
                      c.getId(),
                      c.getTitle(),
                      c.getDescription(),
                      c.getPrice(),
                      c.getInstructor(),
                      c.getThumbnailUrl(),
                      c.getImageUrls(),
                      c.getDate(),
                      c.getStartTime(),
                      c.getEndTime(),
                      rating != null ? rating : 0.0
              );
            })
            .toList();

    return new WorkShopListResponse(result);
  }

  @Override
  public void deleteClassImages(List<String> imageUrls) {
    s3Uploader.deleteFiles(imageUrls);
  }

  @Override
  public WorkShopDetailDto getWorkShopDetail(Long workshopId) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopId));

    int defaultMax = settingUtils.getIntegerSetting("class.default.max.participants", 8);
    return new WorkShopDetailDto(
            ws.getId(),
            ws.getTitle(),
            ws.getDescription(),
            ws.getPrice(),
            ws.getDate(),
            ws.getStartTime(),
            ws.getEndTime(),
            ws.getInstructor(),
            defaultMax,
            getDefaultSupplies(),
            ws.getAddress(),
            ws.getThumbnailUrl(),
            ws.getImageUrls()
    );
  }

  @Override
  @Transactional
  public WorkShopApplicationResponse applyWorkshop(WorkShopApplicationRequest request, Long userId) {
    User owner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

    LocalDate date = LocalDate.parse(request.date());
    LocalTime start = LocalTime.of(request.startTime().hour(), request.startTime().minute(), 0);
    LocalTime end   = LocalTime.of(request.endTime().hour(),   request.endTime().minute(),   0);

    WorkShop ws = WorkShop.builder()
            .owner(owner)
            .title(request.title())
            .description(request.description())
            .price(request.price())
            .date(date)
            .instructor(request.instructor())
            .startTime(start)
            .endTime(end)
            .thumbnailUrl(request.thumbnailUrl())
            .address(request.address())
            .build();
    jpaWorkShopRepository.save(ws);

    if (request.imageUrls() != null) {
      request.imageUrls().forEach(url ->
              workShopImageRepository.save(
                      WorkShopImage.builder()
                              .workShop(ws)
                              .imageUrl(url)
                              .build()
              )
      );
    }

    return new WorkShopApplicationResponse(ws.getId(), ws.getStatus().name().toLowerCase());
  }

  @Override
  public WorkShopApplicationDetailResponse getWorkshopApplicationStatus(Long workshopId) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopId));
    return new WorkShopApplicationDetailResponse(
            ws.getId(),
            ws.getTitle(),
            ws.getStatus().name().toLowerCase(),
            ws.getCreatedAt(),
            getStatusMessage(ws.getStatus())
    );
  }

  @Override
  public List<String> uploadClassImages(List<MultipartFile> files) {
    return s3Uploader.uploadFiles(files, "classes");
  }

  @Override
  @Transactional
  public void updateWorkshop(Long workshopId, WorkShopApplicationRequest request) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopId));

    LocalDate date = LocalDate.parse(request.date());
    LocalTime start = LocalTime.of(request.startTime().hour(), request.startTime().minute(), 0);
    LocalTime end   = LocalTime.of(request.endTime().hour(),   request.endTime().minute(),   0);

    ws.updateBasicInfo(request.title(), request.description(), request.price());
    ws.updateSchedule(date, start, end);
  }

  @Override
  @Transactional
  public void deactivateWorkshop(Long workshopId) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopId));
    ws.deactivate();
  }

  @Override
  @Transactional
  public Long activateAndCreateWorkshop(Long workshopApplicationId, WorkShopCreateCommand cmd, Long adminUserId) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopApplicationId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopApplicationId));

    ws.activate();
    LocalDate date = LocalDate.parse(cmd.date());
    LocalTime start = LocalTime.of(cmd.startTime().hour(), cmd.startTime().minute(), 0);
    LocalTime end   = LocalTime.of(cmd.endTime().hour(),   cmd.endTime().minute(),   0);

    ws.updateBasicInfo(cmd.title(), cmd.description(), cmd.price());
    ws.updateSchedule(date, start, end);
    ws.updateThumbnail(cmd.thumbnailUrl());

    workShopImageRepository.deleteByWorkShop(ws);
    if (cmd.imageUrls() != null) {
      cmd.imageUrls().forEach(url ->
              workShopImageRepository.save(
                      WorkShopImage.builder()
                              .workShop(ws)
                              .imageUrl(url)
                              .build()
              )
      );
    }
    return ws.getId();
  }

  private List<String> getDefaultSupplies() {
    String cfg = settingUtils.getStringSetting("class.default.supplies", "");
    return cfg.isEmpty() ? List.of() : List.of(cfg.split(","));
  }

  private String getStatusMessage(WorkShopStatus status) {
    return switch (status) {
      case PENDING  -> "승인 대기 중입니다.";
      case ACTIVE   -> "운영 중인 클래스입니다.";
      case INACTIVE -> "승인거절된 클래스입니다.";
      case HIDE -> "숨김처리된 클래스입니다.";
    };
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassManageItemResponseDto> getClassManageList(Long ownerUserId) {
    List<WorkShop> workshops = jpaWorkShopRepository.findByOwnerId(ownerUserId);
    List<Long> ids = workshops.stream().map(WorkShop::getId).toList();

    Map<Long, Integer> countMap = jpaReservationRepository
            .countByWorkshopIds(ids).stream()
            .collect(Collectors.toMap(
                    arr -> (Long) arr[0],
                    arr -> ((Long) arr[1]).intValue()
            ));

    Map<Long, BigDecimal> revenueMap = paymentRepository
            .sumPaidAmountByWorkshopIds(ids, PaymentStatus.PAID).stream()
            .collect(Collectors.toMap(
                    arr -> (Long) arr[0],
                    arr -> (BigDecimal) arr[1]
            ));

    return workshops.stream()
            .map(ws -> new ClassManageItemResponseDto(
                    ws.getId(),
                    ws.getTitle(),
                    ws.getDate(),
                    ws.getStatus().getValue(),
                    countMap.getOrDefault(ws.getId(), 0),
                    revenueMap.getOrDefault(ws.getId(), BigDecimal.ZERO)
            ))
            .toList();
  }

  // ────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void updateWorkshopStatus(Long workshopId, String status) {
    WorkShop ws = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다. id=" + workshopId));

    if ("ACTIVE".equalsIgnoreCase(status)) {
      ws.activate();
    } else if ("INACTIVE".equalsIgnoreCase(status)) {
      ws.deactivate();
    } else if ("HIDE".equalsIgnoreCase(status)) {
      ws.hide();  // HIDE와 deactivate()가 동일하다면 이렇게, 혹은 ws.hide()로 분리 구현
    } else {
      throw new IllegalArgumentException("유효하지 않은 상태 값: " + status);
    }
  }

}
