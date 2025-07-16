package org.example.studiopick.application.workshop;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
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

  @Override
  public WorkShopListResponse getWorkShopList(String status, String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    HideStatus hideStatus = HideStatus.valueOf(status.toUpperCase());

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    List<WorkShopListDto> result = jpaWorkShopRepository
        .findByHideStatusAndDate(hideStatus, parsedDate)
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
        getDefaultSupplies() //  별도 메서드로 분리
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
        .status(HideStatus.OPEN)
        .build();

    jpaWorkShopRepository.save(workshop);

    return new WorkShopApplicationResponse(workshop.getId(), workshop.getHideStatus().name().toLowerCase());
  }

  @Override
  public WorkShopApplicationDetailResponse getWorkshopApplicationStatus(Long workshopId) {
    WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
        .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));

    return new WorkShopApplicationDetailResponse(
        workshop.getId(),
        workshop.getTitle(),
        workshop.getHideStatus().name().toLowerCase(),
        workshop.getCreatedAt(),
        getStatusMessage(workshop.getHideStatus())
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

    workshop.close();
  }

  private List<String> getDefaultSupplies() {
    // 시스템 설정에서 기본 준비물 조회하거나, 빈 리스트 반환
    String suppliesConfig = settingUtils.getStringSetting("class.default.supplies", "");
    if (suppliesConfig.isEmpty()) {
      return List.of(); // 빈 목록 반환
    }
    return List.of(suppliesConfig.split(",")); // 콤마로 구분된 문자열을 리스트로 변환
  }

  private String getStatusMessage(HideStatus status) {
    return switch (status) {
      case OPEN -> "운영 중인 공방입니다.";
      case CLOSED -> "운영이 종료되었습니다.";
      case REPORTED -> "신고 접수로 인해 숨김 처리되었습니다.";
    };
  }
}