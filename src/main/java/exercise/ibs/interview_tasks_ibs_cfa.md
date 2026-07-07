# Задачи для прорешивания — IBS / ЦФА (Java Backend)

Минимально необходимый набор под формат live coding на внутреннем ТИ и ТИ клиента.

**Как решать:** в IDE без автодополнения решения, проговаривая вслух ход мысли (на интервью так и будет). Сначала сам, потом сверка с ответом.

**Если времени мало — абсолютный минимум:** Стримы 1–7, Пазлы P1–P6, Конкурентность K1–K4, Рефакторинг R1–R3, SQL S1–S7. Это ~1.5–2 вечера.

---

## 1. Stream API (самый частый live coding на скрининге)

Дано:
```java
record Employee(String name, String dept, BigDecimal salary) {}
record Order(long id, long customerId, List<String> items, BigDecimal amount) {}
```

### 1.1 🔥 Средняя зарплата по отделам
```java
Map<String, Double> avg = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept,
        Collectors.averagingDouble(e -> e.salary().doubleValue())));
```
Вслух: groupingBy с downstream-коллектором. Знай также counting(), summingInt(), mapping().

### 1.2 🔥 Топ-3 по зарплате в каждом отделе
```java
Map<String, List<Employee>> top3 = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept,
        Collectors.collectingAndThen(Collectors.toList(),
            l -> l.stream()
                .sorted(Comparator.comparing(Employee::salary).reversed())
                .limit(3)
                .toList())));
```

### 1.3 🔥 flatMap: все уникальные товары из заказов
```java
List<String> products = orders.stream()
    .flatMap(o -> o.items().stream())
    .distinct()
    .sorted()
    .toList();
```

### 1.4 🔥 Частота слов в тексте + топ-10
```java
Map<String, Long> freq = Arrays.stream(text.toLowerCase().split("\\W+"))
    .filter(s -> !s.isBlank())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

List<Map.Entry<String, Long>> top10 = freq.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(10)
    .toList();
```

### 1.5 🔥 toMap с дубликатами ключей
```java
Map<String, Employee> byName = employees.stream()
    .collect(Collectors.toMap(Employee::name, Function.identity(), (a, b) -> a));
```
Ловушка: без третьего аргумента (merge function) дубликат ключа → IllegalStateException. Про это спрашивают отдельно.

### 1.6 🔥 Сумма денег (финансовый домен — важно)
```java
BigDecimal total = orders.stream()
    .map(Order::amount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```
Обязательно проговори: деньги — только BigDecimal, никогда double (потеря точности в двоичном представлении). Для ЦФА-проекта это маркер зрелости.

### 1.7 🔥 Вторая по величине зарплата
```java
Optional<BigDecimal> second = employees.stream()
    .map(Employee::salary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .skip(1)
    .findFirst();
```
Уточни у интервьюера: считать ли одинаковые зарплаты одной (distinct) — сам вопрос-уточнение даёт очки.

### 1.8 ⚡ Дедупликация по полю (distinct by key)
```java
Set<String> seen = new HashSet<>();
List<Employee> unique = employees.stream()
    .filter(e -> seen.add(e.name()))
    .toList();
```
Альтернатива без внешнего состояния: `toMap(name, identity, (a,b)->a)` → values().

### 1.9 ⚡ partitioningBy
```java
Map<Boolean, List<Employee>> rich = employees.stream()
    .collect(Collectors.partitioningBy(
        e -> e.salary().compareTo(new BigDecimal("100000")) > 0));
```
BigDecimal сравнивать только compareTo, не equals (equals учитывает scale: 1.0 != 1.00).

### 1.10 ⚡ joining
```java
String csv = employees.stream().map(Employee::name)
    .collect(Collectors.joining(", ", "[", "]"));
```

### 1.11 ⚡ Пересечение двух списков
```java
Set<String> set2 = new HashSet<>(list2);
List<String> common = list1.stream().filter(set2::contains).distinct().toList();
```
Вслух: сначала HashSet, иначе O(n*m).

---

## 2. Алгоритмическая разминка (уровень скрининга, не LeetCode)

### 2.1 🔥 Сбалансированные скобки
```java
boolean isBalanced(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
    for (char c : s.toCharArray()) {
        if (pairs.containsValue(c)) stack.push(c);
        else if (pairs.containsKey(c)) {
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
        }
    }
    return stack.isEmpty();
}
```

