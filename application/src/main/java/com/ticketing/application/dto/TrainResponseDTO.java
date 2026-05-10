package com.ticketing.application.dto;

public record TrainResponseDTO(
        Long id,
        String name,
        int capacity
) {
}
