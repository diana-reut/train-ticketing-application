package com.ticketing.application.dto;

public record BookingRequestDTO(
        Long scheduleId,
        String customerEmail,
        int numberOfTickets
) {
}