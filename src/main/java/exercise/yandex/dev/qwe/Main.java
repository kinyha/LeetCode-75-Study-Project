//package exercise.yandex.dev.qwe;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.*;
//
//public class Main {
//    public static void main(String[] args) {
//        // ========== ХРАНИЛИЩЕ ИСТОРИИ ПЛАТЕЖЕЙ ==========
//        Map<String, List<Payment>> paymentHistory = new HashMap<>();
//
//        // Заполняем историю для user1
//        LocalDateTime now = LocalDateTime.now();
//        paymentHistory.put("user1", List.of(
//                new Payment(new BigDecimal("100"), now.minusHours(5)),  // 100 руб 5 часов назад
//                new Payment(new BigDecimal("200"), now.minusHours(3)),  // 200 руб 3 часа назад
//                new Payment(new BigDecimal("150"), now.minusHours(1))   // 150 руб 1 час назад
//                // ИТОГО за сегодня: 450 руб
//        ));
//
//        // Заполняем историю для user2
//        paymentHistory.put("user2", List.of(
//                new Payment(new BigDecimal("50"), now.minusHours(2))    // 50 руб 2 часа назад
//        ));
//
//        // user3 - новый пользователь, платежей еще не было
//        paymentHistory.put("user3", List.of());
//
//        // ========== ХРАНИЛИЩЕ ЛИМИТОВ ПОЛЬЗОВАТЕЛЕЙ ==========
//        Map<String, UserLimits> userLimits = new HashMap<>();
//
//        userLimits.put("user1", new UserLimits(
//                new BigDecimal("1000"),  // дневной лимит: 1000 руб
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
//        // ========== СОЗДАЕМ РЕПОЗИТОРИИ ==========
//        PaymentHistoryRepository historyRepository = new PaymentHistoryRepository() {
//            @Override
//            public List<Payment> getPaymentsForDay(String userId, LocalDateTime timestamp) {
//                LocalDateTime startOfDay = timestamp.toLocalDate().atStartOfDay();
//
//                return paymentHistory.getOrDefault(userId, List.of()).stream()
//                        .filter(p -> p.timestamp().isAfter(startOfDay))
//                        .toList();
//            }
//        };
//
//        UserLimitsRepository userLimitsRepository = new UserLimitsRepository() {
//            @Override
//            public UserLimits getLimits(String userId) {
//                return userLimits.get(userId);
//            }
//        };
//
//        PaymentLimitValidator validator = new PaymentLimitValidator(userLimitsRepository, historyRepository);
//
//        // ========== ТЕСТОВЫЕ СЦЕНАРИИ ==========
//
//        System.out.println("========== ТЕСТ 1: user1 платит 300 руб ==========");
//        Payment payment1 = new Payment(new BigDecimal("300"), now);
//        ValidationResult result1 = validator.validate("user1", payment1);
//        printResult(result1);
//        // user1 уже потратил 450 руб, +300 = 750 руб < 1000 лимит ✅ OK
//
//        System.out.println("\n========== ТЕСТ 2: user1 платит 600 руб ==========");
//        Payment payment2 = new Payment(new BigDecimal("600"), now);
//        ValidationResult result2 = validator.validate("user1", payment2);
//        printResult(result2);
//        // user1 уже потратил 450 руб, +600 = 1050 руб > 1000 лимит ❌ FAIL
//
//        System.out.println("\n========== ТЕСТ 3: user1 платит 700 руб за раз ==========");
//        Payment payment3 = new Payment(new BigDecimal("700"), now);
//        ValidationResult result3 = validator.validate("user1", payment3);
//        printResult(result3);
//        // 700 > 500 (макс за операцию) ❌ FAIL
//
//        System.out.println("\n========== ТЕСТ 4: user3 (новый) платит 30 руб ==========");
//        Payment payment4 = new Payment(new BigDecimal("30"), now);
//        ValidationResult result4 = validator.validate("user3", payment4);
//        printResult(result4);
//        // 30 < 50 (макс операция) и 30 < 100 (дневной лимит) ✅ OK
//
//        System.out.println("\n========== ТЕСТ 5: user2 платит 1500 руб ==========");
//        Payment payment5 = new Payment(new BigDecimal("1500"), now);
//        ValidationResult result5 = validator.validate("user2", payment5);
//        printResult(result5);
//        // user2 потратил 50, +1500 = 1550 < 5000 и 1500 < 2000 ✅ OK
//    }
//
//    private static void printResult(ValidationResult result) {
//        if (result.isSuccess()) {
//            System.out.println("✅ Платеж разрешен");
//        } else {
//            ValidationResult.Failure failure = (ValidationResult.Failure) result;
//            System.out.println("❌ Платеж отклонен: " + failure.reason());
//        }
//    }
//}
