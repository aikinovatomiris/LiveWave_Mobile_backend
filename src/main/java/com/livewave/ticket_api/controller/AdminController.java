package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final EventRepository eventRepository;
    private final EventService eventService;

    @Autowired
    public AdminController(EventRepository eventRepository,
                           EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public Event createEvent(
            @RequestBody Event event,
            @RequestParam(defaultValue = "5") int rows,
            @RequestParam(defaultValue = "10") int cols
    ) {

        if (event.getTitle() == null || event.getDate() == null) {
            throw new BadRequestException(
                    "Поля title и date обязательны."
            );
        }

        return eventService.createEventWithSeats(event, rows, cols);
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@PathVariable Long id) {

        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Event", "id", id
            );
        }

        eventRepository.deleteById(id);
        return "Событие удалено успешно";
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestBody Event updatedEvent
    ) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event", "id", id
                        )
                );

        if (updatedEvent.getTitle() != null)
            event.setTitle(updatedEvent.getTitle());

        if (updatedEvent.getDescription() != null)
            event.setDescription(updatedEvent.getDescription());

        if (updatedEvent.getDate() != null)
            event.setDate(updatedEvent.getDate());

        if (updatedEvent.getPrice() != null)
            event.setPrice(updatedEvent.getPrice());

        if (updatedEvent.getCity() != null)
            event.setCity(updatedEvent.getCity());

        if (updatedEvent.getVenue() != null)
            event.setVenue(updatedEvent.getVenue());

        if (updatedEvent.getImageBanner() != null)
            event.setImageBanner(updatedEvent.getImageBanner());

        if (updatedEvent.getImageKey() != null)
            event.setImageKey(updatedEvent.getImageKey());

        if (updatedEvent.getLocation() != null)
            event.setLocation(updatedEvent.getLocation());

        return eventRepository.save(event);
    }
}
