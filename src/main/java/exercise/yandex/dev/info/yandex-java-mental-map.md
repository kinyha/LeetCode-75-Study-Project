# Яндекс Java Coding: Ментальная карта + Чеклист

## 🧠 Ментальная модель задачи

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ЛЮБАЯ ЗАДАЧА ЯНДЕКСА                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ВХОД           →    ОБРАБОТКА         →    ВЫХОД                  │
│   (что дано)          (бизнес-логика)        (что вернуть)          │
│                                                                     │
│   • userId            • валидация            • boolean              │
│   • данные            • вычисления           • результат + причина  │
│   • параметры         • агрегация            • статистика           │
│                       • фильтрация           • список               │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                    ХРАНИЛИЩЕ (in-memory)                            │
│                                                                     │
│   Map<UserId, List<Entity>>  — история по пользователю              │
│   Map<UserId, Config>        — настройки/лимиты                     │
│   Map<Key, Value>            — кэш/счетчики                         │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Банк задач (типовые формулировки)

### Задача 1: Лимиты платежей (базовая)
```
Дано: userId, Payment(amount, timestamp)
Лимиты: дневной лимит суммы, макс размер одной операции
Вернуть: можно ли провести + причина отказа
```

### Задача 2: Rate Limiter
```
Дано: userId, requestId
Лимит: не более N запросов за M секунд
Вернуть: разрешить запрос или нет
```

### Задача 3: Баланс кошелька
```
Дано: userId, Operation(type=DEPOSIT|WITHDRAW, amount)
Правила: нельзя уйти в минус, макс баланс
Вернуть: успех/ошибка, новый баланс
```

### Задача 4: Система бронирования
```
Дано: resourceId, Booking(userId, startTime, endTime)
Правила: слоты не пересекаются, макс длительность
Вернуть: успех/конфликт
```

### Задача 5: Подписки/Тарифы
```
Дано: userId, Feature(name)
Данные: подписка пользователя, лимиты тарифа
Вернуть: доступна ли фича, сколько осталось
```

### Задача 6: Очередь задач
```
Дано: Task(id, priority, payload)
Правила: дедупликация по id, приоритет
Вернуть: следующая задача для выполнения
```

### Задача 7: Статистика/Аналитика
```
Дано: Event(userId, type, timestamp, value)
Запрос: агрегация за период (час/день/неделя)
Вернуть: Map<Period, Stats> или Summary
```

---

## 🗺️ Пошаговый план (10 мин планирование)

### Шаг 0: Уточнить требования (2-3 мин)
```
□ Какие именно проверки/правила?
□ Что возвращаем при успехе? При ошибке?
□ Нужна ли история операций?
□ Edge cases: null, пустые данные, границы?
□ Нужно ли сохранять после успешной операции?
```

### Шаг 1: Нарисовать схему (2 мин)
```
┌──────────────────────────────────────────────────────────┐
│ Service                                                  │
│   └─ validate(userId, payment) → Result                  │
├──────────────────────────────────────────────────────────┤
│ Repository (история)     │ Repository (настройки)       │
│   └─ getPayments(userId) │   └─ getLimits(userId)       │
├──────────────────────────────────────────────────────────┤
│ Model: Payment, UserLimits, ValidationResult             │
└──────────────────────────────────────────────────────────┘
```

### Шаг 2: Определить модели (1 мин)
```
Входные данные:  Payment, Request, Operation...
Конфигурация:    UserLimits, Subscription, Config...
Результат:       ValidationResult, OperationResult...
```

### Шаг 3: Определить хранилища (1 мин)
```
История:    Map<String, List<Payment>> — что накапливается
Настройки:  Map<String, UserLimits>    — что конфигурируется  
Состояние:  Map<String, Long>          — балансы, счетчики
```

### Шаг 4: Написать сигнатуру сервиса (1 мин)
```java
public ValidationResult validate(String userId, Payment payment)
```

---

## 🏗️ Реализация по слоям

## Layer 1: MODEL (5-7 мин)

### Что нужно создать:
```
1. Входная сущность     — Payment, Request, Event
2. Конфигурация         — UserLimits, RateLimit, Subscription  
3. Результат операции   — ValidationResult, OperationResult
```

### Java API для моделей:

