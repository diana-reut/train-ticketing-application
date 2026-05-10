package com.ticketing.application.service;

import com.ticketing.application.dto.BookingRequestDTO;
import com.ticketing.application.dto.BookingResponseDTO;
import com.ticketing.application.exception.BookingConflictException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private RouteRepository routeRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void createsBookingWhenEnoughSeatsAreAvailable() {
        Train train = trainRepository.save(Train.builder().name("IC 531").capacity(120).build());
        Route route = routeRepository.save(Route.builder().name("Bucharest - Brasov").build());
        TrainSchedule schedule = trainScheduleRepository.save(TrainSchedule.builder()
                .train(train)
                .route(route)
                .departureTime(LocalDateTime.of(2026, 5, 12, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 12, 10, 30))
                .delayMinutes(0)
                .build());

        BookingResponseDTO response = bookingService.createBooking(
                new BookingRequestDTO(schedule.getId(), "client@example.com", 3)
        );

        assertNotNull(response.bookingId());
        assertEquals("client@example.com", response.customerEmail());
        assertEquals(3, response.numberOfTickets());
        assertEquals(3, bookingRepository.countBookedSeats(schedule));
        verify(emailService, times(1)).sendBookingConfirmation(org.mockito.ArgumentMatchers.any(Booking.class));
    }

    @Test
    void rejectsBookingThatWouldOverbookTheTrain() {
        Train train = trainRepository.save(Train.builder().name("RE 402").capacity(10).build());
        Route route = routeRepository.save(Route.builder().name("Cluj - Sibiu").build());
        TrainSchedule schedule = trainScheduleRepository.save(TrainSchedule.builder()
                .train(train)
                .route(route)
                .departureTime(LocalDateTime.of(2026, 5, 13, 9, 0))
                .arrivalTime(LocalDateTime.of(2026, 5, 13, 12, 0))
                .delayMinutes(0)
                .build());

        bookingRepository.save(Booking.builder()
                .customerEmail("existing@example.com")
                .schedule(schedule)
                .numberOfTickets(8)
                .bookingTime(LocalDateTime.now())
                .build());

        BookingConflictException exception = assertThrows(
                BookingConflictException.class,
                () -> bookingService.createBooking(
                        new BookingRequestDTO(schedule.getId(), "other@example.com", 3)
                )
        );

        assertEquals("Only 2 seats are still available for this train", exception.getMessage());
        verify(emailService, times(0)).sendBookingConfirmation(org.mockito.ArgumentMatchers.any(Booking.class));
    }
}
