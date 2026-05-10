package com.ticketing.application.repository;

import com.ticketing.application.model.Route;
import com.ticketing.application.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRoute(Route route);

    List<RouteStop> findByRouteOrderByStopOrderAsc(Route route);
}
