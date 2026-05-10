package com.ticketing.application.service;

import com.ticketing.application.dto.DelayUpdateRequestDTO;
import com.ticketing.application.dto.RouteRequestDTO;
import com.ticketing.application.dto.RouteResponseDTO;
import com.ticketing.application.dto.RouteStopRequestDTO;
import com.ticketing.application.dto.ScheduleResponseDTO;
import com.ticketing.application.dto.TrainBookingResponseDTO;
import com.ticketing.application.dto.TrainRequestDTO;
import com.ticketing.application.dto.TrainResponseDTO;
import com.ticketing.application.model.Booking;
import com.ticketing.application.model.Route;
import com.ticketing.application.model.Train;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.BookingRepository;
import com.ticketing.application.repository.RouteRepository;
import com.ticketing.application.repository.TrainRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void createsTrainForAdministrators() {
        TrainResponseDTO response = adminService.createTrain(new TrainRequestDTO("Admin Test Train", 77));

        assertEquals("Admin Test Train", response.name());
        assertEquals(77, response.capacity());
        assertTrue(trainRepository.findById(response.id()).isPresent());
    }

    @Test
    void createsRouteWithOrderedStations() {
        RouteResponseDTO response = adminService.createRoute(new RouteRequestDTO(
                "Admin Route",
                List.of(
                        new RouteStopRequestDTO("Alpha", 0),
                        new RouteStopRequestDTO("Beta", 45),
                        new RouteStopRequestDTO("Gamma", 95)
                )
        ));

        assertEquals("Admin Route", response.name());
        assertEquals(3, response.stops().size());
        assertEquals("Alpha", response.stops().get(0).stationName());
        assertEquals(1, response.stops().get(0).stopOrder());
        assertEquals("Gamma", response.stops().get(2).stationName());
    }

    @Test
    void showsBookingsForAnyTrain() {
        Train train = trainRepository.save(Train.builder().name("Bookings Train").capacity(50).build());
        Route route = routeRepository.findAll().getFirst();
        TrainSchedule schedule = trainScheduleRepository.save(TrainSchedule.builder()
                .train(train)
                .route(route)
                .departureTime(LocalDateTime.now().plusDays(5))
                .arrivalTime(LocalDateTime.now().plusDays(5).plusHours(2))
                .delayMinutes(0)
                .build());
        bookingRepository.save(Booking.builder()
                .customerEmail("bookings@example.com")
                .schedule(schedule)
                .numberOfTickets(2)
                .bookingTime(LocalDateTime.now())
                .build());

        List<TrainBookingResponseDTO> bookings = adminService.getBookingsForTrain(train.getId());

        assertEquals(1, bookings.size());
        assertEquals("bookings@example.com", bookings.get(0).customerEmail());
        assertEquals(train.getName(), bookings.get(0).trainName());
    }

    @Test
    void updatesDelayAndNotifiesAffectedCustomers() {
        Train train = trainRepository.save(Train.builder().name("Delay Train").capacity(60).build());
        Route route = routeRepository.findAll().getFirst();
        TrainSchedule schedule = trainScheduleRepository.save(TrainSchedule.builder()
                .train(train)
                .route(route)
                .departureTime(LocalDateTime.now().plusDays(6))
                .arrivalTime(LocalDateTime.now().plusDays(6).plusHours(3))
                .delayMinutes(0)
                .build());
        bookingRepository.save(Booking.builder()
                .customerEmail("delay-one@example.com")
                .schedule(schedule)
                .numberOfTickets(1)
                .bookingTime(LocalDateTime.now())
                .build());
        bookingRepository.save(Booking.builder()
                .customerEmail("delay-two@example.com")
                .schedule(schedule)
                .numberOfTickets(1)
                .bookingTime(LocalDateTime.now())
                .build());

        ScheduleResponseDTO response = adminService.updateDelay(schedule.getId(), new DelayUpdateRequestDTO(25));

        assertEquals(25, response.delayMinutes());
        verify(emailService, times(1)).sendDelayNotification(org.mockito.ArgumentMatchers.eq("delay-one@example.com"), any(TrainSchedule.class));
        verify(emailService, times(1)).sendDelayNotification(org.mockito.ArgumentMatchers.eq("delay-two@example.com"), any(TrainSchedule.class));
    }
}
