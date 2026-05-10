package com.ticketing.application.dto;

import java.time.LocalDateTime;

public record ScheduleResponseDTO(
        Long id,
        String trainName,
        String routeName,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        int delayMinutes
) {
}
