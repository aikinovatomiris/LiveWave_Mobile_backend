package com.livewave.ticket_api.service;

import com.livewave.ticket_api.model.Ticket;
import com.livewave.ticket_api.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository repo;

    public TicketService(TicketRepository repo) {
        this.repo = repo;
    }

    public List<Ticket> findAll() {
        return repo.findAll();
    }

    public Ticket save(Ticket ticket) {
        return repo.save(ticket);
    }
}
