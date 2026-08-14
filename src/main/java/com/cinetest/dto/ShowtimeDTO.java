package com.cinetest.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDTO {

    @NotNull
    private UUID movieId;

    @NotBlank
    private String room;

    @NotNull
    @Future
    private LocalDateTime dateTime;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer totalSeats;

    @NotNull
    @Min(0)
    private Integer availableSeats;
}
