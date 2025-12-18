# Яндекс Dev: Задачи для практики

## Чеклист

```
НАЧАЛО:
□ Уточни требования (что возвращаем? edge cases?)
□ Схема классов на бумаге
□ Проговори план

КОД:
□ record + фабричные методы ok()/fail()
□ Map + computeIfAbsent/getOrDefault
□ Сначала ВСЕ проверки, потом действие

ТЕСТЫ:
□ Happy path
□ Все причины отказа
□ Граничные случаи
```

---

## Задача 1: Банкомат (ATM)

Выдать запрошенную сумму минимальным количеством купюр.
Банкомат имеет ограниченный набор купюр разных номиналов.

```java
WithdrawResult withdraw(int amount)
```

---

## Задача 2: Корзина с промокодами

Интернет-магазин. Рассчитать итоговую стоимость корзины с учётом промокодов.
Промокод даёт скидку: процент или фиксированную сумму.
Ограничения: мин. сумма заказа, категории товаров, один промокод на заказ.

```java
CartResult calculate(Cart cart, String promoCode)
```

---

## Задача 3: Проверка платежа

Платёжный сервис. Проверить возможность платежа.
Проверки: статус карты, лимит на операцию, дневной лимит.

```java
PaymentResult check(PaymentRequest request)
void confirm(String paymentId)
```

---

## Задача 4: Валидатор данных

Сервис регистрации. Валидация пользовательских данных.
Проверить: номер карты (алгоритм Луна), телефон, email.
Вернуть все ошибки сразу, не останавливаясь на первой.

```java
ValidationResult validate(UserData data)
```

---

## Задача 5: Калькулятор доставки

Рассчитать стоимость доставки заказа.
Зависит от: веса, габаритов, расстояния, тарифа (эконом/экспресс).
Хрупкий груз — наценка. Превышение габаритов — отказ.

```java
DeliveryResult calculate(Package pkg, String tariff, int distanceKm)
```

---
---
---

# ПОДСКАЗКИ

## Задача 1: Банкомат (ATM)

**Дано:**

```java
record WithdrawResult(boolean success, Map<Integer, Integer> bills, String error) {
    static WithdrawResult ok(Map<Integer, Integer> bills) {
        return new WithdrawResult(true, bills, null);
    }
    static WithdrawResult fail(String error) {
        return new WithdrawResult(false, Map.of(), error);
    }
}
```

**Хранение:**
```java
// номинал -> количество купюр
Map<Integer, Integer> cassettes = new LinkedHashMap<>();
// LinkedHashMap чтобы сохранить порядок (от большего к меньшему)

public ATM() {
    cassettes.put(5000, 10);
    cassettes.put(2000, 10);
    cassettes.put(1000, 10);
    cassettes.put(500, 10);
    cassettes.put(100, 10);
}
```

**Жадный алгоритм:**
```java
WithdrawResult withdraw(int amount) {
    if (amount <= 0) return WithdrawResult.fail("invalid_amount");
    if (amount % 100 != 0) return WithdrawResult.fail("not_multiple_of_100");

    Map<Integer, Integer> result = new LinkedHashMap<>();
    int remaining = amount;

    for (var entry : cassettes.entrySet()) {
        int nominal = entry.getKey();
        int available = entry.getValue();

        int need = remaining / nominal;
        int take = Math.min(need, available);

        if (take > 0) {
            result.put(nominal, take);
            remaining -= nominal * take;
        }
    }

    if (remaining > 0) {
        return WithdrawResult.fail("insufficient_bills");
    }

    // Списать купюры
    result.forEach((nom, cnt) ->
        cassettes.merge(nom, -cnt, Integer::sum));

    return WithdrawResult.ok(result);
}
```

**Edge cases:**
- amount <= 0
- Не кратно минимальному номиналу (100)
- Сумма больше чем есть в банкомате
- Нужных номиналов нет (например 300₽, а есть только 500 и 1000)
- После выдачи кассеты обновляются

**Пример:**
```
withdraw(7600)
→ {5000: 1, 2000: 1, 500: 1, 100: 1}
```

---

## Задача 2: Корзина с промокодами

**Дано:**

