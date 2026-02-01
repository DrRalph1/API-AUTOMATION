package com.usg.apiAutomation.exceptions;

import com.usg.apiAutomation.dtos.ApiResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.usg.apiAutomation.controllers")
public class GlobalExceptionHandler {

    // Custom Exceptions
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    // Handle validation errors - 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        log.warn("Validation failed: {}", ex.getMessage());

        // Log individual field errors for debugging
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                log.warn("Field validation error - Field: {}, Value: {}, Error: {}",
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage())
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, "Validation failed", errors));
    }

    // Handle resource not found - 404 Not Found
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ApiResponseDTO<Void>> handleEntityNotFound(RuntimeException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        // Log stack trace for debugging in non-production environments
        log.debug("Resource not found stack trace:", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.notFound(ex.getMessage()));
    }

    // Handle data integrity violations - 409 Conflict
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        log.warn("Data integrity violation: {}", ex.getMessage());

        // Log root cause for better debugging
        if (ex.getCause() != null) {
            log.warn("Data integrity violation root cause: {}", ex.getCause().getMessage());
        }

        // Log constraint name if available
        if (ex.getMessage() != null) {
            log.warn("Full violation message: {}", ex.getMessage());
        }

        String message = "A resource with the provided data already exists";
        if (ex.getMessage() != null &&
                (ex.getMessage().contains("unique constraint") ||
                        ex.getMessage().contains("duplicate key"))) {
            message = "A resource with these values already exists";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error(409, message));
    }

    // Handle conflict exceptions - 409 Conflict
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConflictException(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());

        // Log additional context for debugging
        log.debug("Conflict exception stack trace:", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error(409, ex.getMessage()));
    }

    // Handle business rule violations - 422 Unprocessable Entity
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusinessRuleException(
            BusinessRuleException ex) {

        log.warn("Business rule violation: {}", ex.getMessage());

        // Log business rule context
        log.debug("Business rule exception details:", ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponseDTO.error(422, ex.getMessage()));
    }

    // Handle illegal arguments - 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());

        // Log stack trace for debugging illegal argument issues
        log.debug("Illegal argument stack trace:", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.badRequest(ex.getMessage()));
    }

    // Handle malformed requests - 400 Bad Request
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleBadRequestExceptions(Exception ex) {

        log.warn("Bad request: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        // Log the full exception for debugging
        if (ex.getCause() != null) {
            log.warn("Root cause: {}", ex.getCause().getMessage());
        }

        String message = "Malformed or invalid request";
        Map<String, String> details = new HashMap<>();

        if (ex instanceof MethodArgumentTypeMismatchException) {
            message = "Invalid parameter type";
            MethodArgumentTypeMismatchException mismatchEx = (MethodArgumentTypeMismatchException) ex;
            details.put("parameter", mismatchEx.getName());
            details.put("expectedType", mismatchEx.getRequiredType() != null ?
                    mismatchEx.getRequiredType().getSimpleName() : "unknown");
            details.put("actualValue", String.valueOf(mismatchEx.getValue()));

            // Log specific mismatch details
            log.warn("Type mismatch - Parameter: {}, Expected: {}, Actual: {}",
                    mismatchEx.getName(),
                    mismatchEx.getRequiredType() != null ? mismatchEx.getRequiredType().getSimpleName() : "unknown",
                    mismatchEx.getValue());

        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "Invalid request body";
            if (ex.getCause() != null) {
                details.put("cause", ex.getCause().getMessage());
                // Log parsing error details
                log.warn("Message not readable - Cause: {}", ex.getCause().getMessage());
            }
            details.put("error", "Failed to parse request body");

            // Log HTTP message reading error
            log.debug("HTTP message not readable stack trace:", ex);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, message, details));
    }

    // Handle access denied - 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDeniedException(
            AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        // Log security context for debugging (be careful with sensitive info)
        log.debug("Access denied exception details:", ex);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.error(403, "Access denied"));
    }

    // Handle empty results - 404 Not Found
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleEmptyResult(
            EmptyResultDataAccessException ex) {

        log.warn("Empty result: {}", ex.getMessage());

        // Log query context if available
        log.debug("Empty result data access details:", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.notFound("Resource not found"));
    }

    // Handle all other exceptions - 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception - Type: {}, Message: {}",
                ex.getClass().getSimpleName(), ex.getMessage());

        // Log full stack trace for unhandled exceptions
        log.error("Unhandled exception stack trace:", ex);

        // Log root cause if available
        if (ex.getCause() != null) {
            log.error("Root cause: {}", ex.getCause().getMessage());
            log.error("Root cause stack trace:", ex.getCause());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.internalError("An unexpected error occurred"));
    }

}