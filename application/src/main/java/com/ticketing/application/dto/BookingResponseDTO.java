package com.ticketing.application.dto;

import java.time.LocalDateTime;

public record BookingResponseDTO(
        Long bookingId,
        String customerEmail,
        int numberOfTickets,
        String trainName,
        LocalDateTime departureTime
) {
}
