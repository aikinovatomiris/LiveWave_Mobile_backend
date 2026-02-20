package com.livewave.ticket_api.service;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===============================
    // findAll
    // ===============================

    @Test
    void findAll_shouldReturnTickets() {

        Ticket ticket = new Ticket();
        ticket.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(ticketRepository).findAll();
    }

    // ===============================
    // save basic
    // ===============================

    @Test
    void save_shouldReturnSavedTicket() {

        Ticket ticket = new Ticket();
        ticket.setId(1L);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.save(ticket);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ticketRepository, atLeastOnce()).save(ticket);
    }

    // ===============================
    // event more than 24 hours
    // ===============================

    @Test
    void save_eventMoreThan24Hours_shouldNotChangeReminderFlag() {

        Event event = new Event();
        event.setDate(LocalDateTime.now().plusDays(2));

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setReminderSent(false);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.save(ticket);

        assertFalse(ticket.isReminderSent());
    }

    // ===============================
    // event within 24 hours
    // ===============================

    @Test
    void save_eventWithin24Hours_shouldSetReminderSentTrue() {

        Event event = new Event();
        event.setDate(LocalDateTime.now().plusHours(5));
        event.setTitle("Concert");

        User user = new User();
        user.setFcmToken("any-token");

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setUser(user);
        ticket.setReminderSent(false);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.save(ticket);

        assertTrue(ticket.isReminderSent());
        verify(ticketRepository, atLeastOnce()).save(ticket);
    }

    // ===============================
    // reminder already sent
    // ===============================

    @Test
    void save_reminderAlreadySent_shouldStayTrue() {

        Event event = new Event();
        event.setDate(LocalDateTime.now().plusHours(3));

        User user = new User();
        user.setFcmToken("token");

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setUser(user);
        ticket.setReminderSent(true);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.save(ticket);

        assertTrue(ticket.isReminderSent());
    }

    // ===============================
    // no user
    // ===============================

    @Test
    void save_noUser_shouldNotChangeReminderFlag() {

        Event event = new Event();
        event.setDate(LocalDateTime.now().plusHours(5));

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setUser(null);
        ticket.setReminderSent(false);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.save(ticket);

        assertFalse(ticket.isReminderSent());
    }

    // ===============================
    // no event
    // ===============================

    @Test
    void save_noEvent_shouldNotFail() {

        Ticket ticket = new Ticket();
        ticket.setEvent(null);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.save(ticket);

        assertNotNull(result);
    }
}