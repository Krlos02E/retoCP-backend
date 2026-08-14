package com.cinetest.service.impl;

import com.cinetest.dto.MovieDTO;
import com.cinetest.dto.MovieResponse;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.Movie;
import com.cinetest.model.entity.Showtime;
import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import com.cinetest.repository.MovieRepository;
import com.cinetest.repository.ShowtimeRepository;
import com.cinetest.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link MovieService}.
 * Manages movie catalog operations including soft deletion.
 */
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    /**
     * {@inheritDoc}
     * Parses genre and rating from string to enum values.
     */
    @Override
    public Page<Movie> getAllMovies(String genre, String rating, Pageable pageable) {
        Genre genreEnum = (genre != null && !genre.isBlank()) ? Genre.valueOf(genre.toUpperCase()) : null;
        Rating ratingEnum = (rating != null && !rating.isBlank()) ? Rating.valueOf(rating.toUpperCase()) : null;
        return movieRepository.findByGenreAndRating(genreEnum, ratingEnum, pageable);
    }

    /**
     * {@inheritDoc}
     * Includes upcoming showtimes for the movie.
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public MovieResponse getMovieById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        List<Showtime> upcoming = showtimeRepository.findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(id, LocalDateTime.now());

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .duration(movie.getDuration())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .upcomingShowtimes(upcoming)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Movie createMovie(MovieDTO dto) {
        Movie movie = Movie.builder()
                .title(dto.getTitle())
                .synopsis(dto.getSynopsis())
                .duration(dto.getDuration())
                .genre(dto.getGenre())
                .rating(dto.getRating())
                .build();
        return movieRepository.save(movie);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public Movie updateMovie(UUID id, MovieDTO dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setTitle(dto.getTitle());
        movie.setSynopsis(dto.getSynopsis());
        movie.setDuration(dto.getDuration());
        movie.setGenre(dto.getGenre());
        movie.setRating(dto.getRating());
        return movieRepository.save(movie);
    }

    /**
     * {@inheritDoc}
     * Performs a soft delete via the entity's {@code @SQLDelete} mapping.
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movieRepository.delete(movie);
    }
}
