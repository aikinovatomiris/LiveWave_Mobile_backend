package com.livewave.ticket_api.service;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventRepository repo;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // findAll
    @Test
    void findAll_withoutCity_shouldReturnAllEvents() {
        Event event = new Event();
        event.setId(1L);

        when(repo.findAll()).thenReturn(List.of(event));

        List<Event> result = eventService.findAll(null);

        assertEquals(1, result.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void findAll_withCity_shouldReturnEvents() {
        Event event = new Event();
        event.setCity("Almaty");

        when(repo.findByCityIgnoreCase("Almaty"))
                .thenReturn(List.of(event));

        List<Event> result = eventService.findAll("Almaty");

        assertEquals(1, result.size());
        verify(repo).findByCityIgnoreCase("Almaty");
    }

    @Test
    void findAll_withCityNotFound_shouldThrowException() {
        when(repo.findByCityIgnoreCase("Astana"))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.findAll("Astana"));
    }

    // findById
    @Test
    void findById_shouldReturnEvent() {
        Event event = new Event();
        event.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_notFound_shouldThrowException() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.findById(1L));
    }

    // createEventWithSeats
    @Test
    void createEventWithSeats_validData_shouldSaveEventAndGenerateSeats() {

        Event event = new Event();
        event.setTitle("Concert");

        when(repo.save(any(Event.class)))
                .thenAnswer(invocation -> {
                    Event saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        Event saved = eventService.createEventWithSeats(
                event,
                2,
                2,
                "2026-05-10 18:30"
        );

        assertNotNull(saved.getId());
        assertEquals("Concert", saved.getTitle());
        assertEquals(
                LocalDateTime.of(2026, 5, 10, 18, 30),
                saved.getDate()
        );

        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createEventWithSeats_invalidRows_shouldThrowException() {

        Event event = new Event();

        assertThrows(BadRequestException.class,
                () -> eventService.createEventWithSeats(event, 0, 5));
    }

    @Test
    void createEventWithSeats_invalidDate_shouldThrowException() {

        Event event = new Event();

        assertThrows(BadRequestException.class,
                () -> eventService.createEventWithSeats(
                        event,
                        5,
                        5,
                        "wrong-date-format"
                ));
    }

    // generateSeatsForEvent
    @Test
    void generateSeatsForEvent_shouldCreateCorrectNumberOfSeats() {

        eventService.generateSeatsForEvent(1L, 3, 4);

        ArgumentCaptor<List<Seat>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(seatRepository).saveAll(captor.capture());

        List<Seat> savedSeats = captor.getValue();

        assertEquals(12, savedSeats.size()); // 3 * 4
        assertEquals("A1", savedSeats.get(0).getSeatNumber());
        assertEquals("C4", savedSeats.get(11).getSeatNumber());
    }
}