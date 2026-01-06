package exercise.yandex.dev.tasks.old.Booking;

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
