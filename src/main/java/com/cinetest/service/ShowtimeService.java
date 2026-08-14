package com.cinetest.service;

import com.cinetest.dto.ShowtimeDTO;
import com.cinetest.model.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ShowtimeService {

    Page<Showtime> getAllShowtimes(UUID movieId, LocalDate date, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Showtime getShowtimeById(UUID id);

    Showtime createShowtime(ShowtimeDTO dto);
}
