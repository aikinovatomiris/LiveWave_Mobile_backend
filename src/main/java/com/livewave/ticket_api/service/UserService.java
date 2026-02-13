package com.livewave.ticket_api.service;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String name, String email, String password) {
        if (email == null || password == null) {
            throw new BadRequestException("Email and password are required");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()
                && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }

        return Optional.empty();
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", id)
                );
    }

    public List<User> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public User save(User user) {
        if (user.getPassword() == null) {
            throw new BadRequestException("Password is required");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User saveDirect(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email)
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();
    }

    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email)
                );

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // пока просто лог, без email
        System.out.println(
                "Reset link: http://localhost:8080/reset-password?token=" + token
        );

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() ->
                        new BadRequestException("Invalid reset token")
                );

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}
