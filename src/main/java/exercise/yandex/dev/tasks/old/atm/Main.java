package exercise.yandex.dev.tasks.atm;



//ATM (Service) → CashStorage (Repository) → Model (Currency, Denomination, WithdrawResult)
/*
/**
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
**/
import java.util.Map;

public class Main {
    void main() {
        CashStorage cashStorage = new CashStorage();
        ATM atm = new ATM(cashStorage);

        //System.out.println(atm.getBalance(Concurency.RUB));

        var a = cashStorage.getBank().getOrDefault(Currency.RUB, Map.of());
        //System.out.println(a);
        var sortA = atm.sortBank(a);
        System.out.println(atm.getBalance(Currency.RUB));
        System.out.println(atm.getBalance(Currency.EUR));
//        System.out.println(atm.takeMoney(sortA, 63600));

        var res = atm.withdraw(Currency.EUR, 0);
        System.out.println(res);
    }
}
