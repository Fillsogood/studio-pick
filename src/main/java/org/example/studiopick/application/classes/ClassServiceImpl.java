package org.example.studiopick.application.classes;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.dto.*;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.class_entity.ClassReservation;
import org.example.studiopick.domain.common.enums.ClassReservationStatus;
import org.example.studiopick.domain.common.enums.ClassStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;
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

  @Override
  public ClassListResponse getClassList(Long studioId, String status, String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    ClassStatus classStatus = ClassStatus.valueOf(status.toUpperCase());

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
            8, // maxParticipants, 예시
            c.getReservations().size() // currentParticipants
        )).toList();

    return new ClassListResponse(result);
  }

  @Override
  public ClassDetailDto getClassDetail(Long classId) {
    ClassEntity ce = classRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

    Studio studio = ce.getStudio();
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
        8, // TODO: 실제 최대 인원
        (int) ce.getReservations().stream().filter(r -> r.getStatus() == ClassReservationStatus.CONFIRMED).count(),
        List.of("점토", "도구") // ★ 예시로 넣은 준비물
    );
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