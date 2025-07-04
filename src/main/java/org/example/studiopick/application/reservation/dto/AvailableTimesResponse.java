package org.example.studiopick.application.reservation.dto;

import java.util.List;

public record AvailableTimesResponse(
    List<String> availableTimes,
    List<String> bookedTimes
){}