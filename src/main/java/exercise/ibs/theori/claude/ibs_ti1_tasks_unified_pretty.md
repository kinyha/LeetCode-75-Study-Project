# ТИ-1 IBS — единый банк задач с решениями

> Единый файл по задачам. Собрано из основного банка задач и отдельного файла с фактической структурой/гипотезами. Внешних ссылок между файлами больше нет: всё нужное встроено здесь.

## 0. Как читать

**Приоритет подготовки:**

1. **Live code review** — самая вероятная категория: обоим коллегам дали именно её.
2. **Spring/Stream coding tasks** — если попросят писать небольшой сервисный код.
3. **Алгоритмы** — базовые задачи, но шанс ниже, чем у review.

**Как отвечать на review:**

- сначала называй риск;
- потом фикс;
- потом edge case;
- потом как тестировать.

Пример формата: «Скорее всего дали такой код. В нём проблема X. Правильно сделать Y, потому что иначе на проде будет Z». То есть не просто “я бы переписал”, а нормальное инженерное объяснение, редкий вид взрослого поведения.

---

## 1. Быстрый приоритет

### Учить первым

- `updateOrderStatus`: enum, transition validation, `@Transactional`, `@Version`, custom exceptions.
- REST client: timeout, typed DTO, error handling, config, resilience.
- Payment/transfer: BigDecimal, транзакции, блокировки, idempotency key.
- Kafka listener: at-least-once, idempotent consumer, DLQ, offset commit.
- Singleton bean concurrency: `HashMap`, `SimpleDateFormat`, `computeIfAbsent`, atomics.
- N+1: pagination, DTO projection, fetch join/entity graph.

### Универсальный senior-набор фраз

- «Сначала уточню бизнес-инварианты и допустимые переходы».
- «Нужна идемпотентность, потому что клиент/брокер может повторить запрос».
- «Транзакция не спасает от всех гонок: read-modify-write требует lock/optimistic version/atomic update».
- «Внешний HTTP/Kafka/blockchain внутри БД-транзакции держит соединение и ломает консистентность, лучше outbox».
- «Проверил бы unit-тестами бизнес-правила и интеграционно на Testcontainers».

---

## 2. Алгоритмические задачи (15) — решения

### 1.1 Количество пар с суммой 0

```java
static long countZeroSumPairs(int[] arr) {
    Map<Integer, Long> cnt = new HashMap<>();
    for (int x : arr) cnt.merge(x, 1L, Long::sum);
    long pairs = 0;
    for (var e : cnt.entrySet()) {
        int v = e.getKey();
        if (v > 0) pairs += e.getValue() * cnt.getOrDefault(-v, 0L);
        else if (v == 0) pairs += e.getValue() * (e.getValue() - 1) / 2;
    }
    return pairs;
}
```
Проговорить: наивно O(n²) двойным циклом; через мапу счётчиков O(n). Нули — пары внутри группы: C(k,2). Уточни у интервьюера: пары по индексам (i<j) или уникальные значения — от этого зависит формула.

### 1.2 Поиск элемента в BST

```java
static TreeNode find(TreeNode root, int key) {
    TreeNode cur = root;
    while (cur != null && cur.val != key)
        cur = key < cur.val ? cur.left : cur.right;
    return cur;
}
```
O(h): log n на сбалансированном, n на вырожденном. Итеративно лучше рекурсии (нет риска StackOverflow).

### 1.3 Перевернуть односвязный список

```java
static Node reverse(Node head) {
    Node prev = null;
    while (head != null) {
        Node next = head.next;
        head.next = prev;
        prev = head;
        head = next;
    }
    return prev;
}
```
Три указателя, O(n)/O(1). Классика — написать без подглядывания дважды.

### 1.4 Слить два отсортированных массива без сортировки

```java
static int[] merge(int[] a, int[] b) {
    int[] r = new int[a.length + b.length];
    int i = 0, j = 0, k = 0;
    while (i < a.length && j < b.length)
        r[k++] = a[i] <= b[j] ? a[i++] : b[j++];
    while (i < a.length) r[k++] = a[i++];
    while (j < b.length) r[k++] = b[j++];
    return r;
}
```
Два указателя, O(n+m). На их примере {3,5,6,14,23} + {2,3,6,7} → {2,3,3,5,6,6,7,14,23} — дубликаты сохраняются (уточни, если нужно без них — пропуск равных).

### 1.5 Найти единственный дубликат

```java
static int findDuplicate(int[] arr) {
    Set<Integer> seen = new HashSet<>();
    for (int x : arr) if (!seen.add(x)) return x;
    throw new IllegalArgumentException("duplicate not found");
}
```
O(n)/O(n). Бонус на «оптимизацию»: если числа 1..n — через сумму `actualSum - n*(n+1)/2` за O(1) памяти; назвать существование алгоритма Флойда (цикл в «связном списке» индексов) — писать не обязательно.

### 1.6 Найти число в матрице (строки отсортированы, конец строки ≤ начало следующей)

```java
static boolean exists(int[][] m, int target) {
    int cols = m[0].length;
    int lo = 0, hi = m.length * cols - 1;
    while (lo <= hi) {
        int mid = (lo + hi) >>> 1;
        int v = m[mid / cols][mid % cols];
        if (v == target) return true;
        if (v < target) lo = mid + 1;
        else hi = mid - 1;
    }
    return false;
}
```
Матрица по условию — один отсортированный массив → бинарный поиск по виртуальному индексу, O(log(n·m)). `>>> 1` вместо `/2` — защита от переполнения (senior-ремарка).

### 1.7 Количество вхождений числа в массив

```java
static long count(int[] arr, int target) {
    return Arrays.stream(arr).filter(x -> x == target).count();
}
```
O(n). Если массив отсортирован — два бинарных поиска (первое и последнее вхождение), O(log n) — скажи как оптимизацию.

### 1.8 Числа 1–100: делятся на 2, но не на 4

```java
IntStream.rangeClosed(1, 100)
    .filter(i -> i % 2 == 0 && i % 4 != 0)
    .forEach(System.out::println);
```
Это числа вида 4k+2: 2, 6, 10, …, 98. Можно циклом `for (int i = 2; i <= 100; i += 4)` — O(1) проверок, красивый ответ на «оптимизируй».

### 1.9 Наибольший из повторяющихся элементов двух массивов

```java
static OptionalInt maxCommon(int[] a, int[] b) {
    Set<Integer> setA = Arrays.stream(a).boxed().collect(Collectors.toSet());
    return Arrays.stream(b).filter(setA::contains).max();
}
```
На их примере пересечение {3, 6, 23, 33} → 33. O(n+m). OptionalInt вместо -1/null — плюс в карму.

### 1.10 Топ-5 элементов с повторами

Их пример: {12,15,17,21,18,14,11,21,35,14,18,18} → {35, 21, 21, 18, 18, 18, 17, 15}. Т.е. топ-5 РАЗЛИЧНЫХ значений, каждое повторено столько раз, сколько встречается:

```java
static List<Integer> top5WithDuplicates(List<Integer> nums) {
    return nums.stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
        .limit(5)
        .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
        .toList();
}
```
Сначала озвучь интерпретацию по их примеру (в выводе 8 элементов при «топ-5» — значит топ-5 значений с повторами) — уточнение условия здесь буквально заложено в задачу.

