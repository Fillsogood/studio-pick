package org.example.studiopick.application.studio.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public record WorkshopApplicationRequest(
    String name,
    String description,
    String location,
    String phone,
    
    // 공방 체험 전용 정보
    String instructorName,
    String instructorCareer,
    List<String> availableClasses,
    Integer maxParticipants,
    String safetyMeasures,
    
    // 파일들
    MultipartFile businessLicense,
    List<MultipartFile> documents,
    List<MultipartFile> images,
    List<MultipartFile> instructorCertificates,  // 강사 자격증
    List<MultipartFile> sampleWorks             // 작품 샘플
) {}
