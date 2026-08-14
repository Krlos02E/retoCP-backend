package com.cinetest.exception;

/**
 * Exception thrown when a requested resource (movie, showtime, booking, user) does not exist.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
