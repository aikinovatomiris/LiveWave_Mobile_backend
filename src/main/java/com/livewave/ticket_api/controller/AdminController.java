package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.model.Event;
import com.livewave.ticket_api.repository.EventRepository;
import com.livewave.ticket_api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final EventRepository eventRepository;
    private final EventService eventService;

    @Autowired
    public AdminController(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Event event,
                                         @RequestParam(defaultValue = "5") int rows,
                                         @RequestParam(defaultValue = "10") int cols) {
        if (event.getTitle() == null || event.getDate() == null) {
            return ResponseEntity.badRequest().body("Ошибка: поля title и date обязательны.");
        }

        Event saved = eventService.createEventWithSeats(event, rows, cols);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Ошибка: событие не найдено.");
        }
        eventRepository.deleteById(id);
        return ResponseEntity.ok("Событие удалено успешно ✅");
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event updatedEvent) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.badRequest().body("Ошибка: событие с id " + id + " не найдено ❌");
        }

        Event event = optionalEvent.get();
        if (updatedEvent.getTitle() != null) event.setTitle(updatedEvent.getTitle());
        if (updatedEvent.getDescription() != null) event.setDescription(updatedEvent.getDescription());
        if (updatedEvent.getDate() != null) event.setDate(updatedEvent.getDate());
        if (updatedEvent.getPrice() != null) event.setPrice(updatedEvent.getPrice());
        if (updatedEvent.getCity() != null) event.setCity(updatedEvent.getCity());
        if (updatedEvent.getVenue() != null) event.setVenue(updatedEvent.getVenue());
        if (updatedEvent.getImageBanner() != null) event.setImageBanner(updatedEvent.getImageBanner());
        if (updatedEvent.getImageKey() != null) event.setImageKey(updatedEvent.getImageKey());
        if (updatedEvent.getLocation() != null) event.setLocation(updatedEvent.getLocation());

        Event saved = eventRepository.save(event);
        return ResponseEntity.ok(saved);
    }
}
