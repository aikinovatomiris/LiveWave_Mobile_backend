package com.livewave.ticket_api.graphql;

public class BuyResult {
    private String message;
    private int created;

    public BuyResult() {}

    public BuyResult(String message, int created) {
        this.message = message;
        this.created = created;
    }

    public String getMessage() { return message; }
    public int getCreated() { return created; }

    public void setMessage(String message) { this.message = message; }
    public void setCreated(int created) { this.created = created; }
}