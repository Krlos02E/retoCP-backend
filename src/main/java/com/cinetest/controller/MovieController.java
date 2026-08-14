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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Movie>>> getAllMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @PageableDefault(sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(genre, rating, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movies retrieved successfully", movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie retrieved successfully", movie));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> createMovie(@Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.createMovie(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Movie created successfully", movie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieDTO dto) {
        Movie movie = movieService.updateMovie(id, dto);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie updated successfully", movie));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Movie deleted successfully"));
    }
}
