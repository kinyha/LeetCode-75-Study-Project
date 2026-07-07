# ТИ-1 IBS — банк задач с решениями (из скоркарт)

Источник: лист «Coding tasks» из файлов оценки — это полный банк, из которого интервьюер выбирает ОДНУ задачу (20 минут). Банки в обоих файлах идентичны. Три категории: 15 алгоритмических, 12 coding tasks (Spring/Stream), 10 code review. Обоим коллегам дали code review — вероятность этой категории наибольшая, но написание кода тоже в скоркарте («Задача на написание кода»), готовь все три.

Кодинг идёт в онлайн-редакторе (code.yandex-team.ru) с шарингом экрана — без IDE-подсказок, компиляция может отсутствовать. Пиши аккуратно синтаксис руками.

Оценка задачи — 3 подпункта: сложность решения, оптимизация/review, дополнительные вопросы. У Вадима 4.00 (code review), у Миши 3.33 (снизили за качество реализации — разбор его кода в разделе 4).

---

## 1. Алгоритмические задачи (15) — решения

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

## 2. Coding tasks — Spring/Stream (12) — решения

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

## 3. Code review задачи (10) — полный список из банка

Обоим коллегам досталась эта категория. Известные названия и где разбор:

| # | Задача | Разбор |
|---|---|---|
| 1 | Циклические зависимости Spring | ниже, 3.1 |
| 2 | Обновление статуса заказа updateOrderStatus | interview_ti1_ibs_structured.md § 1.2 (+ раздел 4 здесь — реальное решение Миши) |
| 3 | Подтверждение заказа с проверкой остатков | ниже, 3.3 |
| 4 | Обработка платежа processPayment | ниже, 3.4 (≈ TransferService из § 1.6 TI-1) |
| 5 | Возврат статуса из БД | ниже, 3.5 |
| 6 | Получение имени пользователя через REST | interview_ti1_ibs_structured.md § 1.3 (задача Вадима) |
| 7 | Обработка с внешней интеграцией и асинхрон | ниже, 3.7 |
| 8 | Интеграция с Kafka для обработки событий | interview_ti1_ibs_structured.md § 1.8 — прогноз подтвердился банком |
| 9 | Управление потоками и синхронизация в Spring | interview_ti1_ibs_structured.md § 1.10 + ниже 3.9 |
| 10 | Интеграция с внешним API | ниже, 3.10 (≈ § 1.3 + resilience) |

### 3.1 Циклические зависимости Spring

Вероятный код:

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

**Разбор:**
1. Цикл A↔B. С constructor injection упал бы на старте (BeanCurrentlyInCreationException) — и это ПЛЮС: ошибка видна сразу. Field injection цикл маскирует; при этом с Boot 2.6+ циклы запрещены по умолчанию в любом виде.
2. Плохие фиксы, которые предложат «на слабо»: @Lazy на одну из зависимостей (откладывает создание — цикл остаётся в дизайне), `spring.main.allow-circular-references=true` (легализация проблемы). Назови их именно как костыли.
3. Правильные фиксы: (а) выделить общую логику в третий сервис — цикл обычно значит, что ответственность спутана; (б) инверсия через события: PaymentService публикует PaymentFailedEvent (ApplicationEventPublisher), OrderService слушает @EventListener — зависимость в одну сторону; (в) интерфейс + узкий callback.
4. Сказать про уровни: циклы бывают и между модулями/микросервисами — там решается оркестратором или событиями (их же критерий сеньорности: метод → сервис → приложение).

### 3.3 Подтверждение заказа с проверкой остатков

Вероятный код:

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

