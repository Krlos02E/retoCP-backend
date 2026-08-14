package com.cinetest.service;

import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.model.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for showtime operations.
 */
public interface ShowtimeService {

    /**
     * Lists all showtimes with optional filters and pagination.
     *
     * @param movieId  optional filter by movie ID
     * @param date     optional filter by date
     * @param minPrice optional minimum price
     * @param maxPrice optional maximum price
     * @param pageable pagination configuration
     * @return a page of showtimes
     */
    Page<Showtime> getAllShowtimes(UUID movieId, LocalDate date, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Retrieves a showtime by its ID.
     *
     * @param id the UUID of the showtime
     * @return the showtime entity
     */
    Showtime getShowtimeById(UUID id);

    /**
     * Creates a new showtime.
     * Validates that the schedule does not overlap with existing showtimes in the same room.
     *
     * @param dto the showtime data
     * @return the persisted showtime entity
     */
    Showtime createShowtime(ShowtimeDTO dto);
}
