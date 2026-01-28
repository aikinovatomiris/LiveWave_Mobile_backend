package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.config.JwtUtil;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> body) {
        return userService.register(body.get("name"), body.get("email"), body.get("password"));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        Optional<User> userOpt = userService.authenticate(body.get("email"), body.get("password"));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return Map.of("token", token, "role", user.getRole());
        }
        throw new RuntimeException("Invalid credentials");
    }


    @GetMapping("/profile")
    public User profile(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userService.getByEmail(email).orElseThrow();
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = userService.createPasswordResetToken(email);
        return Map.of("message", "Reset link sent (check console)", "token", token);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        userService.resetPassword(token, newPassword);
        return Map.of("message", "Password successfully reset");
    }

}
