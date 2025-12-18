package exercise.yandex.dev.tasks.Booking;

public record BookingRequest(
        String idRoom,
        BookingTime interval
) {
    public BookingRequest {

        if (idRoom == null) {
            throw new IllegalArgumentException("idRoom is null");
        }
        if (interval == null) {
            throw new IllegalArgumentException("Interval cant be null");
        }
    }
}
