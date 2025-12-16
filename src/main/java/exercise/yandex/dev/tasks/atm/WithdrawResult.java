package exercise.yandex.dev.tasks.atm;

import java.util.Map;

public record WithdrawResult(
        boolean result,
        Map<Denomination, Integer> banknotes,
        String message
) {

    static WithdrawResult succes(Map<Denomination, Integer> banknotes) {
        return new WithdrawResult(true, banknotes, null);
    }

    static WithdrawResult fail(String reason) {
        return new WithdrawResult(false, Map.of(), reason);
    }
}
