package com.cinetest.exception;

import com.cinetest.dto.ApiResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that catches and formats all exceptions thrown by the application.
 * Ensures consistent {@link ApiResponse} structure for both success and error responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles cases where a requested entity does not exist in the database.
     *
     * @param ex the ResourceNotFoundException containing the error message
     * @return a 404 response with the exception message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    /**
     * Handles business rule violations such as overbooking or schedule conflicts.
     *
     * @param ex the BusinessRuleException containing the violation details
     * @return a 409 response with the exception message
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    /**
     * Handles validation errors from {@code @Valid} annotated request bodies.
     *
     * @param ex the MethodArgumentNotValidException containing field-level errors
     * @return a 400 response with a map of field names to error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(ApiResponseDTO.success(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
    }

    /**
     * Handles invalid request body formats, including malformed enum values.
     *
     * @param ex the HttpMessageNotReadableException containing the parse error
     * @return a 400 response with a descriptive error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidFormat(HttpMessageNotReadableException ex) {
        log.warn("Invalid request body: {}", ex.getMessage());
        String message = "Invalid request body";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            message = "Invalid value for field: " + ife.getPath().get(0).getFieldName();
        }
        return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * Handles authorization failures when a user lacks the required role for an endpoint.
     *
     * @param ex the AccessDeniedException
     * @return a 403 response with a generic access denied message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.error(HttpStatus.FORBIDDEN.value(), "Access denied. Insufficient permissions."));
    }

    /**
     * Fallback handler for any unhandled exception.
     *
     * @param ex the generic Exception
     * @return a 500 response with the exception message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }
}
