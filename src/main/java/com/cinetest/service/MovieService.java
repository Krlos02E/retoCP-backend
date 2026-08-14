package com.cinetest.service;

import com.cinetest.dto.MovieDTO;
import com.cinetest.dto.MovieResponse;
import com.cinetest.model.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for movie operations.
 */
public interface MovieService {

    /**
     * Lists all movies with optional filters and pagination.
     *
     * @param genre    optional genre filter
     * @param rating   optional rating filter
     * @param pageable pagination configuration
     * @return a page of movies
     */
    Page<Movie> getAllMovies(String genre, String rating, Pageable pageable);

    /**
     * Retrieves a movie by ID, including upcoming showtimes.
     *
     * @param id the UUID of the movie
     * @return the movie response with showtimes
     */
    MovieResponse getMovieById(UUID id);

    /**
     * Creates a new movie.
     *
     * @param dto the movie data
     * @return the persisted movie entity
     */
    Movie createMovie(MovieDTO dto);

    /**
     * Updates an existing movie.
     *
     * @param id  the UUID of the movie to update
     * @param dto the updated movie data
     * @return the updated movie entity
     */
    Movie updateMovie(UUID id, MovieDTO dto);

    /**
     * Soft deletes a movie by ID.
     *
     * @param id the UUID of the movie to delete
     */
    void deleteMovie(UUID id);
}
