package com.livewave.ticket_api.repository;

import com.livewave.ticket_api.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdOrderByRowNumAscColNumAsc(Long eventId);
    Optional<Seat> findByEventIdAndSeatNumber(Long eventId, String seatNumber);

    @Query("""
        SELECT s FROM Seat s
        WHERE s.eventId IN :eventIds
        ORDER BY s.rowNum ASC, s.colNum ASC
    """)
    List<Seat> findByEventIdInOrderByRowNumAscColNumAsc(@Param("eventIds") List<Long> eventIds);
}