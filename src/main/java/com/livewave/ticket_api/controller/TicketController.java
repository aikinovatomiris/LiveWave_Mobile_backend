
package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @PostMapping({"/buyTicket", "/seats/book"})
    public ResponseEntity<?> buyTicket(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            Long eventId = ((Number) body.get("eventId")).longValue();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            List<?> rawSeats = (List<?>) body.get("seatNumbers");
            if (rawSeats == null || rawSeats.isEmpty())
                return ResponseEntity.badRequest().body("Список мест пуст");

            List<String> seatNumbers = new ArrayList<>();
            for (Object o : rawSeats) seatNumbers.add(String.valueOf(o));

            String email = (String) request.getAttribute("email");
            Optional<User> userOpt = (email != null) ? userRepository.findByEmail(email) : Optional.empty();

            List<String> failedSeats = new ArrayList<>();
            List<Ticket> savedTickets = new ArrayList<>();

            for (String seatNumber : seatNumbers) {
                Optional<Seat> seatOpt = seatRepository.findByEventIdAndSeatNumber(eventId, seatNumber);
                if (seatOpt.isEmpty()) {
                    failedSeats.add(seatNumber);
                    continue;
                }

                Seat seat = seatOpt.get();
                boolean alreadyBought = ticketRepository.findByEventIdAndSeatId(eventId, seat.getId()).isPresent();
                if (alreadyBought) {
                    failedSeats.add(seatNumber);
                    continue;
                }

                Ticket ticket = new Ticket();
                ticket.setEvent(event);
                ticket.setSeatNumber(seatNumber);
                ticket.setSeatId(seat.getId());
                userOpt.ifPresent(ticket::setUser);

                savedTickets.add(ticketRepository.save(ticket));
            }

            if (!failedSeats.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Следующие места уже заняты: " + String.join(", ", failedSeats));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Покупка успешна!");
            response.put("created", savedTickets.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при покупке: " + e.getMessage());
        }
    }

    @GetMapping("/myTickets")
    public List<Ticket> myTickets(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new RuntimeException("Unauthorized");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser(user);
    }
}

