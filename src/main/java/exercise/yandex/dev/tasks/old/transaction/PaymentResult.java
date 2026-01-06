package exercise.yandex.dev.tasks.transaction;

public record PaymentResult(
        boolean result,
        String message
) {

    public PaymentResult {
        if (message == null) {
            throw new IllegalArgumentException("Message cant be null");
        }
    }

    static PaymentResult success() {
        return new PaymentResult(true, "");
    }
    static PaymentResult failure(String message) {
        return new PaymentResult(false, message);
    }
}
