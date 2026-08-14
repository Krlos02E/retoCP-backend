package com.cinetest.controller;

import com.cinetest.dto.ApiResponseDTO;
import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.model.entity.Showtime;
import com.cinetest.service.ShowtimeService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for showtime endpoints.
 * Handles listing and creation of showtimes.
 */
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
@Tag(name = "Showtimes", description = "Endpoints for managing movie showtimes (screenings)")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * Lists all showtimes with optional filtering by movie, date and price range.
     * Supports pagination and sorting.
     *
     * @param movieId  optional filter by movie ID
     * @param date     optional filter by date
     * @param minPrice optional minimum price filter
     * @param maxPrice optional maximum price filter
     * @param pageable pagination and sorting configuration
     * @return a structured response with a page of showtimes
     */
    @GetMapping
    @RateLimiting(name = "showtimes-read")
    @Operation(
            summary = "List all showtimes",
            description = "Retrieves a paginated list of showtimes. Optionally filter by movie, date or price range.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Showtimes retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Page<Showtime>>> getAllShowtimes(
            @Parameter(description = "Optional filter by movie ID") @RequestParam(required = false) UUID movieId,
            @Parameter(description = "Optional filter by date (yyyy-MM-dd)") @RequestParam(required = false) LocalDate date,
            @Parameter(description = "Optional minimum price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Optional maximum price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @ParameterObject @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Showtime> showtimes = showtimeService.getAllShowtimes(movieId, date, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Showtimes retrieved successfully", showtimes));
    }

    /**
     * Creates a new showtime.
     * Requires ADMIN role.
     * Validates that the schedule does not overlap with existing showtimes in the same room.
     *
     * @param dto the showtime data
     * @return a structured response with the created showtime
     */
    @PostMapping
    @RateLimiting(name = "admin-write")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Create a new showtime",
            description = "Creates a new showtime screening. Validates that the schedule does not overlap with existing showtimes in the same room. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Showtime created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Schedule overlaps with an existing showtime in the same room", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Showtime>> createShowtime(@Valid @RequestBody ShowtimeDTO dto) {
        Showtime showtime = showtimeService.createShowtime(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "Showtime created successfully", showtime));
    }
}
