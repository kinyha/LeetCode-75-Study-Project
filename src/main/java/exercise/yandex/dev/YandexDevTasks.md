# Яндекс Dev: Задачи для практики

## Чеклист

```
НАЧАЛО:
□ Уточни требования (что возвращаем? edge cases?)
□ Схема классов на бумаге
□ Проговори план

КОД:
□ record + фабричные методы ok()/denied()
□ Map + computeIfAbsent/getOrDefault
□ Clock через конструктор
□ Сначала ВСЕ проверки, потом сохранение

ТЕСТЫ:
□ Clock.fixed() в @BeforeEach
□ Happy path
□ Все причины отказа
□ Границы (==, вчера/сегодня)

ВРЕМЯ:
□ clock.instant()
□ LocalDate.now(clock).atStartOfDay(ZoneOffset.UTC).toInstant()
□ instant.isAfter() / isBefore()
```

---

## Задача 1: Бронирование переговорок

Вы — backend-разработчик в компании с офисами.
HR просит систему бронирования переговорных комнат.

Переговорка — id, название, вместимость.
Бронь — комната, кто, начало, конец.

Проверить возможность брони и вернуть результат.

```java
BookingResult book(BookingRequest request)
```

---

## Задача 2: Трекер событий

Вы — разработчик в команде аналитики.
Нужна система сбора и агрегации пользовательских событий.

Событие — userId, тип, значение, время.

Сохранять события и отдавать статистику за период.

```java
void track(Event event)
Stats getStats(String userId, Instant from, Instant to)
```

---

## Задача 3: Очередь уведомлений

Вы — разработчик сервиса нотификаций.
Нужна умная очередь отправки уведомлений.

Уведомление — userId, тип (email/push/sms), приоритет, текст.

Высокий приоритет первым. Не отправлять дубликаты в течение N минут.

```java
void enqueue(Notification notification)
Optional<Notification> pollNext()
```

---

## Задача 4: Контроль доступа по тарифу

Вы — разработчик SaaS-продукта.
Нужна система проверки доступа к фичам по тарифу пользователя.

Тариф — название, фичи с лимитами (вызовов в день).

Проверить доступ и учесть использование.

```java
AccessResult checkAccess(String userId, String featureName)
void recordUsage(String userId, String featureName)
```

---

## Задача 5: Балансировщик задач

Вы — разработчик системы распределённых вычислений.
Нужен компонент распределения задач по воркерам.

Воркер — id, макс. нагрузка, текущие задачи.
Задача — id, сложность (слотов).

Назначить на наименее загруженного. Освободить при завершении.

```java
AssignResult assign(Task task)
void complete(String taskId)
```

---
---
---

# ПОДСКАЗКИ

## Задача 1: Бронирование переговорок

**Дано:**

```java
record Room(String id, String name, int capacity)
record BookingRequest(String roomId, String userId, Instant start, Instant end)
record Booking(String id, String roomId, String userId, Instant start, Instant end)

// Результат
record BookingResult(boolean success, String bookingId, String error) {
    static BookingResult ok(String id) { return new BookingResult(true, id, null); }
    static BookingResult fail(String err) { return new BookingResult(false, null, err); }
}
```

**Правила:**
- Брони одной комнаты не пересекаются
- Время работы офиса 09:00-21:00 (опционально)
- Мин. длительность 15 мин, макс. 4 часа (опционально)

**Хранение:**
```java
Map<String, Room> rooms = new HashMap<>();
Map<String, List<Booking>> bookingsByRoom = new HashMap<>();
```

**Пересечение интервалов:**
```java
// [s1,e1] и [s2,e2] пересекаются если: s1 < e2 AND s2 < e1
boolean overlaps(Booking a, BookingRequest b) {
    return b.start().isBefore(a.end()) && a.start().isBefore(b.end());
}
```

**Edge cases:**
- 10:00-11:00 и 11:00-12:00 — НЕ пересекаются (isBefore, не isBeforeOrEqual)
- start >= end — невалидно
- Комната не существует

---

## Задача 2: Трекер событий

**Дано:**

```java
record Event(String userId, String type, long value, Instant timestamp)

enum AggregationType { SUM, COUNT, AVG }

record Stats(String type, long value, long count)
```

**Хранение:**
```java
Map<String, List<Event>> eventsByUser = new HashMap<>();
```

**Фильтрация:**
```java
// Период [from, to)
events.stream()
    .filter(e -> !e.timestamp().isBefore(from) && e.timestamp().isBefore(to))
```

**Агрегация:**
```java
Map<String, LongSummaryStatistics> byType = events.stream()
    .collect(Collectors.groupingBy(
        Event::type,
        Collectors.summarizingLong(Event::value)
    ));
```

**Edge cases:**
- Нет событий в периоде
- from >= to
- Уточнить границы: `[from, to)` или `[from, to]`

---

## Задача 3: Очередь уведомлений

