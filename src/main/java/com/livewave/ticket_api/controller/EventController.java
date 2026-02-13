package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.SeatRepository;
import com.livewave.ticket_api.repository.TicketRepository;
import com.livewave.ticket_api.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public EventController(
            EventService eventService,
            SeatRepository seatRepository,
            TicketRepository ticketRepository
    ) {
        this.eventService = eventService;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public List<Event> getAllEvents(@RequestParam(required = false) String city) {
        return eventService.findAll(city);
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @PostMapping
    public Event createEventWithHall(@RequestBody Map<String, Object> body) {
        Event e = new Event();
        e.setTitle((String) body.get("title"));
        e.setDescription((String) body.get("description"));
        e.setCity((String) body.get("city"));
        e.setVenue((String) body.get("venue"));
        e.setLocation((String) body.get("location"));
        e.setImageBanner(
                (String) body.getOrDefault(
                        "imageBanner",
                        "assets/images/default_banner.jpg"
                )
        );

        if (body.get("price") != null) {
            e.setPrice(Double.valueOf(String.valueOf(body.get("price"))));
        } else {
            e.setPrice(0.0);
        }

        int rows = ((Number) body.getOrDefault("rows", 10)).intValue();
        int cols = ((Number) body.getOrDefault("cols", 10)).intValue();
        String date = (String) body.get("date");

        return eventService.createEventWithSeats(e, rows, cols, date);
    }

    @GetMapping("/{id}/seats")
    public List<Map<String, Object>> getSeats(@PathVariable Long id) {
        List<Seat> seats =
                seatRepository.findByEventIdOrderByRowNumAscColNumAsc(id);

        List<Ticket> tickets =
                ticketRepository.findByEventId(id);

        Set<Long> bookedSeatIds = tickets.stream()
                .map(Ticket::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return seats.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("seatNumber", s.getSeatNumber());
            m.put("row", s.getRowNum());
            m.put("col", s.getColNum());
            m.put("status",
                    bookedSeatIds.contains(s.getId())
                            ? "booked"
                            : "available"
            );
            return m;
        }).collect(Collectors.toList());
    }
}
