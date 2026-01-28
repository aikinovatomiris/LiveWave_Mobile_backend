package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.dto.SeatDto;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.SeatRepository;
import com.livewave.ticket_api.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public SeatController(SeatRepository seatRepository, TicketRepository ticketRepository) {
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/{eventId}")
    public List<SeatDto> getSeatsByEvent(@PathVariable Long eventId) {
        List<Seat> seats = seatRepository.findByEventIdOrderByRowNumAscColNumAsc(eventId);

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        Set<Long> bookedSeatIds = tickets.stream()
                .map(Ticket::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SeatDto> dto = new ArrayList<>(seats.size());
        for (Seat s : seats) {
            boolean booked = s.getId() != null && bookedSeatIds.contains(s.getId());
            SeatDto seatDto = new SeatDto(
                    s.getId(),
                    s.getEventId(),
                    s.getSeatNumber(),
                    s.getRowNum(),
                    s.getColNum(),
                    booked
            );
            dto.add(seatDto);
        }
        return dto;
    }
}



