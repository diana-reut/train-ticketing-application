package com.ticketing.application.repository;

import com.ticketing.application.model.TrainSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    List<TrainSchedule> findByDepartureTimeAfter(LocalDateTime time);

    List<TrainSchedule> findByDepartureTimeAfterOrderByDepartureTimeAsc(LocalDateTime time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ts from TrainSchedule ts where ts.id = :id")
    Optional<TrainSchedule> findByIdForUpdate(@Param("id") Long id);
}
