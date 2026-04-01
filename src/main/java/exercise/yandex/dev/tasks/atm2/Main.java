package exercise.yandex.dev.tasks.atm2;


/*
 * Банкомат.
 * Инициализируется набором купюр и умеет выдавать купюры для заданной суммы, либо отвечать отказом.
 * При выдаче купюры списываются с баланса банкомата.
 * Допустимые номиналы:
 * -  50₽, 100₽, 500₽, 1000₽, 5000₽.
 * -  20 EUR, 100 EUR, 500 EUR
 *
 * Каждая валюта обрабатывается отдельно, обмен валют банкоматом не поддерживается.
 *
 * Банкомат может использоваться многопоточно (например, резервирование выдачи денег из приложения). Поддержку многопоточности можно вынести в отдельную итерацию.

 *
 *WithdrawResult withdraw(int amount, Currency currency)
 * Currency(
 * Denomination(
 *
 */
public class Main {
    static void main() {
        System.out.println("ATM");

        CashStorage cashStorage = new CashStorage();
        ATMServce atmServce = new ATMServce(cashStorage);

        var test1 = atmServce.withdrawResult(100, Currency.RUB);
        var test2 = atmServce.withdrawResult(130, Currency.EURO);
        var test3 = atmServce.withdrawResult(7600, Currency.RUB);

        System.out.println(cashStorage.getCash());
        System.out.println(test2);
    }
}
