package com.ticketing.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequestDTO(
        @NotNull Long scheduleId,
        @NotBlank @Email String customerEmail,
        @Min(1) int numberOfTickets
) {
}
