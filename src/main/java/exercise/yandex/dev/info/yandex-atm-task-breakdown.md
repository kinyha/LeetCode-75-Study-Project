# Задача: Банкомат (ATM)

## 📋 Условие

```
Банкомат:
- Инициализируется набором купюр
- Выдаёт купюры для заданной суммы ИЛИ отказ
- При выдаче купюры списываются с баланса
- Номиналы RUB: 50, 100, 500, 1000, 5000
- Номиналы EUR: 20, 100, 500
- Валюты обрабатываются отдельно (без обмена)
- Многопоточность — отдельная итерация
```

---

## 🎯 Шаг 0: Уточнение требований (2-3 мин)

**Вопросы интервьюеру:**

| Вопрос | Варианты | Влияние на решение |
|--------|----------|-------------------|
| Алгоритм выдачи? | Жадный / Оптимальный | Жадный проще, оптимальный — DP |
| Что если нельзя выдать? | Отказ / Частичная выдача | Обработка ошибок |
| Формат результата? | List купюр / Map<номинал, кол-во> | Модель ответа |
| Приоритет номиналов? | Крупные сначала / минимум купюр | Логика выдачи |
| Можно ли выдать 0? | Да / Нет | Edge case |

**Предполагаемые ответы (типичные):**
- Жадный алгоритм (крупные купюры сначала)
- Полный отказ если точную сумму выдать нельзя
- Вернуть Map<номинал, количество>
- Минимизировать количество купюр

---

## 🏗️ Шаг 1: Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                        ATM (Service)                        │
│  withdraw(Currency, amount) → WithdrawResult                │
├─────────────────────────────────────────────────────────────┤
│                    CashStorage (Repository)                 │
│  Map<Currency, Map<Denomination, Integer>>                  │
├─────────────────────────────────────────────────────────────┤
│  Model: Currency, Denomination, WithdrawResult              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Шаг 2: MODEL (5-7 мин)

### Currency (enum)
```java
public enum Currency {
    RUB,
    EUR
}
```

### Denomination (номинал купюры)
```java
public enum Denomination {
    // RUB
    RUB_50(Currency.RUB, 50),
    RUB_100(Currency.RUB, 100),
    RUB_500(Currency.RUB, 500),
    RUB_1000(Currency.RUB, 1000),
    RUB_5000(Currency.RUB, 5000),
    
    // EUR
    EUR_20(Currency.EUR, 20),
    EUR_100(Currency.EUR, 100),
    EUR_500(Currency.EUR, 500);
    
    private final Currency currency;
    private final int value;
    
    Denomination(Currency currency, int value) {
        this.currency = currency;
        this.value = value;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public int getValue() {
        return value;
    }
    
    // Получить номиналы для валюты, отсортированные по убыванию (для жадного алгоритма)
    public static List<Denomination> getForCurrency(Currency currency) {
        return Arrays.stream(values())
            .filter(d -> d.currency == currency)
            .sorted(Comparator.comparingInt(Denomination::getValue).reversed())
            .toList();
    }
}
```

### WithdrawResult (результат операции)
```java
public record WithdrawResult(
    boolean success,
    Map<Denomination, Integer> banknotes,  // какие купюры выдать
    String errorMessage                     // причина отказа (если !success)
) {
    // Фабричные методы
    public static WithdrawResult success(Map<Denomination, Integer> banknotes) {
        return new WithdrawResult(true, banknotes, null);
    }
    
    public static WithdrawResult failure(String reason) {
        return new WithdrawResult(false, Map.of(), reason);
    }
    
    // Общая сумма выдачи (для проверки)
    public int getTotalAmount() {
        return banknotes.entrySet().stream()
            .mapToInt(e -> e.getKey().getValue() * e.getValue())
            .sum();
    }
}
```

---

## 🗄️ Шаг 3: REPOSITORY — CashStorage (5 мин)

