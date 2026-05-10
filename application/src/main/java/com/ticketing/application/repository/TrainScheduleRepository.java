package com.ticketing.application.repository;

import com.ticketing.application.model.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    List<TrainSchedule> findByDepartureTimeAfter(LocalDateTime time);
}
