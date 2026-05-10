package com.ticketing.application.service;

import com.ticketing.application.dto.SearchResponseDTO;
import com.ticketing.application.exception.NoConnectionFoundException;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Test
    void findsDirectConnectionsBetweenStationsOnTheSameTrain() {
        List<SearchResponseDTO> results = searchService.searchConnections("Bucharest North", "Predeal", seededScheduleDate());

        SearchResponseDTO itinerary = findItinerary(results, result ->
                result.numberOfChanges() == 0
                        && result.segments().size() == 1
                        && result.segments().get(0).fromStation().equals("Bucharest North")
                        && result.segments().get(0).toStation().equals("Predeal")
        );

        assertEquals("Bucharest North", itinerary.segments().get(0).fromStation());
        assertEquals("Predeal", itinerary.segments().get(0).toStation());
    }

    @Test
    void findsConnectionsWithOneChangeoverWhenNeeded() {
        List<SearchResponseDTO> results = searchService.searchConnections("Bucharest North", "Cluj-Napoca", seededScheduleDate());

        SearchResponseDTO itinerary = findItinerary(results, result -> result.numberOfChanges() == 1);

        assertEquals(2, itinerary.segments().size());
        assertEquals("Bucharest North", itinerary.segments().get(0).fromStation());
        assertEquals("Brasov", itinerary.segments().get(0).toStation());
        assertEquals("Brasov", itinerary.segments().get(1).fromStation());
        assertEquals("Cluj-Napoca", itinerary.segments().get(1).toStation());
        assertTrue(itinerary.segments().get(1).departureTime().isAfter(itinerary.segments().get(0).arrivalTime().minusMinutes(1)));
    }

    @Test
    void findsConnectionsWithMultipleChangeoversWhenNeeded() {
        List<SearchResponseDTO> results = searchService.searchConnections("Bucharest North", "Oradea", seededScheduleDate());

        SearchResponseDTO itinerary = findItinerary(results, result -> result.numberOfChanges() == 2);

        assertEquals(3, itinerary.segments().size());
        assertEquals("Bucharest North", itinerary.segments().get(0).fromStation());
        assertEquals("Brasov", itinerary.segments().get(0).toStation());
        assertEquals("Cluj-Napoca", itinerary.segments().get(1).toStation());
        assertEquals("Cluj-Napoca", itinerary.segments().get(2).fromStation());
        assertEquals("Oradea", itinerary.segments().get(2).toStation());
    }

    @Test
    void throwsHelpfulErrorWhenNoConnectionExists() {
        LocalDate seededScheduleDate = seededScheduleDate();
        NoConnectionFoundException exception = assertThrows(
                NoConnectionFoundException.class,
                () -> searchService.searchConnections("Cluj-Napoca", "Bucharest North", seededScheduleDate)
        );

        assertEquals(
                "No connection found from Cluj-Napoca to Bucharest North on %s".formatted(seededScheduleDate),
                exception.getMessage()
        );
    }

    private LocalDate seededScheduleDate() {
        return trainScheduleRepository.findAll().stream()
                .map(schedule -> schedule.getDepartureTime().toLocalDate())
                .sorted()
                .findFirst()
                .orElseThrow();
    }

    private SearchResponseDTO findItinerary(
            List<SearchResponseDTO> results,
            Predicate<SearchResponseDTO> predicate
    ) {
        return results.stream()
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected itinerary was not found in search results"));
    }
}
