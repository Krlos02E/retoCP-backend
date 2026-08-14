package com.cinetest.service;

import com.cinetest.dto.BookingDTO;
import com.cinetest.model.entity.Booking;

import java.util.UUID;

public interface BookingService {

    Booking createBooking(BookingDTO dto);

    Booking getBookingById(UUID id);
}
