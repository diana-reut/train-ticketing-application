package com.ticketing.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();
}
