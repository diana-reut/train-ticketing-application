package com.ticketing.application.service;

import com.ticketing.application.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailService(
            JavaMailSender mailSender,
            @Value("${booking.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendBookingConfirmation(Booking booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(booking.getCustomerEmail());
        message.setSubject("Train ticket booking confirmation");
        message.setText("""
                Your booking has been confirmed.
                Thank you for booking with us!

                Booking ID: %d
                Train: %s
                Departure: %s
                Tickets: %d
                """
                .formatted(
                        booking.getId(),
                        booking.getSchedule().getTrain().getName(),
                        booking.getSchedule().getDepartureTime(),
                        booking.getNumberOfTickets()
                ));

        mailSender.send(message);
    }
}
