package com.livewave.ticket_api.service;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository repo;
    private final SeatRepository seatRepository;

    public EventService(EventRepository repo, SeatRepository seatRepository) {
        this.repo = repo;
        this.seatRepository = seatRepository;
    }

    public List<Event> findAll(String city) {
        if (city != null && !city.isBlank()) {
            List<Event> events = repo.findByCityIgnoreCase(city);
            if (events.isEmpty()) {
                throw new ResourceNotFoundException("Events", "city", city);
            }
            return events;
        }
        return repo.findAll();
    }

    public Event findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event", "id", id)
                );
    }

    @Transactional
    public Event createEventWithSeats(
            Event event,
            int rows,
            int cols,
            String dateString
    ) {
        if (rows <= 0 || cols <= 0) {
            throw new BadRequestException("Rows and columns must be greater than zero");
        }

        if (dateString != null) {
            try {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                event.setDate(LocalDateTime.parse(dateString, formatter));
            } catch (Exception ex) {
                throw new BadRequestException(
                        "Invalid date format. Use 'yyyy-MM-dd HH:mm'"
                );
            }
        }

        Event saved = repo.save(event);
        generateSeatsForEvent(saved.getId(), rows, cols);
        return saved;
    }

    @Transactional
    public void generateSeatsForEvent(Long eventId, int rows, int cols) {
        List<Seat> seats = new ArrayList<>(rows * cols);

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Seat s = new Seat();
                s.setEventId(eventId);
                s.setRowNum(r);
                s.setColNum(c);

                String rowLabel = String.valueOf((char) ('A' + (r - 1)));
                s.setSeatNumber(rowLabel + c);

                seats.add(s);
            }
        }

        seatRepository.saveAll(seats);
    }


    @Transactional
    public Event createEventWithSeats(Event event, int rows, int cols) {
        return createEventWithSeats(event, rows, cols, null);
    }

}
