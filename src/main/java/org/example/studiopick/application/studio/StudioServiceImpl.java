package org.example.studiopick.application.studio;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioCommission;
import org.example.studiopick.domain.studio.StudioImage;
import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.example.studiopick.infrastructure.studio.JpaStudioImageRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioOperatingHoursRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudioServiceImpl implements StudioService {
  private final JpaStudioRepository studioRepository;
  private final JpaStudioOperatingHoursRepository hoursRepository;
  private final JpaReservationRepository reservationRepository;
//  private final ArtworkRepository artworkRepository;
  private final JpaStudioImageRepository imageRepository;
  private final ReviewRepository reviewRepository;
  private final FileUploader fileUploader;
  private final JpaUserRepository userRepository;
  private final S3Uploader s3Uploader;

  /**
   * 스튜디오(공간) 검색
   */
  @Override
  public StudioListResponse searchStudios(String location, String price, int page, int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit);
    Page<Studio> studios = studioRepository.findActiveStudios(pageable);

    List<StudioListDto> content = studios.getContent().stream()
        .map(studio -> new StudioListDto(
            studio.getId(),
            studio.getName(),
            studio.getLocation(),
            studio.getHourlyBaseRate(),
            calculateAverageRating(studio.getId()),
            calculateReviewCount(studio.getId()),
            studio.getThumbnailImage()
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
  @Transactional(readOnly = true)
  public Page<StudioSearchResponse> activeStudios(String region, String keyword, String sort, Pageable pageable) {
    // 1. 정렬 조건에 따라 정렬된 Pageable 생성
    PageRequest sortedPageable = switch (sort) {
      case "priceLow" -> PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("hourlyBaseRate").ascending());
      case "priceHigh" -> PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("hourlyBaseRate").descending());
      case "popular" -> PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id")); // 정렬용 컬럼 무의미화
      default ->  PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          pageable.getSort()
      );
    };

    Page<Studio> studios = studioRepository.searchStudios(region, keyword, sortedPageable);
    return studios.map(studio -> {
      Double avg = reviewRepository.getAverageRatingByStudioId(studio.getId());
      double averageRating = avg != null ? avg : 0.0;

      return StudioSearchResponse.from(studio, averageRating);
    });
  }
  /**
   * 스튜디오 상세 정보 조회
   */
  @Override
  public StudioDetailDto findById(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new EntityNotFoundException("해당 스튜디오가 없습니다."));

    List<String> imageUrls = studio.getImages().stream()
        .map(StudioImage::getImageUrl)
        .collect(Collectors.toList());

    return new StudioDetailDto(
        studio.getId(),
        studio.getName(),
        studio.getDescription(),
        studio.getPhone(),
        studio.getLocation(),
        studio.getHourlyBaseRate(),
        studio.getWeekendPrice(),
        studio.getPerPersonRate(),
        studio.getMaxPeople(),
        studio.getSize(),
        studio.getFacilities(),
        studio.getRules(),
        studio.getThumbnailImage(),
        imageUrls,
        studio.getOperatingHours().stream()
            .map(OperatingHoursDto::fromEntity)
            .toList()
    );
  }

  private StudioDetailDto getStudioDetail(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new EntityNotFoundException("해당 스튜디오가 없습니다."));

    List<String> imageUrls = studio.getImages().stream()
        .map(StudioImage::getImageUrl)
        .toList();

    return new StudioDetailDto(
        studio.getId(),
        studio.getName(),
        studio.getDescription(),
        studio.getPhone(),
        studio.getLocation(),
        studio.getHourlyBaseRate(),
        studio.getWeekendPrice(),
        studio.getPerPersonRate(),
        studio.getMaxPeople(),
        studio.getSize(),
        studio.getFacilities(),
        studio.getRules(),
        studio.getThumbnailImage(),
        imageUrls,
        studio.getOperatingHours().stream()
            .map(OperatingHoursDto::fromEntity)
            .toList()
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
        .size(request.size())
        .status(StudioStatus.PENDING)
        .owner(owner)
        .thumbnailImage(request.thumbnailImage()) // 있으면
        .build();

    StudioCommission commission = StudioCommission.builder()
        .studio(studio) // 연관관계 주인 설정
        .commissionRate(BigDecimal.valueOf(10)) // 기본 수수료율
        .build();

    studio.setCommission(commission);

    studioRepository.save(studio);

    log.info("📌 studioRental 시작");
    log.info("📌 유저 ID: {}", owner);
    log.info("📌 요청된 이름: {}", request.name());
    log.info("📌 썸네일: {}", request.thumbnailImage());
    log.info("📌 이미지: {}", request.images());

    // 스튜디오 이미지 저장
    List<String> imageUrls = request.images();
    if (imageUrls != null) {
      for (String url : imageUrls) {
        StudioImage studioImage = StudioImage.builder()
            .studio(studio)
            .imageUrl(url)
            .build();
        imageRepository.save(studioImage);
      }
    }

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
  public StudioDetailDto createStudio(StudioCreateRequest request, Long userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    Studio studio = Studio.builder()
        .owner(owner)
        .name(request.name())
        .description(request.description())
        .phone(request.phone())
        .location("임시 위치") // 추후 수정
        .hourlyBaseRate(request.hourlyBaseRate())
        .weekendPrice(request.weekendPrice())
        .perPersonRate(request.perPersonRate())
        .maxPeople(request.maxPeople())
        .size(request.size())
        .facilities(request.facilities())
        .rules(request.rules())
        .thumbnailImage(request.thumbnailImage())
        .build();

    // 운영시간 추가
    request.operatingHours().forEach(hourDto -> {
      StudioOperatingHours hours = hourDto.toEntity();
      studio.addOperatingHour(hours);
    });

    // 이미지 URL들 → StudioImage 엔티티로 변환
    if (request.imageUrls() != null) {
      for (String url : request.imageUrls()) {
        studio.addImage(new StudioImage(url));
      }
    }

    studioRepository.save(studio);
    return getStudioDetail(studio.getId());
  }

  /**
   * 스튜디오 정보 수정
   */
  @Override
  @Transactional
  public StudioDetailDto updateStudio(Long studioId, StudioDetailDto request, Long userId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new EntityNotFoundException("해당 스튜디오가 없습니다."));

    studio.changeStatus(StudioStatus.ACTIVE);
    studio.updateInfo(
        request.description(),
        request.phone(),
        request.hourlyBaseRate(),
        request.weekendPrice(),
        request.perPersonRate(),
        request.maxPeople(),
        request.rules(),
        request.facilities(),
        request.thumbnailImage(),
        request.size()

    );
    if(request.location() != null) {
      studio.updateLocation(request.location());
    }else {
      studio.updateLocation("임시 위치");
    }

    // 운영 시간 교체 로직
    studio.getOperatingHours().clear();
    request.operatingHours().forEach(hourDto -> {
      studio.addOperatingHour(hourDto.toEntity());
    });

    // 이미지 수정
    studio.getImages().clear();
    if (request.imageUrls() != null) {
      for (String url : request.imageUrls()) {
        studio.addImage(new StudioImage(url));
      }
    }

    return getStudioDetail(studio.getId());
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
    return 0.0;
  }

  private int calculateReviewCount(Long studioId) {
    // TODO: 리뷰 시스템과 연동하여 실제 리뷰 개수 계산
    return 0;
  }

  @Override
  public List<String> uploadStudioImages(MultipartFile[] images) {
    if (images.length > 5) {
      throw new IllegalArgumentException("최대 5장의 이미지만 업로드 가능합니다.");
    }

    return Arrays.stream(images)
        .map(image -> {
          validateImageFile(image);
          return s3Uploader.upload(image, "studio-images");
        })
        .toList();
  }

  private void validateImageFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
    }

    if (file.getSize() > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("파일 용량은 10MB 이하만 가능합니다.");
    }
  }

  @Override
  public List<StudioDto> getMyStudios(Long userId) {
    List<Studio> studios = studioRepository.findByOwnerId(userId);
    return studios.stream()
        .map(studio -> new StudioDto(
            studio.getId(),
            studio.getName(),
            studio.getLocation(),
            studio.getHourlyBaseRate(),
            calculateAverageRating(studio.getId()),
            calculateReviewCount(studio.getId()),
            studio.getThumbnailImage(),
            studio.getStatus()
        ))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void toggleVisibility(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new RuntimeException("스튜디오를 찾을 수 없습니다."));

    StudioStatus current = studio.getStatus();

    if (current == StudioStatus.ACTIVE) {
      studio.deactivate();
    } else if (current == StudioStatus.INACTIVE) {
      studio.activate();
    } else {
      throw new IllegalStateException("ACTIVE 또는 INACTIVE 상태만 토글할 수 있습니다.");
    }

    studioRepository.save(studio);
  }

}
