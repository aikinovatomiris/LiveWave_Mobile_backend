package com.livewave.ticket_api.controller;

import com.livewave.ticket_api.config.JwtUtil;
import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.exception.UnauthorizedException;
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

    /* REGISTER */

    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> body) {

        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || email == null || password == null) {
            throw new BadRequestException("Name, email and password are required");
        }

        return userService.register(name, email, password);
    }

    /* LOGIN */

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            throw new BadRequestException("Email and password are required");
        }

        Optional<User> userOpt = userService.authenticate(email, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            return Map.of(
                    "token", token,
                    "role", user.getRole()
            );
        }

        throw new UnauthorizedException("Invalid credentials");
    }

    /* PROFILE (JWT) */

    @GetMapping("/profile")
    public User profile(HttpServletRequest request) {

        String email = (String) request.getAttribute("email");

        if (email == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userService.getByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /* FORGOT PASSWORD */

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null) {
            throw new BadRequestException("Email is required");
        }

        String token = userService.createPasswordResetToken(email);

        return Map.of(
                "message", "Reset link sent (check console)",
                "token", token
        );
    }

    /* RESET PASSWORD */

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            throw new BadRequestException("Token and newPassword are required");
        }

        userService.resetPassword(token, newPassword);

        return Map.of("message", "Password successfully reset");
    }
}
