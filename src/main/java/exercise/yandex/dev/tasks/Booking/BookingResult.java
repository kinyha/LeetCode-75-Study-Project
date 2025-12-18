package exercise.yandex.dev.tasks.Booking;

public record BookingResult(
        boolean result,
        String message
) {
    static BookingResult success() {
        return new BookingResult(true, "null");
    }

    static BookingResult fail(String message) {
        return new BookingResult(false, message);
    }
}