**Дано:**

```java
enum NotificationType { EMAIL, PUSH, SMS }
enum Priority { HIGH(1), MEDIUM(2), LOW(3); int value; }

record Notification(
    String id,
    String userId,
    NotificationType type,
    Priority priority,
    String payload,
    Instant createdAt
)
```

**Очередь с приоритетом:**
```java
PriorityQueue<Notification> queue = new PriorityQueue<>(
    Comparator.comparingInt(n -> n.priority().value)
        .thenComparing(Notification::createdAt)
);
```

**Дедупликация:**
```java
record DedupeKey(String userId, NotificationType type, String payload) {}
Map<DedupeKey, Instant> lastSent = new HashMap<>();
Duration dedupeWindow = Duration.ofMinutes(5);

boolean isDuplicate(Notification n) {
    DedupeKey key = new DedupeKey(n.userId(), n.type(), n.payload());
    Instant last = lastSent.get(key);
    return last != null && last.plus(dedupeWindow).isAfter(clock.instant());
}
```

**pollNext():**
- Достать из очереди
- Если дубликат — пропустить, взять следующий
- Записать в lastSent
- Вернуть

**Edge cases:**
- Очередь пуста
- Все в очереди — дубликаты
- Одинаковый приоритет — FIFO по createdAt

---

## Задача 4: Контроль доступа по тарифу

**Дано:**

```java
record Plan(String name, Map<String, FeatureLimit> features)
record FeatureLimit(boolean enabled, int dailyLimit) // -1 = безлимит
record Subscription(String userId, String planName, Instant expiresAt)

record AccessResult(boolean allowed, String reason) {
    static AccessResult ok() { return new AccessResult(true, null); }
    static AccessResult denied(String r) { return new AccessResult(false, r); }
}
```

**Хранение:**
```java
Map<String, Plan> plans = new HashMap<>();
Map<String, Subscription> subscriptions = new HashMap<>();

// Использование: userId+feature+date -> count
record UsageKey(String userId, String feature, LocalDate date) {}
Map<UsageKey, Integer> usage = new HashMap<>();
```

**Проверка:**
```java
AccessResult checkAccess(String userId, String feature) {
    // 1. Есть подписка?
    // 2. Не истекла?
    // 3. Фича в тарифе?
    // 4. Лимит не превышен?
}

void recordUsage(String userId, String feature) {
    LocalDate today = LocalDate.now(clock);
    UsageKey key = new UsageKey(userId, feature, today);
    usage.merge(key, 1, Integer::sum);
}
```

**Edge cases:**
- Нет подписки
- Подписка истекла
- Фича не в тарифе
- dailyLimit = -1 (безлимит)
- Полночь — новый день, счётчик сбрасывается

---

## Задача 5: Балансировщик задач

**Дано:**

```java
record Task(String id, int complexity)

record AssignResult(boolean success, String workerId, String error) {
    static AssignResult ok(String wid) { return new AssignResult(true, wid, null); }
    static AssignResult fail(String err) { return new AssignResult(false, null, err); }
}

// Worker — mutable, не record!
class Worker {
    String id;
    int maxCapacity;
    int currentLoad;
    Set<String> taskIds = new HashSet<>();

    int available() { return maxCapacity - currentLoad; }

    void assign(Task t) {
        currentLoad += t.complexity();
        taskIds.add(t.id());
    }

    void release(Task t) {
        currentLoad -= t.complexity();
        taskIds.remove(t.id());
    }
}
```

**Хранение:**
```java
Map<String, Worker> workers = new HashMap<>();
Map<String, Task> tasks = new HashMap<>();       // taskId -> Task
Map<String, String> taskToWorker = new HashMap<>(); // taskId -> workerId
```

**Выбор воркера:**
```java
Optional<Worker> findWorker(int slots) {
    return workers.values().stream()
        .filter(w -> w.available() >= slots)
        .min(Comparator.comparingInt(w -> w.currentLoad));
}
```

**complete():**
```java
void complete(String taskId) {
    String workerId = taskToWorker.remove(taskId);
    Task task = tasks.remove(taskId);
    if (workerId != null && task != null) {
        workers.get(workerId).release(task);
    }
}
```

**Edge cases:**
- complexity > maxCapacity любого воркера
- Все воркеры заняты
- complete() несуществующей задачи
- Повторный assign() той же задачи

---

## Паттерны

**Clock:**
```java
public Service(Repo repo, Clock clock) {
    this.clock = clock;
}
public Service(Repo repo) {
    this(repo, Clock.systemUTC());
}
```

**Тест:**
```java
private static final Instant NOW = Instant.parse("2025-01-15T12:00:00Z");

@BeforeEach
void setUp() {
    clock = Clock.fixed(NOW, ZoneOffset.UTC);
}
```

**Начало дня:**
```java
LocalDate.now(clock).atStartOfDay(ZoneOffset.UTC).toInstant()
```
