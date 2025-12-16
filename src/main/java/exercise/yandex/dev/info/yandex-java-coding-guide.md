# Яндекс: Java Coding Section Guide

## Формат секции
- **Время**: 1 час на код + 30 мин организация
- **Структура**: 10 мин планирование → 30 мин код → 20 мин тесты/дебаг/обсуждение
- **Результат**: 3-4 класса + unit-тесты
- **Ограничения**: без БД, без внешних сервисов, in-memory хранилище, без AI-ассистентов

---

## 1. Архитектура типовой задачи

### Слои приложения (3-4 класса)
```
┌─────────────────────────────────────┐
│  Service (бизнес-логика)            │
├─────────────────────────────────────┤
│  Repository (in-memory хранилище)   │
├─────────────────────────────────────┤
│  Model/DTO (сущности + результаты)  │
└─────────────────────────────────────┘
```

### Типичная структура проекта
```
src/
├── main/java/
│   ├── model/
│   │   ├── Payment.java
│   │   ├── UserLimits.java
│   │   └── ValidationResult.java
│   ├── repository/
│   │   └── PaymentRepository.java
│   └── service/
│       └── LimitService.java
└── test/java/
    └── service/
        └── LimitServiceTest.java
```

---

## 2. Model Layer — Сущности

### Record для неизменяемых данных (Java 16+)
```java
// Платеж
public record Payment(
    long amount,
    Instant timestamp
) {}

// Лимиты пользователя
public record UserLimits(
    long dailyLimit,
    long maxSingleOperation
) {}

// Результат валидации
public record ValidationResult(
    boolean allowed,
    String reason  // null если allowed=true
) {
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult denied(String reason) {
        return new ValidationResult(false, reason);
    }
}
```

### Классический вариант (если record недоступен)
```java
public final class Payment {
    private final long amount;
    private final Instant timestamp;
    
    public Payment(long amount, Instant timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    public long getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
}
```

---

## 3. Repository Layer — In-Memory хранилище

### HashMap-based repository
```java
public class PaymentRepository {
    private final Map<String, List<Payment>> paymentsByUser = new HashMap<>();
    
    public void save(String userId, Payment payment) {
        paymentsByUser
            .computeIfAbsent(userId, k -> new ArrayList<>())
            .add(payment);
    }
    
    public List<Payment> getPayments(String userId) {
        return paymentsByUser.getOrDefault(userId, List.of());
    }
    
    // Фильтрация по времени
    public List<Payment> getPaymentsAfter(String userId, Instant after) {
        return getPayments(userId).stream()
            .filter(p -> p.timestamp().isAfter(after))
            .toList();
    }
}
```

### С лимитами пользователей
```java
public class UserLimitsRepository {
    private final Map<String, UserLimits> limits = new HashMap<>();
    
    public void setLimits(String userId, UserLimits userLimits) {
        limits.put(userId, userLimits);
    }
    
    public Optional<UserLimits> getLimits(String userId) {
        return Optional.ofNullable(limits.get(userId));
    }
}
```

---

## 4. Service Layer — Бизнес-логика

### Шаблон сервиса
```java
public class LimitService {
    private final PaymentRepository paymentRepository;
    private final UserLimitsRepository limitsRepository;
    private final Clock clock; // для тестируемости!
    
    public LimitService(PaymentRepository paymentRepository,
                       UserLimitsRepository limitsRepository,
                       Clock clock) {
        this.paymentRepository = paymentRepository;
        this.limitsRepository = limitsRepository;
        this.clock = clock;
    }
    
    // Конструктор для production
    public LimitService(PaymentRepository paymentRepository,
                       UserLimitsRepository limitsRepository) {
        this(paymentRepository, limitsRepository, Clock.systemUTC());
    }
    
    public ValidationResult validatePayment(String userId, Payment payment) {
        var limits = limitsRepository.getLimits(userId);
        if (limits.isEmpty()) {
            return ValidationResult.denied("User limits not found");
        }
        
        var userLimits = limits.get();
        
        // Проверка максимальной суммы операции
        if (payment.amount() > userLimits.maxSingleOperation()) {
            return ValidationResult.denied("Exceeds max single operation limit");
        }
        
        // Проверка дневного лимита
        var todayStart = LocalDate.now(clock)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
            
        long todayTotal = paymentRepository.getPaymentsAfter(userId, todayStart)
            .stream()
            .mapToLong(Payment::amount)
            .sum();
        
        if (todayTotal + payment.amount() > userLimits.dailyLimit()) {
            return ValidationResult.denied("Exceeds daily limit");
        }
        
        return ValidationResult.ok();
    }
}
```

