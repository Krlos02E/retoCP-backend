package com.cinetest.service.impl;

import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.Movie;
import com.cinetest.model.entity.Showtime;
import com.cinetest.repository.MovieRepository;
import com.cinetest.repository.ShowtimeRepository;
import com.cinetest.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ShowtimeService}.
 * Validates showtime scheduling to prevent overlapping in the same room.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeServiceImpl implements ShowtimeService {

    private static final long CLEANUP_MARGIN_MINUTES = 15;

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Showtime> getAllShowtimes(UUID movieId, LocalDate date, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching showtimes with movieId={}, date={}, minPrice={}, maxPrice={}, pageable={}",
                movieId, date, minPrice, maxPrice, pageable);
        Page<Showtime> result = showtimeRepository.findAllWithFilters(movieId, date, minPrice, maxPrice, pageable);
        log.debug("Fetched {} showtimes", result.getTotalElements());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the showtime does not exist
     */
    @Override
    public Showtime getShowtimeById(UUID id) {
        log.debug("Fetching showtime by id={}", id);
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     * Checks for schedule overlap considering movie duration plus a cleanup margin.
     *
     * @throws ResourceNotFoundException if the movie does not exist
     * @throws BusinessRuleException     if the schedule overlaps with an existing showtime in the same room
     */
    @Override
    public Showtime createShowtime(ShowtimeDTO dto) {
        log.info("Creating showtime for movie={} in room='{}' at {}", dto.getMovieId(), dto.getRoom(), dto.getDateTime());
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + dto.getMovieId()));

        LocalDateTime newStart = dto.getDateTime();
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDuration() + CLEANUP_MARGIN_MINUTES);

        List<Showtime> existingShowtimes = showtimeRepository.findByRoom(dto.getRoom());

        for (Showtime existing : existingShowtimes) {
            LocalDateTime existingStart = existing.getDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(
                    existing.getMovie().getDuration() + CLEANUP_MARGIN_MINUTES);

            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                log.warn("Overlapping schedule in room='{}' between new showtime at {} and existing showtime id={}",
                        dto.getRoom(), newStart, existing.getId());
                throw new BusinessRuleException("Overlapping schedule in the same room");
            }
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(dto.getRoom())
                .dateTime(dto.getDateTime())
                .price(dto.getPrice())
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getAvailableSeats())
                .build();

        Showtime saved = showtimeRepository.save(showtime);
        log.info("Showtime created with id={} for movie={} in room='{}'", saved.getId(), dto.getMovieId(), saved.getRoom());
        return saved;
    }
}