### 2.2 🔥 Первый неповторяющийся символ
```java
Character firstUnique(String s) {
    Map<Character, Integer> counts = new LinkedHashMap<>();
    for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
    return counts.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst().orElse(null);
}
```
Вслух: LinkedHashMap ради порядка вставки, merge — идиома счётчика.

### 2.3 🔥 Палиндром (два указателя)
```java
boolean isPalindrome(String s) {
    int l = 0, r = s.length() - 1;
    while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
    return true;
}
```

### 2.4 ⚡ Анаграммы
```java
boolean isAnagram(String a, String b) {
    if (a.length() != b.length()) return false;
    int[] counts = new int[26];
    for (int i = 0; i < a.length(); i++) {
        counts[a.charAt(i) - 'a']++;
        counts[b.charAt(i) - 'a']--;
    }
    return Arrays.stream(counts).allMatch(c -> c == 0);
}
```

### 2.5 ⚡ Перевернуть слова в строке
```java
String reverseWords(String s) {
    List<String> words = Arrays.asList(s.trim().split("\\s+"));
    Collections.reverse(words);
    return String.join(" ", words);
}
```

### 2.6 ⚡ Два числа с суммой target (one-pass HashMap)
```java
int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        Integer j = seen.get(target - nums[i]);
        if (j != null) return new int[]{j, i};
        seen.put(nums[i], i);
    }
    return new int[0];
}
```

---

## 3. Пазлы «что выведет» (любимый формат внутренних ТИ)

### P1 🔥 Integer cache
```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
System.out.println(a == b); // true  (кэш -128..127)
System.out.println(c == d); // false
System.out.println(c.equals(d)); // true
```

### P2 🔥 Потерянный ключ HashMap
```java
List<Integer> key = new ArrayList<>(List.of(1));
Map<List<Integer>, String> map = new HashMap<>();
map.put(key, "v");
key.add(2);
System.out.println(map.get(key));            // null — hashCode изменился
System.out.println(map.get(List.of(1)));     // null — лежит в старом бакете
System.out.println(map.size());              // 1 — элемент есть, но недостижим
```

### P3 🔥 equals без hashCode
```java
class Point { int x; Point(int x){this.x=x;}
    @Override public boolean equals(Object o){ return o instanceof Point p && p.x == x; } }

Set<Point> set = new HashSet<>();
set.add(new Point(1));
System.out.println(set.contains(new Point(1))); // false — разные hashCode → разные бакеты
```

### P4 🔥 remove(int) vs remove(Object)
```java
List<Integer> list = new ArrayList<>(List.of(5, 10, 15));
list.remove(1);                    // по ИНДЕКСУ → [5, 15]
list.remove(Integer.valueOf(15));  // по значению → [5]
```

### P5 🔥 ConcurrentModificationException
```java
for (String s : list)
    if (s.startsWith("x")) list.remove(s); // CME (fail-fast, modCount)
```
Исправления: `list.removeIf(s -> s.startsWith("x"))` или явный Iterator + it.remove().

### P6 🔥 finally перебивает return
```java
int m() {
    try { return 1; }
    finally { return 2; }  // вернёт 2; так писать нельзя
}
```

### P7 ⚡ Тернарник и numeric promotion
```java
System.out.println(true ? 1 : 2.0); // 1.0 — типы ветвей приводятся к общему (double)

Integer x = null;
int y = true ? x : 0; // NPE — анбоксинг null
```

### P8 ⚡ String pool
```java
String a = "ab";
String b = "a" + "b";              // константа времени компиляции
String c = new String("ab");
System.out.println(a == b);        // true
System.out.println(a == c);        // false
System.out.println(a == c.intern()); // true
```

### P9 ⚡ i = i++
```java
int i = 0;
i = i++;
System.out.println(i); // 0 — старое значение вернулось присваиванием
```

---

## 4. Конкурентность (мини-задачи; JD: event-ы и шедулеры)

### K1 🔥 Гонка на счётчике + починить
```java
// сломано: c++ не атомарен (read-modify-write)
class Counter { int c; void inc() { c++; } }

// два потока по 100_000 инкрементов → результат < 200_000

// фикс 1
class Counter { private final AtomicInteger c = new AtomicInteger();
    void inc() { c.incrementAndGet(); } }

// фикс 2
class Counter { private int c;
    synchronized void inc() { c++; } }
```
Проговори: volatile здесь НЕ поможет — даёт видимость, не атомарность.

