package exercise.yandex.dev.tasks.atm;

import java.util.HashMap;
import java.util.Map;

public class CashStorage {
    private Map<Currency, Map<Denomination, Integer>> bank;

    public CashStorage() {
        Map<Currency, Map<Denomination, Integer>> rub = Map.of(
                Currency.RUB, Map.of(
                        Denomination.RUB_50, 31,
                        Denomination.RUB_100, 20,
                        Denomination.RUB_1000, 10,
                        Denomination.RUB_500, 12,
                        Denomination.RUB_5000, 10
                ),
                Currency.EUR, Map.of(
                        Denomination.EUR_20, 30,
                        Denomination.EUR_100, 50,
                        Denomination.EUR_500, 50
                ));
        bank = new HashMap<>(rub);
    }

    public Map<Currency, Map<Denomination, Integer>> getBank() {
        return bank;
    }

    public void newBank(Map<Denomination, Integer> newBank, Currency currency) {
        bank.put(currency, newBank);
    }

}
