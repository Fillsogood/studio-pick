package org.example.studiopick.application.studio.dto;

import java.util.List;

public record SpaceRentalApplicationRequest(
    String name,
    String description,
    String location,
    String phone,
    
    // 공간 대여 전용 정보
    Integer size,
    
    // 파일들
    String thumbnailImage,
    List<String> images
) {}
