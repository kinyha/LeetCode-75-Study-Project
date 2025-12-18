package exercise.yandex.dev.tasks.promoCart;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PromoCodeRepository {
    List<PromoCode> promoCodes;

    public PromoCodeRepository() {
        promoCodes = new ArrayList<>();
        promoCodes.add(new PromoCode("SALE", PromoType.PERCENT, 10, 1000, Set.of()));

    }
}