### 1.11 Первый неповторяющийся символ (вернуть null character, если все повторяются)

```java
static char firstUnique(String s) {
    Map<Character, Integer> cnt = new LinkedHashMap<>();
    for (char c : s.toCharArray()) cnt.merge(c, 1, Integer::sum);
    for (var e : cnt.entrySet())
        if (e.getValue() == 1) return e.getKey();
    return '\0';
}
```
LinkedHashMap — порядок первого появления; два прохода O(n).

### 1.12 Мин и макс температура за сутки

```java
static void minMax(List<Double> temperatures) {
    DoubleSummaryStatistics stats = temperatures.stream()
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();
    System.out.printf("min=%.1f max=%.1f%n", stats.getMin(), stats.getMax());
}
```
Или один цикл с двумя переменными. Edge cases вслух: пустой список (summaryStatistics вернёт +∞/−∞ — проверить count()==0), null-элементы.

### 1.13 Рейтинг поста (класс: добавление оценок 0–5, средняя в любой момент)

```java
public class PostRating {

    private long count;
    private long sum;

    public synchronized void addRating(int rating) {
        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("rating must be 0..5: " + rating);
        sum += rating;
        count++;
    }

    public synchronized double getAverage() {
        return count == 0 ? 0.0 : (double) sum / count;
    }
}
```
Ключевые тезисы (за них дают senior): хранить сумму и счётчик, а не список оценок — O(1) память и время; валидация диапазона; потокобезопасность — оценки прилетают конкурентно: synchronized на оба метода (два отдельных Atomic НЕ дают согласованную пару sum/count); переполнение long практически недостижимо, но упомянуть.

### 1.14 Три стека в одном массиве фиксированного размера

```java
public class ThreeStacks {

    private final int[] data;
    private final int[] top = {-1, -1, -1};
    private final int cap;

    public ThreeStacks(int capacityPerStack) {
        this.cap = capacityPerStack;
        this.data = new int[capacityPerStack * 3];
    }

    public void push(int stack, int value) {
        if (top[stack] + 1 >= cap) throw new IllegalStateException("stack " + stack + " is full");
        data[stack * cap + ++top[stack]] = value;
    }

    public int pop(int stack) {
        if (top[stack] < 0) throw new NoSuchElementException("stack " + stack + " is empty");
        return data[stack * cap + top[stack]--];
    }

    public int peek(int stack) {
        if (top[stack] < 0) throw new NoSuchElementException("stack " + stack + " is empty");
        return data[stack * cap + top[stack]];
    }
}
```
Простое решение — фиксированное деление массива на три части (индекс = stack·cap + top). На «оптимизируй»: гибкое деление (стеки растут навстречу / freelist) — опиши идею словами, писать не проси́ли.

### 1.15 Обход дерева / прочее

Если дадут обход (in-order BST даёт сортировку):

```java
static void inOrder(TreeNode node, List<Integer> out) {
    if (node == null) return;
    inOrder(node.left, out);
    out.add(node.val);
    inOrder(node.right, out);
}
```
BFS — через ArrayDeque (offer/poll), DFS итеративный — через стек. Знай названия и сложность O(n).

---

## 3. Coding tasks — Spring/Stream (12) — решения

Общий контекст задач: домен заказов. Держи в голове модель:

```java
enum OrderStatus { UNPAID, PAID, PENDING, IN_PROGRESS, COMPLETED, CANCELLED }

record OrderItem(Long productId, BigDecimal price, int quantity) {}

class Order {
    Long id; Long customerId; OrderStatus status; List<OrderItem> items;
    BigDecimal getTotal() {
        return items.stream()
            .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

Во всех задачах: деньги — BigDecimal, инжекция — конструктором, вслух edge cases (пустой список, null, деление на ноль).

### 2.1 Группировка заказов по статусам

```java
public Map<OrderStatus, List<Order>> groupByStatus(List<Order> orders) {
    return orders.stream().collect(Collectors.groupingBy(Order::getStatus));
}
```
Доп. вопросы, к которым готовься: отсортированные группы → `groupingBy(..., TreeMap::new, toList())`; только количество → downstream `counting()`.

### 2.2 Проверка: можно ли выполнить заказ по остаткам склада

```java
public boolean canFulfill(Order order, Map<Long, Integer> stock) {
    Map<Long, Integer> required = order.getItems().stream()
        .collect(Collectors.groupingBy(OrderItem::productId,
                 Collectors.summingInt(OrderItem::quantity)));
    return required.entrySet().stream()
        .allMatch(e -> stock.getOrDefault(e.getKey(), 0) >= e.getValue());
}
```
Senior-деталь, которую закладывают: один товар может встретиться в заказе несколькими позициями — сначала агрегируй required по productId, иначе проверка врёт.

### 2.3 То же с обращением к БД

```java
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductIdIn(Collection<Long> productIds);
}

@Transactional(readOnly = true)
public boolean canFulfill(Order order) {
    Map<Long, Integer> required = order.getItems().stream()
        .collect(Collectors.groupingBy(OrderItem::productId,
                 Collectors.summingInt(OrderItem::quantity)));

    Map<Long, Integer> available = stockRepository.findByProductIdIn(required.keySet()).stream()
        .collect(Collectors.toMap(Stock::getProductId, Stock::getQuantity));

    return required.entrySet().stream()
        .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
}
```
Проговорить: ОДИН запрос с IN, а не запрос на товар в цикле (N+1); проверка «можно ли» — момент истины только под блокировкой, если дальше резервируем — FOR UPDATE (мостик к review-задаче 3.3).

### 2.4 Заказы с общей стоимостью ниже лимита

```java
public List<Order> ordersBelow(List<Order> orders, BigDecimal limit) {
    return orders.stream()
        .filter(o -> o.getTotal().compareTo(limit) < 0)
        .toList();
}
```
BigDecimal сравнивать только compareTo (equals учитывает scale).

### 2.5 Заказы, где средняя цена позиций выше целевой

```java
public List<Order> withAvgItemPriceAbove(List<Order> orders, BigDecimal target) {
    return orders.stream()
        .filter(o -> !o.getItems().isEmpty())
        .filter(o -> avgItemPrice(o).compareTo(target) > 0)
        .toList();
}

