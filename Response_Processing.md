# Ticket API – Error Handling and Response Processing

## 1. Overview

This document describes the architecture and implementation of centralized error handling, standardized API responses, request logging, validation, and timeout configuration in the **Ticket API** project.

The goal of this approach is to ensure:

* Consistent API responses across all endpoints
* Centralized and maintainable error handling
* Clear and structured error messages for clients
* Improved observability through logging
* Reliable request processing with timeout control

---

## 2. Project Structure

```
com.livewave.ticket_api
├── config/
│   ├── JwtFilter.java
│   ├── JwtUtil.java
│   ├── PasswordConfig.java
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── RequestLoggingInterceptor.java
├── controller/
│   ├── AuthController.java
│   ├── EventController.java
│   ├── SeatController.java
│   ├── TicketController.java
│   └── UserController.java
├── dto/
│   ├── SeatDto.java
│   └── response/
│       ├── ApiResponse.java
│       └── ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── ApiTimeoutException.java
├── model/
│   ├── Event.java
│   ├── Seat.java
│   ├── Ticket.java
│   └── User.java
├── repository/
│   ├── EventRepository.java
│   ├── SeatRepository.java
│   ├── TicketRepository.java
│   └── UserRepository.java
├── service/
│   ├── EventService.java
│   ├── TicketService.java
│   └── UserService.java
└── TicketApiApplication.java
```

---

## 3. Centralized Exception Handling

All exceptions are handled in a single place using `@RestControllerAdvice`.

### 3.1 GlobalExceptionHandler

`GlobalExceptionHandler` intercepts exceptions thrown by controllers and services and converts them into structured HTTP responses.

Handled exception types include:

* `ResourceNotFoundException` → HTTP 404 (Not Found)
* `BadRequestException` → HTTP 400 (Bad Request)
* `ApiTimeoutException` → HTTP 408 (Request Timeout)
* `MethodArgumentNotValidException` → HTTP 400 (Validation errors)
* `HttpMessageNotReadableException` → HTTP 400 (Malformed JSON)
* `MethodArgumentTypeMismatchException` → HTTP 400 (Invalid parameter type)
* `Exception` → HTTP 500 (Internal Server Error)

---

## 4. Standardized API Responses

### 4.1 Successful Responses

All successful responses use the `ApiResponse<T>` wrapper.

Example:

```json
{
  "success": true,
  "message": "Events retrieved successfully",
  "data": [...],
  "timestamp": "2025-01-29T12:00:00"
}
```

### 4.2 Error Responses

All error responses use the `ErrorResponse` structure.

Example (404 – Not Found):

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: '999'",
  "path": "/events/999",
  "timestamp": "2025-01-29T12:00:00"
}
```

### 4.3 Validation Errors

Validation errors provide detailed field-level information.

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "validationErrors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ],
  "timestamp": "2025-01-29T12:00:00"
}
```

---

## 5. Timeout Configuration

Timeouts are configured to ensure reliable request handling and prevent long-running connections.

Configured values:

* Connection timeout: 5 seconds
* Read timeout: 10 seconds
* Server connection timeout: 20 seconds

These settings protect the API from hanging requests and external service delays.

---

## 6. Request Logging

Request logging is implemented using `RequestLoggingInterceptor` and configured in `WebConfig`.

Logged information includes:

* HTTP method
* Request URI
* Request processing time
* HTTP response status

This improves observability and simplifies debugging and performance monitoring.

---

## 7. Usage in Controllers

### 7.1 Example: TicketController

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<Ticket>> getTicket(@PathVariable Long id) {
    Ticket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

    return ResponseEntity.ok(ApiResponse.success(ticket));
}

@PostMapping
public ResponseEntity<ApiResponse<Ticket>> createTicket(@RequestBody Ticket ticket) {
    if (ticket.getEventId() == null) {
        throw new BadRequestException("Event ID is required");
    }

    Ticket saved = ticketRepository.save(ticket);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Ticket created", saved));
}
```

### 7.2 Example: UserController with Validation

```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody UserDto userDto) {
    User user = userService.register(userDto);
    return ResponseEntity.ok(ApiResponse.success("User registered", user));
}
```

Validation errors are automatically handled by `GlobalExceptionHandler`.

---

## 8. Application Configuration

### 8.1 application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## 9. Testing

### 9.1 Successful Request

```
GET /events
```

Response:

```json
{
  "success": true,
  "message": "Events retrieved successfully",
  "data": [...]
}
```

### 9.2 Resource Not Found

```
GET /events/999
```

Response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: '999'",
  "path": "/events/999"
}
```

### 9.3 Bad Request

```
POST /events
```

Invalid payload:

```json
{
  "city": "Astana"
}
```

Response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required"
}
```

---

## 10. Next Steps

1. Copy all files into their respective packages
2. Update `pom.xml` if required
3. Configure `application.properties`
4. Update remaining controllers to use `ApiResponse` and custom exceptions
5. Run the application using `mvn spring-boot:run`

---

## 11. Summary

The Ticket API now provides:

* Centralized exception handling
* Consistent API response format
* Detailed validation error reporting
* Configurable request timeouts
* Comprehensive request logging

This architecture improves maintainability, reliability, and client-side integration of the API.
