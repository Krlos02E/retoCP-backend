package com.cinetest.dto;

import com.cinetest.model.entity.Showtime;
import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {

    private UUID id;
    private String title;
    private String synopsis;
    private Integer duration;
    private Genre genre;
    private Rating rating;
    private List<Showtime> upcomingShowtimes;
}
