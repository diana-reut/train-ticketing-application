package com.ticketing.application.dto;

import java.util.List;

public record ItineraryBookingResponseDTO(
        String customerEmail,
        int numberOfTickets,
        int segmentCount,
        List<BookingResponseDTO> bookings
) {
}
