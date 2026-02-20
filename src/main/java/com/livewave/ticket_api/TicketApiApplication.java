package com.livewave.ticket_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class TicketApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketApiApplication.class, args);
    }
}
