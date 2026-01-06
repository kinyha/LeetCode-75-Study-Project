package exercise.yandex.dev.tasks.old.atm2;

import java.util.*;
import java.util.stream.Collectors;

public class ATMServce {
    private final CashStorage cashStorage;

    public ATMServce(CashStorage cashStorage) {
        this.cashStorage = cashStorage;
    }





    public WithdrawResult withdrawResult(int amount, Currency currency) {
        if (amount <= 0) {
            WithdrawResult.failure("Amount cant negative or 0");
        }
        if (currency == null) {
            WithdrawResult.failure("Currency null");
        }
        //1) Check sum
        //2) Check can give
        //3)take
        //
        var cash = cashStorage.getCash();

        Map<Denomination, Integer> cashCurrency = cash.entrySet().stream().filter(e -> e.getKey().getCurrency() == currency)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Denomination, Integer> sortedCash = new TreeMap<Denomination,Integer>(Comparator.comparingInt(Denomination::getAmount).reversed());
        sortedCash.putAll(cashCurrency);

        Map<Denomination,Integer> resultMap = new HashMap<>();

        for(Map.Entry<Denomination, Integer> entry : sortedCash.entrySet()) {
            int numDenom = amount / entry.getKey().getAmount();
            int numHas = Math.min(numDenom, entry.getValue());
            amount -= numHas * entry.getKey().getAmount();
            resultMap.put(entry.getKey(),entry.getValue() - numHas);
        }
        if (amount == 0) return WithdrawResult.success(resultMap);
        return WithdrawResult.failure("Cant take");
    }

//    private WithdrawResult take(Map<Denomination, Integer> cash, Currency currency, int amount) {
//        //sort cash
//        Map<Denomination, Integer> cashCurrency = cash.entrySet().stream().filter(e -> e.getKey().getCurrency() == currency)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        Map<Denomination, Integer> sortedCash = new TreeMap<Denomination,Integer>(Comparator.comparingInt(Denomination::getAmount).reversed()).putAll(cashCurrency);
//
//        Map<Denomination,Integer> resultMap = new HashMap<>();
//        for(Map.Entry<Denomination, Integer> entry : sortedCash.entrySet()) {
//            int numDenom = amount / entry.getKey().getAmount();
//            int numHas = Math.min(numDenom, entry.getValue());
//            amount -= numHas * entry.getKey().getAmount();
//            resultMap.put(entry.getKey(),entry.getValue() - numHas);
//            if (amount == 0) return WithdrawResult.success(resultMap);
//        }
//        return WithdrawResult.failure("Cant take");
//    }
}
