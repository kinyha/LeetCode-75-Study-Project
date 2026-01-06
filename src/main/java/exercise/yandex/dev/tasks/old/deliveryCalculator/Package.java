package exercise.yandex.dev.tasks.old.deliveryCalculator;

public record Package(
        String name,
        Size size,
        boolean isGlass
) {
    public Package {
        if (name == null) {
            throw new IllegalArgumentException("Name cant be null");
        }
        if (size == null) {
            throw new IllegalArgumentException("Size cant be null");
        }

    }

    public Package(String name, Size size) {
        this(name, size, false);
    }
}
