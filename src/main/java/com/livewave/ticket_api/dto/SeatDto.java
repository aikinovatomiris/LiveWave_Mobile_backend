package com.livewave.ticket_api.dto;

public class SeatDto {
    private Long id;
    private Long eventId;
    private String seatNumber;
    private Integer rowNum;
    private Integer colNum;
    private boolean booked;

    public SeatDto() {}

    public SeatDto(Long id, Long eventId, String seatNumber, Integer rowNum, Integer colNum, boolean booked) {
        this.id = id;
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.booked = booked;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public Integer getRowNum() { return rowNum; }
    public void setRowNum(Integer rowNum) { this.rowNum = rowNum; }
    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }
}
