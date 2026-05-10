package com.ticketing.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ItineraryBookingRequestDTO(
        @NotEmpty List<@NotNull Long> scheduleIds,
        @NotBlank @Email String customerEmail,
        @Min(1) int numberOfTickets
) {
}
