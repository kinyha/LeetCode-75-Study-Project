package exercise.yandex.dev.tasks.promoCart;

import java.util.Set;

public record PromoCode(
        String code,
        PromoType promoType,
        int value,
        int minOrderSum,
        Set<String> categories
) {
    public PromoCode {
        if (code == null) {
            throw new IllegalArgumentException("Code cant be null");
        }
        if (promoType == null) {
            throw new IllegalArgumentException("PromoType cant bu null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("value cant be less 0");
        }
        if (minOrderSum <= 0) {
            throw new IllegalArgumentException("ninOrderSum cant be less 0");
        }
    }
}
