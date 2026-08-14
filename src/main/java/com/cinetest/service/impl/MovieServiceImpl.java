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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    /**
     * {@inheritDoc}
     * Parses genre and rating from string to enum values.
     */
    @Override
    public Page<Movie> getAllMovies(String genre, String rating, Pageable pageable) {
        log.debug("Fetching movies with genre={}, rating={}, pageable={}", genre, rating, pageable);
        Genre genreEnum = (genre != null && !genre.isBlank()) ? Genre.valueOf(genre.toUpperCase()) : null;
        Rating ratingEnum = (rating != null && !rating.isBlank()) ? Rating.valueOf(rating.toUpperCase()) : null;
        Page<Movie> result = movieRepository.findByGenreAndRating(genreEnum, ratingEnum, pageable);
        log.debug("Fetched {} movies", result.getTotalElements());
        return result;
    }

    /**
     * {@inheritDoc}
     * Includes upcoming showtimes for the movie.
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public MovieResponse getMovieById(UUID id) {
        log.debug("Fetching movie by id={}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        List<Showtime> upcoming = showtimeRepository.findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(id, LocalDateTime.now());
        log.debug("Movie {} has {} upcoming showtimes", id, upcoming.size());

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
        log.info("Creating movie '{}'", dto.getTitle());
        Movie movie = Movie.builder()
                .title(dto.getTitle())
                .synopsis(dto.getSynopsis())
                .duration(dto.getDuration())
                .genre(dto.getGenre())
                .rating(dto.getRating())
                .build();
        Movie saved = movieRepository.save(movie);
        log.info("Movie created with id={} title='{}'", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public Movie updateMovie(UUID id, MovieDTO dto) {
        log.info("Updating movie with id={}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setTitle(dto.getTitle());
        movie.setSynopsis(dto.getSynopsis());
        movie.setDuration(dto.getDuration());
        movie.setGenre(dto.getGenre());
        movie.setRating(dto.getRating());
        Movie saved = movieRepository.save(movie);
        log.info("Movie updated with id={} title='{}'", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * {@inheritDoc}
     * Performs a soft delete via the entity's {@code @SQLDelete} mapping.
     *
     * @throws ResourceNotFoundException if the movie does not exist
     */
    @Override
    public void deleteMovie(UUID id) {
        log.info("Deleting movie with id={}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movieRepository.delete(movie);
        log.info("Movie deleted with id={}", id);
    }
}