```java
public class CashStorage {
    // Currency → (Denomination → количество)
    private final Map<Currency, Map<Denomination, Integer>> storage = new HashMap<>();
    
    public CashStorage() {
        // Инициализируем пустые Map для каждой валюты
        for (Currency currency : Currency.values()) {
            storage.put(currency, new HashMap<>());
        }
    }
    
    /**
     * Загрузить купюры в банкомат
     */
    public void load(Denomination denomination, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        storage.get(denomination.getCurrency())
               .merge(denomination, count, Integer::sum);
    }
    
    /**
     * Получить количество купюр данного номинала
     */
    public int getCount(Denomination denomination) {
        return storage.get(denomination.getCurrency())
                      .getOrDefault(denomination, 0);
    }
    
    /**
     * Получить все номиналы с количеством для валюты
     */
    public Map<Denomination, Integer> getAvailable(Currency currency) {
        return new HashMap<>(storage.get(currency));  // копия!
    }
    
    /**
     * Списать купюры (после успешной выдачи)
     */
    public void withdraw(Map<Denomination, Integer> banknotes) {
        for (var entry : banknotes.entrySet()) {
            Denomination denom = entry.getKey();
            int count = entry.getValue();
            
            Map<Denomination, Integer> currencyStorage = storage.get(denom.getCurrency());
            int available = currencyStorage.getOrDefault(denom, 0);
            
            if (available < count) {
                throw new IllegalStateException(
                    "Not enough " + denom + ": need " + count + ", have " + available
                );
            }
            
            currencyStorage.put(denom, available - count);
        }
    }
    
    /**
     * Общий баланс по валюте
     */
    public int getTotalBalance(Currency currency) {
        return storage.get(currency).entrySet().stream()
            .mapToInt(e -> e.getKey().getValue() * e.getValue())
            .sum();
    }
}
```

---

## ⚙️ Шаг 4: SERVICE — ATM (10 мин)

### Жадный алгоритм
```java
public class ATM {
    private final CashStorage storage;
    
    public ATM(CashStorage storage) {
        this.storage = storage;
    }
    
    /**
     * Выдать указанную сумму в указанной валюте
     */
    public WithdrawResult withdraw(Currency currency, int amount) {
        // Валидация
        if (amount <= 0) {
            return WithdrawResult.failure("Amount must be positive");
        }
        
        // Проверка общего баланса (быстрый отказ)
        if (amount > storage.getTotalBalance(currency)) {
            return WithdrawResult.failure("Insufficient funds in ATM");
        }
        
        // Жадный алгоритм: берём крупные купюры первыми
        Map<Denomination, Integer> result = new LinkedHashMap<>();  // сохраняем порядок
        int remaining = amount;
        
        for (Denomination denom : Denomination.getForCurrency(currency)) {
            if (remaining <= 0) break;
            
            int denomValue = denom.getValue();
            int available = storage.getCount(denom);
            
            if (denomValue <= remaining && available > 0) {
                // Сколько купюр этого номинала нужно и можно взять
                int needed = remaining / denomValue;
                int toTake = Math.min(needed, available);
                
                if (toTake > 0) {
                    result.put(denom, toTake);
                    remaining -= toTake * denomValue;
                }
            }
        }
        
        // Проверяем, удалось ли набрать точную сумму
        if (remaining > 0) {
            return WithdrawResult.failure(
                "Cannot dispense exact amount. Remaining: " + remaining + " " + currency
            );
        }
        
        // Списываем купюры
        storage.withdraw(result);
        
        return WithdrawResult.success(result);
    }
    
    /**
     * Загрузить купюры (для инкассации)
     */
    public void load(Denomination denomination, int count) {
        storage.load(denomination, count);
    }
    
    /**
     * Получить баланс по валюте
     */
    public int getBalance(Currency currency) {
        return storage.getTotalBalance(currency);
    }
}
```

---

## 🧪 Шаг 5: ТЕСТЫ (10 мин)

