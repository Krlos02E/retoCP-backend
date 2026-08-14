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

/**
 * Repository for {@link Showtime} entities.
 * Provides derived queries, custom JPQL filtering, and pessimistic locking for concurrent booking scenarios.
 */
@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    /**
     * Finds all showtimes for a specific movie.
     *
     * @param movieId the UUID of the movie
     * @return list of showtimes ordered by dateTime ascending
     */
    List<Showtime> findByMovieId(UUID movieId);

    /**
     * Finds upcoming showtimes for a specific movie that occur after the given dateTime.
     *
     * @param movieId  the UUID of the movie
     * @param dateTime the cutoff dateTime; only showtimes after this are returned
     * @return list of upcoming showtimes ordered by dateTime ascending
     */
    List<Showtime> findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(UUID movieId, java.time.LocalDateTime dateTime);

    /**
     * Finds all showtimes scheduled in a specific room.
     * Used to detect schedule overlaps during showtime creation.
     *
     * @param room the room name
     * @return list of showtimes in that room
     */
    List<Showtime> findByRoom(String room);

    List<Showtime> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds a showtime by ID with a pessimistic write lock.
     * This prevents concurrent bookings from causing race conditions (overbooking).
     *
     * @param id the UUID of the showtime
     * @return the locked showtime entity
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Showtime> findById(UUID id);

    /**
     * Finds showtimes with optional filtering by movie, date and price range.
     * Null parameters are ignored in the query.
     *
     * @param movieId  optional filter by movie ID
     * @param date     optional filter by date (compares only the date portion)
     * @param minPrice optional minimum price filter
     * @param maxPrice optional maximum price filter
     * @param pageable pagination configuration
     * @return a page of filtered showtimes
     */
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