| Что нужно | Java инструмент | Пример |
|-----------|-----------------|--------|
| Неизменяемые данные | `record` (Java 16+) | `record Payment(long amount, Instant time)` |
| Timestamp | `Instant` | `Instant.now()` |
| Enum результатов | `enum` | `enum Status { OK, DENIED }` |
| Nullable поле | `@Nullable` или просто null | `String reason` |
| Фабричные методы | static методы | `ValidationResult.ok()` |

### Шаблон: Входная сущность
```java
public record Payment(
    long amount,
    Instant timestamp
) {
    // Валидация в конструкторе (compact constructor)
    public Payment {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp required");
        }
    }
}
```

### Шаблон: Конфигурация
```java
public record UserLimits(
    long dailyLimit,
    long maxSingleOperation
) {
    public UserLimits {
        if (dailyLimit <= 0 || maxSingleOperation <= 0) {
            throw new IllegalArgumentException("Limits must be positive");
        }
    }
}
```

### Шаблон: Результат операции
```java
public record ValidationResult(
    boolean allowed,
    String reason
) {
    // Фабричные методы — удобно использовать
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult denied(String reason) {
        return new ValidationResult(false, reason);
    }
    
    // Для цепочки проверок
    public boolean isDenied() {
        return !allowed;
    }
}
```

### Альтернатива: Enum для типов ошибок
```java
public enum DenialReason {
    EXCEEDS_DAILY_LIMIT("Daily limit exceeded"),
    EXCEEDS_SINGLE_LIMIT("Single operation limit exceeded"),
    USER_NOT_FOUND("User limits not configured"),
    INSUFFICIENT_BALANCE("Insufficient balance");
    
    private final String message;
    
    DenialReason(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}

public record ValidationResult(
    boolean allowed,
    DenialReason reason  // null если allowed
) {
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult denied(DenialReason reason) {
        return new ValidationResult(false, reason);
    }
}
```

---

## Layer 2: REPOSITORY (5-7 мин)

### Что нужно создать:
```
1. Хранилище истории     — PaymentRepository
2. Хранилище настроек    — UserLimitsRepository
3. (опционально) Хранилище состояния — BalanceRepository
```

### Java API для репозиториев:

| Что нужно | Java инструмент | Пример |
|-----------|-----------------|--------|
| Хранение по ключу | `Map<K, V>` | `Map<String, UserLimits>` |
| Список по ключу | `Map<K, List<V>>` | `Map<String, List<Payment>>` |
| Безопасное получение | `getOrDefault()` | `map.getOrDefault(key, List.of())` |
| Добавление в список | `computeIfAbsent()` | `map.computeIfAbsent(k, k -> new ArrayList<>()).add(v)` |
| Optional результат | `Optional.ofNullable()` | `Optional.ofNullable(map.get(key))` |
| Фильтрация по времени | `Stream + filter` | `.filter(p -> p.timestamp().isAfter(x))` |

### Шаблон: Репозиторий истории
```java
public class PaymentRepository {
    private final Map<String, List<Payment>> payments = new HashMap<>();
    
    // Сохранить
    public void save(String userId, Payment payment) {
        payments.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(payment);
    }
    
    // Получить все
    public List<Payment> findAll(String userId) {
        return payments.getOrDefault(userId, List.of());
    }
    
    // Получить за период (ЧАСТЫЙ ПАТТЕРН!)
    public List<Payment> findAfter(String userId, Instant after) {
        return findAll(userId).stream()
            .filter(p -> p.timestamp().isAfter(after))
            .toList();
    }
    
    // Получить за день
    public List<Payment> findToday(String userId, Clock clock) {
        Instant startOfDay = LocalDate.now(clock)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
        return findAfter(userId, startOfDay);
    }
}
```

### Шаблон: Репозиторий настроек
```java
public class UserLimitsRepository {
    private final Map<String, UserLimits> limits = new HashMap<>();
    
    public void save(String userId, UserLimits userLimits) {
        limits.put(userId, userLimits);
    }
    
    public Optional<UserLimits> find(String userId) {
        return Optional.ofNullable(limits.get(userId));
    }
    
    // Или с default значением
    public UserLimits findOrDefault(String userId, UserLimits defaultLimits) {
        return limits.getOrDefault(userId, defaultLimits);
    }
}
```

