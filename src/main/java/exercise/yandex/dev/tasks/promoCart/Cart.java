package exercise.yandex.dev.tasks.promoCart;

import java.util.List;

public record Cart(
        List<Product> items
) {
    public Cart(List<Product> items) {
        this.items = items;
    }
}
