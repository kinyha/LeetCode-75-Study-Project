package exercise.yandex.dev.tasks.old.paymentLimit;

public record CheckResult(
        boolean access,
        String message
) {
    static CheckResult accessed() {
        return new CheckResult(true, null);
    }
    static CheckResult failure(String message) {
        return new CheckResult(false, message);
    }
}
