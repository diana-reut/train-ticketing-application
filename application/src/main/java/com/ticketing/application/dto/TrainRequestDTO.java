package com.ticketing.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TrainRequestDTO(
        @NotBlank String name,
        @Min(1) int capacity
) {
}