### Шаблон: Репозиторий состояния (баланс/счетчик)
```java
public class BalanceRepository {
    private final Map<String, Long> balances = new HashMap<>();
    
    public long getBalance(String userId) {
        return balances.getOrDefault(userId, 0L);
    }
    
    public void setBalance(String userId, long balance) {
        balances.put(userId, balance);
    }
    
    // Атомарное изменение
    public long addAndGet(String userId, long delta) {
        return balances.merge(userId, delta, Long::sum);
    }
}
```

---

## Layer 3: SERVICE (10-15 мин)

### Что нужно создать:
```
1. Основной сервис с бизнес-логикой
2. Внедрить зависимости через конструктор
3. Добавить Clock для тестируемости времени
```

### Java API для сервиса:

| Что нужно | Java инструмент | Пример |
|-----------|-----------------|--------|
| Текущее время | `Clock` + `clock.instant()` | для тестируемости |
| Начало дня | `LocalDate.now(clock).atStartOfDay()` | границы периода |
| Сумма | `stream().mapToLong().sum()` | агрегация |
| Проверка всех | `stream().allMatch()` | валидация |
| Проверка любого | `stream().anyMatch()` | поиск нарушений |
| Optional chain | `optional.map().orElse()` | безопасная навигация |

### Шаблон: Сервис валидации
```java
public class LimitValidationService {
    private final PaymentRepository paymentRepository;
    private final UserLimitsRepository limitsRepository;
    private final Clock clock;
    
    // Конструктор для тестов (с Clock)
    public LimitValidationService(
            PaymentRepository paymentRepository,
            UserLimitsRepository limitsRepository,
            Clock clock) {
        this.paymentRepository = paymentRepository;
        this.limitsRepository = limitsRepository;
        this.clock = clock;
    }
    
    // Конструктор для production
    public LimitValidationService(
            PaymentRepository paymentRepository,
            UserLimitsRepository limitsRepository) {
        this(paymentRepository, limitsRepository, Clock.systemUTC());
    }
    
    public ValidationResult validate(String userId, Payment payment) {
        // 1. Получить лимиты
        var limitsOpt = limitsRepository.find(userId);
        if (limitsOpt.isEmpty()) {
            return ValidationResult.denied("User limits not configured");
        }
        var limits = limitsOpt.get();
        
        // 2. Проверка макс. операции (простая проверка — первой!)
        if (payment.amount() > limits.maxSingleOperation()) {
            return ValidationResult.denied("Exceeds single operation limit");
        }
        
        // 3. Проверка дневного лимита (сложная — второй)
        long todayTotal = calculateTodayTotal(userId);
        if (todayTotal + payment.amount() > limits.dailyLimit()) {
            return ValidationResult.denied("Exceeds daily limit");
        }
        
        // 4. Всё ок
        return ValidationResult.ok();
    }
    
    private long calculateTodayTotal(String userId) {
        Instant startOfDay = LocalDate.now(clock)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
            
        return paymentRepository.findAfter(userId, startOfDay).stream()
            .mapToLong(Payment::amount)
            .sum();
    }
}
```

### Паттерн: Цепочка проверок
```java
public ValidationResult validate(String userId, Payment payment) {
    return checkUserExists(userId)
        .or(() -> checkSingleLimit(userId, payment))
        .or(() -> checkDailyLimit(userId, payment))
        .orElse(ValidationResult.ok());
}

private Optional<ValidationResult> checkUserExists(String userId) {
    if (limitsRepository.find(userId).isEmpty()) {
        return Optional.of(ValidationResult.denied("User not found"));
    }
    return Optional.empty();
}

private Optional<ValidationResult> checkSingleLimit(String userId, Payment payment) {
    var limits = limitsRepository.find(userId).get();
    if (payment.amount() > limits.maxSingleOperation()) {
        return Optional.of(ValidationResult.denied("Single limit exceeded"));
    }
    return Optional.empty();
}
// ... и так далее
```

### Паттерн: Сервис с сохранением после успеха
```java
public ValidationResult processPayment(String userId, Payment payment) {
    // Сначала валидация
    var result = validate(userId, payment);
    
    // Если успех — сохраняем
    if (result.allowed()) {
        paymentRepository.save(userId, payment);
    }
    
    return result;
}
```

