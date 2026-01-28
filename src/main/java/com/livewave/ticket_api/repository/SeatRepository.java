package com.livewave.ticket_api.repository;

import com.livewave.ticket_api.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdOrderByRowNumAscColNumAsc(Long eventId);
    Optional<Seat> findByEventIdAndSeatNumber(Long eventId, String seatNumber);
}
