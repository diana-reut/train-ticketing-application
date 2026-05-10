package com.ticketing.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RouteStopRequestDTO(
        @NotBlank String stationName,
        @Min(0) int minutesFromDeparture
) {
}
