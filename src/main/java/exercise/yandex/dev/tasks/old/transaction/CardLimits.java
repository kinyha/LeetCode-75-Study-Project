package exercise.yandex.dev.tasks.transaction;

import java.math.BigDecimal;

public record CardLimits(
        BigDecimal dayLimits,
        BigDecimal transactionLimits
) {

    public CardLimits {
        if (dayLimits == null || transactionLimits == null) {
            throw new IllegalArgumentException("cant be null");
        }
        if (dayLimits.compareTo(new BigDecimal(0)) < 1) {
            throw new IllegalArgumentException("day limits cant be not positive");
        }
        if (transactionLimits.compareTo(new BigDecimal(0)) < 1) {
            throw new IllegalArgumentException("transaction limits cant be not positive");
        }

        if (dayLimits.compareTo(transactionLimits) < 0) {
            throw new IllegalArgumentException("day limits cant be less than transactionLimits");
        }

    }

    public boolean isDayLimitsAccept(BigDecimal amount) {
        return dayLimits.compareTo(amount) >= 0;
    }


    public boolean isTransactionLimitsAccept(BigDecimal amount) {
        return transactionLimits.compareTo(amount) >= 0;
    }
}
