package exercise.yandex.dev.tasks.old.Booking;

import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    BookingResult book(BookingRequest request) {
        List<BookingRequest> bookings = bookingRepository.bookingRequests;

        List<BookingRequest> list = bookings.stream()
                .filter(b -> b.idRoom().equals(request.idRoom()))
                .filter(b1 -> timeIsFree(b1.interval(), request.interval()))
                .toList();

        if (list.isEmpty()) {
            return BookingResult.success();
        }
        return BookingResult.fail("Time is not free");
    }

    boolean timeIsFree(BookingTime x, BookingTime y) {
        return x.from().isBefore(y.to()) && y.from().isBefore(x.to());
    }
}
