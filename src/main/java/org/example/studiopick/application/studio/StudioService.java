package org.example.studiopick.application.studio;

import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.application.studio.dto.SpaceRentalApplicationRequest;
import org.example.studiopick.domain.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudioService {
  StudioDetailDto findById(Long studioId);
  List<GalleryDto> gallery(Long studioId);
  PricingDto pricing(Long studioId);
  List<StudioAvailableDto> availableNow();
  StudioListResponse searchStudios(String location, String sort, int page, int limit);
  List<StudioSearchDto> searchByKeyword(String keyword, String location, String sort);

  // 분리된 상태 조회 메서드들
  StudioDetailDto createStudio(StudioCreateRequest request, Long userId);
  StudioDetailDto updateStudio(Long studioId, StudioDetailDto request, Long userId);
  StudioApplicationResponse studioRental(SpaceRentalApplicationRequest request, Long userId);
  StudioApplicationDetailResponse studioRentalApplicationStatus(Long studioId);
  void deactivateStudio(Long studioId);
  List<String> uploadStudioImages(MultipartFile[] images);
  List<StudioDto> getMyStudios(Long userId);
}
