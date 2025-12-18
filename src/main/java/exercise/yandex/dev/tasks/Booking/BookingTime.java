package exercise.yandex.dev.tasks.Booking;

import java.time.LocalDateTime;

public record BookingTime(
        LocalDateTime from,
        LocalDateTime to
) {
    public BookingTime {
        if(from == null || to == null) {
            throw new IllegalArgumentException("time  is null");
        }
    }
}
