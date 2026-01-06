package exercise.yandex.dev.tasks.old.atm2;

import java.util.HashMap;
import java.util.Map;

public class CashStorage {

    Map<Denomination, Integer> getCash() {
        Map<Denomination, Integer> bank = new HashMap<>();

        bank.put(Denomination.RUB_50, 10);
        bank.put(Denomination.RUB_100, 10);
        bank.put(Denomination.RUB_500, 10);
        bank.put(Denomination.RUB_1000, 10);
        bank.put(Denomination.RUB_5000, 10);

        bank.put(Denomination.EURO_20, 10);
        bank.put(Denomination.EURO_100, 10);
        bank.put(Denomination.EURO_500, 10);
        return bank;
    }
}
