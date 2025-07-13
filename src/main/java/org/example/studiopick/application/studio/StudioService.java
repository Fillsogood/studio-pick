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
  void updateGalleryOrder(Long studioId, List<StudioGalleryOrderUpdate> requestList);
  StudioListResponse searchStudios(String category, String location, String sort, int page, int limit);
  List<StudioSearchDto> searchByKeyword(String keyword, String location, String sort);
  StudioApplicationResponse applyStudio(StudioApplicationRequest request);
  StudioApplicationDetailResponse getApplicationStatus(Long studioId);
  
  // 분리된 운영신청 메서드들
  StudioApplicationResponse applySpaceRental(SpaceRentalApplicationRequest request);
  StudioApplicationResponse applyWorkshop(WorkshopApplicationRequest request);
  
  // 분리된 상태 조회 메서드들
  StudioApplicationDetailResponse getSpaceRentalApplicationStatus(Long studioId);
  StudioApplicationDetailResponse getWorkshopApplicationStatus(Long studioId);
  StudioCreateResponse createStudio(StudioCreateRequest request);
  void updateStudio(Long studioId, StudioUpdateRequest request);
  void deactivateStudio(Long studioId);
}
