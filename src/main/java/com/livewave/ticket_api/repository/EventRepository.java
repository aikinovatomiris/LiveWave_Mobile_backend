package com.livewave.ticket_api.repository;

import com.livewave.ticket_api.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCityIgnoreCase(String city);
}
