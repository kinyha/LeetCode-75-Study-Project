package exercise.yandex.dev.tasks.deliveryCalculator;

public record Size(
        int length,
        int wide,
        int height
) {
    public Size {
        if (length <= 0 || wide <=0 || height <= 0) {
            throw new IllegalArgumentException("Size cant be <= 0");
        }
     }
     int getVolum() {
        return length * wide * height;
     }
}
