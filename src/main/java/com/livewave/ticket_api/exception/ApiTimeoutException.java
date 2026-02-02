package com.livewave.ticket_api.exception;

public class ApiTimeoutException extends RuntimeException {
    public ApiTimeoutException(String message) {
        super(message);
    }

    public ApiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}