package com.cinetest.service;

import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.model.entity.Movie;
import com.cinetest.model.entity.Showtime;
import com.cinetest.repository.MovieRepository;
import com.cinetest.repository.ShowtimeRepository;
import com.cinetest.service.impl.ShowtimeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ShowtimeServiceImpl showtimeService;

    private Movie movie;
    private Showtime existingShowtime;
    private ShowtimeDTO newShowtimeDTO;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .duration(120)
                .build();

        existingShowtime = Showtime.builder()
                .id(UUID.randomUUID())
                .movie(movie)
                .room("Room 1")
                .dateTime(LocalDateTime.of(2026, 8, 15, 14, 0))
                .price(BigDecimal.valueOf(30.00))
                .totalSeats(100)
                .availableSeats(100)
                .build();

        newShowtimeDTO = ShowtimeDTO.builder()
                .movieId(movie.getId())
                .room("Room 1")
                .dateTime(LocalDateTime.of(2026, 8, 15, 15, 0)) // Overlaps: existing ends at 14:00+120+15=16:15
                .price(BigDecimal.valueOf(30.00))
                .totalSeats(100)
                .availableSeats(100)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenShowtimeOverlapsInSameRoom() {
        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findByRoom("Room 1")).thenReturn(List.of(existingShowtime));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            showtimeService.createShowtime(newShowtimeDTO);
        });

        assertEquals("Overlapping schedule in the same room", exception.getMessage());
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void shouldCreateShowtimeWhenNoOverlap() {
        newShowtimeDTO.setDateTime(LocalDateTime.of(2026, 8, 15, 18, 0)); // Starts at 18:00, existing ends at 16:15

        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findByRoom("Room 1")).thenReturn(List.of(existingShowtime));
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

        Showtime result = showtimeService.createShowtime(newShowtimeDTO);

        assertNotNull(result);
        assertEquals("Room 1", result.getRoom());
        assertEquals(movie, result.getMovie());
        verify(showtimeRepository).save(any(Showtime.class));
    }

    @Test
    void shouldCreateShowtimeWhenDifferentRoom() {
        newShowtimeDTO.setRoom("Room 2");

        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        when(showtimeRepository.findByRoom("Room 2")).thenReturn(List.of());
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

        Showtime result = showtimeService.createShowtime(newShowtimeDTO);

        assertNotNull(result);
        assertEquals("Room 2", result.getRoom());
        verify(showtimeRepository).save(any(Showtime.class));
    }
}
