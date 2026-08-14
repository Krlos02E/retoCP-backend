package com.cinetest.service.impl;

import com.cinetest.dto.BookingDTO;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.Booking;
import com.cinetest.model.entity.Showtime;
import com.cinetest.repository.BookingRepository;
import com.cinetest.repository.ShowtimeRepository;
import com.cinetest.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingDTO dto) {
        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + dto.getShowtimeId()));

        if (showtime.getAvailableSeats() < dto.getSeatsBooked()) {
            throw new BusinessRuleException("Not enough seats available for this showtime");
        }

        showtime.setAvailableSeats(showtime.getAvailableSeats() - dto.getSeatsBooked());
        showtimeRepository.save(showtime);

        BigDecimal totalPrice = showtime.getPrice().multiply(BigDecimal.valueOf(dto.getSeatsBooked()));

        Booking booking = Booking.builder()
                .showtime(showtime)
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .seatsBooked(dto.getSeatsBooked())
                .totalPrice(totalPrice)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }
}
