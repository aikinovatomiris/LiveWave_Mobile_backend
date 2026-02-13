package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.exception.*;
import com.livewave.ticket_api.model.*;
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

        Long eventId = ((Number) body.get("eventId")).longValue();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        List<?> rawSeats = (List<?>) body.get("seatNumbers");
        if (rawSeats == null || rawSeats.isEmpty()) {
            throw new IllegalArgumentException("Список мест пуст");
        }

        List<String> seatNumbers = new ArrayList<>();
        for (Object o : rawSeats) seatNumbers.add(String.valueOf(o));

        String email = (String) request.getAttribute("email");
        Optional<User> userOpt = (email != null)
                ? userRepository.findByEmail(email)
                : Optional.empty();

        List<String> failedSeats = new ArrayList<>();
        List<Ticket> savedTickets = new ArrayList<>();

        for (String seatNumber : seatNumbers) {

            Seat seat = seatRepository
                    .findByEventIdAndSeatNumber(eventId, seatNumber)
                    .orElse(null);

            if (seat == null) {
                failedSeats.add(seatNumber);
                continue;
            }

            boolean alreadyBought = ticketRepository
                    .findByEventIdAndSeatId(eventId, seat.getId())
                    .isPresent();

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
            throw new SeatConflictException(failedSeats);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Покупка успешна!");
        response.put("created", savedTickets.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/myTickets")
    public List<Ticket> myTickets(HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ticketRepository.findByUser(user);
    }
}
