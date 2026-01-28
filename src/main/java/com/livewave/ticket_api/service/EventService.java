package com.livewave.ticket_api.service;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Event> findAll() {
        return repo.findAll();
    }

    public Event save(Event event) {
        return repo.save(event);
    }

    @Transactional
    public Event createEventWithSeats(Event event, int rows, int cols) {
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
                // Формат номера: A1, A2... (ряд -> буква), для >26 рядов можно сменить на R{r}C{c}
                String rowLabel = String.valueOf((char) ('A' + (r - 1)));
                s.setSeatNumber(rowLabel + c);
                seats.add(s);
            }
        }
        seatRepository.saveAll(seats);
    }
}