---

## Layer 4: TESTS (10-15 мин)

### Структура тестов:
```
1. setUp()              — создание зависимостей и сервиса
2. Happy path           — успешный сценарий
3. Каждая причина отказа — по тесту на каждую ветку
4. Edge cases           — граничные значения, вчерашние данные
```

### Java API для тестов:

| Что нужно | JUnit инструмент | Пример |
|-----------|------------------|--------|
| Инициализация | `@BeforeEach` | `void setUp()` |
| Тест | `@Test` | `void shouldAllowValidPayment()` |
| Проверка true/false | `assertTrue/assertFalse` | `assertTrue(result.allowed())` |
| Проверка равенства | `assertEquals` | `assertEquals(expected, actual)` |
| Проверка null | `assertNull/assertNotNull` | `assertNull(result.reason())` |
| Проверка исключения | `assertThrows` | `assertThrows(IAE.class, () -> ...)` |
| Фиксированное время | `Clock.fixed()` | для предсказуемости |
| Параметризация | `@ParameterizedTest` | несколько входов |

### Шаблон: Полный тестовый класс
```java
class LimitValidationServiceTest {
    
    private PaymentRepository paymentRepository;
    private UserLimitsRepository limitsRepository;
    private Clock fixedClock;
    private LimitValidationService service;
    
    // Константы для тестов
    private static final String USER_ID = "user-1";
    private static final long DAILY_LIMIT = 10_000L;
    private static final long SINGLE_LIMIT = 5_000L;
    private static final Instant NOW = Instant.parse("2025-01-15T12:00:00Z");
    
    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        limitsRepository = new UserLimitsRepository();
        fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new LimitValidationService(
            paymentRepository, limitsRepository, fixedClock
        );
        
        // Дефолтные лимиты для большинства тестов
        limitsRepository.save(USER_ID, new UserLimits(DAILY_LIMIT, SINGLE_LIMIT));
    }
    
    // ===== HAPPY PATH =====
    
    @Test
    void shouldAllowPaymentWithinAllLimits() {
        // given
        var payment = new Payment(1000L, NOW);
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed());
        assertNull(result.reason());
    }
    
    // ===== SINGLE LIMIT =====
    
    @Test
    void shouldDenyPaymentExceedingSingleLimit() {
        // given
        var payment = new Payment(6000L, NOW);  // > 5000
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertFalse(result.allowed());
        assertTrue(result.reason().contains("single"));
    }
    
    @Test
    void shouldAllowPaymentAtExactSingleLimit() {
        // given — граничное значение
        var payment = new Payment(SINGLE_LIMIT, NOW);  // ровно 5000
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed());
    }
    
    // ===== DAILY LIMIT =====
    
    @Test
    void shouldDenyPaymentExceedingDailyLimit() {
        // given — уже есть платежи сегодня
        paymentRepository.save(USER_ID, new Payment(4000L, NOW.minusSeconds(3600)));
        paymentRepository.save(USER_ID, new Payment(4000L, NOW.minusSeconds(1800)));
        
        var payment = new Payment(3000L, NOW);  // 4000 + 4000 + 3000 = 11000 > 10000
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertFalse(result.allowed());
        assertTrue(result.reason().contains("daily"));
    }
    
    @Test
    void shouldNotCountYesterdayPaymentsInDailyLimit() {
        // given — вчерашний платеж не должен учитываться
        Instant yesterday = NOW.minus(1, ChronoUnit.DAYS);
        paymentRepository.save(USER_ID, new Payment(9000L, yesterday));
        
        var payment = new Payment(5000L, NOW);
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed(), "Yesterday's payment should not affect today's limit");
    }
    
    @Test
    void shouldAllowPaymentAtExactDailyLimit() {
        // given
        paymentRepository.save(USER_ID, new Payment(5000L, NOW.minusSeconds(3600)));
        
        var payment = new Payment(5000L, NOW);  // 5000 + 5000 = 10000 = лимит
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed());
    }
    
    // ===== USER NOT FOUND =====
    
    @Test
    void shouldDenyPaymentForUnknownUser() {
        // given
        var payment = new Payment(100L, NOW);
        
        // when
        var result = service.validate("unknown-user", payment);
        
        // then
        assertFalse(result.allowed());
        assertTrue(result.reason().toLowerCase().contains("not"));
    }
    
    // ===== EDGE CASES =====
    
    @Test
    void shouldAllowFirstPaymentOfTheDay() {
        // given — нет предыдущих платежей
        var payment = new Payment(5000L, NOW);
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed());
    }
    
    @Test
    void shouldHandleMultipleSmallPayments() {
        // given — много мелких платежей
        for (int i = 0; i < 9; i++) {
            paymentRepository.save(USER_ID, new Payment(1000L, NOW.minusSeconds(i * 60)));
        }
        
        var payment = new Payment(1000L, NOW);  // 10 * 1000 = 10000 = лимит
        
        // when
        var result = service.validate(USER_ID, payment);
        
        // then
        assertTrue(result.allowed());
    }
}
```

