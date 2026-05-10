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
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;

    @ManyToOne
    @JoinColumn(nullable = false)
    private TrainSchedule schedule;

    private int numberOfTickets;

    private LocalDateTime bookingTime;
}