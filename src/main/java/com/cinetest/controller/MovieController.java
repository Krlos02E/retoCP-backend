package com.cinetest.controller;

import com.cinetest.dto.ApiResponseDTO;
import com.cinetest.dto.MovieDTO;
import com.cinetest.dto.MovieResponse;
import com.cinetest.model.entity.Movie;
import com.cinetest.service.MovieService;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST controller for movie endpoints.
 * Supports listing, detail view, creation, update and soft deletion of movies.
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Endpoints for managing the movie catalog")
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
    @RateLimiting(name = "movies-read")
    @Operation(
            summary = "List all movies",
            description = "Retrieves a paginated list of movies. Optionally filter by genre or rating.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Page<Movie>>> getAllMovies(
            @Parameter(description = "Optional genre filter (e.g., ACTION, DRAMA)") @RequestParam(required = false) String genre,
            @Parameter(description = "Optional rating filter (e.g., PG, PG_13, R)") @RequestParam(required = false) String rating,
            @ParameterObject @PageableDefault(sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(genre, rating, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Movies retrieved successfully", movies));
    }

    /**
     * Retrieves a single movie by ID, including upcoming showtimes.
     *
     * @param id the UUID of the movie
     * @return a structured response with movie details and showtimes
     */
    @GetMapping("/{id}")
    @RateLimiting(name = "movies-read")
    @Operation(
            summary = "Get movie by ID",
            description = "Retrieves detailed information about a specific movie, including its upcoming showtimes.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movie retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<MovieResponse>> getMovieById(
            @Parameter(description = "UUID of the movie to retrieve") @PathVariable UUID id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Movie retrieved successfully", movie));
    }

    /**
     * Creates a new movie.
     * Requires ADMIN role.
     *
     * @param dto the movie data
     * @return a structured response with the created movie
     */
    @PostMapping
    @RateLimiting(name = "admin-write")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Create a new movie",
            description = "Creates a new movie in the catalog. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Movie created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Movie>> createMovie(@Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.createMovie(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "Movie created successfully", movie));
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
    @RateLimiting(name = "admin-write")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Update a movie",
            description = "Updates an existing movie's information. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movie updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Movie>> updateMovie(
            @Parameter(description = "UUID of the movie to update") @PathVariable UUID id,
            @Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.updateMovie(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Movie updated successfully", movie));
    }

    /**
     * Soft deletes a movie by ID.
     * Requires ADMIN role.
     *
     * @param id the UUID of the movie to delete
     * @return a structured response confirming deletion
     */
    @DeleteMapping("/{id}")
    @RateLimiting(name = "admin-write")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Delete a movie",
            description = "Performs a soft delete of a movie by ID. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movie deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Void>> deleteMovie(
            @Parameter(description = "UUID of the movie to delete") @PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Movie deleted successfully"));
    }
}