---

## 5. Java Time API — Основные операции

### Получение текущего времени
```java
// Текущий момент (UTC)
Instant now = Instant.now();
Instant nowWithClock = clock.instant(); // через Clock для тестов

// Текущая дата
LocalDate today = LocalDate.now();
LocalDate todayWithClock = LocalDate.now(clock);

// Дата-время с зоной
ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
```

### Начало дня / конец дня
```java
// Начало дня (00:00:00)
Instant startOfDay = LocalDate.now(clock)
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant();

// Конец дня (23:59:59.999...)
Instant endOfDay = LocalDate.now(clock)
    .atTime(LocalTime.MAX)
    .toInstant(ZoneOffset.UTC);
```

### Вычисления с датами
```java
// N дней назад
Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

// Начало текущего месяца
Instant monthStart = LocalDate.now()
    .withDayOfMonth(1)
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant();

// Период между датами
Duration duration = Duration.between(start, end);
long hours = duration.toHours();
```

### Clock для тестируемости
```java
// В тестах - фиксированное время
Clock fixedClock = Clock.fixed(
    Instant.parse("2025-01-15T10:00:00Z"),
    ZoneOffset.UTC
);

// В production
Clock systemClock = Clock.systemUTC();
```

---

## 6. Stream API — Частые паттерны

### Фильтрация + сумма
```java
long total = payments.stream()
    .filter(p -> p.timestamp().isAfter(startOfDay))
    .mapToLong(Payment::amount)
    .sum();
```

### Группировка
```java
// По дате
Map<LocalDate, List<Payment>> byDate = payments.stream()
    .collect(Collectors.groupingBy(
        p -> LocalDate.ofInstant(p.timestamp(), ZoneOffset.UTC)
    ));

// По дате с суммой
Map<LocalDate, Long> sumByDate = payments.stream()
    .collect(Collectors.groupingBy(
        p -> LocalDate.ofInstant(p.timestamp(), ZoneOffset.UTC),
        Collectors.summingLong(Payment::amount)
    ));
```

### Поиск
```java
// Первый элемент
Optional<Payment> first = payments.stream()
    .filter(p -> p.amount() > 1000)
    .findFirst();

// Любой (для параллельных стримов)
Optional<Payment> any = payments.parallelStream()
    .filter(p -> p.amount() > 1000)
    .findAny();
```

### Проверки
```java
// Все соответствуют
boolean allPositive = payments.stream()
    .allMatch(p -> p.amount() > 0);

// Хотя бы один
boolean hasLarge = payments.stream()
    .anyMatch(p -> p.amount() > 10000);

// Ни один не соответствует
boolean noNegative = payments.stream()
    .noneMatch(p -> p.amount() < 0);
```

### Max/Min
```java
Optional<Payment> largest = payments.stream()
    .max(Comparator.comparingLong(Payment::amount));

// С default значением
long maxAmount = payments.stream()
    .mapToLong(Payment::amount)
    .max()
    .orElse(0L);
```

### Сортировка
```java
// По одному полю
List<Payment> sorted = payments.stream()
    .sorted(Comparator.comparingLong(Payment::amount))
    .toList();

// По нескольким полям
List<Payment> sorted = payments.stream()
    .sorted(Comparator
        .comparing(Payment::timestamp)
        .thenComparingLong(Payment::amount))
    .toList();

// В обратном порядке
List<Payment> sortedDesc = payments.stream()
    .sorted(Comparator.comparingLong(Payment::amount).reversed())
    .toList();
```

---

## 7. Collections — Quick Reference

### List операции
```java
// Создание
List<String> immutable = List.of("a", "b", "c");
List<String> mutable = new ArrayList<>(List.of("a", "b"));

// Добавление с дефолтом
map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

// Безопасное получение
List<Payment> list = map.getOrDefault(userId, List.of());
```

### Map операции
```java
// Создание
Map<String, Integer> map = Map.of("a", 1, "b", 2);
Map<String, Integer> mutable = new HashMap<>(Map.of("a", 1));

// Условное добавление
map.putIfAbsent(key, defaultValue);

// Обновление
map.merge(key, 1, Integer::sum); // count++

// Compute
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
```

