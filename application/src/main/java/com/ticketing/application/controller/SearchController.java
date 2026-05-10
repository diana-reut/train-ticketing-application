package com.ticketing.application.controller;

import com.ticketing.application.dto.SearchResponseDTO;
import com.ticketing.application.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<SearchResponseDTO> searchConnections(
            @RequestParam String fromStation,
            @RequestParam String toStation,
            @RequestParam LocalDate departureDate
    ) {
        return searchService.searchConnections(fromStation, toStation, departureDate);
    }
}
