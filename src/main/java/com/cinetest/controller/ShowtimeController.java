package com.cinetest.controller;

import com.cinetest.dto.ApiResponse;
import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.model.entity.Showtime;
import com.cinetest.service.ShowtimeService;
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

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Showtime>>> getAllShowtimes(
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Showtime> showtimes = showtimeService.getAllShowtimes(movieId, date, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Showtimes retrieved successfully", showtimes));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Showtime>> createShowtime(@Valid @RequestBody ShowtimeDTO dto) {
        Showtime showtime = showtimeService.createShowtime(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Showtime created successfully", showtime));
    }
}
