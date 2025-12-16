//package exercise.yandex.dev.qwe;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//public record Payment(
//        BigDecimal amount,
//        LocalDateTime timestamp
//) {
//    public Payment {
//        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("Amount must be positive");
//        }
//        if (timestamp == null) {
//            throw new IllegalArgumentException("Timestamp is required");
//        }
//    }
//}