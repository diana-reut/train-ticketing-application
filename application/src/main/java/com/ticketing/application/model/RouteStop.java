package com.ticketing.application.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Station station;

    private int stopOrder;

    private int minutesFromDeparture;
}