private BigDecimal avgItemPrice(Order o) {
    BigDecimal sum = o.getItems().stream()
        .map(OrderItem::price)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(o.getItems().size()), 2, RoundingMode.HALF_UP);
}
```
Заложенные грабли: деление на ноль при пустых items (фильтр до), divide без scale/RoundingMode кинет ArithmeticException на непериодической дроби. Уточни: средняя по цене позиций или взвешенная по количеству — бизнес-вопрос, за который хвалят.

### 2.6 Средняя стоимость заказов UNPAID из Map<OrderStatus, List<Order>>

```java
public BigDecimal avgUnpaidCost(Map<OrderStatus, List<Order>> ordersByStatus) {
    List<Order> unpaid = ordersByStatus.getOrDefault(OrderStatus.UNPAID, List.of());
    if (unpaid.isEmpty()) return BigDecimal.ZERO;
    BigDecimal sum = unpaid.stream()
        .map(Order::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(unpaid.size()), 2, RoundingMode.HALF_UP);
}
```
getOrDefault против NPE; пустой список — договорись, что возвращаем (ZERO или Optional).

### 2.7 Остатки склада с пагинацией: Page<Stock> getStockWithPagination(int page, int size)

```java
public Page<Stock> getStockWithPagination(int page, int size) {
    if (page < 0 || size <= 0 || size > 200)
        throw new IllegalArgumentException("invalid pagination: page=" + page + " size=" + size);
    return stockRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
}
```
Проговорить: явная сортировка обязательна (без неё порядок страниц не гарантирован — дубли/пропуски между страницами); cap на size — защита от выгрузки всей таблицы; Page делает дополнительный count-запрос — если счётчик не нужен, Slice дешевле; на больших глубинах OFFSET деградирует → keyset (твой delta-endpoint).

### 2.8 Заказы по статусу с фильтрацией на уровне БД (+ Specification)

```java
public interface OrderRepository extends JpaRepository<Order, Long>,
                                         JpaSpecificationExecutor<Order> {
    List<Order> findByStatus(OrderStatus status);
}

public List<Order> getOrdersByStatus(OrderStatus status) {
    return orderRepository.findByStatus(status);
}

// вариант со спецификацией (название задачи намекает) — для комбинируемых фильтров
public static Specification<Order> hasStatus(OrderStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
}
// orderRepository.findAll(hasStatus(status).and(createdAfter(date)))
```
Смысл задачи — «НЕ findAll().stream().filter(...)»: фильтрует БД (индекс по status), в приложение едут только нужные строки. Скажи это первой фразой.

### 2.9 REST-контроллер POST создания заказа

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto created = orderService.create(request);
        return ResponseEntity
            .created(URI.create("/orders/" + created.id()))
            .body(created);
    }
}

public record CreateOrderRequest(
    @NotNull Long customerId,
    @NotEmpty List<@Valid OrderItemDto> items
) {}
```
Чек: 201 + Location (не 200), DTO на вход/выход (не entity), @Valid + advice на MethodArgumentNotValidException → 400. Ремарка: в условии URL /create-order — глагол в URI не RESTful, правильнее POST /orders; озвучь мягко, это очки.

### 2.10 POST с несколькими параметрами в теле → список заказов

```java
public record OrderSearchRequest(
    OrderStatus status,
    LocalDate from,
    LocalDate to,
    @Positive BigDecimal minTotal
) {}

@PostMapping("/orders/search")
public List<OrderDto> search(@Valid @RequestBody OrderSearchRequest request) {
    return orderService.search(request);
}
```
Несколько параметров = один DTO, не пачка @RequestParam. POST /search — принятая конвенция для сложных фильтров (GET с телом не используют); фильтрация — на уровне БД (Specification из 2.8).

### 2.11 Платёжные транзакции: количество успешных + их сумма

```java
public record PaymentSummary(long count, BigDecimal total) {}

public PaymentSummary summarize(List<Transaction> transactions) {
    return transactions.stream()
        .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
        .collect(Collectors.teeing(
            Collectors.counting(),
            Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add),
            PaymentSummary::new));
}
```
teeing (Java 12) — два агрегата за один проход; если не вспомнишь на собесе — честный вариант с промежуточным списком или циклом тоже ок, teeing назови как оптимизацию.

### 2.12 Итоговая стоимость покупок со скидками

```java
public interface DiscountPolicy {
    boolean isApplicable(Purchase purchase);
    BigDecimal apply(BigDecimal currentPrice, Purchase purchase);
}

public BigDecimal totalPrice(List<Purchase> purchases, List<DiscountPolicy> policies) {
    BigDecimal total = BigDecimal.ZERO;
    for (Purchase p : purchases) {
        BigDecimal price = p.getPrice();
        for (DiscountPolicy policy : policies)
            if (policy.isApplicable(p))
                price = policy.apply(price, p);
        total = total.add(price);
    }
    return total.setScale(2, RoundingMode.HALF_UP);
}
```
Это задача на Strategy (у Миши по Strategy — junior, у Вадима спрашивали её же — тема у них любимая). Обязательно уточни бизнес: скидки складываются или берётся максимальная? порядок применения? скидка не уводит цену ниже нуля? Каждое уточнение — плюс к оценке.

---

## 4. Live code review задачи

Это главный раздел. По факту обоим коллегам дали именно code review, а не чистый алгоритм. Поэтому учить его первым, потом уже Spring/Stream и алгоритмы.

### 4.0 Методика ревью

1. Прочитай код целиком молча 30–60 секунд.
2. Скажи: «Пройдусь сверху вниз: корректность, обработка ошибок, транзакции, конкурентность, дизайн и тесты».
3. Задай бизнес-вопросы: какие статусы валидны, какие переходы разрешены, кто имеет право менять ресурс, что делать при повторном запросе.
4. Каждую правку объясняй через риск: потеря денег, дубль, NPE, lost update, N+1, зависший поток.
5. В конце скажи, как бы тестировал: unit + integration на Testcontainers + конкурентный сценарий, если он есть.

### 4.0.1 Быстрый чек-лист

Инжекция → Optional/NPE → enum вместо строк → кастомные исключения + advice → проверки существования и прав → `@Valid` → `@Transactional` → конкурентность (`@Version` / lock) → DTO на границе → логирование → тесты.

### 4.0.2 Основной список code review задач

| # | Задача | Статус |
|---|---|---|
| 1 | Циклические зависимости Spring | банк |
| 2 | Обновление статуса заказа `updateOrderStatus` | факт: давали Мише |
| 3 | Подтверждение заказа с проверкой остатков | банк |
| 4 | Обработка платежа `processPayment` | банк / банковский must-know |
| 5 | Возврат статуса из БД | банк |
| 6 | Получение имени пользователя через REST | факт: давали Вадиму |
| 7 | Внешняя интеграция и асинхрон | банк |
| 8 | Kafka listener | банк подтвердил гипотезу |
| 9 | Потоки и синхронизация в Spring | банк / гипотеза |
| 10 | Интеграция с внешним API | банк |

Формат карточки: **что скорее всего дали** → **что не так** → **как правильно**. Да, прямо как нормальная документация, редкое животное.


### 4.1 Циклические зависимости Spring

**Скорее всего дали такой код:**

```java
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService;

    public void placeOrder(Order o) { paymentService.charge(o); }
    public void cancelOrder(Long id) { /* ... */ }
}

@Service
public class PaymentService {
    @Autowired
    private OrderService orderService;

    public void charge(Order o) { /* ... */ }
    public void onPaymentFailed(Long orderId) { orderService.cancelOrder(orderId); }
}
```

****Что не так:****
1. Цикл A↔B. С constructor injection упал бы на старте (BeanCurrentlyInCreationException) — и это ПЛЮС: ошибка видна сразу. Field injection цикл маскирует; при этом с Boot 2.6+ циклы запрещены по умолчанию в любом виде.
2. Плохие фиксы, которые предложат «на слабо»: @Lazy на одну из зависимостей (откладывает создание — цикл остаётся в дизайне), `spring.main.allow-circular-references=true` (легализация проблемы). Назови их именно как костыли.
3. Правильные фиксы: (а) выделить общую логику в третий сервис — цикл обычно значит, что ответственность спутана; (б) инверсия через события: PaymentService публикует PaymentFailedEvent (ApplicationEventPublisher), OrderService слушает @EventListener — зависимость в одну сторону; (в) интерфейс + узкий callback.
4. Сказать про уровни: циклы бывают и между модулями/микросервисами — там решается оркестратором или событиями (их же критерий сеньорности: метод → сервис → приложение).


