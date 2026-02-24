package com.livewave.ticket_api.service;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.livewave.ticket_api.dto.SeatDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Ticket save(Ticket ticket) {
        Ticket saved = ticketRepository.save(ticket);

        if (saved.getSeatId() != null && saved.getEvent() != null) {

            SeatDto seatDto = new SeatDto(
                    saved.getSeatId(),
                    saved.getEvent().getId(),
                    null,
                    0,
                    0,
                    true
            );

            webSocketService.sendSeatUpdate(
                    saved.getEvent().getId(),
                    seatDto
            );
        }

        sendInstantReminderIfNeeded(saved);

        return saved;
    }

    private void sendInstantReminderIfNeeded(Ticket ticket) {

        try {

            Event event = ticket.getEvent();
            if (event == null || event.getDate() == null) return;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventDate = event.getDate();

            long hours = Duration.between(now, eventDate).toHours();

            System.out.println("Instant reminder check. Hours left: " + hours);

            // если событие менее чем через 24 часа
            if (hours < 24 && hours >= 0 && !ticket.isReminderSent()) {

                if (ticket.getUser() == null) return;

                String token = ticket.getUser().getFcmToken();
                if (token == null || token.isBlank()) return;

                notificationService.sendPush(
                        token,
                        "Билет успешно куплен",
                        "Событие \"" + event.getTitle() + "\" начнётся менее чем через 24 часа"
                );

                ticket.setReminderSent(true);
                ticketRepository.save(ticket);

                System.out.println("Instant reminder SENT");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}