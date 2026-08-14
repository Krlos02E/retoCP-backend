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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementation of {@link BookingService}.
 * Handles seat reservation logic with pessimistic locking to prevent overbooking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    /**
     * {@inheritDoc}
     * Uses pessimistic write locking on the showtime to avoid race conditions.
     *
     * @throws ResourceNotFoundException if the showtime does not exist
     * @throws BusinessRuleException     if there are not enough available seats
     */
    @Override
    @Transactional
    public Booking createBooking(BookingDTO dto) {
        log.info("Creating booking for customer='{}' showtime={} seats={}",
                dto.getCustomerName(), dto.getShowtimeId(), dto.getSeatsBooked());
        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + dto.getShowtimeId()));

        if (showtime.getAvailableSeats() < dto.getSeatsBooked()) {
            log.warn("Booking rejected: not enough seats. Showtime={}, available={}, requested={}",
                    dto.getShowtimeId(), showtime.getAvailableSeats(), dto.getSeatsBooked());
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

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with id={} for customer='{}' total={}",
                saved.getId(), saved.getCustomerName(), saved.getTotalPrice());
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the booking does not exist
     */
    @Override
    public Booking getBookingById(UUID id) {
        log.debug("Fetching booking by id={}", id);
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }
}
