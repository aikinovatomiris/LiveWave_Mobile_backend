package com.livewave.ticket_api.exception;

import java.util.List;

public class SeatConflictException extends RuntimeException {

    private final List<String> seats;

    public SeatConflictException(List<String> seats) {
        super("Следующие места уже заняты: " + String.join(", ", seats));
        this.seats = seats;
    }

    public List<String> getSeats() {
        return seats;
    }
}
