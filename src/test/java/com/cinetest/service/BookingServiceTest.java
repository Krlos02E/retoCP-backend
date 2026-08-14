package com.cinetest.service;

import com.cinetest.dto.BookingDTO;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.model.entity.Booking;
import com.cinetest.model.entity.Showtime;
import com.cinetest.repository.BookingRepository;
import com.cinetest.repository.ShowtimeRepository;
import com.cinetest.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Showtime showtime;
    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        showtime = Showtime.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(25.00))
                .totalSeats(100)
                .availableSeats(5)
                .build();

        bookingDTO = BookingDTO.builder()
                .showtimeId(showtime.getId())
                .customerName("John Doe")
                .customerEmail("juan@test.com")
                .seatsBooked(10)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenBookingExceedsAvailableSeats() {
        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.of(showtime));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });

        assertEquals("Not enough seats available for this showtime", exception.getMessage());
        verify(showtimeRepository, times(1)).findById(showtime.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldCreateBookingWhenEnoughSeatsAvailable() {
        showtime.setAvailableSeats(15);
        bookingDTO.setSeatsBooked(5);

        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.of(showtime));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(showtime);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(bookingDTO);

        assertNotNull(result);
        assertEquals(5, result.getSeatsBooked());
        assertEquals(BigDecimal.valueOf(125.00), result.getTotalPrice()); // 25 * 5
        assertEquals(10, showtime.getAvailableSeats()); // 15 - 5
        verify(showtimeRepository).save(showtime);
        verify(bookingRepository).save(any(Booking.class));
    }
}
