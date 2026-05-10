package com.ticketing.application.exception;

public class NoConnectionFoundException extends RuntimeException {

    public NoConnectionFoundException(String message) {
        super(message);
    }
}
