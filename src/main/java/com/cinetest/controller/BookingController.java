package com.cinetest.controller;

import com.cinetest.dto.ApiResponseDTO;
import com.cinetest.dto.BookingDTO;
import com.cinetest.model.entity.Booking;
import com.cinetest.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for booking endpoints.
 * Handles creation and retrieval of bookings.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for managing seat reservations")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a new booking for a showtime.
     * Requires an authenticated user with CUSTOMER role.
     *
     * @param dto the booking request containing showtime, customer info and seats
     * @return a structured response with the created booking
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Create a booking",
            description = "Reserves seats for a specific showtime. Validates seat availability and updates stock. Requires CUSTOMER role.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Booking created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - requires CUSTOMER role", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Showtime not found", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Not enough seats available for this showtime", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Booking>> createBooking(@Valid @RequestBody BookingDTO dto) {
        Booking booking = bookingService.createBooking(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "Booking created successfully", booking));
    }

    /**
     * Retrieves a booking by its ID.
     * Requires an authenticated user.
     *
     * @param id the UUID of the booking
     * @return a structured response with the booking details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Get booking by ID",
            description = "Retrieves details of a specific booking. Any authenticated user can access this endpoint.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Booking>> getBookingById(
            @Parameter(description = "UUID of the booking to retrieve") @PathVariable UUID id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Booking retrieved successfully", booking));
    }
}
