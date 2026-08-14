package com.cinetest.controller;

import com.cinetest.dto.ApiResponse;
import com.cinetest.dto.MovieDTO;
import com.cinetest.dto.MovieResponse;
import com.cinetest.model.entity.Movie;
import com.cinetest.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /**
     * Lists all movies with optional filtering by genre and rating.
     * Supports pagination and sorting.
     *
     * @param genre    optional genre filter
     * @param rating   optional rating filter
     * @param pageable pagination and sorting configuration
     * @return a structured response with a page of movies
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Movie>>> getAllMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @PageableDefault(sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(genre, rating, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movies retrieved successfully", movies));
    }

    /**
     * Retrieves a single movie by ID, including upcoming showtimes.
     *
     * @param id the UUID of the movie
     * @return a structured response with movie details and showtimes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie retrieved successfully", movie));
    }

    /**
     * Creates a new movie.
     * Requires ADMIN role.
     *
     * @param dto the movie data
     * @return a structured response with the created movie
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> createMovie(@Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.createMovie(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Movie created successfully", movie));
    }

    /**
     * Updates an existing movie.
     * Requires ADMIN role.
     *
     * @param id  the UUID of the movie to update
     * @param dto the updated movie data
     * @return a structured response with the updated movie
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.updateMovie(id, dto);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie updated successfully", movie));
    }

    /**
     * Soft deletes a movie by ID.
     * Requires ADMIN role.
     *
     * @param id the UUID of the movie to delete
     * @return a structured response confirming deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie deleted successfully"));
    }
}