```java
record Product(String id, String name, int price, String category)
record Cart(List<Product> items)

record PromoCode(
    String code,
    PromoType type,        // PERCENT, FIXED
    int value,             // 10 = 10% или 10₽
    int minOrderSum,       // мин. сумма заказа
    Set<String> categories // пустой = все категории
)

enum PromoType { PERCENT, FIXED }

record CartResult(int originalSum, int discount, int finalSum, String error) {
    static CartResult ok(int original, int discount) {
        return new CartResult(original, discount, original - discount, null);
    }
    static CartResult fail(String error, int original) {
        return new CartResult(original, 0, original, error);
    }
}
```

**Хранение:**
```java
Map<String, PromoCode> promoCodes = new HashMap<>();
```

**Расчёт:**
```java
CartResult calculate(Cart cart, String code) {
    int originalSum = cart.items().stream()
        .mapToInt(Product::price)
        .sum();

    if (code == null || code.isEmpty()) {
        return CartResult.ok(originalSum, 0);
    }

    PromoCode promo = promoCodes.get(code);
    if (promo == null) {
        return CartResult.fail("promo_not_found", originalSum);
    }

    if (originalSum < promo.minOrderSum()) {
        return CartResult.fail("min_sum_not_reached", originalSum);
    }

    // Сумма товаров подходящих категорий
    int eligibleSum = cart.items().stream()
        .filter(p -> promo.categories().isEmpty()
                  || promo.categories().contains(p.category()))
        .mapToInt(Product::price)
        .sum();

    if (eligibleSum == 0) {
        return CartResult.fail("no_eligible_products", originalSum);
    }

    int discount = switch (promo.type()) {
        case PERCENT -> eligibleSum * promo.value() / 100;
        case FIXED -> Math.min(promo.value(), eligibleSum);
    };

    return CartResult.ok(originalSum, discount);
}
```

**Edge cases:**
- Пустая корзина
- Промокод не существует
- Сумма меньше минимальной
- Нет товаров нужной категории
- FIXED скидка больше суммы товаров
- Промокод на категорию "электроника", а в корзине только "одежда"

---

## Задача 3: Проверка платежа

**Дано:**

```java
record Card(String id, CardStatus status, int singleLimit, int dailyLimit)
enum CardStatus { ACTIVE, BLOCKED, EXPIRED }

record PaymentRequest(String cardId, int amount)

record PaymentResult(boolean allowed, String reason) {
    static PaymentResult ok() { return new PaymentResult(true, null); }
    static PaymentResult denied(String r) { return new PaymentResult(false, r); }
}
```

**Хранение:**
```java
Map<String, Card> cards = new HashMap<>();
Map<String, Integer> dailySpent = new HashMap<>(); // cardId -> сумма за сегодня
```

**Проверка:**
```java
PaymentResult check(PaymentRequest req) {
    Card card = cards.get(req.cardId());

    // 1. Карта существует?
    if (card == null) {
        return PaymentResult.denied("card_not_found");
    }

    // 2. Статус карты
    if (card.status() != CardStatus.ACTIVE) {
        return PaymentResult.denied("card_" + card.status().name().toLowerCase());
    }

    // 3. Лимит на операцию
    if (req.amount() > card.singleLimit()) {
        return PaymentResult.denied("single_limit_exceeded");
    }

    // 4. Дневной лимит
    int spent = dailySpent.getOrDefault(req.cardId(), 0);
    if (spent + req.amount() > card.dailyLimit()) {
        return PaymentResult.denied("daily_limit_exceeded");
    }

    return PaymentResult.ok();
}

void confirm(String cardId, int amount) {
    dailySpent.merge(cardId, amount, Integer::sum);
}
```

**Edge cases:**
- Карта не найдена
- Карта заблокирована / истекла
- Превышен лимит на одну операцию
- Превышен дневной лимит (несколько операций)
- amount <= 0

**Важно:** `check()` не списывает лимит, только `confirm()`. Это позволяет откатить операцию.

---

## Задача 4: Валидатор данных

**Дано:**

```java
record UserData(String cardNumber, String phone, String email)

record ValidationResult(boolean valid, List<String> errors) {
    static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }
    static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }
}
```

