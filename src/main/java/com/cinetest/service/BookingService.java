package com.cinetest.service;

import com.cinetest.dto.BookingDTO;
import com.cinetest.model.entity.Booking;

import java.util.UUID;

/**
 * Service interface for booking operations.
 */
public interface BookingService {

    /**
     * Creates a new booking.
     * Validates seat availability and updates showtime stock.
     *
     * @param dto the booking request
     * @return the persisted booking entity
     */
    Booking createBooking(BookingDTO dto);

    /**
     * Retrieves a booking by its ID.
     *
     * @param id the UUID of the booking
     * @return the booking entity
     */
    Booking getBookingById(UUID id);
}
