package exercise.yandex.dev.tasks.promoCart;

public record Product(
        String id,
        String name,
        int price,
        String category
) {
    public Product {
        if (id == null) {
            throw new IllegalArgumentException("Id cant null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cant null");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("price cant zero");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cant null");
        }
    }

}
