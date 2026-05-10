package com.ticketing.application.dto;

import java.time.LocalDateTime;

public record SearchSegmentDTO(
        Long scheduleId,
        String trainName,
        String fromStation,
        String toStation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime
) {
}
