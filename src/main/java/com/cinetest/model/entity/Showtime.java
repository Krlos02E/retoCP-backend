package com.cinetest.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "showtime")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotBlank
    @Column(nullable = false)
    private String room;

    @NotNull
    @Future
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(1)
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull
    @Min(0)
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;
}
