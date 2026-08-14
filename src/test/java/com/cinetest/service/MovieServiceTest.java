package com.cinetest.service;

import com.cinetest.model.entity.Movie;
import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import com.cinetest.repository.MovieRepository;
import com.cinetest.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Pageable pageable;
    private Movie actionMovie;
    private Movie dramaMovie;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        actionMovie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Action Movie")
                .genre(Genre.ACTION)
                .rating(Rating.PG_13)
                .build();

        dramaMovie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Drama Movie")
                .genre(Genre.DRAMA)
                .rating(Rating.R)
                .build();
    }

    @Test
    void shouldReturnMoviesFilteredByGenre() {
        Page<Movie> expectedPage = new PageImpl<>(List.of(actionMovie));
        when(movieRepository.findByGenreAndRating(Genre.ACTION, null, pageable)).thenReturn(expectedPage);

        Page<Movie> result = movieService.getAllMovies("ACTION", null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(Genre.ACTION, result.getContent().get(0).getGenre());
        verify(movieRepository).findByGenreAndRating(Genre.ACTION, null, pageable);
    }

    @Test
    void shouldReturnMoviesFilteredByRating() {
        Page<Movie> expectedPage = new PageImpl<>(List.of(dramaMovie));
        when(movieRepository.findByGenreAndRating(null, Rating.R, pageable)).thenReturn(expectedPage);

        Page<Movie> result = movieService.getAllMovies(null, "R", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(Rating.R, result.getContent().get(0).getRating());
        verify(movieRepository).findByGenreAndRating(null, Rating.R, pageable);
    }

    @Test
    void shouldReturnAllMoviesWhenNoFilters() {
        Page<Movie> expectedPage = new PageImpl<>(List.of(actionMovie, dramaMovie));
        when(movieRepository.findByGenreAndRating(null, null, pageable)).thenReturn(expectedPage);

        Page<Movie> result = movieService.getAllMovies(null, null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(movieRepository).findByGenreAndRating(null, null, pageable);
    }
}
