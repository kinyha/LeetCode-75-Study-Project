package exercise.yandex.dev.OneNew;

import java.math.BigDecimal;

public record UserLimits(BigDecimal dayLimit, BigDecimal transactionalLimit) {
    public UserLimits {
        if (dayLimit.compareTo(BigDecimal.ZERO) < 0  || transactionalLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new NumberFormatException("Limits should more than 0");
        }
    }
}