### K2 🔥 Producer–Consumer на BlockingQueue
```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
String POISON = "__STOP__";

Runnable producer = () -> {
    try {
        for (int i = 0; i < 1000; i++) queue.put("task-" + i);
        queue.put(POISON);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};

Runnable consumer = () -> {
    try {
        while (true) {
            String t = queue.take();
            if (POISON.equals(t)) break;
            process(t);
        }
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};
```
Ключевые слова вслух: ограниченная очередь = backpressure; put/take блокирующие; poison pill для остановки; InterruptedException → восстановить флаг interrupt.

### K3 🔥 Два потока печатают по очереди (ping-pong, wait/notify)
```java
class PingPong {
    private boolean pingTurn = true;

    synchronized void ping() throws InterruptedException {
        while (!pingTurn) wait();
        System.out.println("ping");
        pingTurn = false;
        notifyAll();
    }

    synchronized void pong() throws InterruptedException {
        while (pingTurn) wait();
        System.out.println("pong");
        pingTurn = true;
        notifyAll();
    }
}
```
Обязательные пункты: wait в цикле while (spurious wakeup), оба метода на одном мониторе, notifyAll.

### K4 🔥 Запустить N задач и дождаться всех
```java
ExecutorService pool = Executors.newFixedThreadPool(4);
try {
    List<Callable<Integer>> tasks = /* ... */;
    List<Future<Integer>> results = pool.invokeAll(tasks); // блокируется до завершения всех
    for (Future<Integer> f : results) total += f.get();
} finally {
    pool.shutdown();
}
```
Альтернатива — CompletableFuture.allOf; для «дождаться N событий» — CountDownLatch.

### K5 ⚡ Deadlock: воспроизвести и починить
```java
// deadlock: встречный порядок захвата
void t1() { synchronized (a) { synchronized (b) { } } }
void t2() { synchronized (b) { synchronized (a) { } } }

// фикс: единый глобальный порядок захвата (например, по identityHashCode / id сущности)
void transfer(Account x, Account y) {
    Account first = x.id() < y.id() ? x : y;
    Account second = x.id() < y.id() ? y : x;
    synchronized (first) { synchronized (second) { /* ... */ } }
}
```
Диагностика на проде: jstack / thread dump — JVM пишет "Found one Java-level deadlock".

### K6 ⚡ Два параллельных вызова + объединение (CompletableFuture)
```java
ExecutorService io = Executors.newFixedThreadPool(8);

CompletableFuture<BigDecimal> price = CompletableFuture.supplyAsync(this::fetchPrice, io);
CompletableFuture<BigDecimal> rate  = CompletableFuture.supplyAsync(this::fetchRate, io);

BigDecimal result = price
    .thenCombine(rate, BigDecimal::multiply)
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> BigDecimal.ZERO)
    .join();
```
Проговори: свой executor для IO (не common ForkJoinPool), таймаут обязателен.

---

## 5. Рефакторинг (формат «вот код — что не так, исправь»)

### R1 🔥 Цепочка if-else по типу → Strategy
```java
// ДО
void handle(AuctionEvent e) {
    if (e.type() == BID_PLACED) { /* 30 строк */ }
    else if (e.type() == AUCTION_FINISHED) { /* 40 строк */ }
    else if (e.type() == INVOICE_CREATED) { /* 25 строк */ }
}

// ПОСЛЕ (Spring-способ: карта хендлеров из бинов)
interface EventHandler {
    EventType type();
    void handle(AuctionEvent e);
}

@Component
class BidPlacedHandler implements EventHandler { /* ... */ }

@Component
class EventDispatcher {
    private final Map<EventType, EventHandler> handlers;

    EventDispatcher(List<EventHandler> list) {
        this.handlers = list.stream()
            .collect(Collectors.toMap(EventHandler::type, Function.identity()));
    }

    void dispatch(AuctionEvent e) {
        EventHandler h = handlers.get(e.type());
        if (h == null) throw new IllegalStateException("No handler: " + e.type());
        h.handle(e);
    }
}
```
Что назвать: Strategy + Open/Closed (новый тип = новый бин, диспетчер не трогаем). Это твой реальный кейс с auction events — скажи об этом.

