package org.example.studiopick.web.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioReservationResponseDto;
import org.example.studiopick.application.studio.service.StudioReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studio/reservations")
public class StudioReservationController {

    private final StudioReservationService studioReservationService;

    @GetMapping
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
}

