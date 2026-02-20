package com.livewave.ticket_api.scheduler;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.TicketRepository;
import com.livewave.ticket_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    // каждые 10 минут
    @Scheduled(fixedRate = 600000)
    public void sendEventReminders() {

        LocalDateTime now = LocalDateTime.now();
        System.out.println("Scheduler run at: " + now);

        List<Ticket> tickets = ticketRepository.findTicketsForReminder();
        System.out.println("Tickets to check: " + tickets.size());

        for (Ticket ticket : tickets) {
            try {

                Event event = ticket.getEvent();

                if (event == null || event.getDate() == null)
                    continue;

                LocalDateTime eventDate = event.getDate();

                long hours = Duration.between(now, eventDate).toHours();

                System.out.println("Event: " + event.getTitle());
                System.out.println("Hours until event: " + hours);

                // окно 24 часа
                if (hours <= 24 && hours >= 23) {

                    if (ticket.getUser() == null)
                        continue;

                    String token = ticket.getUser().getFcmToken();

                    if (token == null || token.isBlank())
                        continue;

                    notificationService.sendPush(
                            token,
                            "Напоминание о событии",
                            "Событие \"" + event.getTitle() + "\" начнётся через 24 часа"
                    );

                    ticket.setReminderSent(true);
                    ticketRepository.save(ticket);

                    System.out.println("Reminder sent for ticket " + ticket.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
