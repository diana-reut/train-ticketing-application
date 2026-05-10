package com.ticketing.application.config;

import com.ticketing.application.model.Route;
import com.ticketing.application.model.RouteStop;
import com.ticketing.application.model.Station;
import com.ticketing.application.model.Train;
import com.ticketing.application.model.TrainSchedule;
import com.ticketing.application.repository.RouteRepository;
import com.ticketing.application.repository.RouteStopRepository;
import com.ticketing.application.repository.StationRepository;
import com.ticketing.application.repository.TrainRepository;
import com.ticketing.application.repository.TrainScheduleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner seedDemoData(
            TrainRepository trainRepository,
            StationRepository stationRepository,
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            TrainScheduleRepository trainScheduleRepository
    ) {
        return args -> {
            if (trainScheduleRepository.count() > 0) {
                return;
            }

            Train train = trainRepository.save(Train.builder()
                    .name("IR 1581")
                    .capacity(10)
                    .build());

            Station bucharest = stationRepository.save(Station.builder()
                    .name("Bucharest North")
                    .build());
            Station ploiesti = stationRepository.save(Station.builder()
                    .name("Ploiesti West")
                    .build());
            Station sinaia = stationRepository.save(Station.builder()
                    .name("Sinaia")
                    .build());
            Station predeal = stationRepository.save(Station.builder()
                    .name("Predeal")
                    .build());
            Station brasov = stationRepository.save(Station.builder()
                    .name("Brasov")
                    .build());

            Route route = routeRepository.save(Route.builder()
                    .name("Bucharest - Brasov")
                    .build());

            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(bucharest)
                    .stopOrder(1)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(ploiesti)
                    .stopOrder(2)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(sinaia)
                    .stopOrder(3)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(predeal)
                    .stopOrder(4)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(brasov)
                    .stopOrder(5)
                    .build());

            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(train)
                    .route(route)
                    .departureTime(LocalDateTime.of(2026, 5, 15, 8, 0))
                    .arrivalTime(LocalDateTime.of(2026, 5, 15, 10, 30))
                    .delayMinutes(0)
                    .build());
        };
    }
}
