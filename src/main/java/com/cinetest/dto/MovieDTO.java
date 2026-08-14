package com.cinetest.dto;

import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String synopsis;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    private Genre genre;

    @NotNull
    private Rating rating;
}
