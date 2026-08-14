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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private static final long CLEANUP_MARGIN_MINUTES = 15;

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    @Override
    public Page<Showtime> getAllShowtimes(UUID movieId, LocalDate date, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return showtimeRepository.findAllWithFilters(movieId, date, minPrice, maxPrice, pageable);
    }

    @Override
    public Showtime getShowtimeById(UUID id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
    }

    @Override
    public Showtime createShowtime(ShowtimeDTO dto) {
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

        return showtimeRepository.save(showtime);
    }
}
