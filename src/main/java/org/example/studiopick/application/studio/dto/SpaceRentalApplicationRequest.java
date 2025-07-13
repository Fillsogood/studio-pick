package org.example.studiopick.application.studio.dto;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

public record SpaceRentalApplicationRequest(
    String name,
    String description,
    String location,
    String phone,
    
    // 공간 대여 전용 정보
    Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Long perPersonRate,
    Integer maxPeople,
    List<String> facilities,        // 시설 정보
    
    // 파일들
    MultipartFile businessLicense,
    List<MultipartFile> documents,
    List<MultipartFile> images
) {}
