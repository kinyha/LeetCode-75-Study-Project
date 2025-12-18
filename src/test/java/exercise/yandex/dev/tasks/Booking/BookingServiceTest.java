package exercise.yandex.dev.tasks.Booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    BookingService bookingService;
    BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository = new BookingRepository();
        bookingService = new BookingService(bookingRepository);
    }

    @Test
    @DisplayName("happy test")
    void canBook() {
        //given
        BookingTime bookingTime = new BookingTime(LocalDateTime.now().plusHours(4),LocalDateTime.now().plusHours(5));
        BookingRequest want = new BookingRequest("room1",bookingTime);

        //when
        var result = bookingService.book(want);


        //then
        assertTrue(result.result());
    }
    @Test
    @DisplayName("Not happy test")
    void canTBook() {
        //given
        BookingTime bookingTime = new BookingTime(LocalDateTime.now().minusHours(1),LocalDateTime.now().plusHours(5));
        BookingRequest want = new BookingRequest("room1",bookingTime);

        //when
        var result = bookingService.book(want);


        //then
        assertFalse(result.result());
        assertEquals("Time is not free", result.message());
    }

    @Test
    @DisplayName("In intervals")
    void canBookInIntervals() {
        //given
        BookingTime bookingTime = new BookingTime(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(2));
        BookingRequest want = new BookingRequest("room1",bookingTime);

        //when
        var result = bookingService.book(want);

        //then
        assertTrue(result.result());
    }
}