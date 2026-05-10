package com.ticketing.application.dto;

import jakarta.validation.constraints.Min;

public record DelayUpdateRequestDTO(
        @Min(0) int delayMinutes
) {
}
