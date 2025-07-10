package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioOperatingHoursRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.studio.StudioCommissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioServiceImpl implements StudioService {
  private final JpaStudioRepository jpaStudioRepository;
  private final JpaStudioOperatingHoursRepository hoursRepository;
  private final StudioCommissionRepository commissionRepository;
  private final ArtworkRepository artworkRepository;
  private final JpaReservationRepository reservationRepository;

  @Override
  public StudioListResponse searchStudios(String category, String location, String price, int page, int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit);

    // 현재는 모든 스튜디오 다 가져오는 방식
    Page<Studio> studios = jpaStudioRepository.findAll(pageable);

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
    List<Studio> studios = jpaStudioRepository.searchStudios(keyword, location);

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
    Studio studio = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("Studio not found"));

    // 운영시간 DTO 변환
    List<OperatingHoursDto> hours = hoursRepository.findByStudioId(studioId).stream()
        .map(h -> new OperatingHoursDto(h.getWeekday(), h.getOpenTime(), h.getCloseTime()))
        .collect(Collectors.toList());

    // 이미지 URL만 추출
    List<String> images = artworkRepository.findByStudioId(studioId).stream()
        .map(Artwork::getImageUrl)
        .collect(Collectors.toList());

    // 부가서비스: 더미 데이터 (프론트에서 선택 기반)
    List<String> facilities = List.of("wifi", "parking", "tools");

    // 가격 DTO 구성
    PricingDto pricing = new PricingDto(
        studio.getWeekdayPrice(),
        studio.getWeekendPrice()
    );

    return new StudioDetailDto(
        studio.getId(),
        studio.getName(),
        studio.getDescription(),
        studio.getPhone(),
        studio.getLocation(),
        images,
        pricing,
        hours,
        facilities
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
    Studio s = jpaStudioRepository.findById(studioId)
        .orElseThrow(() -> new IllegalArgumentException("Studio not found"));

    return new PricingDto(
        s.getWeekdayPrice(),
        s.getWeekendPrice()
    );
  }

  @Override
  public List<StudioAvailableDto> availableNow() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime limit = now.plusMinutes(30);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    return jpaStudioRepository.findAll().stream()
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
  public StudioApplicationResponse applyStudio(StudioApplicationRequest request) {
    Studio studio = Studio.builder()
        .name(request.name())
        .description(request.description())
        .location(request.location())
        .phone(request.phone())
        .status(StudioStatus.PENDING)
        .build();

    Studio saved = jpaStudioRepository.save(studio);
    return new StudioApplicationResponse(saved.getId(), saved.getStatus().name().toLowerCase());
  }

  @Override
  public StudioApplicationDetailResponse getApplicationStatus(Long studioId) {
    Studio studio = jpaStudioRepository.findByIdAndStatus(studioId, StudioStatus.PENDING)
        .orElseThrow(() -> new IllegalArgumentException("승인 대기 중인 스튜디오를 찾을 수 없습니다."));

    return new StudioApplicationDetailResponse(
        studio.getId(),
        studio.getName(),
        studio.getStatus().name().toLowerCase(),
        studio.getCreatedAt(),  // BaseEntity 기준
        "서류 검토 중입니다"
    );
  }

}

