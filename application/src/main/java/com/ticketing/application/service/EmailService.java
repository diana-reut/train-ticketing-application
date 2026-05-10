package com.ticketing.application.service;

import com.ticketing.application.model.Booking;

import java.util.List;

public interface EmailService {

    void sendBookingConfirmation(Booking booking);

    void sendItineraryBookingConfirmation(String customerEmail, List<Booking> bookings);
}