**Как правильно мыслить:**

```java
@Service
public class PaymentService {

    private final ApplicationEventPublisher events;

    public PaymentService(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void onPaymentFailed(Long orderId) {
        events.publishEvent(new PaymentFailedEvent(orderId));
    }
}

@Component
public class OrderPaymentListener {

    private final OrderService orderService;

    public OrderPaymentListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handle(PaymentFailedEvent event) {
        orderService.cancelOrder(event.orderId());
    }
}
```

`@Lazy` и `allow-circular-references=true` можно назвать как временные костыли, но правильный ответ — разорвать ответственность: общий сервис, события или оркестратор.


---

### 4.2 Обновление статуса заказа `updateOrderStatus` `[факт: давали Мише]`

Важно: скрин с решением Миши, скорее всего, был уже **его отрефакторенной попыткой**, а не исходным кодом задачи. Значит, на вход могли дать более простой плохой сервисный метод: обновить статус заказа, сохранить его и отправить уведомление пользователю для некоторых статусов.

**Скорее всего дали такой код:**

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).get();

        if (newStatus.equals("COMPLETED")) {
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            notificationService.notifyUser(
                order.getUserId(),
                "Your order is completed"
            );
        } else if (newStatus.equals("CANCELLED")) {
            order.setStatus("CANCELLED");
            order.setReason("CANCELLED reason");
            orderRepository.save(order);
            notificationService.notifyUser(
                order.getUserId(),
                "Your order is cancelled"
            );
        } else if (newStatus.equals("PENDING")) {
            order.setStatus("PENDING");
            orderRepository.save(order);
        } else if (newStatus.equals("IN_PROGRESS")) {
            order.setStatus("IN_PROGRESS");
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Unsupported status: " + newStatus);
        }
    }
}
```

**Что не так:**

1. **Field injection через @Autowired** → constructor injection: поля final, зависимости обязательны и видны, тестируется без Spring, циклы падают на старте. С одним конструктором аннотация не нужна.
2. **`findById(...).get()`** — NoSuchElementException на пустом Optional. → `orElseThrow(() -> new OrderNotFoundException(orderId))`.
3. **Статус строкой** — magic strings, опечатки не ловятся компилятором. → `enum OrderStatus`, в entity `@Enumerated(EnumType.STRING)`.
4. **`newStatus.equals(...)` упадёт NPE при `newStatus == null`**. Но правильный фикс — не переворачивать equals, а принимать enum/DTO с validation.
5. **Длинная if-else цепочка** → лучше `switch` по enum или отдельная логика переходов.
6. **Дублирование `save` и `notifyUser`** в ветках.
7. **Нет проверки `userId` перед уведомлением**: если `order.getUserId()` null, уведомление сломается или уйдёт некорректно.
8. **`IllegalArgumentException` как бизнес-ошибка** → лучше custom exceptions + маппинг через `@RestControllerAdvice`.
9. **Нет `@Transactional`** — операция read-modify-write должна быть атомарной.
10. **Уведомление внутри бизнес-операции**: если сохранение/транзакция откатится, пользователь может получить уведомление о событии, которого фактически нет. Лучше событие после успешного коммита или outbox.
11. **Нет валидации переходов статусов**: если бизнес запрещает, например, `CANCELLED -> COMPLETED`, это надо проверять явно.
12. Метод `void` — часто удобнее вернуть обновлённый DTO.

**Нормальный вариант ответа на интервью:**

```java
public enum OrderStatus {
    COMPLETED,
    CANCELLED,
    PENDING,
    IN_PROGRESS
}

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            order.setReason("CANCELLED reason");
        }

        if (requiresNotification(newStatus)) {
            if (order.getUserId() == null) {
                throw new OrderUserNotFoundException(orderId);
            }
            events.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                order.getUserId(),
                newStatus
            ));
        }

        return OrderDto.from(order);
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Здесь должна быть бизнес-валидация переходов, если она требуется.
    }

    private boolean requiresNotification(OrderStatus status) {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }
}
```

```java
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String message = switch (event.status()) {
            case COMPLETED -> "Your order is completed";
            case CANCELLED -> "Your order is cancelled";
            default -> null;
        };

        if (message != null) {
            notificationService.notifyUser(event.userId(), message);
        }
    }
}
```

Если не хочется усложнять событиями, минимальный acceptable-вариант — оставить `NotificationService` в `OrderService`, но всё равно сделать constructor injection, enum, `orElseThrow`, `switch`, custom exceptions и `@Transactional`.

**Более простой вариант, если интервьюер ждёт именно рефакторинг метода без событий:**

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        switch (newStatus) {
            case COMPLETED -> complete(order);
            case CANCELLED -> cancel(order);
            case PENDING, IN_PROGRESS -> order.setStatus(newStatus);
            default -> throw new UnsupportedOrderStatusException(newStatus);
        }

        return OrderDto.from(order);
    }

    private void complete(Order order) {
        order.setStatus(OrderStatus.COMPLETED);
        notifyUser(order, "Your order is completed");
    }

    private void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setReason("CANCELLED reason");
        notifyUser(order, "Your order is cancelled");
    }

    private void notifyUser(Order order, String message) {
        if (order.getUserId() == null) {
            throw new OrderUserNotFoundException(order.getId());
        }
        notificationService.notifyUser(order.getUserId(), message);
    }
}
```

#### Фактическое решение Миши: почему 3.33, а не 4.00

Его код из скоркарты был уже попыткой такого рефакторинга. По скрину видно, что направление было правильное:

1. Заменил field injection на constructor injection через Lombok.
2. Заменил `String newStatus` на `OrderStatus newStatus`.
3. Добавил `enum OrderStatus`.
4. Попытался заменить `.get()` на `orElseThrow`.
5. Добавил custom exception.
6. Добавил проверку `userId != null`.
7. Попытался заменить if-else на switch.
8. Вынес часть логики в отдельный метод.

Почему реализация всё равно не идеальная:

1. `@RequiredAgrsContruct` написано с ошибкой, должно быть `@RequiredArgsConstructor`.
2. `getOrThrow` не существует, нужно `orElseThrow`.
3. `orElseThrow(() -> { throw new ServExp(); })` неверно: supplier должен вернуть exception, правильно `orElseThrow(() -> new ServExp())`.
4. `throw ServExp;` невалидно, нужен объект исключения: `throw new ServExp(...)`.
5. Смешаны `switch` и `else if` — такой код не скомпилируется.
6. После перехода на enum остались сравнения со строками: `newStatus.equals("CANCELLED")`.
7. В `case ... ->` не нужен `break`.
8. Метод `setOrderStatusToComplete()` использует `order`, но не принимает его параметром.
9. `catch (RepExp e) { throw ServExp; }` не добавляет полезного контекста и написан невалидно.
10. Всё ещё не видно `@Transactional` и нормальной валидации переходов.

