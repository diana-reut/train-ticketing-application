package com.ticketing.application.controller;

import com.ticketing.application.dto.DelayUpdateRequestDTO;
import com.ticketing.application.dto.RouteRequestDTO;
import com.ticketing.application.dto.RouteResponseDTO;
import com.ticketing.application.dto.ScheduleResponseDTO;
import com.ticketing.application.dto.TrainBookingResponseDTO;
import com.ticketing.application.dto.TrainRequestDTO;
import com.ticketing.application.dto.TrainResponseDTO;
import com.ticketing.application.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/trains")
    public List<TrainResponseDTO> getTrains() {
        return adminService.getTrains();
    }

    @GetMapping("/trains/{trainId}")
    public TrainResponseDTO getTrain(@PathVariable Long trainId) {
        return adminService.getTrain(trainId);
    }

    @PostMapping("/trains")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainResponseDTO createTrain(@Valid @RequestBody TrainRequestDTO request) {
        return adminService.createTrain(request);
    }

    @PutMapping("/trains/{trainId}")
    public TrainResponseDTO updateTrain(@PathVariable Long trainId, @Valid @RequestBody TrainRequestDTO request) {
        return adminService.updateTrain(trainId, request);
    }

    @DeleteMapping("/trains/{trainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrain(@PathVariable Long trainId) {
        adminService.deleteTrain(trainId);
    }

    @GetMapping("/routes")
    public List<RouteResponseDTO> getRoutes() {
        return adminService.getRoutes();
    }

    @GetMapping("/routes/{routeId}")
    public RouteResponseDTO getRoute(@PathVariable Long routeId) {
        return adminService.getRoute(routeId);
    }

    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponseDTO createRoute(@Valid @RequestBody RouteRequestDTO request) {
        return adminService.createRoute(request);
    }

    @PutMapping("/routes/{routeId}")
    public RouteResponseDTO updateRoute(@PathVariable Long routeId, @Valid @RequestBody RouteRequestDTO request) {
        return adminService.updateRoute(routeId, request);
    }

    @DeleteMapping("/routes/{routeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@PathVariable Long routeId) {
        adminService.deleteRoute(routeId);
    }

    @GetMapping("/trains/{trainId}/bookings")
    public List<TrainBookingResponseDTO> getBookingsForTrain(@PathVariable Long trainId) {
        return adminService.getBookingsForTrain(trainId);
    }

    @GetMapping("/schedules")
    public List<ScheduleResponseDTO> getSchedules() {
        return adminService.getSchedules();
    }

    @PutMapping("/schedules/{scheduleId}/delay")
    public ScheduleResponseDTO updateDelay(
            @PathVariable Long scheduleId,
            @Valid @RequestBody DelayUpdateRequestDTO request
    ) {
        return adminService.updateDelay(scheduleId, request);
    }
}