```java
class ATMTest {
    
    private CashStorage storage;
    private ATM atm;
    
    @BeforeEach
    void setUp() {
        storage = new CashStorage();
        atm = new ATM(storage);
    }
    
    // ===== HAPPY PATH =====
    
    @Test
    void shouldWithdrawExactAmount() {
        // given
        atm.load(Denomination.RUB_1000, 5);
        atm.load(Denomination.RUB_500, 10);
        atm.load(Denomination.RUB_100, 20);
        
        // when
        var result = atm.withdraw(Currency.RUB, 2600);
        
        // then
        assertTrue(result.success());
        assertEquals(2600, result.getTotalAmount());
        
        // Проверяем что выдали оптимально (минимум купюр)
        // 2600 = 2*1000 + 1*500 + 1*100
        assertEquals(2, result.banknotes().get(Denomination.RUB_1000));
        assertEquals(1, result.banknotes().get(Denomination.RUB_500));
        assertEquals(1, result.banknotes().get(Denomination.RUB_100));
    }
    
    @Test
    void shouldWithdrawUsingOnlyAvailableDenominations() {
        // given - только мелкие купюры
        atm.load(Denomination.RUB_100, 10);
        atm.load(Denomination.RUB_50, 20);
        
        // when
        var result = atm.withdraw(Currency.RUB, 350);
        
        // then
        assertTrue(result.success());
        assertEquals(350, result.getTotalAmount());
        // 350 = 3*100 + 1*50
        assertEquals(3, result.banknotes().get(Denomination.RUB_100));
        assertEquals(1, result.banknotes().get(Denomination.RUB_50));
    }
    
    @Test
    void shouldDeductFromBalance() {
        // given
        atm.load(Denomination.RUB_1000, 10);
        int initialBalance = atm.getBalance(Currency.RUB);
        
        // when
        atm.withdraw(Currency.RUB, 3000);
        
        // then
        assertEquals(initialBalance - 3000, atm.getBalance(Currency.RUB));
    }
    
    // ===== РАЗНЫЕ ВАЛЮТЫ =====
    
    @Test
    void shouldHandleEurCurrency() {
        // given
        atm.load(Denomination.EUR_500, 2);
        atm.load(Denomination.EUR_100, 5);
        atm.load(Denomination.EUR_20, 10);
        
        // when
        var result = atm.withdraw(Currency.EUR, 740);
        
        // then
        assertTrue(result.success());
        assertEquals(740, result.getTotalAmount());
        // 740 = 1*500 + 2*100 + 2*20
    }
    
    @Test
    void shouldNotMixCurrencies() {
        // given
        atm.load(Denomination.RUB_1000, 10);  // 10000 RUB
        atm.load(Denomination.EUR_100, 5);    // 500 EUR
        
        // when - пытаемся снять EUR, которых не хватает
        var result = atm.withdraw(Currency.EUR, 1000);
        
        // then - должен отказать, не трогая RUB
        assertFalse(result.success());
        assertEquals(10000, atm.getBalance(Currency.RUB));  // RUB не тронуты
    }
    
    // ===== ОТКАЗЫ =====
    
    @Test
    void shouldFailWhenInsufficientFunds() {
        // given
        atm.load(Denomination.RUB_1000, 2);  // всего 2000
        
        // when
        var result = atm.withdraw(Currency.RUB, 5000);
        
        // then
        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("Insufficient"));
    }
    
    @Test
    void shouldFailWhenCannotMakeExactAmount() {
        // given - только крупные купюры
        atm.load(Denomination.RUB_1000, 10);
        
        // when - пытаемся снять некратную сумму
        var result = atm.withdraw(Currency.RUB, 1500);
        
        // then
        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("Cannot dispense exact"));
        // Баланс не изменился
        assertEquals(10000, atm.getBalance(Currency.RUB));
    }
    
    @Test
    void shouldFailWhenAmountNotPositive() {
        // given
        atm.load(Denomination.RUB_1000, 10);
        
        // when/then
        assertFalse(atm.withdraw(Currency.RUB, 0).success());
        assertFalse(atm.withdraw(Currency.RUB, -100).success());
    }
    
    @Test
    void shouldFailWhenNoBanknotesLoaded() {
        // when - пустой банкомат
        var result = atm.withdraw(Currency.RUB, 1000);
        
        // then
        assertFalse(result.success());
    }
    
    // ===== EDGE CASES =====
    
    @Test
    void shouldUseAllAvailableBanknotes() {
        // given - ровно столько, сколько нужно
        atm.load(Denomination.RUB_1000, 2);
        atm.load(Denomination.RUB_500, 1);
        
        // when
        var result = atm.withdraw(Currency.RUB, 2500);
        
        // then
        assertTrue(result.success());
        assertEquals(0, atm.getBalance(Currency.RUB));  // всё выдали
    }
    
    @Test
    void shouldHandleGreedyAlgorithmLimitation() {
        // given - пример где жадный алгоритм может не найти решение
        // Допустим номиналы: 500, 100, 50
        // Сумма: 600
        // Жадный: 500 + ? (нет 100, есть только 50) = 500 + 50 + 50 = 600 ✓
        
        atm.load(Denomination.RUB_500, 1);
        atm.load(Denomination.RUB_50, 10);
        // НЕТ купюр по 100
        
        // when
        var result = atm.withdraw(Currency.RUB, 600);
        
        // then
        assertTrue(result.success());
        assertEquals(600, result.getTotalAmount());
    }
    
    @Test
    void shouldHandleMultipleWithdrawals() {
        // given
        atm.load(Denomination.RUB_1000, 5);
        
        // when - несколько снятий подряд
        var result1 = atm.withdraw(Currency.RUB, 2000);
        var result2 = atm.withdraw(Currency.RUB, 2000);
        var result3 = atm.withdraw(Currency.RUB, 2000);  // должен отказать
        
        // then
        assertTrue(result1.success());
        assertTrue(result2.success());
        assertFalse(result3.success());
        assertEquals(1000, atm.getBalance(Currency.RUB));  // осталась 1 купюра
    }
}
```

