package com.ticketing.application.service;

import com.ticketing.application.dto.SearchResponseDTO;
import com.ticketing.application.dto.SearchSegmentDTO;
import com.ticketing.application.exception.NoConnectionFoundException;
import com.ticketing.application.exception.ResourceNotFoundException;
import com.ticketing.application.model.RouteStop;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.RouteStopRepository;
import com.ticketing.application.repository.StationRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    private static final int MIN_TRANSFER_MINUTES = 30;
    private static final int MAX_SEGMENTS = 4;

    private final TrainScheduleRepository trainScheduleRepository;
    private final RouteStopRepository routeStopRepository;
    private final StationRepository stationRepository;

    public SearchService(
            TrainScheduleRepository trainScheduleRepository,
            RouteStopRepository routeStopRepository,
            StationRepository stationRepository
    ) {
        this.trainScheduleRepository = trainScheduleRepository;
        this.routeStopRepository = routeStopRepository;
        this.stationRepository = stationRepository;
    }

    public List<SearchResponseDTO> searchConnections(String fromStation, String toStation, LocalDate departureDate) {
        String normalizedFrom = requireStation(fromStation);
        String normalizedTo = requireStation(toStation);

        if (normalizedFrom.equalsIgnoreCase(normalizedTo)) {
            throw new NoConnectionFoundException("Departure and arrival stations must be different");
        }

        LocalDateTime dayStart = departureDate.atStartOfDay();
        LocalDateTime dayEnd = departureDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<TrainSchedule> schedules = trainScheduleRepository
                .findByDepartureTimeBetweenOrderByDepartureTimeAsc(dayStart, dayEnd);

        List<LegCandidate> allLegs = schedules.stream()
                .flatMap(this::buildLegCandidates)
                .toList();

        Map<String, List<LegCandidate>> legsByDepartureStation = allLegs.stream()
                .collect(Collectors.groupingBy(LegCandidate::fromStation));

        List<SearchResponseDTO> results = new ArrayList<>();
        buildItineraries(
                normalizedFrom,
                normalizedTo,
                legsByDepartureStation,
                new ArrayList<>(),
                new HashSet<>(Set.of(normalizedFrom)),
                results
        );

        results = results.stream()
                .sorted(Comparator.comparing(SearchResponseDTO::departureTime)
                        .thenComparing(SearchResponseDTO::arrivalTime))
                .toList();

        if (results.isEmpty()) {
            throw new NoConnectionFoundException(
                    "No connection found from %s to %s on %s".formatted(normalizedFrom, normalizedTo, departureDate)
            );
        }

        return results;
    }

    private String requireStation(String stationName) {
        return stationRepository.findByNameIgnoreCase(stationName)
                .map(station -> station.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Station '%s' was not found".formatted(stationName)
                ));
    }

    /**
     * Returns all segments for a train schedule.
     */
    private Stream<LegCandidate> buildLegCandidates(TrainSchedule schedule) {
        List<RouteStop> stops = routeStopRepository.findByRouteOrderByStopOrderAsc(schedule.getRoute());
        List<LegCandidate> legs = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            for (int j = i + 1; j < stops.size(); j++) {
                RouteStop fromStop = stops.get(i);
                RouteStop toStop = stops.get(j);
                legs.add(new LegCandidate(
                        schedule.getId(),
                        schedule.getTrain().getName(),
                        fromStop.getStation().getName(),
                        toStop.getStation().getName(),
                        schedule.getDepartureTime().plusMinutes(fromStop.getMinutesFromDeparture() + schedule.getDelayMinutes()),
                        schedule.getDepartureTime().plusMinutes(toStop.getMinutesFromDeparture() + schedule.getDelayMinutes())
                ));
            }
        }

        return legs.stream();
    }

    private void buildItineraries(
            String currentStation,
            String destinationStation,
            Map<String, List<LegCandidate>> legsByDepartureStation,
            List<LegCandidate> currentPath,
            Set<String> visitedStations,
            List<SearchResponseDTO> results
    ) {
        if (currentPath.size() >= MAX_SEGMENTS) {
            return;
        }

        List<LegCandidate> nextLegs = legsByDepartureStation.getOrDefault(currentStation, List.of());
        for (LegCandidate nextLeg : nextLegs) {
            if (!canAppend(currentPath, nextLeg) || visitedStations.contains(nextLeg.toStation())) {
                continue;
            }

            currentPath.add(nextLeg);
            boolean reachedDestination = nextLeg.toStation().equalsIgnoreCase(destinationStation);

            if (reachedDestination) {
                results.add(toResponse(currentPath));
            } else {
                visitedStations.add(nextLeg.toStation());
                buildItineraries(
                        nextLeg.toStation(),
                        destinationStation,
                        legsByDepartureStation,
                        currentPath,
                        visitedStations,
                        results
                );
                visitedStations.remove(nextLeg.toStation());
            }

            currentPath.remove(currentPath.size() - 1);
        }
    }

    private boolean canAppend(List<LegCandidate> currentPath, LegCandidate nextLeg) {
        if (currentPath.isEmpty()) {
            return true;
        }

        LegCandidate lastLeg = currentPath.get(currentPath.size() - 1);
        return !lastLeg.scheduleId().equals(nextLeg.scheduleId())
                && !nextLeg.departureTime().isBefore(lastLeg.arrivalTime().plusMinutes(MIN_TRANSFER_MINUTES));
    }

    private SearchResponseDTO toResponse(List<LegCandidate> legs) {
        List<SearchSegmentDTO> segments = legs.stream()
                .map(LegCandidate::toSegment)
                .toList();

        return new SearchResponseDTO(
                segments,
                Math.max(segments.size() - 1, 0),
                segments.get(0).departureTime(),
                segments.get(segments.size() - 1).arrivalTime()
        );
    }

    private record LegCandidate(
            Long scheduleId,
            String trainName,
            String fromStation,
            String toStation,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime
    ) {
        private SearchSegmentDTO toSegment() {
            return new SearchSegmentDTO(
                    scheduleId,
                    trainName,
                    fromStation,
                    toStation,
                    departureTime,
                    arrivalTime
            );
        }
    }
}
