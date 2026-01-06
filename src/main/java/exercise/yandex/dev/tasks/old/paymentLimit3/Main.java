//package exercise.yandex.dev.qwe1;
//
//import exercise.yandex.dev.qwe.Payment;
//import exercise.yandex.dev.qwe.PaymentLimitValidator;
//import exercise.yandex.dev.qwe.UserLimits;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class Main {
//    public static void main(String[] args) {
////        Map<String, UserLimits> userLimits = Map.of(
////                "1",new UserLimits(new BigDecimal(100), new BigDecimal(1000)),
////                "2",new UserLimits(new BigDecimal(10), new BigDecimal(100))
////        );
////        Map<String, List<Payment>> paymentHistory = Map.of(
////                "1", List.of(new Payment(),new Payment(), new Payment())
////        );
//
//        // ========== ХРАНИЛИЩЕ ИСТОРИИ ПЛАТЕЖЕЙ ==========
//        Map<String, List<Payment>> paymentHistory = new HashMap<>();
//
//        LocalDateTime now = LocalDateTime.now();
//
//        paymentHistory.put("user1", List.of(
//                new Payment(new BigDecimal("100"), now.minusHours(5)),  // 100 руб 5 часов назад
//                new Payment(new BigDecimal("200"), now.minusHours(3)),  // 200 руб 3 часа назад
//                new Payment(new BigDecimal("150"), now.minusHours(1))   // 150 руб 1 час назад
//                // ИТОГО за сегодня: 450 руб
//        ));
//
//        paymentHistory.put("user2", List.of(
//                new Payment(new BigDecimal("50"), now.minusHours(2))    // 50 руб 2 часа назад
//        ));
//
//        // user3 - новый пользователь, платежей еще не было
//        paymentHistory.put("user3", List.of());
//
//        Map<String, UserLimits> userLimits = new HashMap<>();
//
//        userLimits.put("user1", new UserLimits(
//                new BigDecimal("800"),  // дневной лимит: 1000 руб
//                new BigDecimal("500")    // максимум за одну операцию: 500 руб
//        ));
//
//        userLimits.put("user2", new UserLimits(
//                new BigDecimal("5000"),  // дневной лимит: 5000 руб
//                new BigDecimal("2000")   // максимум за одну операцию: 2000 руб
//        ));
//
//        userLimits.put("user3", new UserLimits(
//                new BigDecimal("100"),   // дневной лимит: 100 руб (низкий)
//                new BigDecimal("50")     // максимум за одну операцию: 50 руб
//        ));
//
//
//        PaymentLimitChecker validator = new PaymentLimitChecker(userLimits, paymentHistory);
//
//
//        System.out.println("========== ТЕСТ 1: user1 платит 300 руб ==========");
//        Payment payment1 = new Payment(new BigDecimal("400"), now);
//        System.out.println(validator.checkPayment("user1", payment1));
//
//    }
//}
