package com.ticketing.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RouteRequestDTO(
        @NotBlank String name,
        @Size(min = 2) List<@Valid RouteStopRequestDTO> stops
) {
}
