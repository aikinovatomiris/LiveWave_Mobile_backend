package com.livewave.ticket_api.service;

import com.livewave.ticket_api.dto.SeatDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendSeatUpdate(Long eventId, SeatDto seatDto) {
        messagingTemplate.convertAndSend(
                "/topic/seats/" + eventId,
                seatDto
        );
    }
}