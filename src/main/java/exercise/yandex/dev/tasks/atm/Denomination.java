package exercise.yandex.dev.tasks.atm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum Denomination {
    RUB_50(Currency.RUB, 50),
    RUB_100(Currency.RUB, 100),
    RUB_500(Currency.RUB, 500),
    RUB_1000(Currency.RUB, 1000),
    RUB_5000(Currency.RUB, 5000),

    EUR_20(Currency.EUR, 20),
    EUR_100(Currency.EUR, 100),
    EUR_500(Currency.EUR, 500);

    private final Currency currency;
    private final Integer amount;

    Denomination(Currency currency, Integer amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public Currency getConcurency() {
        return currency;
    }

    public Integer getAmount() {
        return amount;
    }

    public static List<Denomination> getForCurrency(Currency currency) {
        return Arrays.stream(values())
                .filter(d -> d.currency == currency)
                .sorted(Comparator.comparingInt(Denomination::getAmount).reversed())
                .toList();
    }
}
