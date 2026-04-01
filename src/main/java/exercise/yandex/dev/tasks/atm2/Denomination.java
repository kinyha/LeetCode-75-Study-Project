package exercise.yandex.dev.tasks.atm2;

public enum Denomination {
    RUB_50(50, Currency.RUB),
    RUB_100(100, Currency.RUB),
    RUB_500(500, Currency.RUB),
    RUB_1000(1000, Currency.RUB),
    RUB_5000(5000, Currency.RUB),

    EURO_20(20, Currency.EURO),
    EURO_100(100, Currency.EURO),
    EURO_500(500, Currency.EURO);


    private final int amount;
    private final Currency currency;

    Denomination(int amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public int getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }
}