### R2 🔥 Внешний вызов внутри транзакции → Outbox
```java
// ДО: что не так?
@Transactional
public void finishAuction(long auctionId) {
    Auction a = repo.findById(auctionId).orElseThrow();
    a.finish();
    blockchainClient.publishResult(a);   // 1
    kafkaTemplate.send("auction-finished", toEvent(a)); // 2
}
```
Проблемы: (1) медленный внешний вызов держит БД-соединение и транзакцию; (2) при rollback после отправки внешний эффект уже случился — рассинхрон; (3) при падении отправки после коммита — событие потеряно.
```java
// ПОСЛЕ
@Transactional
public void finishAuction(long auctionId) {
    Auction a = repo.findById(auctionId).orElseThrow();
    a.finish();
    outboxRepo.save(OutboxRecord.of("AUCTION_FINISHED", toPayload(a))); // та же транзакция
}

@Scheduled(fixedDelay = 5000)
public void publishOutbox() {
    List<OutboxRecord> batch = outboxRepo.lockPending(10); // FOR UPDATE SKIP LOCKED
    for (OutboxRecord r : batch) {
        try {
            blockchainClient.publish(r.payload());
            r.markSent(txHash);
        } catch (Exception e) {
            r.incAttempts(); // ретрай следующим проходом, после N — алерт/DLQ
        }
    }
}
```
Это одновременно ответ на «как надёжно публиковать в блокчейн» — центральная тема вакансии.

### R3 🔥 Self-invocation @Transactional
```java
// ДО: saveOne выполняется БЕЗ транзакции — вызов через this минует прокси
@Service
public class ImportService {
    public void importAll(List<Row> rows) {
        rows.forEach(this::saveOne);
    }
    @Transactional
    public void saveOne(Row r) { /* ... */ }
}

// ПОСЛЕ: вынести в отдельный бин
@Service
public class ImportService {
    private final RowSaver saver;
    public void importAll(List<Row> rows) { rows.forEach(saver::saveOne); }
}

@Service
public class RowSaver {
    @Transactional
    public void saveOne(Row r) { /* ... */ }
}
```
Альтернатива — TransactionTemplate (программные транзакции). Заодно ответь, где ещё @Transactional молча не работает: private-методы, исключение поймано внутри, checked без rollbackFor.

### R4 🔥 Проглоченное исключение
```java
// ДО
try {
    blockchainClient.publish(payload);
} catch (Exception e) {
    e.printStackTrace(); // проглотили: вызывающий считает, что всё ок
}

// ПОСЛЕ
try {
    blockchainClient.publish(payload);
} catch (BlockchainClientException e) {
    log.error("Publish failed, lotId={}", lotId, e);
    throw new PublicationException("Blockchain publish failed for lot " + lotId, e);
}
```
Правила: не глотать; логировать с контекстом и cause; ловить конкретный тип; не логировать И пробрасывать одновременно на каждом уровне (дубли в логах).

### R5 ⚡ null-цепочка → Optional
```java
// ДО
String city = null;
if (user != null && user.getAddress() != null && user.getAddress().getCity() != null)
    city = user.getAddress().getCity().toUpperCase();

// ПОСЛЕ
String city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity)
    .map(String::toUpperCase)
    .orElse(null);
```

### R6 ⚡ double для денег → BigDecimal
```java
// ДО
double total = price * quantity * (1 - discount); // 0.1 + 0.2 != 0.3

// ПОСЛЕ
BigDecimal total = price
    .multiply(BigDecimal.valueOf(quantity))
    .multiply(BigDecimal.ONE.subtract(discount))
    .setScale(2, RoundingMode.HALF_UP);
```
BigDecimal: создавать из String/valueOf (не из double), сравнивать compareTo, явный RoundingMode.

---

## 6. SQL (PostgreSQL)

Схема для задач:
```sql
employees(id, name, dept_id, salary)
departments(id, name)
customers(id, name, email)
orders(id, customer_id, amount, created_at)
bids(id, lot_id, bidder_id, amount, created_at)
```

### S1 🔥 Вторая по величине зарплата
```sql
SELECT DISTINCT salary FROM employees ORDER BY salary DESC OFFSET 1 LIMIT 1;
-- или
SELECT MAX(salary) FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```

### S2 🔥 Сотрудники с зарплатой выше средней по СВОЕМУ отделу
```sql
-- коррелированный подзапрос
SELECT e.* FROM employees e
WHERE e.salary > (SELECT AVG(salary) FROM employees WHERE dept_id = e.dept_id);

-- оконная (назови оба способа)
SELECT * FROM (
    SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg
    FROM employees e
) t
WHERE salary > dept_avg;
```

