package com.ticketing.application.dto;

import java.time.LocalDateTime;

public record TrainBookingResponseDTO(
        Long bookingId,
        Long scheduleId,
        String trainName,
        String customerEmail,
        int numberOfTickets,
        LocalDateTime bookingTime,
        LocalDateTime scheduledDepartureTime
) {
}
