package com.cinetest.exception;

/**
 * Exception thrown when a business rule is violated.
 * Examples: overlapping showtime schedules, insufficient seats, duplicate username.
 * Maps to HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
