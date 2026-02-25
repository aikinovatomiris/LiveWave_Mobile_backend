package com.livewave.ticket_api.graphql;

import java.time.LocalDateTime;

/**
 * Lightweight DTO for GraphQL to avoid lazy-loading/N+1 issues.
 */
public class TicketView {
    private Long id;
    private String seatNumber;
    private Long seatId;
    private LocalDateTime purchaseDate;
    private Long eventId;

    public TicketView() {}

    public TicketView(Long id, String seatNumber, Long seatId, LocalDateTime purchaseDate, Long eventId) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.seatId = seatId;
        this.purchaseDate = purchaseDate;
        this.eventId = eventId;
    }

    public Long getId() { return id; }
    public String getSeatNumber() { return seatNumber; }
    public Long getSeatId() { return seatId; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public Long getEventId() { return eventId; }

    public void setId(Long id) { this.id = id; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}