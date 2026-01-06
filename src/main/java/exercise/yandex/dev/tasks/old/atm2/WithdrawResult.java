package exercise.yandex.dev.tasks.old.atm2;

import java.util.Map;

public record WithdrawResult(
        boolean result,
        String message,
        Map<Denomination,Integer> bank
) {
    static WithdrawResult success(Map<Denomination,Integer> bank) {
        return new WithdrawResult(true, null, bank);
    }

    static WithdrawResult failure(String message) {
        return  new WithdrawResult(false, message, Map.of());
    }
}
