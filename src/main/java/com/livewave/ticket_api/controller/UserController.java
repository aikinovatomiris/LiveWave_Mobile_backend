package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.UserRepository;
import com.livewave.ticket_api.service.UserService;
import com.livewave.ticket_api.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return service.save(user);
    }

    @GetMapping("/profile")
    public Map<String, String> getProfile(@RequestParam String email) {
        return service.getByEmail(email)
                .map(user -> Map.of(
                        "name", user.getName(),
                        "email", user.getEmail()
                ))
                .orElseThrow(() ->
                        new com.livewave.ticket_api.exception.ResourceNotFoundException(
                                "User", "email", email
                        )
                );
    }

    @PatchMapping("/{id}/role")
    public User updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        User user = service.getById(id);

        if (!body.containsKey("role")) {
            throw new BadRequestException("Role is required");
        }

        user.setRole(body.get("role"));
        return service.saveDirect(user);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> updates
    ) {
        String jwt = token.replace("Bearer ", "").trim();
        String email = jwtUtil.extractEmail(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new com.livewave.ticket_api.exception.ResourceNotFoundException(
                                "User", "email", email
                        )
                );

        if (updates.containsKey("name") && updates.get("name") != null) {
            user.setName(updates.get("name"));
        }

        if (updates.containsKey("email") && updates.get("email") != null) {
            user.setEmail(updates.get("email"));
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully")
        );
    }
}
