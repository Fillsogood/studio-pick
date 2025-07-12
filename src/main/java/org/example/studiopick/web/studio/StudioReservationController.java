package org.example.studiopick.web.studio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioReservationResponseDto;
import org.example.studiopick.application.studio.service.StudioReservationService;
import org.example.studiopick.domain.studio.Studio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Studio 예약", description = "스튜디오 예약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studio/reservations")
public class StudioReservationController {

    private final StudioReservationService studioReservationService;

    @GetMapping
    @Operation(summary = "스튜디오 예약 목록 조회", description = "스튜디오 ID, 날짜, 상태에 따라 예약 목록을 조회합니다.")
    public ResponseEntity<List<StudioReservationResponseDto>> getReservations(
            @RequestParam Long studioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<StudioReservationResponseDto> reservations =
                studioReservationService.getReservations(studioId, date, status, page, size);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/available-now")
    @Operation(summary = "실시간 이용 가능 스튜디오 조회", description = "30분 이내 예약 가능한 스튜디오 목록을 반환합니다.")
    public ResponseEntity<?> getAvailableStudiosWithin30Minutes() {
        List<Studio> studios = studioReservationService.getAvailableStudiosWithin30Minutes();

        List<Map<String, Object>> availableStudios = studios.stream().map(studio -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", studio.getId());
            map.put("name", studio.getName());
            map.put("location", studio.getLocation());
            map.put("nextAvailableTime", LocalTime.now().plusMinutes(30).toString()); // 예시로 30분 뒤 시간
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of("availableStudios", availableStudios));

        return ResponseEntity.ok(response);
    }

}

