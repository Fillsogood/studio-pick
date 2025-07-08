package org.example.studiopick.application.studio.dto;

public record StudioSearchDto(
    Long id,
    String name,
    String location,
    Double rating // 평균 평점
) {}
