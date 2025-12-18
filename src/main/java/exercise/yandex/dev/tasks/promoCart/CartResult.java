package exercise.yandex.dev.tasks.promoCart;

public record CartResult(
        boolean result,
        String message,
        int cartSum
) {
    static CartResult succes(int cartSum) {
        return new CartResult(true, null, cartSum);
    }

    static CartResult failure(String message) {
        return new CartResult(false, message, 0);
    }
}

