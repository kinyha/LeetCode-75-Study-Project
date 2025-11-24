package exercise.yandex.dev.OneNew;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        String userId,
        BigDecimal amount,
        LocalDateTime timestamp
) {
    public Payment {
        if(userId == null) {
            throw new RuntimeException("User should have id");
        }
        if(amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new NumberFormatException("Amount should be positive");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required");
        }
    }
}
