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

            LocalDateTime baseDepartureDay = LocalDateTime.now()
                    .plusDays(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

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
            Station sighisoara = stationRepository.save(Station.builder()
                    .name("Sighisoara")
                    .build());
            Station cluj = stationRepository.save(Station.builder()
                    .name("Cluj-Napoca")
                    .build());
            Station oradea = stationRepository.save(Station.builder()
                    .name("Oradea")
                    .build());

            Route route = routeRepository.save(Route.builder()
                    .name("Bucharest - Brasov")
                    .build());
            Route transylvaniaRoute = routeRepository.save(Route.builder()
                    .name("Brasov - Cluj-Napoca")
                    .build());
            Route westernRoute = routeRepository.save(Route.builder()
                    .name("Cluj-Napoca - Oradea")
                    .build());

            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(bucharest)
                    .stopOrder(1)
                    .minutesFromDeparture(0)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(ploiesti)
                    .stopOrder(2)
                    .minutesFromDeparture(35)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(sinaia)
                    .stopOrder(3)
                    .minutesFromDeparture(70)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(predeal)
                    .stopOrder(4)
                    .minutesFromDeparture(105)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(brasov)
                    .stopOrder(5)
                    .minutesFromDeparture(150)
                    .build());

            routeStopRepository.save(RouteStop.builder()
                    .route(transylvaniaRoute)
                    .station(brasov)
                    .stopOrder(1)
                    .minutesFromDeparture(0)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(transylvaniaRoute)
                    .station(sighisoara)
                    .stopOrder(2)
                    .minutesFromDeparture(110)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(transylvaniaRoute)
                    .station(cluj)
                    .stopOrder(3)
                    .minutesFromDeparture(240)
                    .build());

            routeStopRepository.save(RouteStop.builder()
                    .route(westernRoute)
                    .station(cluj)
                    .stopOrder(1)
                    .minutesFromDeparture(0)
                    .build());
            routeStopRepository.save(RouteStop.builder()
                    .route(westernRoute)
                    .station(oradea)
                    .stopOrder(2)
                    .minutesFromDeparture(150)
                    .build());

            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(train)
                    .route(route)
                    .departureTime(baseDepartureDay.withHour(8))
                    .arrivalTime(baseDepartureDay.withHour(10).withMinute(30))
                    .delayMinutes(0)
                    .build());
            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(trainRepository.save(Train.builder()
                            .name("IR 1645")
                            .capacity(12)
                            .build()))
                    .route(route)
                    .departureTime(baseDepartureDay.withHour(12))
                    .arrivalTime(baseDepartureDay.withHour(14).withMinute(30))
                    .delayMinutes(0)
                    .build());
            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(trainRepository.save(Train.builder()
                            .name("IR 1734")
                            .capacity(8)
                            .build()))
                    .route(transylvaniaRoute)
                    .departureTime(baseDepartureDay.withHour(11))
                    .arrivalTime(baseDepartureDay.withHour(15))
                    .delayMinutes(0)
                    .build());
            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(trainRepository.save(Train.builder()
                            .name("IR 1832")
                            .capacity(8)
                            .build()))
                    .route(transylvaniaRoute)
                    .departureTime(baseDepartureDay.withHour(15).withMinute(15))
                    .arrivalTime(baseDepartureDay.withHour(19).withMinute(15))
                    .delayMinutes(0)
                    .build());
            trainScheduleRepository.save(TrainSchedule.builder()
                    .train(trainRepository.save(Train.builder()
                            .name("IR 1920")
                            .capacity(8)
                            .build()))
                    .route(westernRoute)
                    .departureTime(baseDepartureDay.withHour(20))
                    .arrivalTime(baseDepartureDay.withHour(22).withMinute(30))
                    .delayMinutes(0)
                    .build());
        };
    }
}
