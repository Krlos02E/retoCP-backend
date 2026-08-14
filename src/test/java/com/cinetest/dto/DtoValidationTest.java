package com.cinetest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void movieDTOWithInvalidDataShouldFailValidation() {
        MovieDTO dto = MovieDTO.builder()
                .title("")
                .synopsis("Synopsis")
                .duration(0)
                .genre(null)
                .rating(null)
                .build();

        Set<ConstraintViolation<MovieDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("genre")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }

    @Test
    void showtimeDTOWithInvalidDataShouldFailValidation() {
        ShowtimeDTO dto = ShowtimeDTO.builder()
                .movieId(null)
                .room("") // blank
                .dateTime(LocalDateTime.now().minusDays(1)) // past
                .price(BigDecimal.ZERO) // less than 0.01
                .totalSeats(0) // less than 1
                .availableSeats(-1) // less than 0
                .build();

        Set<ConstraintViolation<ShowtimeDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("room")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateTime")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("totalSeats")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("availableSeats")));
    }

    @Test
    void bookingDTOWithInvalidDataShouldFailValidation() {
        BookingDTO dto = BookingDTO.builder()
                .showtimeId(null)
                .customerName("") // blank
                .customerEmail("invalid-email") // invalid email
                .seatsBooked(0) // less than 1
                .build();

        Set<ConstraintViolation<BookingDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("seatsBooked")));
    }

    @Test
    void validMovieDTOShouldPassValidation() {
        MovieDTO dto = MovieDTO.builder()
                .title("Valid Title")
                .synopsis("Valid Synopsis")
                .duration(120)
                .genre(com.cinetest.model.enums.Genre.ACTION)
                .rating(com.cinetest.model.enums.Rating.PG_13)
                .build();

        Set<ConstraintViolation<MovieDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
