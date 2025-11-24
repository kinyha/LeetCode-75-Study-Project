//package exercise.yandex.dev.qwe1;
//
//import exercise.yandex.dev.qwe.Payment;
//import exercise.yandex.dev.qwe.UserLimits;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class PaymentLimitChecker {
//    // Хранилище лимитов пользователей
//    Map<String, UserLimits> userLimits = new HashMap<>();
//
//    // История платежей
//    Map<String, List<Payment>> paymentHistory = new HashMap<>();
//
//    public PaymentLimitChecker(Map<String, UserLimits> userLimits, Map<String, List<Payment>> paymentHistory) {
//        this.userLimits = userLimits;
//        this.paymentHistory = paymentHistory;
//    }
//
//    public CheckResult checkPayment(String userId, Payment payment) {
//        UserLimits limits = userLimits.get(userId);
//        List<Payment> payments = paymentHistory.get(userId);
//
//        if (payment.amount().compareTo(limits.maxSingleTransaction()) > 0) {
//            return CheckResult.failure(String.format("Payment(%s) more than single transaction limit %s",payment.amount(),limits.maxSingleTransaction()));
//        }
//
//        LocalDateTime day = payment.timestamp().toLocalDate().atStartOfDay();
//
//        LocalDateTime startOfDay = payment.timestamp().toLocalDate().atStartOfDay();
//        LocalDateTime endOfDay = startOfDay.plusDays(1);
//
//        BigDecimal daySum = findDaySum(payments, day);
//        BigDecimal result = daySum.add(payment.amount());
//        if (result.compareTo(limits.dailyLimit()) > 0) {
//            return CheckResult.failure(String.format("Payment(%s + %s) more than day limit %s",daySum,payment.amount(),limits.dailyLimit()));
//        }
//
//        return CheckResult.success();
//    }
//
//    private BigDecimal findDaySum(List<Payment> payments, LocalDateTime day) {
//        return payments.stream()
//                .filter(payment -> payment.timestamp().isAfter(day)
//                        && payment.timestamp().isBefore(day.plusDays(1)))
//                .map(Payment::amount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//    record CheckResult(
//            boolean allowed,  // Лучше назвать allowed вместо access
//            String message
//    ) {
//        static CheckResult success() {
//            return new CheckResult(true, null);
//        }
//
//        static CheckResult failure(String message) {
//            return new CheckResult(false, message);
//        }
//    }
//}