### Set операции
```java
Set<String> set = new HashSet<>();
set.add("a");
boolean added = set.add("a"); // false - уже есть

// LinkedHashSet сохраняет порядок вставки
Set<String> ordered = new LinkedHashSet<>();
```

---

## 8. Unit Tests — JUnit 5

### Структура теста
```java
class LimitServiceTest {
    
    private PaymentRepository paymentRepository;
    private UserLimitsRepository limitsRepository;
    private Clock fixedClock;
    private LimitService service;
    
    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        limitsRepository = new UserLimitsRepository();
        fixedClock = Clock.fixed(
            Instant.parse("2025-01-15T10:00:00Z"),
            ZoneOffset.UTC
        );
        service = new LimitService(paymentRepository, limitsRepository, fixedClock);
    }
    
    @Test
    void shouldAllowPaymentWithinLimits() {
        // given
        limitsRepository.setLimits("user1", new UserLimits(10000, 5000));
        var payment = new Payment(1000, fixedClock.instant());
        
        // when
        var result = service.validatePayment("user1", payment);
        
        // then
        assertTrue(result.allowed());
        assertNull(result.reason());
    }
    
    @Test
    void shouldDenyPaymentExceedingMaxSingle() {
        // given
        limitsRepository.setLimits("user1", new UserLimits(10000, 5000));
        var payment = new Payment(6000, fixedClock.instant());
        
        // when
        var result = service.validatePayment("user1", payment);
        
        // then
        assertFalse(result.allowed());
        assertEquals("Exceeds max single operation limit", result.reason());
    }
    
    @Test
    void shouldDenyPaymentExceedingDailyLimit() {
        // given
        limitsRepository.setLimits("user1", new UserLimits(10000, 5000));
        
        // Добавляем предыдущие платежи сегодня
        paymentRepository.save("user1", new Payment(4000, fixedClock.instant()));
        paymentRepository.save("user1", new Payment(4000, fixedClock.instant()));
        
        var payment = new Payment(3000, fixedClock.instant());
        
        // when
        var result = service.validatePayment("user1", payment);
        
        // then
        assertFalse(result.allowed());
        assertTrue(result.reason().contains("daily limit"));
    }
    
    @Test
    void shouldNotCountYesterdayPayments() {
        // given
        limitsRepository.setLimits("user1", new UserLimits(10000, 5000));
        
        // Вчерашний платеж
        Instant yesterday = fixedClock.instant().minus(1, ChronoUnit.DAYS);
        paymentRepository.save("user1", new Payment(9000, yesterday));
        
        var payment = new Payment(5000, fixedClock.instant());
        
        // when
        var result = service.validatePayment("user1", payment);
        
        // then
        assertTrue(result.allowed());
    }
}
```

### Полезные assertions
```java
// Базовые
assertTrue(condition);
assertFalse(condition);
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);
assertNull(value);
assertNotNull(value);

// Коллекции
assertEquals(3, list.size());
assertTrue(list.isEmpty());
assertTrue(list.contains(element));

// Исключения
assertThrows(IllegalArgumentException.class, () -> service.process(null));

// С сообщениями
assertTrue(result.allowed(), "Payment should be allowed");
```

### Параметризованные тесты
```java
@ParameterizedTest
@CsvSource({
    "1000, true",
    "5000, true",
    "6000, false",
    "10000, false"
})
void shouldValidatePaymentAmount(long amount, boolean expectedAllowed) {
    limitsRepository.setLimits("user1", new UserLimits(10000, 5000));
    var payment = new Payment(amount, fixedClock.instant());
    
    var result = service.validatePayment("user1", payment);
    
    assertEquals(expectedAllowed, result.allowed());
}
```

---

## 9. Частые задачи — готовые решения

### Rate Limiter (ограничение запросов)
```java
public class RateLimiter {
    private final Map<String, Deque<Instant>> requests = new HashMap<>();
    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    
    public RateLimiter(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
    }
    
    public boolean allowRequest(String userId) {
        var now = clock.instant();
        var windowStart = now.minus(window);
        
        var userRequests = requests.computeIfAbsent(userId, k -> new LinkedList<>());
        
        // Удаляем старые запросы
        while (!userRequests.isEmpty() && userRequests.peekFirst().isBefore(windowStart)) {
            userRequests.pollFirst();
        }
        
        if (userRequests.size() >= maxRequests) {
            return false;
        }
        
        userRequests.addLast(now);
        return true;
    }
}
```

### LRU Cache
```java
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }
    
    public V get(K key) {
        return cache.get(key);
    }
    
    public void put(K key, V value) {
        cache.put(key, value);
    }
}
```