**Короткий ответ, который надо уметь сказать вслух:**

В исходном коде основная проблема в том, что статус передаётся строкой, есть field injection, опасный `findById(...).get()`, дублирование логики и нет транзакционной границы. Я бы заменил строку на enum, использовал constructor injection, доставал заказ через `orElseThrow`, обработал статусы через `switch`, вынес уведомления отдельно и добавил `@Transactional`. Если уведомление должно отправляться только после успешного сохранения, лучше публиковать доменное событие и слушать его через `@TransactionalEventListener(AFTER_COMMIT)`.

**Если хочешь звучать сильнее:**

1. «Если есть бизнес-правила переходов, я бы сделал state machine: не каждый статус может перейти в любой другой».
2. «Для конкурентных обновлений добавил бы `@Version` на entity, чтобы ловить lost update».
3. «Ошибки сервиса замаппил бы через `@RestControllerAdvice`: not found в 404, invalid transition в 409/400».
4. «Уведомления лучше не отправлять внутри транзакции напрямую; минимум after commit, надежнее outbox».

---

### 4.3 Подтверждение заказа с проверкой остатков

**Скорее всего дали такой код:**

```java
public void confirmOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).get();
    for (OrderItem item : order.getItems()) {
        Stock stock = stockRepository.findByProductId(item.getProductId());
        if (stock.getQuantity() >= item.getQuantity()) {
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stockRepository.save(stock);
        }
    }
    order.setStatus("CONFIRMED");
    orderRepository.save(order);
}
```

****Что не так:****
1. **Нет транзакции**: падение в середине цикла — часть остатков списана, заказ не подтверждён. → @Transactional.
2. **Молчаливый скип**: если остатка нет, позиция просто пропускается, а заказ всё равно CONFIRMED — бизнес-баг серьёзнее технических. → сначала проверить ВСЁ, потом списывать; нехватка → InsufficientStockException, ничего не списано.
3. **Гонка (oversell)**: два заказа параллельно читают один остаток. → пессимистичный лок (`findByProductIdInForUpdate`, захват в порядке productId против дедлока) или атомарный `UPDATE stock SET quantity = quantity - :q WHERE product_id = :id AND quantity >= :q` с проверкой updated rows.
4. **N+1**: запрос на каждый товар → один `findByProductIdIn(...)`.
5. **Идемпотентность**: повторный confirm того же заказа спишет остатки второй раз → проверка текущего статуса (переход только из NEW/PAID), стейт-машина.
6. Мелочи: `.get()`, строка-статус, field injection (если есть), нет валидации принадлежности заказа.


**Как правильно мыслить:**

```java
@Transactional
public void confirmOrder(Long orderId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.canTransitionTo(OrderStatus.CONFIRMED)) {
        throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.CONFIRMED);
    }

    Map<Long, Integer> required = order.getItems().stream()
        .collect(Collectors.groupingBy(
            OrderItem::getProductId,
            Collectors.summingInt(OrderItem::getQuantity)
        ));

    List<Stock> stocks = stockRepository.findByProductIdInForUpdate(
        required.keySet().stream().sorted().toList()
    );

    Map<Long, Stock> byProduct = stocks.stream()
        .collect(Collectors.toMap(Stock::getProductId, Function.identity()));

    for (var e : required.entrySet()) {
        Stock stock = byProduct.get(e.getKey());
        if (stock == null || stock.getQuantity() < e.getValue()) {
            throw new InsufficientStockException(e.getKey(), e.getValue());
        }
    }

    required.forEach((productId, qty) -> byProduct.get(productId).decrease(qty));
    order.setStatus(OrderStatus.CONFIRMED);
}
```

Фишка ответа: сначала проверить всё, потом списывать. Локи брать в стабильном порядке `productId`, чтобы не ловить дедлоки как коллекционные карточки.


---

### 4.4 Обработка платежа `processPayment`

**Скорее всего дали гибрид** TransferService (задачу 4.6 ниже) + внешний платёжный шлюз:

```java
public String processPayment(Long orderId, double amount) {
    Order order = orderRepository.findById(orderId).get();
    PaymentResponse resp = paymentGateway.charge(order.getCustomerId(), amount);
    if (resp.getStatus().equals("OK")) {
        order.setStatus("PAID");
        orderRepository.save(order);
        return "success";
    }
    return "fail";
}
```

****Что не так:**** double для денег; `.get()`; строки-статусы и строковый возврат вместо enum/исключений; **вызов шлюза без таймаута/ретраев/идемпотентности** — а ретраить charge без idempotency key нельзя (двойное списание); что если charge прошёл, а save упал → рассинхрон с деньгами клиента: нужна фиксация платёжного намерения ДО вызова (запись PENDING-платежа), подтверждение после, reconciliation по вебхуку/статусу; при этом сам HTTP-вызов — вне БД-транзакции. Нет проверки текущего статуса заказа (повторная оплата PAID). Это самая «банковская» задача банка — идемпотентность и консистентность с внешним миром здесь главный ответ.


**Как это лучше строить:**

```java
@Transactional
public PaymentDto createPaymentIntent(Long orderId, BigDecimal amount, String idempotencyKey) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (order.getStatus() == OrderStatus.PAID) {
        throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.PAID);
    }

    Payment payment = paymentRepository.findByIdempotencyKey(idempotencyKey)
        .orElseGet(() -> paymentRepository.save(
            Payment.pending(orderId, amount, idempotencyKey)
        ));

    outboxRepository.save(OutboxEvent.paymentChargeRequested(payment.getId()));
    return PaymentDto.from(payment);
}
```

Внешний `paymentGateway.charge(...)` делает отдельный воркер, не внутри длинной БД-транзакции. После ответа шлюза статус платежа и заказа обновляются через callback/webhook/reconciliation. Главные слова: `BigDecimal`, idempotency key, timeout, outbox, retry осторожно, не повторять charge без ключа.


---

### 4.5 Перевод денег: `@Transactional`-минное поле `[вероятная гипотеза]`

```java
@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;

    public void transfer(Long fromId, Long toId, double amount) {
        Account from = accountRepository.findById(fromId).get();
        Account to = accountRepository.findById(toId).get();

        if (from.getBalance() >= amount) {
            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);
            accountRepository.save(from);
            accountRepository.save(to);
        }
    }
}
```

**Замечания (в порядке серьёзности):**

1. **Нет @Transactional** — два save не атомарны: падение между ними = деньги списаны, но не зачислены.
2. **double для денег** → BigDecimal (двоичная плавающая точка не представляет 0.1 точно).
3. **Check-then-act гонка**: два параллельных перевода прочитают один баланс и оба спишут — уход в минус. Транзакция сама по себе НЕ спасает (read committed позволяет обеим прочитать старое значение). Варианты фикса: пессимистичный лок (SELECT FOR UPDATE), optimistic @Version + retry, атомарный `UPDATE ... SET balance = balance - :amt WHERE id = :id AND balance >= :amt` с проверкой affected rows.
4. **Дедлок** при встречных переводах A→B и B→A с локами → захват в детерминированном порядке (по возрастанию id).
5. `.get()` без orElseThrow; **молчаливый no-op** при нехватке средств — должен быть InsufficientFundsException.
6. Валидации: amount > 0, fromId != toId.
7. Field injection; для финтеха назови **идемпотентность операции** (operationId против повторного дебета при ретрае клиента) — прямой мостик к ЦФА.

