package com.livewave.ticket_api.repository;

import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByEventIdAndSeatNumber(Long eventId, String seatNumber);

    Optional<Ticket> findByEventIdAndSeatId(Long eventId, Long seatId);

    List<Ticket> findByUser(User user);

    List<Ticket> findByEventId(Long eventId);

    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.event e
        WHERE e.id IN :eventIds
    """)
    List<Ticket> findByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.user
        JOIN FETCH t.event
        WHERE t.reminderSent = false
    """)
    List<Ticket> findTicketsForReminder();
}