### Параметризованные тесты (если время есть)
```java
@ParameterizedTest
@CsvSource({
    "1000, true",      // в пределах лимита
    "5000, true",      // ровно single limit  
    "5001, false",     // превышает single limit
    "10000, false"     // превышает оба лимита
})
void shouldValidateSingleOperationLimit(long amount, boolean expectedAllowed) {
    var payment = new Payment(amount, NOW);
    
    var result = service.validate(USER_ID, payment);
    
    assertEquals(expectedAllowed, result.allowed());
}
```

---

## ⏱️ Тайминг по фазам

```
┌─────────────────────────────────────────────────────────────┐
│ 0:00-0:10  ПЛАНИРОВАНИЕ                                     │
│            • Уточнить требования (2-3 мин)                  │
│            • Нарисовать схему (2 мин)                       │
│            • Определить классы (3 мин)                      │
│            • Проговорить план вслух                         │
├─────────────────────────────────────────────────────────────┤
│ 0:10-0:40  КОД                                              │
│            • Model (5-7 мин): record + ValidationResult     │
│            • Repository (5-7 мин): Map + методы             │
│            • Service (10-15 мин): логика + Clock            │
│            • Первый тест (3-5 мин): happy path              │
├─────────────────────────────────────────────────────────────┤
│ 0:40-1:00  ТЕСТЫ + ДЕБАГ                                    │
│            • Запустить happy path                           │
│            • Добавить negative cases                        │
│            • Edge cases если время есть                     │
│            • Обсуждение решения                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Приоритеты: Если мало времени

### Минимум (must have):
```
□ 2 record: входные данные + результат
□ 1 repository: Map + save/find
□ 1 service: основная логика
□ 2-3 теста: happy path + 1-2 отказа
```

### Средний уровень (should have):
```
□ Валидация в конструкторах record
□ Clock для тестируемости времени
□ Фабричные методы в ValidationResult
□ Все ветки покрыты тестами
```

### Полный (nice to have):
```
□ Enum для типов ошибок
□ Цепочка проверок
□ Параметризованные тесты
□ Edge cases (граничные значения)
□ JavaDoc комментарии
```

---

## ✅ ЧЕКЛИСТ: Помню ли я?

### Java Time API
```
□ Instant.now()                              — текущий момент
□ clock.instant()                            — через Clock (для тестов)
□ LocalDate.now(clock)                       — текущая дата
□ LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant()  — начало дня
□ instant.minus(1, ChronoUnit.DAYS)          — вчера
□ instant.isAfter(other)                     — сравнение
□ Clock.fixed(instant, zone)                 — фиксированный Clock для тестов
```

### Collections
```
□ Map<K,V> map = new HashMap<>()             — создание
□ map.put(key, value)                        — добавить
□ map.get(key)                               — получить (может null)
□ map.getOrDefault(key, defaultValue)        — получить с дефолтом
□ map.computeIfAbsent(key, k -> new ArrayList<>())  — создать если нет
□ map.merge(key, 1, Integer::sum)            — счетчик++
□ Optional.ofNullable(map.get(key))          — обернуть в Optional
□ List.of("a", "b")                          — immutable список
□ new ArrayList<>(List.of(...))              — mutable копия
```

### Stream API — Фильтрация
```
□ list.stream().filter(x -> condition)       — отфильтровать
□ .filter(p -> p.timestamp().isAfter(x))     — по времени
□ .filter(p -> p.amount() > 1000)            — по значению
```

### Stream API — Преобразование
```
□ .map(Payment::amount)                      — извлечь поле
□ .map(x -> new Dto(x.a(), x.b()))          — преобразовать
□ .mapToLong(Payment::amount)                — в примитив
□ .flatMap(x -> x.getItems().stream())       — развернуть вложенные
```

### Stream API — Агрегация
```
□ .count()                                   — количество
□ .sum()                                     — сумма (после mapToLong)
□ .max(Comparator.comparingLong(X::value))   — максимум
□ .min(...)                                  — минимум
□ .average()                                 — среднее (после mapToLong)
```

### Stream API — Проверки
```
□ .anyMatch(x -> condition)                  — есть хотя бы один
□ .allMatch(x -> condition)                  — все соответствуют
□ .noneMatch(x -> condition)                 — ни один не соответствует
```

### Stream API — Сбор результата
```
□ .toList()                                  — в List (Java 16+)
□ .collect(Collectors.toList())              — в List (старый способ)
□ .collect(Collectors.toSet())               — в Set
□ .collect(Collectors.groupingBy(X::key))    — группировка
□ .collect(Collectors.summingLong(X::val))   — сумма
□ .findFirst()                               — первый Optional
□ .findAny()                                 — любой Optional
```

### Optional
```
□ Optional.of(value)                         — создать (not null)
□ Optional.ofNullable(value)                 — создать (может null)
□ Optional.empty()                           — пустой
□ opt.isPresent() / opt.isEmpty()            — проверка
□ opt.get()                                  — получить (осторожно!)
□ opt.orElse(defaultValue)                   — получить или дефолт
□ opt.orElseThrow()                          — получить или исключение
□ opt.map(x -> transform(x))                 — преобразовать
□ opt.flatMap(x -> optionalResult)           — цепочка Optional
□ opt.or(() -> anotherOptional)              — альтернатива (Java 9+)
```

### JUnit 5
```
□ @BeforeEach void setUp()                   — инициализация
□ @Test void shouldDoSomething()             — тест
□ assertTrue(condition)                      — проверка true
□ assertFalse(condition)                     — проверка false
□ assertEquals(expected, actual)             — равенство
□ assertNull(value)                          — проверка null
□ assertNotNull(value)                       — проверка not null
□ assertThrows(Exception.class, () -> ...)   — ожидание исключения
□ @ParameterizedTest + @CsvSource            — параметризация
```

### Record (Java 16+)
```
□ public record Name(Type field1, Type field2) {}
□ Compact constructor: public Name { validation; }
□ Автоматически: constructor, getters, equals, hashCode, toString
□ Поля final и private
□ Можно добавлять static методы
```

---

## 🚨 Типичные ошибки

```
❌ Сразу писать код без уточнения требований
❌ Начинать с тестов (сначала нужна хотя бы сигнатура)
❌ Усложнять: делать абстракции "на будущее"
❌ Забывать про Clock — время нетестируемо
❌ Молчать — интервьюер не видит ход мыслей
❌ Паниковать если что-то забыл — можно спросить
❌ Писать тесты в конце — не успеешь
❌ Игнорировать edge cases — это проверяют
```

---

## 📝 Шпаргалка на одну страницу

```
ПЛАН: уточнить → схема → модели → репо → сервис → тесты

MODEL:
  record Payment(long amount, Instant timestamp) {}
  record ValidationResult(boolean allowed, String reason) {
      static ok() / denied(reason)
  }

REPOSITORY:
  Map<String, List<Payment>> payments = new HashMap<>();
  payments.computeIfAbsent(userId, k -> new ArrayList<>()).add(p);
  payments.getOrDefault(userId, List.of());

SERVICE:
  constructor(repo, limitsRepo, Clock clock)
  validate(userId, payment) → ValidationResult
  
  Instant startOfDay = LocalDate.now(clock)
      .atStartOfDay(ZoneOffset.UTC).toInstant();
  long sum = repo.findAfter(userId, startOfDay).stream()
      .mapToLong(Payment::amount).sum();

TEST:
  @BeforeEach: repos + Clock.fixed(NOW, UTC) + service
  @Test: given-when-then
  assertTrue/assertFalse/assertEquals
```