**Целевой вид:**

```java
@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount.signum() <= 0) throw new InvalidAmountException(amount);
        if (fromId.equals(toId)) throw new SameAccountTransferException(fromId);

        Account first = lockById(Math.min(fromId, toId));
        Account second = lockById(Math.max(fromId, toId));
        Account from = first.getId().equals(fromId) ? first : second;
        Account to = from == first ? second : first;

        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundsException(fromId, amount);

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    private Account lockById(long id) {
        return accountRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
```

```java
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id);
}
```

**Follow-up, которые зададут:**
- «Почему не synchronized на методе?» — не работает при нескольких инстансах сервиса (кластер) и блокирует ВСЕ переводы, а не пару счетов.
- «Optimistic или pessimistic здесь?» — счета — горячие строки с высокой вероятностью конфликта → пессимистичный честнее; optimistic + retry — при редких конфликтах.
- «А если счета в разных сервисах/банках?» — распределённая транзакция невозможна → сага с компенсацией + идемпотентность шагов.

---

### 4.6 Возврат статуса из БД

Два вероятных флейвора:

```java
// Флейвор А
public String getOrderStatus(Long id) {
    Order order = orderRepository.findById(id).get();
    if (order != null) {
        return order.getStatus().toString();
    }
    return "";
}
```
**Что не так:** мёртвая проверка — `.get()` кинет NoSuchElementException ДО if (order никогда не null); "" как маркер отсутствия — вызывающий не отличит «нет заказа» от пустого статуса → orElseThrow + 404 через advice, либо Optional<OrderStatus>; String вместо enum/DTO; для чтения — @Transactional(readOnly = true) или вообще проекция `findStatusById`.

```java
// Флейвор Б — если увидишь JdbcTemplate
String status = jdbcTemplate.queryForObject(
    "SELECT status FROM orders WHERE id = " + id, String.class);
```
**Что не так:** **SQL-инъекция** (конкатенация) → параметризованный запрос `... WHERE id = ?`; queryForObject кидает EmptyResultDataAccessException при отсутствии строки — не обработано.


**Как правильно:**

```java
@Transactional(readOnly = true)
public OrderStatus getOrderStatus(Long id) {
    return orderRepository.findStatusById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));
}
```

```java
@Query("select o.status from Order o where o.id = :id")
Optional<OrderStatus> findStatusById(Long id);
```

Если это JDBC:

```java
String sql = "select status from orders where id = ?";
return jdbcTemplate.queryForObject(sql, OrderStatus.class, id);
```

Только параметризованный запрос. Конкатенация SQL с id — это не оптимизация, это заявка на позор.


---

### 4.7 Получение имени пользователя через REST `[факт: давали Вадиму]`

**Скорее всего дали такой код или очень похожий антипример:**

```java
@Service
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;

    public String getUserName(Long userId) {
        String url = "http://user-service/users/" + userId;
        Map response = restTemplate.getForObject(url, Map.class);
        return response.get("name").toString();
    }
}
```

**Что не так:**

- `RestTemplate` без connect/read timeout может зависнуть навсегда. Внешний сервис, конечно, «никогда не падает», как и все сказочные существа.
- URL собирается конкатенацией: нет encoding, легко сломать path/query.
- `Map` вместо типизированного DTO: касты, NPE и сюрпризы Jackson.
- Нет обработки 404/5xx/timeout.
- URL захардкожен, должен быть в `@ConfigurationProperties`.
- Field injection вместо constructor injection.
- Нет resilience-слоя: retry/backoff/circuit breaker, но retry только для идемпотентных вызовов.
- Если это вызывается внутри `@Transactional`, БД-соединение держится во время HTTP-вызова.

**Как правильно отвечать:**

```java
@ConfigurationProperties(prefix = "clients.user-service")
public record UserClientProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {}

public record UserResponse(Long id, String name) {}

@Service
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder, UserClientProperties props) {
        this.restClient = builder
            .baseUrl(props.baseUrl())
            .requestFactory(ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                    .withConnectTimeout(props.connectTimeout())
                    .withReadTimeout(props.readTimeout())))
            .build();
    }

    public String getUserName(Long userId) {
        UserResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/users/{id}").build(userId))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                (request, resp) -> { throw new UserNotFoundException(userId); })
            .onStatus(HttpStatusCode::is5xxServerError,
                (request, resp) -> { throw new ExternalServiceException("user-service failed"); })
            .body(UserResponse.class);

        if (response == null || response.name() == null) {
            throw new ExternalServiceException("user-service returned invalid response");
        }
        return response.name();
    }
}
```

**Что проговорить на senior:**

- В новом коде лучше `RestClient` / `WebClient`, `RestTemplate` уже legacy/maintenance.
- Таймауты обязательны, иначе один зависший dependency съедает поток.
- Retry только на безопасные/идемпотентные операции, с backoff + jitter.
- Circuit Breaker нужен против каскадных отказов.
- В логах не светить токены и персональные данные.


---

### 4.8 Обработка с внешней интеграцией и `@Async`

**Скорее всего дали такой код:**

```java
@Service
public class ExportService {

    @Async
    @Transactional
    public void export(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        externalApi.send(toDto(order));
        order.setStatus("EXPORTED");
    }

    public void exportAll(List<Long> ids) {
        ids.forEach(this::export);
    }
}
```

****Что не так:****
1. **Self-invocation**: exportAll вызывает export через this — ни @Async, ни @Transactional не сработают (мимо прокси). Самая жирная закладка.
2. **@Async void глотает исключения** — упавший экспорт исчезает молча. → возвращать CompletableFuture / настроить AsyncUncaughtExceptionHandler; + @EnableAsync вообще включён?
3. **Executor для @Async**: без явной настройки (до Boot 3.2) — SimpleAsyncTaskExecutor: новый поток на КАЖДУЮ задачу, без ограничений → исчерпание ресурсов. Настроить ThreadPoolTaskExecutor (core/max/queue).
4. **Внешний вызов внутри транзакции** + статус после вызова: send прошёл, коммит упал → внешняя система получила, у нас не отмечено (или наоборот). → outbox: намерение в БД → воркер шлёт → фиксирует результат; таймауты и ретрай с идемпотентностью на externalApi.
5. Порядок/наблюдаемость: как узнать, что экспортировалось? статусы + метрики + алерты на зависшие.


**Как правильно мыслить:**

```java
@Service
public class ExportFacade {

    private final ExportService exportService;

    public ExportFacade(ExportService exportService) {
        this.exportService = exportService;
    }

    public List<CompletableFuture<Void>> exportAll(List<Long> ids) {
        return ids.stream()
            .map(exportService::export)
            .toList();
    }
}

@Service
public class ExportService {

    @Async("exportExecutor")
    public CompletableFuture<Void> export(Long orderId) {
        // короткая загрузка данных/DTO
        // внешний вызов с timeout/idempotency
        // фиксация результата отдельной транзакцией или через outbox
        return CompletableFuture.completedFuture(null);
    }
}
```

