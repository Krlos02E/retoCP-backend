package com.cinetest.repository;

import com.cinetest.model.entity.Movie;
import com.cinetest.model.enums.Genre;
import com.cinetest.model.enums.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    @Query("SELECT m FROM Movie m WHERE " +
           "(:genre IS NULL OR m.genre = :genre) AND " +
           "(:rating IS NULL OR m.rating = :rating)")
    Page<Movie> findByGenreAndRating(@Param("genre") Genre genre,
                                       @Param("rating") Rating rating,
                                       Pageable pageable);
}
