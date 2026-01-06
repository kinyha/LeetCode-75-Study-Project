//package exercise.yandex.dev.qwe;
//
//
///*
//Яндекс
//
//Вы — backend-разработчик в финтех компании.
//Компания предоставляет платежные услуги и должна контролировать лимиты пользователей.
//Product owner просит создать систему проверки лимитов перед проведением платежей.
//
//### Определения
//
//Платеж:
//- сумма (в рублях)
//- тип операции (только списание)
//- timestamp
//
//Лимиты пользователя:
//- дневной лимит по сумме
//- максимальный размер одной операции
//
//История операций:
//- список совершенных платежей пользователя
//
//### Задача
//Написать систему проверки лимитов, которая:
//- на вход получает платеж и id пользователя
//- проверяет все установленные лимиты
//- возвращает результат: можно ли провести операцию
//- если нельзя, то указывает причину (какой лимит превышен)
//
//### Ограничения
//- в рамках данной задачи считаем, что все платежи одного пользователя происходят строго последовательно.
//Во время проверки лимита не может быть проведен платеж того же пользователя.
//* */
//
///*
//package yandex.interview;
//
//import java.util.*;
//
//public class PaymentLimitChecker {
//
//    // key - userId, value - daySum
//    Map<Long, Long> userDaySum = new HashMap<>();
//    // key - userId, value - paymentsUser
//    Map<Long, Queue<Payment>> userPayments = new HashMap<>();
//    long limitDayUserSum;
//    long maxSizeUserSumOneOperation;
//
//    public PaymentLimitChecker(long limitDayUserSum, long maxSizeUserSumOneOperation) {
//        this.limitDayUserSum = limitDayUserSum;
//        this.maxSizeUserSumOneOperation = maxSizeUserSumOneOperation;
//    }
//
//    static class Payment {
//        long sum;
//        String typeOperation;
//        long timestamp;
//
//        public Payment(long sum, String typeOperation, long timestamp) {
//            this.sum = sum;
//            this.typeOperation = typeOperation;
//            this.timestamp = timestamp;
//        }
//    }
//
//    static class CheckResult {
//        boolean allowed;
//        String reason;
//
//        public CheckResult(boolean allowed, String reason) {
//            this.allowed = allowed;
//            this.reason = reason;
//        }
//    }
//
//    public CheckResult checkPayment(long userId, Payment payment) {
//        removeOldPayments(userId, payment.timestamp);
//        if (payment.sum > maxSizeUserSumOneOperation) {
//            return new CheckResult(false, "Превышен размер операции");
//        }
//
//        long currentSum = userDaySum.getOrDefault(userId, 0L);
//        if (currentSum + payment.sum >= limitDayUserSum) {
//            return new CheckResult(false, "Превышен лимит суммы дневных операций");
//        }
//
//        Queue<Payment> paymentsUser = userPayments.get(userId);
//        paymentsUser.add(payment);
//        userDaySum.put(userId, userDaySum.get(userId) + payment.sum);
//
//        return new CheckResult(true, null);
//    }
//
//    public void removeOldPayments(long userId, long now) {
//        long dayAgo = now - 24 * 60 * 60 * 1000L;
//        Queue<Payment> paymentsUser = userPayments.get(userId);
//        while (!paymentsUser.isEmpty() && paymentsUser.peek().timestamp < dayAgo) {
//            Payment payment = paymentsUser.poll();
//            userDaySum.put(userId, userDaySum.get(userId) - payment.sum);
//        }
//    }
//}
//
//*/
//
//
//import java.math.BigDecimal;
//import java.util.List;
//
//public class PaymentLimitValidator {
//    private final UserLimitsRepository limitsRepository;
//    private final PaymentHistoryRepository historyRepository;
//
//    public PaymentLimitValidator(
//            UserLimitsRepository limitsRepository,
//            PaymentHistoryRepository historyRepository
//    ) {
//        this.limitsRepository = limitsRepository;
//        this.historyRepository = historyRepository;
//    }
//
//    public ValidationResult validate(String userId, Payment payment) {
//        UserLimits limits = limitsRepository.getLimits(userId);
//
//        // Проверка максимального размера транзакции
//        if (payment.amount().compareTo(limits.maxSingleTransaction()) > 0) {
//            return new ValidationResult.Failure(
//                    String.format("Transaction amount %.2f exceeds max single transaction limit %.2f",
//                            payment.amount(), limits.maxSingleTransaction())
//            );
//        }
//
//        // Проверка дневного лимита
//        List<Payment> todayPayments = historyRepository.getPaymentsForDay(userId, payment.timestamp());
//        BigDecimal todayTotal = calculateTotalAmount(todayPayments);
//        BigDecimal projectedTotal = todayTotal.add(payment.amount());
//
//        if (projectedTotal.compareTo(limits.dailyLimit()) > 0) {
//            return new ValidationResult.Failure(
//                    String.format("Daily limit exceeded. Current: %.2f, Limit: %.2f, Attempted: %.2f",
//                            todayTotal, limits.dailyLimit(), payment.amount())
//            );
//        }
//
//        return new ValidationResult.Success();
//    }
//
//    private BigDecimal calculateTotalAmount(List<Payment> payments) {
//        return payments.stream()
//                .map(Payment::amount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//}
//
//
