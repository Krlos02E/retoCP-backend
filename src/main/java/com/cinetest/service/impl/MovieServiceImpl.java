package com.cinetest.service.impl;

import com.cinetest.dto.MovieDTO;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.Movie;
import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import com.cinetest.repository.MovieRepository;
import com.cinetest.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public Page<Movie> getAllMovies(String genre, String rating, Pageable pageable) {
        Genre genreEnum = (genre != null && !genre.isBlank()) ? Genre.valueOf(genre.toUpperCase()) : null;
        Rating ratingEnum = (rating != null && !rating.isBlank()) ? Rating.valueOf(rating.toUpperCase()) : null;
        return movieRepository.findByGenreAndRating(genreEnum, ratingEnum, pageable);
    }

    @Override
    public Movie getMovieById(UUID id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Override
    public Movie createMovie(MovieDTO dto) {
        Movie movie = Movie.builder()
                .title(dto.getTitle())
                .synopsis(dto.getSynopsis())
                .duration(dto.getDuration())
                .genre(dto.getGenre())
                .rating(dto.getRating())
                .build();
        return movieRepository.save(movie);
    }

    @Override
    public Movie updateMovie(UUID id, MovieDTO dto) {
        Movie movie = getMovieById(id);
        movie.setTitle(dto.getTitle());
        movie.setSynopsis(dto.getSynopsis());
        movie.setDuration(dto.getDuration());
        movie.setGenre(dto.getGenre());
        movie.setRating(dto.getRating());
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(UUID id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }
}