### Счетчик с TTL (время жизни)
```java
public class ExpiringCounter {
    private final Map<String, Long> counts = new HashMap<>();
    private final Map<String, Instant> expiry = new HashMap<>();
    private final Duration ttl;
    private final Clock clock;
    
    public ExpiringCounter(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }
    
    public long increment(String key) {
        cleanupExpired();
        long newValue = counts.merge(key, 1L, Long::sum);
        expiry.put(key, clock.instant().plus(ttl));
        return newValue;
    }
    
    public long get(String key) {
        cleanupExpired();
        return counts.getOrDefault(key, 0L);
    }
    
    private void cleanupExpired() {
        var now = clock.instant();
        expiry.entrySet().removeIf(e -> {
            if (e.getValue().isBefore(now)) {
                counts.remove(e.getKey());
                return true;
            }
            return false;
        });
    }
}
```

### Валидация входных данных
```java
public class Validator {
    
    public static void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
    
    public static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }
    
    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
```

---

## 10. Чеклист перед собеседованием

### Подготовка проекта
- [ ] Пустой Maven/Gradle проект готов
- [ ] JUnit 5 подключен и работает
- [ ] Mockito подключен (на всякий случай)
- [ ] IDE настроена (shortcuts, formatting)
- [ ] Zoom работает + шаринг экрана
- [ ] Copilot/AI отключены

### Зависимости pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.7.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Во время секции
1. **Первые 10 минут** — уточнить требования:
   - Какие именно лимиты проверяем?
   - Что возвращаем при ошибке?
   - Edge cases?

2. **Проговорить план** перед написанием кода

3. **Начать с модели** (record/class для данных)

4. **Писать тесты параллельно** с кодом

5. **Не молчать** — комментировать свои действия

### Типичные ошибки
- Сразу бросаться писать код без плана
- Не уточнять требования
- Писать тесты в конце (когда времени нет)
- Усложнять архитектуру
- Забывать про edge cases (null, пустые коллекции, граничные значения)

---

## 11. Примеры задач Яндекса

### Задача 1: Лимиты платежей
> Система проверки лимитов: дневной лимит, макс. операция, история платежей

### Задача 2: Rate Limiter
> N запросов в минуту на пользователя

### Задача 3: Кэш с TTL
> Хранилище с автоматическим удалением по таймауту

### Задача 4: Агрегация данных
> Статистика по временным окнам (час/день/неделя)

### Задача 5: Очередь задач
> Приоритетная очередь с дедупликацией

---

## Quick Reference Card

```
┌────────────────────────────────────────────────────────────┐
│                    ВРЕМЯ                                    │
├────────────────────────────────────────────────────────────┤
│ Instant.now()                  - текущий момент UTC        │
│ clock.instant()                - через Clock (для тестов)  │
│ LocalDate.now(clock)           - текущая дата              │
│ instant.minus(1, ChronoUnit.DAYS)  - вычитание             │
│ date.atStartOfDay(ZoneOffset.UTC).toInstant()  - начало    │
├────────────────────────────────────────────────────────────┤
│                    STREAMS                                  │
├────────────────────────────────────────────────────────────┤
│ .filter(x -> condition)        - фильтрация                │
│ .map(X::getField)              - преобразование            │
│ .mapToLong(X::getValue)        - в примитив                │
│ .sum() / .max() / .count()     - агрегация                 │
│ .collect(Collectors.toList())  - в список                  │
│ .collect(groupingBy(X::getKey))- группировка               │
│ .findFirst() / .findAny()      - поиск Optional            │
├────────────────────────────────────────────────────────────┤
│                    COLLECTIONS                              │
├────────────────────────────────────────────────────────────┤
│ map.computeIfAbsent(k, k -> new ArrayList<>())             │
│ map.getOrDefault(key, defaultValue)                        │
│ map.merge(key, 1, Integer::sum)  - счетчик                 │
│ List.of() / Map.of()             - immutable               │
├────────────────────────────────────────────────────────────┤
│                    ТЕСТЫ                                    │
├────────────────────────────────────────────────────────────┤
│ @BeforeEach void setUp()       - инициализация             │
│ @Test void shouldDoSomething() - тест                      │
│ assertTrue/assertFalse/assertEquals/assertThrows           │
│ Clock.fixed(instant, zone)     - фиксированное время       │
└────────────────────────────────────────────────────────────┘
```