Для продового варианта лучше не `@Async` вокруг внешнего мира, а outbox + воркер + retry/DLQ/метрики. `@Async` — инструмент, не архитектурная религия.


---

### 4.9 Kafka listener для обработки событий `[банк подтвердил прогноз]`

```java
@Component
public class OrderEventListener {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "orders")
    public void listen(String message) throws Exception {
        Map<String, Object> event = new ObjectMapper().readValue(message, Map.class);
        String type = (String) event.get("type");
        if (type.equals("CREATED")) {
            orderService.create(event);
        }
        if (type.equals("PAID")) {
            orderService.markPaid((Long) event.get("orderId"));
        }
    }
}
```

**Замечания:**

1. **Нет идемпотентности** — Kafka даёт at-least-once: дубль события = двойная обработка. Дедуп по event id (processed_events + ON CONFLICT DO NOTHING в одной транзакции с бизнес-эффектом).
2. **throws Exception наружу листенера** — постоянная ошибка (poison message) уходит в бесконечные ретраи и блокирует партицию → DefaultErrorHandler с backoff + DeadLetterPublishingRecoverer (DLT).
3. `new ObjectMapper()` на каждое сообщение — тяжёлый объект, потокобезопасный → бин; лучше JsonDeserializer в конфиге консюмера и типизированный метод `listen(OrderEvent event)`.
4. Map<String, Object> + касты: Jackson числа в Map кладёт как Integer → `(Long)` даст ClassCastException. → record OrderEvent.
5. `type.equals(...)` — NPE при отсутствии поля; строки вместо enum; два независимых if вместо switch/диспетчера (Strategy из раздела 3).
6. Нет валидации события и логирования с ключами (eventId, orderId).

**Follow-up:** ручной vs авто-коммит offset'ов (когда возможна потеря/дубль), что происходит при rebalance, как гарантировать порядок (ключ партиционирования = orderId).


**Как правильно мыслить:**

```java
public record OrderEvent(UUID eventId, Long orderId, OrderEventType type) {}

@KafkaListener(topics = "orders", groupId = "order-service")
@Transactional
public void listen(OrderEvent event) {
    if (!processedEventRepository.tryMarkProcessed(event.eventId())) {
        return; // дубль
    }

    handlerRegistry.get(event.type()).handle(event);
}
```

Конфигурационно: `DefaultErrorHandler` + backoff + `DeadLetterPublishingRecoverer`, manual ack/commit после успешной обработки, JSON-deserializer/Schema Registry, ключ партиционирования `orderId` для порядка по заказу.


---

### 4.10 Интеграция с внешним API

**Скорее всего дали такой код:**

```java
@Service
public class ExternalOrderService {

    @Autowired
    private RestTemplate restTemplate;

    public void sendOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        String response = restTemplate.postForObject(
            "http://external-api/orders",
            order,
            String.class
        );
        order.setExternalStatus(response);
        orderRepository.save(order);
    }
}
```

**Что не так:**

- Field injection.
- `.get()` на `Optional`.
- Entity уходит наружу вместо DTO.
- URL захардкожен.
- Нет таймаутов, обработки 4xx/5xx, retry/backoff, circuit breaker.
- Нет идемпотентного ключа. Повторный запрос может создать дубль во внешней системе.
- Внешний вызов смешан с изменением БД: если API принял заказ, а save упал, получаем рассинхрон.
- Если метод под `@Transactional`, HTTP-вызов держит БД-соединение.

**Как правильно мыслить:**

```java
@Transactional
public void scheduleExternalExport(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.canBeExported()) {
        throw new InvalidOrderStateException(orderId, order.getStatus());
    }

    outboxRepository.save(OutboxEvent.externalOrderExport(orderId));
}
```

Дальше отдельный воркер читает outbox-события, вызывает внешний API с timeout + idempotency key, фиксирует `SENT/FAILED`, делает retry с backoff и после лимита отправляет в DLQ/алерт.

**Senior-формула:** не пытаться сделать «БД + внешний HTTP» одной магической транзакцией. Делать outbox, идемпотентность и reconciliation. Да, скучно, зато деньги и данные не превращаются в художественную инсталляцию.


---

### 4.11 Потоконебезопасный singleton-бин

```java
@Service
public class RateService {

    private final Map<String, BigDecimal> cache = new HashMap<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public BigDecimal getRate(String currency) {
        if (!cache.containsKey(currency)) {
            cache.put(currency, loadRate(currency));
        }
        return cache.get(currency);
    }

    public String today() {
        return fmt.format(new Date());
    }
}
```

**Замечания:**

1. Spring-бин — singleton, метод дёргают параллельно: **HashMap под конкурентной записью** — гонки, потерянные записи, битые бакеты. → ConcurrentHashMap.
2. Но и с CHM `containsKey → put` — **check-then-act**, не атомарно: две загрузки одного курса. → `cache.computeIfAbsent(currency, this::loadRate)` — атомарно и грузит один раз.
3. **SimpleDateFormat не потокобезопасен** (мутирующий Calendar внутри) — классическая закладка: под нагрузкой кривые даты и исключения. → DateTimeFormatter (immutable) + java.time; Date вообще не использовать.
4. Кэш без TTL, инвалидации и лимита — устаревшие курсы и утечка памяти. → Caffeine / Spring @Cacheable с TTL. Для курсов валют устаревание — бизнес-риск, скажи об этом.

```java
private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();
private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

public BigDecimal getRate(String currency) {
    return cache.computeIfAbsent(currency, this::loadRate);
}

public String today() {
    return LocalDate.now().format(FMT);
}
```

#### Дополнительный вариант: ручной `new Thread` в цикле

Кроме HashMap/SimpleDateFormat-варианта возможен «ручной поток»:

```java
@Service
public class ReportService {
    private int processed = 0;

    public void generate(List<Report> reports) {
        for (Report r : reports) {
            new Thread(() -> {
                process(r);
                processed++;
            }).start();
        }
    }
}
```
**Что не так:** new Thread в цикле — неограниченное число потоков, нет переиспользования, нет контроля ошибок → ExecutorService/TaskExecutor-бин с ограниченным пулом; `processed++` — гонка (не атомарен) → AtomicInteger; никто не ждёт завершения → invokeAll/CompletableFuture.allOf; исключение в потоке теряется; graceful shutdown пула при остановке приложения (@PreDestroy). И финальное: synchronized/атомики чинят ОДИН инстанс — в кластере счётчик/лок должен жить в БД или Redis.


**Как правильно мыслить:**

```java
@Service
public class ReportService {

    private final TaskExecutor reportExecutor;
    private final AtomicInteger processed = new AtomicInteger();

    public ReportService(TaskExecutor reportExecutor) {
        this.reportExecutor = reportExecutor;
    }

    public CompletableFuture<Void> generateAsync(Report report) {
        return CompletableFuture.runAsync(() -> {
            process(report);
            processed.incrementAndGet();
        }, reportExecutor);
    }
}
```

Но важная оговорка: `AtomicInteger` чинит только один JVM-инстанс. В кластере общий счётчик/лок должен жить в БД, Redis или другом внешнем coordination-механизме.


---

### 4.12 JPA-сервис с N+1 `[вероятная гипотеза]`

