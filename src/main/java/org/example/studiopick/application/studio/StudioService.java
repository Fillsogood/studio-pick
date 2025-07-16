package org.example.studiopick.application.studio;

import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.application.studio.dto.SpaceRentalApplicationRequest;
import org.example.studiopick.application.studio.dto.WorkshopApplicationRequest;

import java.util.List;

public interface StudioService {
  StudioDetailDto findById(Long studioId);
  List<GalleryDto> gallery(Long studioId);
  PricingDto pricing(Long studioId);
  List<StudioAvailableDto> availableNow();
  StudioListResponse searchStudios(String category, String location, String sort, int page, int limit);
  List<StudioSearchDto> searchByKeyword(String keyword, String location, String sort);

  // 분리된 상태 조회 메서드들
  StudioCreateResponse createStudio(StudioCreateRequest request);
  void updateStudio(Long studioId, StudioUpdateRequest request);
  StudioApplicationResponse studioRental(SpaceRentalApplicationRequest request, Long userId);
  StudioApplicationDetailResponse studioRentalApplicationStatus(Long studioId);
  void deactivateStudio(Long studioId);
}
