package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.domain.common.enums.OperationType;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioImage;
import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioOperatingHoursRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
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
  private final ArtworkRepository artworkRepository;
  private final JpaReservationRepository reservationRepository;
  private final FileUploader fileUploader;

  @Override
  public StudioListResponse searchStudios(String category, String location, String price, int page, int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit);

    // 현재는 모든 스튜디오 다 가져오는 방식
    Page<Studio> studios = studioRepository.findAll(pageable);

    List<StudioListDto> content = studios.getContent().stream().map(studio ->
        new StudioListDto(
            studio.getId(),
            studio.getName(),
            studio.getDescription(),
            studio.getLocation(),
            4.5, // TODO: averageRating 계산
            120  // TODO: reviewCount 계산
        )
    ).toList();

    PaginationDto pagination = new PaginationDto(
        page,
        limit,
        studios.getTotalElements(),
        studios.getTotalPages()
    );

    return new StudioListResponse(content, pagination);
  }

  @Override
  public List<StudioSearchDto> searchByKeyword(String keyword, String location, String price) {
    List<Studio> studios = studioRepository.searchStudios(keyword, location);

    return studios.stream()
        .map(s -> new StudioSearchDto(
            s.getId(),
            s.getName(),
            s.getLocation(),
            4.5  // TODO: 평균 평점 계산 로직
        ))
        .sorted((a, b) -> {
          if ("rating".equals(price)) {
            return Double.compare(b.rating(), a.rating()); // 평점 내림차순
          } else {
            return 0; // 기본 정렬
          }
        })
        .toList();
  }

  @Override
  public StudioDetailDto findById(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("Studio not found"));

    // 운영시간 DTO 변환
    List<OperatingHoursDto> operatingHours = hoursRepository.findByStudioId(studioId).stream()
        .map(h -> new OperatingHoursDto(h.getWeekday(), h.getOpenTime(), h.getCloseTime()))
        .collect(Collectors.toList());

    List<String> images = studio.getImages().stream()
        .sorted(Comparator.comparingInt(StudioImage::getDisplayOrder))
        .map(StudioImage::getImageUrl)
        .collect(Collectors.toList());

    return new StudioDetailDto(
        studio.getId(), studio.getName(), studio.getDescription(), studio.getPhone(), images, studio.getLocation(), studio.getStatus(),
        studio.getHourlyBaseRate(), studio.getWeekendPrice(), studio.getMaxPeople(), studio.getPerPersonRate(), operatingHours
    );
  }

  @Override
  public List<GalleryDto> gallery(Long studioId) {
    return artworkRepository.findByStudioId(studioId).stream()
        .map(img -> new GalleryDto(
            img.getId(),
            img.getImageUrl(),
            img.getDescription(),
            img.getOrder()
        ))
        .collect(Collectors.toList());
  }

  @Override
  public void updateGalleryOrder(Long studioId, List<StudioGalleryOrderUpdate> requestList) {
    for (StudioGalleryOrderUpdate dto : requestList) {
      Artwork artwork = artworkRepository.findById(dto.artworkId())
          .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + dto.artworkId()));

      if (!artwork.getStudio().getId().equals(studioId)) {
        throw new IllegalArgumentException("해당 스튜디오에 속한 이미지가 아닙니다.");
      }

      artwork.changeOrder(dto.order());
    }
  }

  @Override
  public PricingDto pricing(Long studioId) {
    Studio s = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("Studio not found"));

    return new PricingDto(
        s.getHourlyBaseRate(),
        s.getWeekendPrice(),
        s.getPerPersonRate()
    );
  }

  @Override
  public List<StudioAvailableDto> availableNow() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime limit = now.plusMinutes(30);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    return studioRepository.findAll().stream()
        .filter(studio -> {
          List<Reservation> reservations =
              reservationRepository.findByStudioIdAndReservationDateAndStatus(
                  studio.getId(), today, ReservationStatus.CONFIRMED);

          return reservations.stream().noneMatch(r -> {
            LocalDateTime start = LocalDateTime.of(r.getReservationDate(), r.getStartTime());
            LocalDateTime end = LocalDateTime.of(r.getReservationDate(), r.getEndTime());
            return now.toLocalDateTime().isBefore(end) && limit.toLocalDateTime().isAfter(start);
          });
        })
        .map(s -> new StudioAvailableDto(
            s.getId(), s.getName(), s.getLocation(), now.format(formatter)
        ))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public StudioApplicationResponse applyStudio(StudioApplicationRequest request) {
    Studio studio = Studio.builder()
        .name(request.name())
        .description(request.description())
        .location(request.location())
        .phone(request.phone())
        .status(StudioStatus.PENDING)
        .build();

    studioRepository.save(studio);

    int index = 0;
    for (MultipartFile file : request.images()) {
      String url = fileUploader.upload(file); // 실제 구현에 따라 다름

      StudioImage image = StudioImage.builder()
          .imageUrl(url)
          .isThumbnail(index == 0)
          .displayOrder(index++)
          .build();

      studio.addImage(image); // 양방향 설정
    }

    return new StudioApplicationResponse(studio.getId(), studio.getStatus().name().toLowerCase());
  }

  @Override
  public StudioApplicationDetailResponse getApplicationStatus(Long studioId) {
    Studio studio = studioRepository.findByIdAndStatus(studioId, StudioStatus.PENDING)
        .orElseThrow(() -> new IllegalArgumentException("승인 대기 중인 스튜디오를 찾을 수 없습니다."));

    return new StudioApplicationDetailResponse(
        studio.getId(),
        studio.getName(),
        studio.getStatus().name().toLowerCase(),
        studio.getCreatedAt(),  // BaseEntity 기준
        "서류 검토 중입니다"
    );
  }

  @Override
  @Transactional
  public StudioCreateResponse createStudio(StudioCreateRequest request) {
    Studio studio = studioRepository.findById(request.studioId())
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));
    if (studio.getStatus() != StudioStatus.PENDING) {
      throw new IllegalStateException("해당 스튜디오는 생성할 수 없는 상태입니다.");
    }

    // 신청 정보에 추가적인 정보 반영
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
    for (StudioImage image : studio.getImages()) {
      boolean isThumbnail = image.getId().equals(request.thumbnailId());
      image.setThumbnail(isThumbnail);
    }

    studio.changeStatus(StudioStatus.ACTIVE); // 활성화 처리

    return new StudioCreateResponse(studio.getId(), studio.getName(), studio.getStatus().name().toLowerCase());
  }

  @Override
  @Transactional
  public void updateStudio(Long studioId, StudioUpdateRequest request) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 스튜디오를 찾을 수 없습니다."));

    if (request.description() != null) studio.updateDescription(request.description());
    if (request.phone() != null) studio.updatePhone(request.phone());
    if (request.hourlyBaseRate() != null) studio.updateHourlyBaseRate(request.hourlyBaseRate());
    if (request.weekendPrice() != null) studio.updateWeekendPrice(request.weekendPrice());
    if (request.maxPeople() != null) studio.updateMaxPeople(request.maxPeople());
    if (request.perPersonRate() != null) studio.updatePerPersonRate(request.perPersonRate());


    // 운영 시간 수정
    if (request.operatingHours() != null) {
      hoursRepository.deleteByStudioId(studioId);
      for (OperatingHoursDto dto : request.operatingHours()) {
        studio.addOperatingHour(StudioOperatingHours.builder()
            .studio(studio)
            .weekday(dto.weekday())
            .openTime(dto.openTime())
            .closeTime(dto.closeTime())
            .build());
      }
    }

    // 이미지 순서 및 대표 이미지 수정
    if (request.images() != null) {
      for (StudioImageUpdateDto dto : request.images()) {
        StudioImage image = studio.getImages().stream()
            .filter(i -> i.getId().equals(dto.imageId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 이미지가 존재하지 않습니다."));

        image.updateImageOrder(dto.displayOrder(), dto.isThumbnail());
      }
    }
  }

  @Override
  @Transactional
  public void deactivateStudio(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 스튜디오가 존재하지 않습니다."));

    studio.changeStatus(StudioStatus.INACTIVE); // 또는 SUSPENDED
  }

  /**
   * 공간 대여 신청 처리
   */
  @Override
  @Transactional
  public StudioApplicationResponse applySpaceRental(SpaceRentalApplicationRequest request) {
    // TODO: 현재 로그인한 사용자 정보 가져오기 (예: SecurityContext에서)
    // User currentUser = getCurrentUser();
    
    Studio studio = Studio.builder()
        // .owner(currentUser)
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
        .build();

    // 파일 업로드 처리
    processFileUploads(studio, request.businessLicense(), request.documents(), request.images());
    
    studioRepository.save(studio);

    return new StudioApplicationResponse(
        studio.getId(), 
        studio.getStatus().name().toLowerCase()
    );
  }

  /**
   * 공방 체험 신청 처리
   */
  @Override
  @Transactional
  public StudioApplicationResponse applyWorkshop(WorkshopApplicationRequest request) {
    // TODO: 현재 로그인한 사용자 정보 가져오기
    // User currentUser = getCurrentUser();
    
    Studio studio = Studio.builder()
        // .owner(currentUser)
        .name(request.name())
        .description(request.description())
        .location(request.location())
        .phone(request.phone())
        .operationType(OperationType.CLASS_WORKSHOP)
        .instructorName(request.instructorName())
        .instructorCareer(request.instructorCareer())
        .availableClasses(String.join(",", request.availableClasses()))
        .maxPeople(request.maxParticipants())
        .status(StudioStatus.PENDING)
        .build();

    // 파일 업로드 처리 (공방 전용 파일들 포함)
    processWorkshopFileUploads(studio, request);
    
    studioRepository.save(studio);

    return new StudioApplicationResponse(
        studio.getId(), 
        studio.getStatus().name().toLowerCase()
    );
  }

  /**
   * 공간 대여 신청 상태 조회
   */
  @Override
  public StudioApplicationDetailResponse getSpaceRentalApplicationStatus(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));
        
    if (!studio.isSpaceRental()) {
      throw new IllegalArgumentException("공간 대여 신청이 아닙니다.");
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
   * 공방 체험 신청 상태 조회
   */
  @Override
  public StudioApplicationDetailResponse getWorkshopApplicationStatus(Long studioId) {
    Studio studio = studioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));
        
    if (!studio.isClassWorkshop()) {
      throw new IllegalArgumentException("공방 체험 신청이 아닙니다.");
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
   * 공방 전용 파일 업로드 처리
   */
  private void processWorkshopFileUploads(Studio studio, WorkshopApplicationRequest request) {
    // 기본 파일들 (기존 로직)
    processFileUploads(studio, request.businessLicense(), request.documents(), request.images());
    
    // 강사 자격증 업로드
    if (request.instructorCertificates() != null) {
      for (MultipartFile cert : request.instructorCertificates()) {
        String certUrl = fileUploader.upload(cert);
        // TODO: 강사 자격증 정보 저장
      }
    }
    
    // 작품 샘플 업로드
    if (request.sampleWorks() != null) {
      for (MultipartFile sample : request.sampleWorks()) {
        String sampleUrl = fileUploader.upload(sample);
        // TODO: 작품 샘플 정보 저장
      }
    }
  }

  /**
   * 기본 파일 업로드 처리
   */
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
            .isThumbnail(order == 0) // 첫 번째 이미지를 대표 이미지로
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
      default -> "상태를 확인할 수 없습니다.";
    };
  }

}

