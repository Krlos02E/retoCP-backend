package com.cinetest.service;

import com.cinetest.dto.MovieDTO;
import com.cinetest.dto.MovieResponse;
import com.cinetest.model.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MovieService {

    Page<Movie> getAllMovies(String genre, String rating, Pageable pageable);

    MovieResponse getMovieById(UUID id);

    Movie createMovie(MovieDTO dto);

    Movie updateMovie(UUID id, MovieDTO dto);

    void deleteMovie(UUID id);
}
