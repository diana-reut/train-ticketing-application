package com.ticketing.application.service;

import com.ticketing.application.model.Booking;

public interface EmailService {

    void sendBookingConfirmation(Booking booking);
}
