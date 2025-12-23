package exercise.yandex.dev.tasks.deliveryCalculator;

public record DeliveryResult(
        int price,
        String message
) {
    static DeliveryResult success(int price) {
        return new DeliveryResult(price,"Price for delivery " + price);
    }
    static DeliveryResult failure(String message) {
        return new DeliveryResult(-1, message);
    }
}
