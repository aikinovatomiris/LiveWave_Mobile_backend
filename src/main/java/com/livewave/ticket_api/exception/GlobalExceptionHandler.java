package com.livewave.ticket_api.exception;

import com.livewave.ticket_api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* =========================
       404 — NOT FOUND
     ========================= */

    @ExceptionHandler({
            ResourceNotFoundException.class,
            NotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /* =========================
       400 — BAD REQUEST
     ========================= */

    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /* =========================
       401 — UNAUTHORIZED
     ========================= */

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /* =========================
       409 — CONFLICT (Seats)
     ========================= */

    @ExceptionHandler(SeatConflictException.class)
    public ResponseEntity<ErrorResponse> handleSeatConflict(
            SeatConflictException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /* =========================
       408 — REQUEST TIMEOUT
     ========================= */

    @ExceptionHandler(ApiTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(
            ApiTimeoutException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.REQUEST_TIMEOUT.value(),
                "Request Timeout",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    /* =========================
       VALIDATION ERRORS
     ========================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.ValidationError> validationErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new ErrorResponse.ValidationError(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .collect(Collectors.toList());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid input data",
                request.getRequestURI()
        );
        error.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /* =========================
       JSON PARSE ERROR
     ========================= */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON",
                "Invalid JSON format or data type mismatch",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /* =========================
       TYPE MISMATCH
     ========================= */

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType().getSimpleName()
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Type Mismatch",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /* =========================
       500 — INTERNAL SERVER ERROR
     ========================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );

        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
