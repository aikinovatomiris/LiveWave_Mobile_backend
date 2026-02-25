package com.livewave.ticket_api.graphql;

import com.livewave.ticket_api.dto.SeatDto;
import com.livewave.ticket_api.exception.SeatConflictException;
import com.livewave.ticket_api.exception.UnauthorizedException;
import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.repository.SeatRepository;
import com.livewave.ticket_api.repository.TicketRepository;
import com.livewave.ticket_api.repository.UserRepository;
import com.livewave.ticket_api.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TicketGraphQLController {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    public TicketGraphQLController(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketService ticketService
    ) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    // -------------------- Queries --------------------

    @QueryMapping
    public List<Event> events(
            @Argument String city,
            @Argument Integer limit,
            @Argument Integer offset
    ) {
        List<Event> list = (city != null && !city.isBlank())
                ? eventRepository.findByCityIgnoreCase(city)
                : eventRepository.findAll();

        int off = (offset == null) ? 0 : Math.max(0, offset);
        int lim = (limit == null) ? 20 : Math.max(1, Math.min(200, limit));

        if (off >= list.size()) return List.of();
        int to = Math.min(list.size(), off + lim);
        return list.subList(off, to);
    }

    @QueryMapping
    public Event event(@Argument Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<SeatDto> seats(@Argument Long eventId) {
        List<Seat> seats = seatRepository.findByEventIdOrderByRowNumAscColNumAsc(eventId);

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        Set<Long> bookedSeatIds = tickets.stream()
                .map(Ticket::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SeatDto> dto = new ArrayList<>(seats.size());
        for (Seat s : seats) {
            boolean booked = s.getId() != null && bookedSeatIds.contains(s.getId());
            dto.add(new SeatDto(
                    s.getId(),
                    s.getEventId(),
                    s.getSeatNumber(),
                    s.getRowNum(),
                    s.getColNum(),
                    booked
            ));
        }
        return dto;
    }

    @QueryMapping
    public List<TicketView> myTickets() {
        HttpServletRequest req = getRequest();
        String email = (req != null) ? (String) req.getAttribute("email") : null;

        if (email == null) throw new UnauthorizedException("Unauthorized");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<Ticket> tickets = ticketRepository.findByUser(user);

        return tickets.stream()
                .map(t -> new TicketView(
                        t.getId(),
                        t.getSeatNumber(),
                        t.getSeatId(),
                        t.getPurchaseDate(),
                        (t.getEvent() != null) ? t.getEvent().getId() : null
                ))
                .collect(Collectors.toList());
    }

    // -------------------- Mutations --------------------

    @MutationMapping
    public BuyResult buyTickets(
            @Argument Long eventId,
            @Argument List<String> seatNumbers
    ) {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            return new BuyResult("Список мест пуст", 0);
        }

        HttpServletRequest req = getRequest();
        String email = (req != null) ? (String) req.getAttribute("email") : null;
        if (email == null) throw new UnauthorizedException("Unauthorized");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<String> failedSeats = new ArrayList<>();
        int created = 0;

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
            ticket.setUser(user);
            ticket.setSeatNumber(seatNumber);
            ticket.setSeatId(seat.getId());

            ticketService.save(ticket);
            created++;
        }

        if (!failedSeats.isEmpty()) {
            throw new SeatConflictException(failedSeats);
        }

        return new BuyResult("Покупка успешна!", created);
    }

    // -------------------- Field resolvers (optimized) --------------------

    @BatchMapping(typeName = "TicketView", field = "event")
    public Map<TicketView, Event> ticketEvent(List<TicketView> tickets) {
        Set<Long> ids = tickets.stream()
                .map(TicketView::getEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Event> eventsById = eventRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        Map<TicketView, Event> result = new LinkedHashMap<>();
        for (TicketView t : tickets) {
            result.put(t, eventsById.get(t.getEventId()));
        }
        return result;
    }

    @BatchMapping(typeName = "Event", field = "seats")
    public Map<Event, List<SeatDto>> eventSeats(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .toList();

        if (eventIds.isEmpty()) {
            return events.stream().collect(Collectors.toMap(e -> e, e -> List.of()));
        }

        List<Seat> seats = seatRepository.findByEventIdInOrderByRowNumAscColNumAsc(eventIds);

        List<Ticket> tickets = ticketRepository.findByEventIds(eventIds);

        Map<Long, Set<Long>> bookedByEvent = new HashMap<>();
        for (Ticket t : tickets) {
            if (t.getEvent() == null || t.getEvent().getId() == null || t.getSeatId() == null) continue;
            bookedByEvent.computeIfAbsent(t.getEvent().getId(), k -> new HashSet<>()).add(t.getSeatId());
        }

        Map<Long, List<Seat>> seatsByEvent = seats.stream()
                .collect(Collectors.groupingBy(Seat::getEventId, LinkedHashMap::new, Collectors.toList()));

        Map<Event, List<SeatDto>> out = new LinkedHashMap<>();
        for (Event e : events) {
            List<Seat> list = seatsByEvent.getOrDefault(e.getId(), List.of());
            Set<Long> booked = bookedByEvent.getOrDefault(e.getId(), Set.of());

            List<SeatDto> dto = new ArrayList<>(list.size());
            for (Seat s : list) {
                dto.add(new SeatDto(
                        s.getId(),
                        s.getEventId(),
                        s.getSeatNumber(),
                        s.getRowNum(),
                        s.getColNum(),
                        s.getId() != null && booked.contains(s.getId())
                ));
            }
            out.put(e, dto);
        }
        return out;
    }
}