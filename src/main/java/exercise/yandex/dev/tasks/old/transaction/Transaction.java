package exercise.yandex.dev.tasks.transaction;

import java.math.BigDecimal;

public record Transaction(
        BigDecimal amount,
        CardLimits cardLimits,
        CardStatus cardStatus
) {

    public Transaction {
        if (amount == null || amount.compareTo(new BigDecimal(0)) < 1) {
            throw new IllegalArgumentException("amount cant be null or negative");
        }

        if (cardLimits == null) {
            throw new IllegalArgumentException("cardLimits cant be null");
        }
        if (cardStatus == null) {
            throw new IllegalArgumentException("cardStatus cant be null");
        }
    }
}
