package exercise.yandex.dev.tasks.Booking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {
    List<BookingRequest> bookingRequests;

    public BookingRepository() {
        bookingRequests = new ArrayList<>();
        bookingRequests.add(new BookingRequest("room1", new BookingTime(LocalDateTime.now(), LocalDateTime.now().plusHours(1))));
        bookingRequests.add(new BookingRequest("room1", new BookingTime(LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4))));
    }
}
