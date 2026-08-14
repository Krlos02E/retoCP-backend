package com.cinetest.repository;

import com.cinetest.model.entity.Showtime;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(UUID movieId, java.time.LocalDateTime dateTime);

    List<Showtime> findByRoom(String room);

    List<Showtime> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Showtime> findById(UUID id);

    @Query("SELECT s FROM Showtime s WHERE " +
           "(:movieId IS NULL OR s.movie.id = :movieId) AND " +
           "(:date IS NULL OR FUNCTION('DATE', s.dateTime) = :date) AND " +
           "(:minPrice IS NULL OR s.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.price <= :maxPrice)")
    Page<Showtime> findAllWithFilters(@Param("movieId") UUID movieId,
                                      @Param("date") LocalDate date,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      Pageable pageable);
}