---

## 🔒 Шаг 6: МНОГОПОТОЧНОСТЬ (если время есть)

### Вариант 1: synchronized на методе
```java
public class ATMThreadSafe {
    private final CashStorage storage;
    
    public ATMThreadSafe(CashStorage storage) {
        this.storage = storage;
    }
    
    // Простейший вариант — synchronized на весь метод
    public synchronized WithdrawResult withdraw(Currency currency, int amount) {
        // ... та же логика
    }
    
    public synchronized void load(Denomination denomination, int count) {
        storage.load(denomination, count);
    }
}
```

### Вариант 2: Lock на валюту (лучше параллелизм)
```java
public class ATMWithLocks {
    private final CashStorage storage;
    private final Map<Currency, ReentrantLock> locks = new EnumMap<>(Currency.class);
    
    public ATMWithLocks(CashStorage storage) {
        this.storage = storage;
        for (Currency c : Currency.values()) {
            locks.put(c, new ReentrantLock());
        }
    }
    
    public WithdrawResult withdraw(Currency currency, int amount) {
        ReentrantLock lock = locks.get(currency);
        lock.lock();
        try {
            // ... логика выдачи
        } finally {
            lock.unlock();
        }
    }
}
```

### Вариант 3: ConcurrentHashMap + AtomicInteger
```java
public class CashStorageConcurrent {
    private final Map<Currency, ConcurrentHashMap<Denomination, AtomicInteger>> storage;
    
    // Используем compareAndSet для атомарного списания
    public boolean tryWithdraw(Denomination denom, int count) {
        AtomicInteger available = storage.get(denom.getCurrency()).get(denom);
        while (true) {
            int current = available.get();
            if (current < count) {
                return false;  // не хватает
            }
            if (available.compareAndSet(current, current - count)) {
                return true;  // успешно списали
            }
            // Иначе кто-то изменил — пробуем снова
        }
    }
}
```

---

## 📊 Сложные случаи (обсуждение)

### Проблема жадного алгоритма
```
Номиналы: 25, 10, 1
Сумма: 30
Жадный: 25 + 1 + 1 + 1 + 1 + 1 = 6 купюр
Оптимальный: 10 + 10 + 10 = 3 купюры

НО! Для стандартных номиналов (50, 100, 500, 1000, 5000) жадный работает корректно!
```

### Если нужен оптимальный алгоритм (DP)
```java
// Динамическое программирование — минимум купюр
public Map<Denomination, Integer> findOptimal(Currency currency, int amount) {
    List<Denomination> denoms = Denomination.getForCurrency(currency);
    int[] dp = new int[amount + 1];
    int[] parent = new int[amount + 1];  // для восстановления ответа
    Arrays.fill(dp, Integer.MAX_VALUE);
    dp[0] = 0;
    
    for (int i = 1; i <= amount; i++) {
        for (Denomination d : denoms) {
            int v = d.getValue();
            if (v <= i && dp[i - v] != Integer.MAX_VALUE) {
                if (dp[i - v] + 1 < dp[i]) {
                    dp[i] = dp[i - v] + 1;
                    parent[i] = v;
                }
            }
        }
    }
    
    // Восстанавливаем ответ
    // ...
}
```

---

## ✅ Чеклист задачи

```
□ Уточнить: жадный или оптимальный алгоритм?
□ Уточнить: формат результата (Map или List)?
□ Уточнить: что при невозможности выдать?

□ enum Currency (RUB, EUR)
□ enum Denomination с currency + value + static getForCurrency()
□ record WithdrawResult с фабричными методами ok()/failure()

□ CashStorage: load(), getCount(), withdraw(), getTotalBalance()
□ ATM: withdraw(), load(), getBalance()

□ Тесты:
  □ Happy path — точная сумма
  □ Разные валюты отдельно
  □ Insufficient funds
  □ Cannot make exact amount
  □ Negative/zero amount
  □ Multiple withdrawals

□ (Опционально) Многопоточность: synchronized или Lock по валюте
```

---

## 📝 Итоговая структура файлов

```
src/
├── main/java/
│   ├── model/
│   │   ├── Currency.java
│   │   ├── Denomination.java
│   │   └── WithdrawResult.java
│   ├── storage/
│   │   └── CashStorage.java
│   └── service/
│       └── ATM.java
└── test/java/
    └── service/
        └── ATMTest.java
```

**Время: ~40-45 минут на код + тесты**
