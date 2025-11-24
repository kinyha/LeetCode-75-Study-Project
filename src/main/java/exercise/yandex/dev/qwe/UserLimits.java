//package exercise.yandex.dev.qwe;
//
//import java.math.BigDecimal;
//
//public record UserLimits(
//        BigDecimal dailyLimit,
//        BigDecimal maxSingleTransaction
//) {
//    public UserLimits {
//        if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("Daily limit must be positive");
//        }
//        if (maxSingleTransaction == null || maxSingleTransaction.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("Max transaction must be positive");
//        }
//    }
//}
