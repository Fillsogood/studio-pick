package org.example.studiopick.application.studio.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioClassRequestDto;
import org.example.studiopick.application.studio.dto.StudioClassResponseDto;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.class_entity.StudioClassRepository;
import org.example.studiopick.domain.common.enums.ClassStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.repository.StudioClassJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudioClassService {

    private final StudioClassRepository classRepository;
    private final StudioClassJpaRepository jpaStudioRepository;

    // 클래스 등록
    @Transactional
    public Long createClass(StudioClassRequestDto dto) {
        Studio studio = jpaStudioRepository.findById(dto.getStudioId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스튜디오입니다."));

        ClassEntity classEntity = ClassEntity.builder()
                .studio(studio)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .instructor(dto.getInstructor())
                .status(ClassStatus.valueOf(dto.getStatus()))
                .build();

        return classRepository.save(classEntity).getId();
    }

    // 클래스 전체 조회 (스튜디오 기준)
    @Transactional(readOnly = true)
    public List<StudioClassResponseDto> getClassesByStudio(Long studioId) {
        return classRepository.findByStudioId(studioId)
                .stream()
                .map(classEntity -> StudioClassResponseDto.builder()
                        .classId(classEntity.getId())
                        .title(classEntity.getTitle())
                        .description(classEntity.getDescription())
                        .price(classEntity.getPrice())
                        .date(classEntity.getDate())
                        .startTime(classEntity.getStartTime())
                        .endTime(classEntity.getEndTime())
                        .instructor(classEntity.getInstructor())
                        .status(classEntity.getStatus().name())
                        .build())
                .toList();
    }

    // 클래스 수정
    @Transactional
    public void updateClass(Long classId, StudioClassRequestDto dto) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        classEntity.updateBasicInfo(dto.getTitle(), dto.getDescription(), dto.getPrice());
        classEntity.updateSchedule(dto.getDate(), dto.getStartTime(), dto.getEndTime());
        classEntity.changeStatus(ClassStatus.valueOf(dto.getStatus()));
    }

    // 클래스 삭제
    @Transactional
    public void deleteClass(Long classId) {
        classRepository.deleteById(classId);
    }
}

