//package exercise.yandex.dev.tasks.old.Booking.promoCart;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class PromoServiceTest {
//    PromoService promoService;
//    Cart cart;
//    PromoCodeRepository promoCodeRepository;
//
//
//
//    @BeforeEach
//    void setUp() {
//        List<Product> products = List.of(
//                new Product("pr1", "iPhone 17", 80000, "electronics"),
//                new Product("pr2", "Чехол для iPhone", 2000, "electronics"),
//                new Product("pr3", "Футболка", 10000, "clothes"),
//                new Product("pr4", "Джинсы", 5000, "clothes"),
//                new Product("pr5", "Кофе 1кг", 3000, "food")
//        );
//
//        cart = new Cart(products);
//        promoCodeRepository = new PromoCodeRepository();
//        promoService = new PromoService(cart, promoCodeRepository);
//
//    }
//
//    @Test
//    @DisplayName("Happe Test")
//    void cantFindDisc() {
//        //given
//        String promoName = "SALE";
//
//        //when
//        var result = promoService.calculate(cart, promoName);
//        //than
//        System.out.println("was" + promoService.findSum(cart));
//        assertTrue(result.result());
//        System.out.println(result.cartSum());
//    }
//}