### S3 🔥 JOIN + GROUP BY + HAVING: отделы, где >5 сотрудников
```sql
SELECT d.name, COUNT(*) AS cnt, ROUND(AVG(e.salary), 2) AS avg_salary
FROM employees e
JOIN departments d ON d.id = e.dept_id
GROUP BY d.name
HAVING COUNT(*) > 5
ORDER BY avg_salary DESC;
```
Проговори: WHERE — до группировки, HAVING — после.

### S4 🔥 Клиенты без заказов
```sql
SELECT c.* FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE o.id IS NULL;

-- или (безопаснее и часто быстрее)
SELECT c.* FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.id);
```
Ловушка для проговора: NOT IN с NULL в подзапросе вернёт пустой результат — поэтому NOT EXISTS.

### S5 🔥 Найти и удалить дубликаты (оставить минимальный id)
```sql
SELECT email, COUNT(*) FROM customers GROUP BY email HAVING COUNT(*) > 1;

DELETE FROM customers c
USING customers c2
WHERE c.email = c2.email AND c.id > c2.id;
```

### S6 🔥 Максимальная ставка по каждому лоту (твой домен — расскажут пальцы)
```sql
-- универсально: оконная функция
SELECT * FROM (
    SELECT b.*, ROW_NUMBER() OVER (PARTITION BY lot_id ORDER BY amount DESC) AS rn
    FROM bids b
) t WHERE rn = 1;

-- PG-идиома
SELECT DISTINCT ON (lot_id) *
FROM bids
ORDER BY lot_id, amount DESC;
```
Разница ROW_NUMBER / RANK / DENSE_RANK — спросят следом: rank даёт пропуски при равенстве, dense_rank — нет.

### S7 🔥 Топ-3 зарплаты в каждом отделе
```sql
SELECT * FROM (
    SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk
    FROM employees e
) t WHERE rnk <= 3;
```

### S8 ⚡ Нарастающий итог по дням
```sql
SELECT created_at::date AS day,
       SUM(amount) AS day_total,
       SUM(SUM(amount)) OVER (ORDER BY created_at::date) AS running_total
FROM orders
GROUP BY created_at::date
ORDER BY day;
```

### S9 ⚡ Keyset-пагинация (расскажи через свой delta-endpoint)
```sql
SELECT * FROM bids
WHERE (created_at, id) > (:last_created_at, :last_id)
ORDER BY created_at, id
LIMIT 100;
```
Почему не OFFSET: сканирует и выбрасывает все пропускаемые строки, деградирует линейно.

### S10 ⚡ Идемпотентная вставка (дедуп событий)
```sql
INSERT INTO processed_events(event_id, processed_at)
VALUES (:event_id, now())
ON CONFLICT (event_id) DO NOTHING;
```
Вставилась строка → обрабатываем; конфликт → дубль, скип. Основа идемпотентного Kafka/blockchain-консюмера.

### S11 ⚡ Очередь задач: FOR UPDATE SKIP LOCKED (связка с outbox из R2)
```sql
UPDATE outbox SET status = 'SENDING', locked_at = now()
WHERE id IN (
    SELECT id FROM outbox
    WHERE status = 'PENDING'
    ORDER BY id
    LIMIT 10
    FOR UPDATE SKIP LOCKED
)
RETURNING *;
```
Несколько воркеров разбирают задачи без конфликтов и без внешнего брокера.

### S12 ⚡ Почему индекс не работает + починить
```sql
-- индекс по created_at не используется:
SELECT * FROM orders WHERE date(created_at) = '2026-07-01';

-- фикс: диапазон вместо функции над колонкой
SELECT * FROM orders
WHERE created_at >= '2026-07-01' AND created_at < '2026-07-02';
```
Другие причины из той же серии: неявный каст типов, LIKE '%x', не-leading колонка составного индекса. Проверка — EXPLAIN (ANALYZE, BUFFERS).

---

## 7. Порядок прорешивания (2–3 вечера)

1. **Вечер 1:** Стримы 1.1–1.7 + пазлы P1–P6 (пазлы просто прочитать, предсказав вывод до ответа).
2. **Вечер 2:** Конкурентность K1–K4 (K3 написать с нуля дважды — самая частая «напиши руками») + рефакторинг R1–R3.
3. **Вечер 3:** SQL S1–S7 руками в psql/DataGrip на игрушечных таблицах + добить ⚡ по остатку времени.

Критерий готовности: любую 🔥-задачу решаешь за 5–7 минут с проговором вслух, без подглядывания.
