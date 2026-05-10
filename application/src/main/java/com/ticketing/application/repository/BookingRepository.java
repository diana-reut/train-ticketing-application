package com.ticketing.application.repository;

import com.ticketing.application.model.Booking;
import com.ticketing.application.model.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT COALESCE(SUM(b.numberOfTickets), 0)
            FROM Booking b
            WHERE b.schedule = :schedule
            """)
    int countBookedSeats(@Param("schedule") TrainSchedule schedule);

    List<Booking> findByScheduleTrainIdOrderByBookingTimeDesc(Long trainId);

    List<Booking> findByScheduleIdOrderByBookingTimeAsc(Long scheduleId);
}