**Разбор:**
1. **Нет транзакции**: падение в середине цикла — часть остатков списана, заказ не подтверждён. → @Transactional.
2. **Молчаливый скип**: если остатка нет, позиция просто пропускается, а заказ всё равно CONFIRMED — бизнес-баг серьёзнее технических. → сначала проверить ВСЁ, потом списывать; нехватка → InsufficientStockException, ничего не списано.
3. **Гонка (oversell)**: два заказа параллельно читают один остаток. → пессимистичный лок (`findByProductIdInForUpdate`, захват в порядке productId против дедлока) или атомарный `UPDATE stock SET quantity = quantity - :q WHERE product_id = :id AND quantity >= :q` с проверкой updated rows.
4. **N+1**: запрос на каждый товар → один `findByProductIdIn(...)`.
5. **Идемпотентность**: повторный confirm того же заказа спишет остатки второй раз → проверка текущего статуса (переход только из NEW/PAID), стейт-машина.
6. Мелочи: `.get()`, строка-статус, field injection (если есть), нет валидации принадлежности заказа.

### 3.4 Обработка платежа processPayment

Ожидай гибрид TransferService (TI-1 § 1.6) + внешний платёжный шлюз:

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

**Разбор:** double для денег; `.get()`; строки-статусы и строковый возврат вместо enum/исключений; **вызов шлюза без таймаута/ретраев/идемпотентности** — а ретраить charge без idempotency key нельзя (двойное списание); что если charge прошёл, а save упал → рассинхрон с деньгами клиента: нужна фиксация платёжного намерения ДО вызова (запись PENDING-платежа), подтверждение после, reconciliation по вебхуку/статусу; при этом сам HTTP-вызов — вне БД-транзакции. Нет проверки текущего статуса заказа (повторная оплата PAID). Это самая «банковская» задача банка — идемпотентность и консистентность с внешним миром здесь главный ответ.

### 3.5 Возврат статуса из БД

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
Разбор: мёртвая проверка — `.get()` кинет NoSuchElementException ДО if (order никогда не null); "" как маркер отсутствия — вызывающий не отличит «нет заказа» от пустого статуса → orElseThrow + 404 через advice, либо Optional<OrderStatus>; String вместо enum/DTO; для чтения — @Transactional(readOnly = true) или вообще проекция `findStatusById`.

```java
// Флейвор Б — если увидишь JdbcTemplate
String status = jdbcTemplate.queryForObject(
    "SELECT status FROM orders WHERE id = " + id, String.class);
```
Разбор: **SQL-инъекция** (конкатенация) → параметризованный запрос `... WHERE id = ?`; queryForObject кидает EmptyResultDataAccessException при отсутствии строки — не обработано.

### 3.7 Обработка с внешней интеграцией и асинхрон

Вероятный код:

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

**Разбор:**
1. **Self-invocation**: exportAll вызывает export через this — ни @Async, ни @Transactional не сработают (мимо прокси). Самая жирная закладка.
2. **@Async void глотает исключения** — упавший экспорт исчезает молча. → возвращать CompletableFuture / настроить AsyncUncaughtExceptionHandler; + @EnableAsync вообще включён?
3. **Executor для @Async**: без явной настройки (до Boot 3.2) — SimpleAsyncTaskExecutor: новый поток на КАЖДУЮ задачу, без ограничений → исчерпание ресурсов. Настроить ThreadPoolTaskExecutor (core/max/queue).
4. **Внешний вызов внутри транзакции** + статус после вызова: send прошёл, коммит упал → внешняя система получила, у нас не отмечено (или наоборот). → outbox: намерение в БД → воркер шлёт → фиксирует результат; таймауты и ретрай с идемпотентностью на externalApi.
5. Порядок/наблюдаемость: как узнать, что экспортировалось? статусы + метрики + алерты на зависшие.

### 3.9 Управление потоками и синхронизация в Spring — дополнение к § 1.10

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
Разбор: new Thread в цикле — неограниченное число потоков, нет переиспользования, нет контроля ошибок → ExecutorService/TaskExecutor-бин с ограниченным пулом; `processed++` — гонка (не атомарен) → AtomicInteger; никто не ждёт завершения → invokeAll/CompletableFuture.allOf; исключение в потоке теряется; graceful shutdown пула при остановке приложения (@PreDestroy). И финальное: synchronized/атомики чинят ОДИН инстанс — в кластере счётчик/лок должен жить в БД или Redis.

### 3.10 Интеграция с внешним API