```java
@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    public List<OrderDto> buildReport() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> result = new ArrayList<>();
        for (Order o : orders) {
            OrderDto dto = new OrderDto();
            dto.setCustomerName(o.getCustomer().getName());
            dto.setItemsCount(o.getItems().size());
            result.add(dto);
        }
        return result;
    }
}
```

**Замечания:**

1. **findAll без пагинации** — на проде таблица в миллионы строк: OOM и вечный запрос. → Pageable / стриминг / ограничение по фильтру.
2. **N+1**: customer и items ленивые → 1 + 2N запросов. → JOIN FETCH / @EntityGraph, а для отчёта правильнее всего **DTO-проекция сразу в JPQL** (select new OrderDto(...) с join и count) — БД агрегирует, приложение не тащит entity.
3. Вне @Transactional lazy-обращения дадут LazyInitializationException — если «работает», значит включён OSIV, и это повод сказать, почему OSIV на проде выключают (соединение БД на весь HTTP-запрос).
4. Агрегация в Java вместо SQL (count/sum — работа БД).
5. Field injection, ручной маппинг.

Здесь обязательно: «ровно это я чинил на проде — поиск инвойсов, ~1900 запросов и 8–11 секунд; JOIN FETCH + @BatchSize → 15 запросов и ~180 мс».

---

### 4.13 `@Scheduled` джоба `[вероятная гипотеза]`

```java
@Component
public class InvoiceJob {

    @Autowired
    private InvoiceRepository repo;
    @Autowired
    private EmailClient emailClient;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendPending() {
        List<Invoice> pending = repo.findByStatus("PENDING");
        for (Invoice i : pending) {
            emailClient.send(i);
            i.setStatus("SENT");
        }
    }
}
```

**Замечания:**

1. **Кластер из N подов → джоба выполняется N раз** — двойные письма. → ShedLock (распределённый лок в PG) или выборка порции через FOR UPDATE SKIP LOCKED.
2. **Одна транзакция на всю пачку + внешний вызов внутри**: падение на 500-м письме откатит статусы 499 уже отправленных → при следующем прогоне все уйдут повторно. → порционно: залочить батч, пометить SENDING, отправлять ВНЕ БД-транзакции, фиксировать SENT/FAILED + attempts по одному.
3. Ошибка send() убивает весь прогон (остальные инвойсы ждут следующего цикла) — per-item try/catch с учётом попыток и алертом после N.
4. fixedRate vs fixedDelay: при обработке дольше минуты fixedRate ставит следующий запуск немедленно после текущего (шедулер однопоточный — параллельно не запустит, но пауз не будет); fixedDelay отсчитывает от завершения — здесь честнее.
5. findByStatus без лимита; строковые статусы; field injection; нет идемпотентности повторной отправки.

Правильный скелет совпадает с outbox-воркером (R2 + S11 из дока с задачами) — проговори эту связь, это ровно паттерн публикации в блокчейн из JD.

---

### 4.14 Мини-фрагменты: почему `@Transactional` не работает `[вероятная гипотеза]`

Могут показать не сервис целиком, а фрагменты в стиле «что не так»:

```java
// A — self-invocation: вызов через this минует прокси, транзакции НЕТ
public void importAll(List<Row> rows) { rows.forEach(this::saveOne); }
@Transactional
public void saveOne(Row r) { /* ... */ }
// фикс: вынести saveOne в отдельный бин (или TransactionTemplate)
```

```java
// B — проглоченное исключение: прокси его не видит, rollback НЕ произойдёт,
// транзакция закоммитит частичное состояние
@Transactional
public void process() {
    try {
        repo.save(x);
        riskyCall();
    } catch (Exception e) {
        log.error("failed", e);
    }
}
// фикс: rethrow; если глотать осознанно — setRollbackOnly()
```

```java
// C — checked exception: по умолчанию rollback только на RuntimeException/Error,
// IOException закоммитит транзакцию
@Transactional
public void export() throws IOException { /* ... */ }
// фикс: @Transactional(rollbackFor = Exception.class)
```

```java
// D — private-метод: CGLIB-прокси наследует класс, private не переопределить,
// аннотация молча игнорируется
@Transactional
private void doWork() { /* ... */ }
```

```java
// E — внешний вызов внутри транзакции: HTTP/блокчейн держит БД-соединение,
// а при откате уведомление уже ушло
@Transactional
public void finish(long id) {
    repo.markFinished(id);
    blockchainClient.publish(id);
}
// фикс: outbox (см. R2 в доке с задачами)
```

Сквозной ответ на «почему так»: @Transactional реализован через AOP-прокси — работает только на public-методах при вызове снаружи бина; rollback-правила — по типу вылетевшего ИЗ метода исключения.

---

## 5. Банк багов: признак → диагноз

| Видишь | Говоришь |
|---|---|
| @Autowired на поле | constructor injection, final |
| `.get()` на Optional, цепочки `!= null` | orElseThrow с доменным исключением / Optional.map |
| Статусы/типы строками, magic numbers | enum (@Enumerated(STRING)), константы |
| IllegalArgument/RuntimeException как бизнес-ошибка | кастомные исключения + @RestControllerAdvice → 404/409/422 |
| catch (Exception e) { log } | проглатывание: rethrow; в @Transactional — ещё и потерянный rollback |
| read-modify-write без @Transactional | атомарность |
| HTTP/Kafka/blockchain внутри @Transactional | outbox, транзакция короткая |
| double/float рядом с деньгами | BigDecimal (из String/valueOf, compareTo, RoundingMode) |
| new BigDecimal(0.1) | двоичный мусор: BigDecimal.valueOf(0.1) или из String |
| HashMap/SimpleDateFormat/mutable поле в @Service | singleton + конкурентность: CHM/computeIfAbsent, DateTimeFormatter |
| findAll + цикл с getX().getY() | пагинация + N+1 (fetch join / проекция) |
| Entity наружу из контроллера | DTO на границе (lazy, лишние поля, связность API со схемой БД) |
| Сравнение строк через == | equals / enum |
| Нет проверки «ресурс принадлежит юзеру» | broken access control (OWASP) — в банке это красный флаг |
| Пароль/токен в логах | ИБ: маскирование |
| Ресурс без try-with-resources | утечка |
| synchronized как фикс гонки в вебе | не работает в кластере — лок должен жить в БД/распределённо |

Финальный ход после разбора любого варианта: «как бы я это протестировал» — параметризованный unit на переходы/валидации + интеграционный на Testcontainers. Закрывает сразу секцию 9.

---

## 6. Как выжать 4.00

1. Оценивают три вещи: сложность решения, оптимизацию, ответы на доп. вопросы. Значит: рабочее решение → сам предложи оптимизацию (сложность, один проход, индекс, батч) → жди доп. вопросов про конкурентность и edge cases.
2. Формула ответа на доп. вопрос — из их же критериев сеньорности: суть → как под капотом → пример из своего проекта → что на уровне системы (кластер, нагрузка).
3. Уточняющие бизнес-вопросы в начале — записывают в плюс дословно.
4. Синтаксис руками без IDE — отдельно тренируй (Мишин главный минус).
5. Любой код прогоняй по чек-листу в разделе 5.
