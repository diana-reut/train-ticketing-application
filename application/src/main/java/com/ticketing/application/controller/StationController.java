package com.ticketing.application.controller;

import com.ticketing.application.repository.StationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public List<String> getStations() {
        return stationRepository.findAll().stream()
                .map(station -> station.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
