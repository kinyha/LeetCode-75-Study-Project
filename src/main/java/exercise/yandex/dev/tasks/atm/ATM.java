package exercise.yandex.dev.tasks.atm;

import org.jetbrains.annotations.NotNull;

import java.util.*;


public class ATM {
    private final CashStorage cashStorage;

    public ATM(CashStorage cashStorage) {
        this.cashStorage = cashStorage;
    }

    @NotNull
    private static Map<Denomination, Integer> canTake(Map<Denomination, Integer> cash, long amount) {
        Map<Denomination, Integer> newMap = new HashMap<>();
        // find can take -> if can take
        for (Map.Entry<Denomination, Integer> entry : cash.entrySet()) {
            if (amount == 0) {
                break;
            }

            long count = amount / entry.getKey().getAmount();
            long min = Math.min(count, entry.getValue());
            if (min > 0) {
                newMap.put(entry.getKey(), (int) min);
                amount -= min * entry.getKey().getAmount();
            }

        }
        if (amount == 0) {
            return newMap;
        }

        return Map.of();
    }

    public WithdrawResult withdraw(Currency currency, long amount) {
        if (currency == null) {
            return WithdrawResult.fail("Concurenc cant be null");
        }
        if (amount <= 0) {
            return WithdrawResult.fail("Amount should be positive");

        }

        if (amount > getBalance(currency)) {
            return WithdrawResult.fail("ATM dont enough money");

        }

        var cash = cashStorage.getBank().getOrDefault(currency, Map.of());
        var sortedCash = sortBank(cash);

        var whatTake = takeMoney(sortedCash, amount, currency);

        if (whatTake.isEmpty()) {
            return WithdrawResult.fail("Need another paper");
        }

        return WithdrawResult.succes(whatTake);
    }

    Map<Denomination, Integer> takeMoney(Map<Denomination, Integer> cash, long amount, Currency currency) {
        var a = canTake(cash, amount);
        if (!a.isEmpty()) {
            //cash - a
            for (Map.Entry<Denomination, Integer> entry : a.entrySet()) {
                System.out.println(cash);
                 cash.put(entry.getKey(), cash.getOrDefault(entry.getKey(), 0) - entry.getValue());
                System.out.println(cash);
            }

            cashStorage.newBank(cash, currency);
        }
        return a;
    }

    public Integer getBalance(Currency currency) {
        var mapRub = cashStorage.getBank().getOrDefault(currency, Map.of());
        int amount = 0;
        for (Map.Entry<Denomination, Integer> entry : mapRub.entrySet()) {
            Denomination key = entry.getKey();
            Integer value = entry.getValue();

            amount += key.getAmount() * value;
        }
        return amount;
    }

    Map<Denomination, Integer> sortBank(Map<Denomination, Integer> bank) {
        SortedMap<Denomination, Integer> sortedMap = new TreeMap<>(Comparator.comparingInt(Denomination::getAmount).reversed());
        sortedMap.putAll(bank);
        return sortedMap;
    }


}