**Алгоритм Луна (проверка номера карты):**
```java
boolean isValidCardNumber(String number) {
    if (number == null || !number.matches("\\d{16}")) {
        return false;
    }

    int sum = 0;
    boolean alternate = false;

    for (int i = number.length() - 1; i >= 0; i--) {
        int digit = number.charAt(i) - '0';

        if (alternate) {
            digit *= 2;
            if (digit > 9) digit -= 9;
        }

        sum += digit;
        alternate = !alternate;
    }

    return sum % 10 == 0;
}
```

**Валидация:**
```java
ValidationResult validate(UserData data) {
    List<String> errors = new ArrayList<>();

    // Карта
    if (!isValidCardNumber(data.cardNumber())) {
        errors.add("invalid_card_number");
    }

    // Телефон: +7XXXXXXXXXX или 8XXXXXXXXXX
    if (data.phone() == null ||
        !data.phone().matches("(\\+7|8)\\d{10}")) {
        errors.add("invalid_phone");
    }

    // Email: простая проверка
    if (data.email() == null ||
        !data.email().matches(".+@.+\\..+")) {
        errors.add("invalid_email");
    }

    return errors.isEmpty()
        ? ValidationResult.ok()
        : ValidationResult.fail(errors);
}
```

**Edge cases:**
- null значения
- Пустые строки
- Карта не проходит алгоритм Луна
- Карта не 16 цифр
- Телефон с пробелами/скобками
- Email без точки в домене

**Важно:** Собрать ВСЕ ошибки, не останавливаться на первой.

---

## Задача 5: Калькулятор доставки

**Дано:**

```java
record Package(
    double weightKg,
    int lengthCm, int widthCm, int heightCm,
    boolean fragile
)

record Tariff(
    String name,
    int basePrice,
    int pricePerKg,
    int pricePerKm,
    int maxWeightKg,
    int maxDimensionCm  // макс. любая сторона
)

record DeliveryResult(boolean possible, int price, String error) {
    static DeliveryResult ok(int price) {
        return new DeliveryResult(true, price, null);
    }
    static DeliveryResult fail(String error) {
        return new DeliveryResult(false, 0, error);
    }
}
```

**Хранение:**
```java
Map<String, Tariff> tariffs = new HashMap<>();

public DeliveryService() {
    tariffs.put("economy", new Tariff("economy", 100, 10, 5, 30, 150));
    tariffs.put("express", new Tariff("express", 300, 20, 15, 20, 100));
}
```

**Расчёт:**
```java
DeliveryResult calculate(Package pkg, String tariffName, int distanceKm) {
    Tariff tariff = tariffs.get(tariffName);
    if (tariff == null) {
        return DeliveryResult.fail("unknown_tariff");
    }

    // Проверка веса
    if (pkg.weightKg() > tariff.maxWeightKg()) {
        return DeliveryResult.fail("weight_exceeded");
    }

    // Проверка габаритов
    int maxSide = Math.max(pkg.lengthCm(),
                  Math.max(pkg.widthCm(), pkg.heightCm()));
    if (maxSide > tariff.maxDimensionCm()) {
        return DeliveryResult.fail("dimensions_exceeded");
    }

    // Расчёт
    int price = tariff.basePrice()
        + (int)(pkg.weightKg() * tariff.pricePerKg())
        + distanceKm * tariff.pricePerKm();

    // Хрупкий груз +50%
    if (pkg.fragile()) {
        price = price * 150 / 100;
    }

    return DeliveryResult.ok(price);
}
```

**Edge cases:**
- Неизвестный тариф
- Вес превышает максимум
- Габариты превышают максимум
- Расстояние 0 или отрицательное
- Хрупкий груз (наценка)
- Объёмный вес (опционально): `length * width * height / 5000`

---

## Паттерны

**Result-тип:**
```java
record Result(boolean success, Data data, String error) {
    static Result ok(Data d) { return new Result(true, d, null); }
    static Result fail(String e) { return new Result(false, null, e); }
}
```

**Сбор всех ошибок:**
```java
List<String> errors = new ArrayList<>();
if (condition1) errors.add("error1");
if (condition2) errors.add("error2");
return errors.isEmpty() ? ok() : fail(errors);
```

**Порядок проверок:**
```java
// 1. Существование (карта, пользователь, тариф)
// 2. Статус (активен, не заблокирован)
// 3. Валидация данных (формат, диапазон)
// 4. Бизнес-правила (лимиты, ограничения)
// 5. Действие (списание, сохранение)
```
