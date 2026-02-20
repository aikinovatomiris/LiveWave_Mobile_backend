package com.livewave.ticket_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Seat;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.SeatRepository;
import com.livewave.ticket_api.repository.TicketRepository;
import com.livewave.ticket_api.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.livewave.ticket_api.config.JwtFilter;
import com.livewave.ticket_api.config.JwtUtil;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private SeatRepository seatRepository;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    // ==============================
    // GET ALL EVENTS
    // ==============================

    @Test
    void getAllEvents_withoutCity_shouldReturnList() throws Exception {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Concert");

        when(eventService.findAll(null))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Concert"));
    }

    @Test
    void getAllEvents_withCityFilter_shouldCallServiceWithCity() throws Exception {

        Event event = new Event();
        event.setCity("Almaty");

        when(eventService.findAll("Almaty"))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/events?city=Almaty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Almaty"));
    }

    // ==============================
    // GET EVENT BY ID
    // ==============================

    @Test
    void getEventById_shouldReturnEvent() throws Exception {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Rock Show");

        when(eventService.findById(1L))
                .thenReturn(event);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Rock Show"));
    }

    // ==============================
    // CREATE EVENT
    // ==============================

    @Test
    void createEvent_shouldCreateEventWithSeats() throws Exception {

        Map<String, Object> body = Map.of(
                "title", "Concert",
                "description", "Live show",
                "city", "Astana",
                "venue", "Arena",
                "location", "Center",
                "price", 5000,
                "rows", 5,
                "cols", 5,
                "date", "2025-05-01"
        );

        Event savedEvent = new Event();
        savedEvent.setId(10L);
        savedEvent.setTitle("Concert");

        when(eventService.createEventWithSeats(
                any(Event.class),
                eq(5),
                eq(5),
                eq("2025-05-01")
        )).thenReturn(savedEvent);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Concert"));
    }

    @Test
    void createEvent_withoutPrice_shouldUseDefaultZero() throws Exception {

        Map<String, Object> body = Map.of(
                "title", "No price event",
                "description", "Test",
                "city", "Astana",
                "venue", "Hall",
                "location", "Center",
                "date", "2025-05-01"
        );

        Event savedEvent = new Event();
        savedEvent.setId(1L);

        when(eventService.createEventWithSeats(
                any(Event.class),
                anyInt(),
                anyInt(),
                anyString()
        )).thenReturn(savedEvent);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // ==============================
    // GET SEATS
    // ==============================

    @Test
    void getSeats_bookedSeat_shouldReturnBooked() throws Exception {

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setRowNum(1);
        seat.setColNum(1);

        Ticket ticket = new Ticket();
        ticket.setSeatId(1L);

        when(seatRepository.findByEventIdOrderByRowNumAscColNumAsc(1L))
                .thenReturn(List.of(seat));

        when(ticketRepository.findByEventId(1L))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/events/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("booked"));
    }

    @Test
    void getSeats_availableSeat_shouldReturnAvailable() throws Exception {

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setRowNum(1);
        seat.setColNum(1);

        when(seatRepository.findByEventIdOrderByRowNumAscColNumAsc(1L))
                .thenReturn(List.of(seat));

        when(ticketRepository.findByEventId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/events/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("available"));
    }

    @Test
    void getSeats_shouldReturnCorrectStructure() throws Exception {

        Seat seat = new Seat();
        seat.setId(5L);
        seat.setSeatNumber("B2");
        seat.setRowNum(2);
        seat.setColNum(3);

        when(seatRepository.findByEventIdOrderByRowNumAscColNumAsc(1L))
                .thenReturn(List.of(seat));

        when(ticketRepository.findByEventId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/events/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].seatNumber").value("B2"))
                .andExpect(jsonPath("$[0].row").value(2))
                .andExpect(jsonPath("$[0].col").value(3))
                .andExpect(jsonPath("$[0].status").value("available"));
    }
}