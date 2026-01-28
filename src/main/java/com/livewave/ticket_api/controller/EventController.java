package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.repository.SeatRepository;
import com.livewave.ticket_api.repository.TicketRepository;
import com.livewave.ticket_api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<Event> getAllEvents(@RequestParam(required = false) String city) {
        if (city != null && !city.isEmpty()) {
            return eventRepository.findByCityIgnoreCase(city);
        }
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @PostMapping
    public Event createEventWithHall(@RequestBody Map<String, Object> body) {
        Event e = new Event();
        e.setTitle((String) body.get("title"));
        e.setDescription((String) body.get("description"));
        e.setCity((String) body.get("city"));
        e.setVenue((String) body.get("venue"));
        e.setLocation((String) body.get("location"));

        if (body.get("date") != null) {
            String dateString = (String) body.get("date");
            try {
                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                e.setDate(java.time.LocalDateTime.parse(dateString, formatter));
            } catch (Exception ex) {
                throw new RuntimeException("Invalid date format. Use 'yyyy-MM-dd HH:mm' (e.g. 2025-11-01 18:00)");
            }
        }

        if (body.get("price") != null) {
            e.setPrice(Double.valueOf(String.valueOf(body.get("price"))));
        } else {
            e.setPrice(0.0);
        }

        e.setImageBanner((String) body.getOrDefault("imageBanner", "assets/images/default_banner.jpg"));

        int rows = ((Number) body.getOrDefault("rows", 10)).intValue();
        int cols = ((Number) body.getOrDefault("cols", 10)).intValue();

        return eventService.createEventWithSeats(e, rows, cols);
    }

    @GetMapping("/{id}/seats")
    public List<Map<String, Object>> getSeats(@PathVariable Long id) {
        List<Seat> seats = seatRepository.findByEventIdOrderByRowNumAscColNumAsc(id);
        List<com.livewave.ticket_api.model.Ticket> tickets = ticketRepository.findByEventId(id);

        Set<Long> bookedSeatIds = tickets.stream()
                .map(t -> t.getSeatId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return seats.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("seatNumber", s.getSeatNumber());
            m.put("row", s.getRowNum());
            m.put("col", s.getColNum());
            m.put("status", bookedSeatIds.contains(s.getId()) ? "booked" : "available");
            return m;
        }).collect(Collectors.toList());
    }
}
