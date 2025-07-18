package org.example.studiopick.application.workshop;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.workshop.WorkShopImage;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopImageRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkShopServiceImpl implements WorkShopService {

  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final JpaUserRepository userRepository;
  private final SystemSettingUtils settingUtils;
  private final JpaWorkShopImageRepository workShopImageRepository;

  @Override
  public WorkShopListResponse getWorkShopList(String status, String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    WorkShopStatus workshopStatus = WorkShopStatus.valueOf(status.toUpperCase());

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    List<WorkShopListDto> result = jpaWorkShopRepository
            .findByStatusAndDate(workshopStatus, parsedDate)
            .stream()
            .map(c -> new WorkShopListDto(
                    c.getId(),
                    c.getTitle(),
                    c.getDescription(),
                    c.getPrice(),
                    c.getDate(),
                    c.getStartTime(),
                    c.getEndTime(),
                    defaultMaxParticipants,
                    c.getReservations().size()
            ))
            .toList();

    return new WorkShopListResponse(result);
  }

  @Override
  public WorkShopDetailDto getWorkShopDetail(Long workshopId) {
    WorkShop ce = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방를 찾을 수 없습니다."));

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

    WorkShop workshop = WorkShop.builder()
            .owner(owner)
            .title(request.title())
            .description(request.description())
            .price(request.price())
            .date(request.date())
            .instructor(request.instructor())
            .startTime(request.startTime())
            .endTime(request.endTime())
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
  @Transactional
  public void updateWorkshop(Long workshopId, WorkShopApplicationRequest request) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    workshop.updateBasicInfo(request.title(), request.description(), request.price());
    workshop.updateSchedule(request.date(), request.startTime(), request.endTime());
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

    workshop.updateBasicInfo(command.title(), command.description(), command.price());
    workshop.updateSchedule(command.date(), command.startTime(), command.endTime());
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
