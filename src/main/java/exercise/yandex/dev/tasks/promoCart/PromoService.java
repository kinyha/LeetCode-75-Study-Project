package exercise.yandex.dev.tasks.promoCart;

public class PromoService {
    private final Cart cart;
    private final PromoCodeRepository promoCodeRepository;

    public PromoService(Cart cart, PromoCodeRepository promoCodeRepository) {
        this.cart = cart;
        this.promoCodeRepository = promoCodeRepository;
    }

    CartResult calculate(Cart cart, String promoCode) {
        if (cart.items().isEmpty()) {
            return CartResult.failure("Cart  is empty");
        }

        int cartSum = cart.items().stream()
                .mapToInt(Product::price)
                .sum();
        PromoCode promo = promoCodeRepository.promoCodes.stream()
                .filter(p -> p.code().equals(promoCode))
                .findFirst()
                .orElse(null);
        if (promo == null) {
            return CartResult.failure("Promo not found");
        }

        if (cartSum < promo.minOrderSum()) {
            return CartResult.failure("Order sum less than minOder");
        }
        int sale;
        var result  = 0;
        //countByCat
        if (!promo.categories().isEmpty()) {
            var promoSumByCat = cart.items().stream()
                    .filter(product -> promo.categories().contains(product.category()))                    .mapToInt(Product::price)
                    .sum();
            var disc = countDisc(promoSumByCat, promo);
            result = cartSum - (promoSumByCat - disc);
        } else {
            result = countDisc(cartSum, promo);
        }
        return CartResult.succes(result);

    }

    private int countDisc(int cartSum, PromoCode promo) {
        if (PromoType.FIXED == promo.promoType()) {
            return cartSum - promo.value();
        }
        if (PromoType.PERCENT == promo.promoType()) {
            double sale = 1 - ((double) promo.value() / 100);
            return (int) ((double)cartSum * sale);
        }
        return 0;
    }

    int findSum(Cart cart) {
        return cart.items().stream()
                .mapToInt(Product::price)
                .sum();
    }
}
