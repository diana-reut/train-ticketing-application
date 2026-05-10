package com.ticketing.application.service;

import com.ticketing.application.dto.DelayUpdateRequestDTO;
import com.ticketing.application.dto.RouteRequestDTO;
import com.ticketing.application.dto.RouteResponseDTO;
import com.ticketing.application.dto.RouteStopResponseDTO;
import com.ticketing.application.dto.ScheduleResponseDTO;
import com.ticketing.application.dto.TrainBookingResponseDTO;
import com.ticketing.application.dto.TrainRequestDTO;
import com.ticketing.application.dto.TrainResponseDTO;
import com.ticketing.application.exception.BookingConflictException;
import com.ticketing.application.exception.ResourceNotFoundException;
import com.ticketing.application.model.Booking;
import com.ticketing.application.model.Route;
import com.ticketing.application.model.RouteStop;
import com.ticketing.application.model.Station;
import com.ticketing.application.model.Train;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.BookingRepository;
import com.ticketing.application.repository.RouteRepository;
import com.ticketing.application.repository.StationRepository;
import com.ticketing.application.repository.TrainRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public AdminService(
            TrainRepository trainRepository,
            RouteRepository routeRepository,
            StationRepository stationRepository,
            TrainScheduleRepository trainScheduleRepository,
            BookingRepository bookingRepository,
            EmailService emailService
    ) {
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
        this.trainScheduleRepository = trainScheduleRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    public List<TrainResponseDTO> getTrains() {
        return trainRepository.findAll().stream()
                .map(this::toTrainResponse)
                .toList();
    }

    public TrainResponseDTO getTrain(Long trainId) {
        return toTrainResponse(findTrain(trainId));
    }

    @Transactional
    public TrainResponseDTO createTrain(TrainRequestDTO request) {
        Train train = trainRepository.save(Train.builder()
                .name(request.name().trim())
                .capacity(request.capacity())
                .build());
        return toTrainResponse(train);
    }

    @Transactional
    public TrainResponseDTO updateTrain(Long trainId, TrainRequestDTO request) {
        Train train = findTrain(trainId);
        train.setName(request.name().trim());
        train.setCapacity(request.capacity());
        return toTrainResponse(trainRepository.save(train));
    }

    @Transactional
    public void deleteTrain(Long trainId) {
        findTrain(trainId);
        if (trainScheduleRepository.existsByTrainId(trainId)) {
            throw new BookingConflictException("Train %d cannot be deleted because schedules already use it".formatted(trainId));
        }
        trainRepository.deleteById(trainId);
    }

    public List<RouteResponseDTO> getRoutes() {
        return routeRepository.findAll().stream()
                .map(this::toRouteResponse)
                .toList();
    }

    public RouteResponseDTO getRoute(Long routeId) {
        return toRouteResponse(findRoute(routeId));
    }

    @Transactional
    public RouteResponseDTO createRoute(RouteRequestDTO request) {
        Route route = new Route();
        applyRoute(route, request);
        return toRouteResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponseDTO updateRoute(Long routeId, RouteRequestDTO request) {
        Route route = findRoute(routeId);
        applyRoute(route, request);
        return toRouteResponse(routeRepository.save(route));
    }

    @Transactional
    public void deleteRoute(Long routeId) {
        findRoute(routeId);
        if (trainScheduleRepository.existsByRouteId(routeId)) {
            throw new BookingConflictException("Route %d cannot be deleted because schedules already use it".formatted(routeId));
        }
        routeRepository.deleteById(routeId);
    }

    public List<TrainBookingResponseDTO> getBookingsForTrain(Long trainId) {
        Train train = findTrain(trainId);
        return bookingRepository.findByScheduleTrainIdOrderByBookingTimeDesc(train.getId()).stream()
                .map(this::toTrainBookingResponse)
                .toList();
    }

    public List<ScheduleResponseDTO> getSchedules() {
        return trainScheduleRepository.findAllByOrderByDepartureTimeAsc().stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponseDTO updateDelay(Long scheduleId, DelayUpdateRequestDTO request) {
        TrainSchedule schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train schedule %d was not found".formatted(scheduleId)
                ));

        schedule.setDelayMinutes(request.delayMinutes());
        TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);

        if (savedSchedule.getDelayMinutes() > 0) {
            List<String> recipients = bookingRepository.findByScheduleIdOrderByBookingTimeAsc(scheduleId).stream()
                    .map(Booking::getCustomerEmail)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .toList();

            for (String recipient : recipients) {
                emailService.sendDelayNotification(recipient, savedSchedule);
            }
        }

        return toScheduleResponse(savedSchedule);
    }

    private Train findTrain(Long trainId) {
        return trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train %d was not found".formatted(trainId)
                ));
    }

    private Route findRoute(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Route %d was not found".formatted(routeId)
                ));
    }

    private void applyRoute(Route route, RouteRequestDTO request) {
        route.setName(request.name().trim());
        List<RouteStop> stops = new ArrayList<>();

        for (int i = 0; i < request.stops().size(); i++) {
            var stopRequest = request.stops().get(i);
            Station station = stationRepository.findByNameIgnoreCase(stopRequest.stationName().trim())
                    .orElseGet(() -> stationRepository.save(Station.builder()
                            .name(stopRequest.stationName().trim())
                            .build()));

            stops.add(RouteStop.builder()
                    .route(route)
                    .station(station)
                    .stopOrder(i + 1)
                    .minutesFromDeparture(stopRequest.minutesFromDeparture())
                    .build());
        }

        route.getStops().clear();
        route.getStops().addAll(stops);
    }

    private TrainResponseDTO toTrainResponse(Train train) {
        return new TrainResponseDTO(train.getId(), train.getName(), train.getCapacity());
    }

    private RouteResponseDTO toRouteResponse(Route route) {
        return new RouteResponseDTO(
                route.getId(),
                route.getName(),
                route.getStops().stream()
                        .map(stop -> new RouteStopResponseDTO(
                                stop.getStopOrder(),
                                stop.getStation().getName(),
                                stop.getMinutesFromDeparture()
                        ))
                        .toList()
        );
    }

    private TrainBookingResponseDTO toTrainBookingResponse(Booking booking) {
        return new TrainBookingResponseDTO(
                booking.getId(),
                booking.getSchedule().getId(),
                booking.getSchedule().getTrain().getName(),
                booking.getCustomerEmail(),
                booking.getNumberOfTickets(),
                booking.getBookingTime(),
                booking.getSchedule().getDepartureTime()
        );
    }

    private ScheduleResponseDTO toScheduleResponse(TrainSchedule schedule) {
        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getTrain().getName(),
                schedule.getRoute().getName(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getDelayMinutes()
        );
    }
}
