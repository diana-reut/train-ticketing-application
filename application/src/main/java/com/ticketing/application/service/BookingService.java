package com.ticketing.application.service;

import com.ticketing.application.dto.BookingRequestDTO;
import com.ticketing.application.dto.BookingResponseDTO;
import com.ticketing.application.dto.ItineraryBookingRequestDTO;
import com.ticketing.application.dto.ItineraryBookingResponseDTO;
import com.ticketing.application.exception.BookingConflictException;
import com.ticketing.application.exception.ResourceNotFoundException;
import com.ticketing.application.model.Booking;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.BookingRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
        Booking booking = createBookingRecord(request.scheduleId(), request.customerEmail(), request.numberOfTickets());

        emailService.sendBookingConfirmation(booking);

        return toResponse(booking);
    }

    @Transactional
    public ItineraryBookingResponseDTO createItineraryBooking(ItineraryBookingRequestDTO request) {
        List<Long> distinctScheduleIds = request.scheduleIds().stream()
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
        List<Booking> bookings = new ArrayList<>();

        for (Long scheduleId : distinctScheduleIds) {
            bookings.add(createBookingRecord(scheduleId, request.customerEmail(), request.numberOfTickets()));
        }

        emailService.sendItineraryBookingConfirmation(request.customerEmail(), bookings);

        return new ItineraryBookingResponseDTO(
                request.customerEmail(),
                request.numberOfTickets(),
                bookings.size(),
                bookings.stream().map(this::toResponse).toList()
        );
    }

    private Booking createBookingRecord(Long scheduleId, String customerEmail, int numberOfTickets) {
        TrainSchedule schedule = trainScheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Train schedule %d was not found".formatted(scheduleId)
                ));

        int alreadyBookedTickets = bookingRepository.countBookedSeats(schedule);
        int remainingSeats = schedule.getTrain().getCapacity() - alreadyBookedTickets;

        if (numberOfTickets > remainingSeats) {
            throw new BookingConflictException(
                    "Only %d seats are still available for this train".formatted(Math.max(remainingSeats, 0))
            );
        }

        return bookingRepository.save(Booking.builder()
                .customerEmail(customerEmail)
                .schedule(schedule)
                .numberOfTickets(numberOfTickets)
                .bookingTime(LocalDateTime.now())
                .build());
    }

    private BookingResponseDTO toResponse(Booking booking) {
        TrainSchedule schedule = booking.getSchedule();
        return new BookingResponseDTO(
                booking.getId(),
                booking.getCustomerEmail(),
                booking.getNumberOfTickets(),
                schedule.getTrain().getName(),
                schedule.getDepartureTime()
        );
    }
}
