package org.example.studiopick.application.workshop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.review.service.ReviewService; // ✅ 추가
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.workshop.WorkShopImage;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopImageRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkShopServiceImpl implements WorkShopService {

  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final JpaUserRepository userRepository;
  private final SystemSettingUtils settingUtils;
  private final JpaWorkShopImageRepository workShopImageRepository;
  private final S3Uploader s3Uploader;
  private final ReviewService reviewService; // 추가됨

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

    List<WorkShopListDto> result = workshops.stream()
            .map(c -> {
              Double rating = reviewService.getAverageRatingByWorkshopId(c.getId()); // ✅ 평점 계산

              return new WorkShopListDto(
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
                      rating != null ? rating : 0.0 // ✅ DTO에 주입
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
    WorkShop ce = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    return new WorkShopDetailDto(
            ce.getId(),
            ce.getTitle(),
            ce.getDescription(),
            ce.getPrice(),
            ce.getDate(),
            ce.getStartTime(),
            ce.getEndTime(),
            ce.getInstructor(),
            defaultMaxParticipants,
            getDefaultSupplies()
    );
  }

  @Override
  @Transactional
  public WorkShopApplicationResponse applyWorkshop(WorkShopApplicationRequest request, Long userId) {
    User owner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    LocalDate date = LocalDate.parse(request.date());
    LocalTime startTime = LocalTime.of(request.startTime().hour(), request.startTime().minute(), request.startTime().second(), request.startTime().nano());
    LocalTime endTime = LocalTime.of(request.endTime().hour(), request.endTime().minute(), request.endTime().second(), request.endTime().nano());

    WorkShop workshop = WorkShop.builder()
            .owner(owner)
            .title(request.title())
            .description(request.description())
            .price(request.price())
            .date(date)
            .instructor(request.instructor())
            .startTime(startTime)
            .endTime(endTime)
            .thumbnailUrl(request.thumbnailUrl())
            .address(request.address())
            .build();

    jpaWorkShopRepository.save(workshop);

    if (request.imageUrls() != null) {
      request.imageUrls().forEach(url -> {
        WorkShopImage image = WorkShopImage.builder()
                .workShop(workshop)
                .imageUrl(url)
                .build();
        workShopImageRepository.save(image);
      });
    }

    return new WorkShopApplicationResponse(workshop.getId(), workshop.getStatus().name().toLowerCase());
  }

  @Override
  public WorkShopApplicationDetailResponse getWorkshopApplicationStatus(Long workshopId) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    return new WorkShopApplicationDetailResponse(
            workshop.getId(),
            workshop.getTitle(),
            workshop.getStatus().name().toLowerCase(),
            workshop.getCreatedAt(),
            getStatusMessage(workshop.getStatus())
    );
  }

  @Override
  public List<String> uploadClassImages(List<MultipartFile> files) {
    return s3Uploader.uploadFiles(files, "classes");
  }

  @Override
  @Transactional
  public void updateWorkshop(Long workshopId, WorkShopApplicationRequest request) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    LocalDate date = LocalDate.parse(request.date());
    LocalTime startTime = LocalTime.of(request.startTime().hour(), request.startTime().minute(), request.startTime().second(), request.startTime().nano());
    LocalTime endTime = LocalTime.of(request.endTime().hour(), request.endTime().minute(), request.endTime().second(), request.endTime().nano());

    workshop.updateBasicInfo(request.title(), request.description(), request.price());
    workshop.updateSchedule(date, startTime, endTime);
  }

  @Override
  @Transactional
  public void deactivateWorkshop(Long workshopId) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));
    workshop.deactivate();
  }

  @Override
  @Transactional
  public Long activateAndCreateWorkshop(Long workshopApplicationId, WorkShopCreateCommand command, Long adminUserId) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopApplicationId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    workshop.activate();

    LocalDate date = LocalDate.parse(command.date());
    LocalTime startTime = LocalTime.of(command.startTime().hour(), command.startTime().minute(), command.startTime().second(), command.startTime().nano());
    LocalTime endTime = LocalTime.of(command.endTime().hour(), command.endTime().minute(), command.endTime().second(), command.endTime().nano());

    workshop.updateBasicInfo(command.title(), command.description(), command.price());
    workshop.updateSchedule(date, startTime, endTime);
    workshop.updateThumbnail(command.thumbnailUrl());

    workShopImageRepository.deleteByWorkShop(workshop);

    if (command.imageUrls() != null) {
      command.imageUrls().forEach(url -> {
        WorkShopImage image = WorkShopImage.builder()
                .workShop(workshop)
                .imageUrl(url)
                .build();
        workShopImageRepository.save(image);
      });
    }

    return workshop.getId();
  }

  private List<String> getDefaultSupplies() {
    String suppliesConfig = settingUtils.getStringSetting("class.default.supplies", "");
    if (suppliesConfig.isEmpty()) {
      return List.of();
    }
    return List.of(suppliesConfig.split(","));
  }

  private String getStatusMessage(WorkShopStatus status) {
    return switch (status) {
      case PENDING -> "승인 대기 중입니다.";
      case ACTIVE -> "운영 중인 클래스입니다.";
      case INACTIVE -> "비활성화된 클래스입니다.";
    };
  }
}
