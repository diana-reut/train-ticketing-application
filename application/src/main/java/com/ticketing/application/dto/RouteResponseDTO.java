package com.ticketing.application.dto;

import java.util.List;

public record RouteResponseDTO(
        Long id,
        String name,
        List<RouteStopResponseDTO> stops
) {
}
