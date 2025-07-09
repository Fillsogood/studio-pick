package org.example.studiopick.application.classes;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.class_entity.ClassReservation;
import org.example.studiopick.domain.common.enums.ClassReservationStatus;
import org.example.studiopick.domain.common.enums.ClassStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.classes.ClassRepository;
import org.example.studiopick.infrastructure.classes.ClassReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

  private final ClassRepository classRepository;
  private final ClassReservationRepository classReservationRepository;
  private final JpaUserRepository userRepository;
  private final SystemSettingUtils settingUtils;

  @Override
  public ClassListResponse getClassList(Long studioId, String status, String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    ClassStatus classStatus = ClassStatus.valueOf(status.toUpperCase());

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    List<ClassListDto> result = classRepository
        .findByStudioIdAndStatusAndDate(studioId, classStatus, parsedDate)
        .stream()
        .map(c -> new ClassListDto(
            c.getId(),
            c.getTitle(),
            c.getDescription(),
            c.getPrice(),
            c.getDate(),
            c.getStartTime(),
            c.getEndTime(),
            c.getStudio().getName(),
            defaultMaxParticipants,
            c.getReservations().size() // currentParticipants
        )).toList();

    return new ClassListResponse(result);
  }

  @Override
  public ClassDetailDto getClassDetail(Long classId) {
    ClassEntity ce = classRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

    Studio studio = ce.getStudio();

    int defaultMaxParticipants = settingUtils.getIntegerSetting("class.default.max.participants", 8);

    return new ClassDetailDto(
        ce.getId(),
        ce.getTitle(),
        ce.getDescription(),
        ce.getPrice(),
        ce.getDate(),
        ce.getStartTime(),
        ce.getEndTime(),
        ce.getInstructor(),
        studio.getName(),
        studio.getLocation(),
        defaultMaxParticipants,
        (int) ce.getReservations().stream().filter(r -> r.getStatus() == ClassReservationStatus.CONFIRMED).count(),
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

  @Override
  @Transactional
  public ClassReservationResponse reserveClass(Long classId, Long userId, int participants) {
    ClassEntity classEntity = classRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    BigDecimal totalAmount = classEntity.getPrice().multiply(BigDecimal.valueOf(participants));

    ClassReservation reservation = ClassReservation.builder()
        .user(user)
        .classEntity(classEntity)
        .status(ClassReservationStatus.PENDING)
        .build();

    classReservationRepository.save(reservation);

    return new ClassReservationResponse(
        reservation.getId(),
        classEntity.getTitle(),
        classEntity.getInstructor(),
        participants,
        totalAmount,
        reservation.getStatus().name().toLowerCase()
    );
  }
}