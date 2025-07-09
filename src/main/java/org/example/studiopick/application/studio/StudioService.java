package org.example.studiopick.application.studio;

import org.example.studiopick.application.studio.dto.*;

import java.util.List;

public interface StudioService {
  StudioDetailDto findById(Long studioId);
  List<GalleryDto> gallery(Long studioId);
  PricingDto pricing(Long studioId);
  List<StudioAvailableDto> availableNow();
  void updateGalleryOrder(Long studioId, List<StudioGalleryOrderUpdate> requestList);
  StudioListResponse searchStudios(String category, String location, String sort, int page, int limit);
  List<StudioSearchDto> searchByKeyword(String keyword, String location, String sort);
  StudioApplicationResponse applyStudio(StudioApplicationRequest request);
  StudioApplicationDetailResponse getApplicationStatus(Long studioId);
}
