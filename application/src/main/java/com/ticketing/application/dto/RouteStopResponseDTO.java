package com.ticketing.application.dto;

public record RouteStopResponseDTO(
        int stopOrder,
        String stationName,
        int minutesFromDeparture
) {
}
