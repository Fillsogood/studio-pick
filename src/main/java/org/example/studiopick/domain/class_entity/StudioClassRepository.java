package org.example.studiopick.domain.class_entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudioClassRepository extends JpaRepository<ClassEntity, Long> {

    // 스튜디오 ID로 클래스 목록 조회
    List<ClassEntity> findByStudioId(Long studioId);
}
