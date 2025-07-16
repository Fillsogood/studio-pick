package org.example.studiopick.application.workshop;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkShopServiceImpl implements WorkShopService {

  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final JpaUserRepository userRepository;
  private final SystemSettingUtils settingUtils;

  @Override
  public WorkShopListResponse getClassList(Long studioId, String status, String date) {
    LocalDate parsedDate = LocalDate.parse(date);

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    List<WorkShopListDto> result = jpaWorkShopRepository
        .findByStudioId_IdAndStatusAndDate(parsedDate)
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
            c.getReservations().size() // currentParticipants
        )).toList();

    return new WorkShopListResponse(result);
  }

  @Override
  public WorkShopDetailDto getClassDetail(Long classId) {
    WorkShop ce = jpaWorkShopRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));


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

  private List<String> getDefaultSupplies() {
    // 시스템 설정에서 기본 준비물 조회하거나, 빈 리스트 반환
    String suppliesConfig = settingUtils.getStringSetting("class.default.supplies", "");
    if (suppliesConfig.isEmpty()) {
      return List.of(); // 빈 목록 반환
    }
    return List.of(suppliesConfig.split(",")); // 콤마로 구분된 문자열을 리스트로 변환
  }
}