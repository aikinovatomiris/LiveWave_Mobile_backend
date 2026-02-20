package com.livewave.ticket_api.service;

import com.livewave.ticket_api.exception.BadRequestException;
import com.livewave.ticket_api.exception.ResourceNotFoundException;
import com.livewave.ticket_api.model.User;
import com.livewave.ticket_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // REGISTER
    @Test
    void register_shouldCreateUser() {
        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");

        User savedUser = new User();
        savedUser.setEmail("test@mail.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register("John", "test@mail.com", "1234");

        assertEquals("test@mail.com", result.getEmail());
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowIfEmailNull() {
        assertThrows(BadRequestException.class,
                () -> userService.register("John", null, "1234"));
    }

    // AUTHENTICATE
    @Test
    void authenticate_shouldReturnUserIfPasswordMatches() {
        User user = new User();
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded"))
                .thenReturn(true);

        Optional<User> result =
                userService.authenticate("test@mail.com", "1234");

        assertTrue(result.isPresent());
    }

    @Test
    void authenticate_shouldReturnEmptyIfWrongPassword() {
        User user = new User();
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        Optional<User> result =
                userService.authenticate("test@mail.com", "wrong");

        assertTrue(result.isEmpty());
    }

    // GET BY ID
    @Test
    void getById_shouldReturnUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getById(1L));
    }

    // GET ALL USERS
    @Test
    void getAllUsers_shouldReturnList() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    // SAVE
    @Test
    void save_shouldEncodePassword() {
        User user = new User();
        user.setPassword("1234");

        when(passwordEncoder.encode("1234"))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertEquals("encoded", result.getPassword());
        verify(passwordEncoder).encode("1234");
    }

    @Test
    void save_shouldThrowIfPasswordNull() {
        User user = new User();
        user.setPassword(null);

        assertThrows(BadRequestException.class,
                () -> userService.save(user));
    }

    // LOAD USER BY USERNAME
    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("encoded");
        user.setRole("USER");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        UserDetails details =
                userService.loadUserByUsername("test@mail.com");

        assertEquals("test@mail.com", details.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowIfNotFound() {
        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("test@mail.com"));
    }

    // PASSWORD RESET TOKEN
    @Test
    void createPasswordResetToken_shouldGenerateToken() {
        User user = new User();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        String token =
                userService.createPasswordResetToken("test@mail.com");

        assertNotNull(token);
        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());

        verify(userRepository).save(user);
    }

    // RESET PASSWORD
    @Test
    void resetPassword_shouldUpdatePassword() {
        User user = new User();
        user.setResetToken("token");
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByResetToken("token"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newpass"))
                .thenReturn("encoded");

        userService.resetPassword("token", "newpass");

        assertEquals("encoded", user.getPassword());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());

        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_shouldThrowIfExpired() {
        User user = new User();
        user.setResetToken("token");
        user.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByResetToken("token"))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> userService.resetPassword("token", "1234"));
    }

    // UPDATE FCM TOKEN
    @Test
    void updateFcmToken_shouldUpdateToken() {
        User user = new User();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        userService.updateFcmToken("test@mail.com", "fcm123");

        assertEquals("fcm123", user.getFcmToken());
        verify(userRepository).save(user);
    }

}