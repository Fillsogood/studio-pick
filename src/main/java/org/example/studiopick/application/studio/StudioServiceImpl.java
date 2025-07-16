package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.domain.common.enums.OperationType;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioImage;
import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioOperatingHoursRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioServiceImpl implements StudioService {
  private final JpaStudioRepository studioRepository;
  private final JpaStudioOperatingHoursRepository hoursRepository;
  private final JpaReservationRepository reservationRepository;
//  private final ArtworkRepository artworkRepository;
  private final FileUploader fileUploader;
  private final JpaUserRepository userRepository;
  /**
   * 스튜디오(공간) 검색
   */
  @Override
  public StudioListResponse searchStudios(String category, String location, String price, int page, int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit);
    Page<Studio> studios = studioRepository.findActiveStudios(pageable);

    List<StudioListDto> content = studios.getContent().stream()
        .map(studio -> new StudioListDto(
            studio.getId(),
            studio.getName(),
            studio.getDescription(),
            studio.getLocation(),
            calculateAverageRating(studio.getId()),
            calculateReviewCount(studio.getId())
        ))
        .toList();

    PaginationDto pagination = new PaginationDto(
        page,
        limit,
        studios.getTotalElements(),
        studios.getTotalPages()
    );

    return new StudioListResponse(content, pagination);
  }

  /**
   * 키워드로 스튜디오 검색
   */
  @Override
  public List<StudioSearchDto> searchByKeyword(String keyword, String location, String price) {
    List<Studio> studios = studioRepository.searchStudios(keyword, location);

    return studios.stream()
        .map(s -> new StudioSearchDto(
            s.getId(),
            s.getName(),
            s.getLocation(),
            calculateAverageRating(s.getId())
        ))
        .sorted((a, b) -> {
          if ("rating".equals(price)) {
            return Double.compare(b.rating(), a.rating());
          }
          return 0;
        })
        .toList();
  }
  /**
   * 스튜디오 상세 정보 조회
   */
  @Override
  public StudioDetailDto findById(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    List<OperatingHoursDto> operatingHours = hoursRepository.findByStudioId(studioId).stream()
        .map(h -> new OperatingHoursDto(h.getWeekday(), h.getOpenTime(), h.getCloseTime()))
        .collect(Collectors.toList());

    List<String> images = studio.getImages().stream()
        .sorted(Comparator.comparingInt(StudioImage::getDisplayOrder))
        .map(StudioImage::getImageUrl)
        .collect(Collectors.toList());

    return new StudioDetailDto(
        studio.getId(), 
        studio.getName(), 
        studio.getDescription(), 
        studio.getPhone(), 
        images, 
        studio.getLocation(), 
        studio.getStatus(),
        studio.getHourlyBaseRate(), 
        studio.getWeekendPrice(), 
        studio.getMaxPeople(), 
        studio.getPerPersonRate(), 
        operatingHours
    );
  }

  /**
   * 스튜디오 갤러리 조회
   */
  @Override
  public List<GalleryDto> gallery(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    return studio.getImages().stream()
        .map(image -> new GalleryDto(
            image.getId(),
            image.getImageUrl()
        ))
        .collect(Collectors.toList());
  }

  /**
   * 스튜디오 가격 정보 조회
   */
  @Override
  public PricingDto pricing(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    return new PricingDto(
        studio.getHourlyBaseRate(),
        studio.getWeekendPrice(),
        studio.getPerPersonRate()
    );
  }

  /**
   * 현재 이용 가능한 스튜디오 조회
   */
  @Override
  public List<StudioAvailableDto> availableNow() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime limit = now.plusMinutes(30);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    List<Studio> activeStudios = studioRepository.findAllByStatus(StudioStatus.ACTIVE);

    return activeStudios.stream()
        .filter(studio -> isStudioAvailable(studio, today, now, limit))
        .map(s -> new StudioAvailableDto(
            s.getId(),
            s.getName(),
            s.getLocation(),
            now.format(formatter)
        ))
        .collect(Collectors.toList());
  }

  /**
   * 공간 운영신청 신청
   */
  @Override
  @Transactional
  public StudioApplicationResponse studioRental(SpaceRentalApplicationRequest request, Long userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Studio studio = Studio.builder()
        .name(request.name())
        .description(request.description())
        .location(request.location())
        .phone(request.phone())
        .operationType(OperationType.SPACE_RENTAL)
        .hourlyBaseRate(request.hourlyBaseRate())
        .weekendPrice(request.weekendPrice())
        .perPersonRate(request.perPersonRate())
        .maxPeople(request.maxPeople())
        .status(StudioStatus.PENDING)
        .owner(owner)
        .build();

    processFileUploads(studio, request.businessLicense(), request.documents(), request.images());
    studioRepository.save(studio);

    return new StudioApplicationResponse(
        studio.getId(),
        studio.getStatus().name().toLowerCase()
    );
  }

  /**
   * 공간 운영 신청 상태 조회
   */
  @Override
  public StudioApplicationDetailResponse studioRentalApplicationStatus(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

    if (!studio.isSpaceRental()) {
      throw new IllegalArgumentException("공간 운영 신청이 아닙니다.");
    }

    return new StudioApplicationDetailResponse(
        studio.getId(),
        studio.getName(),
        studio.getStatus().name().toLowerCase(),
        studio.getCreatedAt(),
        getStatusMessage(studio.getStatus())
    );
  }

  /**
   * 스튜디오 생성 (관리자 승인 후)
   */
  @Override
  @Transactional
  public StudioCreateResponse createStudio(StudioCreateRequest request) {
    Studio studio = studioRepository.findById(request.studioId())
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));
        
    if (studio.getStatus() != StudioStatus.PENDING) {
      throw new IllegalStateException("해당 스튜디오는 생성할 수 없는 상태입니다.");
    }

    // 추가 정보 업데이트
    studio.updateInfo(
        request.description(),
        request.phone(),
        request.hourlyBaseRate(),
        request.weekendPrice(),
        request.perPersonRate(),
        request.maxPeople()
    );

    // 운영 시간 저장
    List<StudioOperatingHours> newHours = request.operatingHours().stream()
        .map(dto -> StudioOperatingHours.builder()
            .studio(studio)
            .weekday(dto.weekday())
            .openTime(dto.openTime())
            .closeTime(dto.closeTime())
            .build())
        .toList();

    hoursRepository.saveAll(newHours);

    // 대표 이미지 설정
    studio.getImages().forEach(image -> {
      boolean isThumbnail = image.getId().equals(request.thumbnailId());
      image.setThumbnail(isThumbnail);
    });

    studio.changeStatus(StudioStatus.ACTIVE);

    return new StudioCreateResponse(
        studio.getId(), 
        studio.getName(), 
        studio.getStatus().name().toLowerCase()
    );
  }

  /**
   * 스튜디오 정보 수정
   */
  @Override
  @Transactional
  public void updateStudio(Long studioId, StudioUpdateRequest request) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    // 기본 정보 수정
    if (request.description() != null) studio.updateDescription(request.description());
    if (request.phone() != null) studio.updatePhone(request.phone());
    if (request.hourlyBaseRate() != null) studio.updateHourlyBaseRate(request.hourlyBaseRate());
    if (request.weekendPrice() != null) studio.updateWeekendPrice(request.weekendPrice());
    if (request.maxPeople() != null) studio.updateMaxPeople(request.maxPeople());
    if (request.perPersonRate() != null) studio.updatePerPersonRate(request.perPersonRate());

    // 운영 시간 수정
    if (request.operatingHours() != null) {
      hoursRepository.deleteByStudioId(studioId);
      request.operatingHours().forEach(dto -> {
        studio.addOperatingHour(StudioOperatingHours.builder()
            .studio(studio)
            .weekday(dto.weekday())
            .openTime(dto.openTime())
            .closeTime(dto.closeTime())
            .build());
      });
    }

    // 이미지 순서 및 대표 이미지 수정
    if (request.images() != null) {
      request.images().forEach(dto -> {
        StudioImage image = studio.getImages().stream()
            .filter(i -> i.getId().equals(dto.imageId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 이미지를 찾을 수 없습니다."));

        image.updateImageOrder(dto.displayOrder(), dto.isThumbnail());
      });
    }
  }

  /**
   * 스튜디오 비활성화
   */
  @Override
  @Transactional
  public void deactivateStudio(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    studio.changeStatus(StudioStatus.INACTIVE);
  }

  // === Private Helper Methods ===

  private boolean isStudioAvailable(Studio studio, LocalDate today, ZonedDateTime now, ZonedDateTime limit) {
    List<Reservation> reservations = reservationRepository.findByStudioIdAndReservationDateAndStatus(
        studio.getId(), today, ReservationStatus.CONFIRMED);

    return reservations.stream().noneMatch(r -> {
      LocalDateTime start = LocalDateTime.of(r.getReservationDate(), r.getStartTime());
      LocalDateTime end = LocalDateTime.of(r.getReservationDate(), r.getEndTime());
      return now.toLocalDateTime().isBefore(end) && limit.toLocalDateTime().isAfter(start);
    });
  }

  private void processFileUploads(Studio studio, MultipartFile businessLicense, 
                                 List<MultipartFile> documents, List<MultipartFile> images) {
    // 사업자등록증 업로드
    if (businessLicense != null && !businessLicense.isEmpty()) {
      String licenseUrl = fileUploader.upload(businessLicense);
      // TODO: 사업자등록증 정보 저장
    }

    // 스튜디오 이미지 업로드
    if (images != null) {
      int order = 0;
      for (MultipartFile imageFile : images) {
        String imageUrl = fileUploader.upload(imageFile);
        
        StudioImage image = StudioImage.builder()
            .imageUrl(imageUrl)
            .isThumbnail(order == 0)
            .displayOrder(order++)
            .build();
            
        studio.addImage(image);
      }
    }
  }

  private String getStatusMessage(StudioStatus status) {
    return switch (status) {
      case PENDING -> "관리자 검토 중입니다.";
      case ACTIVE -> "승인되었습니다.";
      case REJECTED -> "신청이 거부되었습니다.";
      case INACTIVE -> "운영이 중단되었습니다.";
      default -> "상태를 확인할 수 없습니다.";
    };
  }

  private double calculateAverageRating(Long studioId) {
    // TODO: 리뷰 시스템과 연동하여 실제 평점 계산
    return 4.5;
  }

  private int calculateReviewCount(Long studioId) {
    // TODO: 리뷰 시스템과 연동하여 실제 리뷰 개수 계산
    return 120;
  }
}
