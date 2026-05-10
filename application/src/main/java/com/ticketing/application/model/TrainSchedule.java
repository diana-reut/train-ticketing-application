package com.ticketing.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Train train;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Route route;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private int delayMinutes;
}