База — задача Вадима (TI-1 § 1.3: таймауты, обработка 4xx/5xx, UriComponentsBuilder, DTO, конфиг вместо хардкода). Сверху добавь resilience-слой, если код «продовый»: retry с backoff только на идемпотентные вызовы, circuit breaker (resilience4j) против каскадных отказов, fallback-поведение, логирование с correlation id, маскирование секретов в логах. RestTemplate → RestClient/WebClient как современная замена.

---

## 4. Разбор фактического решения Миши (почему 3.33, а не 4.00)

Его код из скоркарты (дословно, с его ошибками):

```java
@Service
@RequiredAgrsContruct
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId).getOrThrow(
            () -> { throw new ServExp(); });

        if (order.userId == null) { throw ServExp; }

        switch (newStatus) {
            OrderStatus.COMPLETED -> { setOrderStatusToComplete() break; }
        }
        else if (newStatus.equals("CANCELLED")) {
            order.setStatus("CANCELLED");
            order.setReason("CANCELLED reason");
            orderRepository.save(order);
            notificationService.notifyUser(order.getUserId(), "Your order is cancelled");
        } else if (newStatus.equals("PENDING")) { /* ... */ }
        // ...
    }

    private setOrderStatusToComplete() {
        order.setStatus(OrderStatus.COMPLETED);
        try {
            orderRepository.save(order);
            notificationService.notifyUser(order.getUserId(), "Your order is completed");
        } catch (RepExp e) { throw ServExp; }
    }
}
```

Что он сделал правильно (за это regular-senior): constructor injection через Lombok, enum OrderStatus, кастомные исключения вместо IllegalArgument, orElseThrow-идея, уточнял бизнес-требования вслух.

Что срезало балл — не повторяй:

1. **Синтаксис развалился**: `getOrThrow` не существует (`orElseThrow`); в supplier'е orElseThrow не `{ throw ... }`, а `() -> new NotFoundException(...)` — supplier ВОЗВРАЩАЕТ исключение; смесь switch и else-if — не скомпилируется; в стрелочном case не нужен break; enum перечислен через `;` вместо `,`; `throw ServExp` без new. В редакторе без компилятора твой компилятор — ты. Медленнее, но валидно.
2. **Enum сравнивается со строкой**: `newStatus.equals("CANCELLED")` всегда false — введя enum, он продолжил мыслить строками. Ветки должны быть `case CANCELLED ->`.
3. **Приватный метод использует order вне скоупа** — параметр не передан.
4. **Нет @Transactional** и нет валидации переходов (стейт-машина) — ядро задачи.
5. **Notification внутри «транзакционного» кода**: уведомление должно уходить после успешного коммита (@TransactionalEventListener(AFTER_COMMIT) или outbox), иначе при откате юзер получит письмо о несуществующем событии. Озвучишь это — сразу senior-маркер.
6. try/catch вокруг save с перебросом «RepExp → ServExp» — бессмысленная обёртка, Spring и так транслирует в DataAccessException; глотать/оборачивать без добавления контекста не нужно.

Эталон решения — целевой код в interview_ti1_ibs_structured.md § 1.2 (enum со стейт-машиной переходов + @Transactional + advice). Напиши его руками в пустом текстовом редакторе (не IDE!) минимум дважды — условия собеса именно такие.

---

## 5. Как выжать 4.00 (по скоркартам)

1. Оценивают три вещи: сложность решения, оптимизацию, ответы на доп. вопросы. Значит: рабочее решение → сам предложи оптимизацию (сложность, один проход, индекс, батч) → жди доп. вопросов про конкурентность и edge cases.
2. Формула ответа на доп. вопрос — из их же критериев сеньорности: суть → как под капотом → пример из своего проекта → что на уровне системы (кластер, нагрузка).
3. Уточняющие бизнес-вопросы в начале — записывают в плюс дословно.
4. Синтаксис руками без IDE — отдельно тренируй (Мишин главный минус).
5. Любой код прогоняй по чек-листу § 1.12 из interview_ti1_ibs_structured.md.
