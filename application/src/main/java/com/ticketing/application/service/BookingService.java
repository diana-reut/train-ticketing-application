package com.ticketing.application.service;

import com.ticketing.application.dto.BookingRequestDTO;
import com.ticketing.application.dto.BookingResponseDTO;
import com.ticketing.application.exception.BookingConflictException;
import com.ticketing.application.exception.ResourceNotFoundException;
import com.ticketing.application.model.Booking;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.BookingRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final EmailService emailService;

    public BookingService(
            BookingRepository bookingRepository,
            TrainScheduleRepository trainScheduleRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.trainScheduleRepository = trainScheduleRepository;
        this.emailService = emailService;
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        TrainSchedule schedule = trainScheduleRepository.findByIdForUpdate(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train schedule %d was not found".formatted(request.scheduleId())
                ));

        int alreadyBookedTickets = bookingRepository.countBookedSeats(schedule);
        int remainingSeats = schedule.getTrain().getCapacity() - alreadyBookedTickets;

        if (request.numberOfTickets() > remainingSeats) {
            throw new BookingConflictException(
                    "Only %d seats are still available for this train".formatted(Math.max(remainingSeats, 0))
            );
        }

        Booking booking = bookingRepository.save(Booking.builder()
                .customerEmail(request.customerEmail())
                .schedule(schedule)
                .numberOfTickets(request.numberOfTickets())
                .bookingTime(LocalDateTime.now())
                .build());

        emailService.sendBookingConfirmation(booking);

        return new BookingResponseDTO(
                booking.getId(),
                booking.getCustomerEmail(),
                booking.getNumberOfTickets(),
                schedule.getTrain().getName(),
                schedule.getDepartureTime()
        );
    }
}
