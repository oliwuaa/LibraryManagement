package com.example.library.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String value = String.valueOf(ex.getValue());
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        String message = "Invalid value '" + value + "' for parameter '" + name + "'. Expected type: " + expectedType;

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String[] enumValues = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .toArray(String[]::new);
            message += ". Allowed values: " + String.join(", ", enumValues);
        }

        logger.error(message);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParsingError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            Class<?> targetType = invalidFormatEx.getTargetType();
            Object invalidValue = invalidFormatEx.getValue();

            if (targetType.isEnum()) {
                String[] enumValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new);

                String message = "Invalid value '" + invalidValue + "' for parameter. Expected type: "
                        + targetType.getSimpleName() + ". Allowed values: " + String.join(", ", enumValues);

                logger.error(message);
                return ResponseEntity
                        .badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("error", message));
            }
        }

        String message = "Malformed JSON or invalid enum value. Please check the request body format.";
        logger.error(message);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        logger.error("Not Found: {}", ex.getMessage());
        return ResponseEntity
                .status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        logger.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        logger.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = "Validation error: " + ex.getMessage();
        logger.error(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}