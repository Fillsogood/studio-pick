package org.example.studiopick.application.studio.dto;

import org.example.studiopick.domain.studio.Studio;

import java.util.List;

public record StudioSearchResponse(
    Long id,
    String name,
    String location,
    Long hourlyBaseRate,
    double averageRating,
    String thumbnailImage
) {
  public static StudioSearchResponse from(Studio studio, double averageRating) {
    return new StudioSearchResponse(
        studio.getId(),
        studio.getName(),
        studio.getLocation(),
        studio.getHourlyBaseRate(),
        averageRating,
        studio.getThumbnailImage()
    );
  }
}
