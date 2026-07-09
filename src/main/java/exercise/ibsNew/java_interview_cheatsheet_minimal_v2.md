# Java/Kotlin Backend Interview Cheat Sheet

<a id="toc"></a>

## Оглавление

### 1. Алгоритмы

1. [Количество пар с суммой 0](#alg-1-1)  
2. [Поиск элемента в BST](#alg-1-2)  
3. [Перевернуть односвязный список](#alg-1-3)  
4. [Слить два отсортированных массива](#alg-1-4)  
5. [Найти единственный дубликат](#alg-1-5)  
6. [Найти число в матрице](#alg-1-6)  
7. [Количество вхождений числа](#alg-1-7)  
8. [Числа 1–100: делятся на 2, но не на 4](#alg-1-8)  
9. [Наибольший общий элемент двух массивов](#alg-1-9)  
10. [Топ-5 значений с повторами](#alg-1-10)  
11. [Первый неповторяющийся символ](#alg-1-11)  
12. [Мин/макс температура за сутки](#alg-1-12)  
13. [Рейтинг поста](#alg-1-13)  
14. [Три стека в одном массиве](#alg-1-14)  
15. [Обход дерева](#alg-1-15)  

### 2. Spring / Stream / REST

1. [Группировка заказов по статусам](#spring-2-1)  
2. [Можно ли выполнить заказ по остаткам](#spring-2-2)  
3. [Проверка остатков через БД](#spring-2-3)  
4. [Заказы дешевле лимита](#spring-2-4)  
5. [Средняя цена позиций выше target](#spring-2-5)  
6. [Средняя стоимость UNPAID-заказов](#spring-2-6)  
7. [Остатки склада с пагинацией](#spring-2-7)  
8. [Заказы по статусу через БД / Specification](#spring-2-8)  
9. [POST создание заказа](#spring-2-9)  
10. [POST search с несколькими параметрами в body](#spring-2-10)  
11. [Успешные платежи: count + total](#spring-2-11)  
12. [Итоговая стоимость со скидками](#spring-2-12)  

### 4. Live code review

1. [Циклические зависимости Spring](#review-4-1)  
2. [updateOrderStatus](#review-4-2)  
3. [Подтверждение заказа с проверкой остатков](#review-4-3)  
4. [processPayment](#review-4-4)  
5. [Перевод денег](#review-4-5)  
6. [Возврат статуса из БД](#review-4-6)  
7. [Получение имени пользователя через REST](#review-4-7)  
8. [Внешняя интеграция и @Async](#review-4-8)  
9. [Kafka listener](#review-4-9)  
10. [Интеграция с внешним API](#review-4-10)  
11. [Потоконебезопасный singleton-бин](#review-4-11)  
12. [JPA N+1](#review-4-12)  
13. [@Scheduled job](#review-4-13)  
14. [Почему @Transactional не работает](#review-4-14)  

---

<a id="alg-1-1"></a>

## 1.1 Количество пар с суммой 0

```java
static long countZeroSumPairs(int[] arr) {
    // ВАЖНО: СЧИТАЕМ КОЛ-ВО КАЖДОГО ЧИСЛА, ЧТОБЫ НЕ ДЕЛАТЬ O(N^2)
    Map<Integer, Long> count = new HashMap<>();
    for (int x : arr) {
        count.merge(x, 1L, Long::sum);
    }

    long pairs = 0;
    for (var e : count.entrySet()) {
        int x = e.getKey();
        long c = e.getValue();

        // ВАЖНО: БЕРЁМ ТОЛЬКО X > 0, ЧТОБЫ НЕ ПОСЧИТАТЬ ПАРУ ДВАЖДЫ
        if (x > 0) {
            pairs += c * count.getOrDefault(-x, 0L);
        }

        // ВАЖНО: НУЛИ ПАРУЮТСЯ МЕЖДУ СОБОЙ: C(N, 2)
        if (x == 0) {
            pairs += c * (c - 1) / 2;
        }
    }
    return pairs;
}
```

---

<a id="alg-1-2"></a>

## 1.2 Поиск элемента в BST

```java
static TreeNode find(TreeNode root, int key) {
    TreeNode cur = root;

    while (cur != null && cur.val != key) {
        // ВАЖНО: BST ДАЁТ ВЫБОР НАПРАВЛЕНИЯ, НЕ ОБХОДИМ ВСЁ ДЕРЕВО
        cur = key < cur.val ? cur.left : cur.right;
    }

    return cur;
}
```

---

<a id="alg-1-3"></a>

## 1.3 Перевернуть односвязный список

```java
static Node reverse(Node head) {
    Node prev = null;

    while (head != null) {
        // ВАЖНО: СНАЧАЛА СОХРАНЯЕМ NEXT, ИНАЧЕ ПОТЕРЯЕМ ХВОСТ СПИСКА
        Node next = head.next;
        head.next = prev;
        prev = head;
        head = next;
    }

    return prev;
}
```

---

<a id="alg-1-4"></a>

## 1.4 Слить два отсортированных массива

```java
static int[] merge(int[] a, int[] b) {
    int[] result = new int[a.length + b.length];
    int i = 0, j = 0, k = 0;

    while (i < a.length && j < b.length) {
        // ВАЖНО: ДВА УКАЗАТЕЛЯ, НЕ ВЫЗЫВАЕМ SORT ПОСЛЕ СЛИЯНИЯ
        result[k++] = a[i] <= b[j] ? a[i++] : b[j++];
    }

    // ВАЖНО: ДОКИДЫВАЕМ ОСТАТОК ОДНОГО ИЗ МАССИВОВ
    while (i < a.length) result[k++] = a[i++];
    while (j < b.length) result[k++] = b[j++];

    return result;
}
```

---

<a id="alg-1-5"></a>

## 1.5 Найти единственный дубликат

```java
static int findDuplicate(int[] arr) {
    Set<Integer> seen = new HashSet<>();

    for (int x : arr) {
        // ВАЖНО: ADD ВЕРНЁТ FALSE, ЕСЛИ ЭЛЕМЕНТ УЖЕ БЫЛ
        if (!seen.add(x)) {
            return x;
        }
    }

    throw new IllegalArgumentException("duplicate not found");
}
```

---

<a id="alg-1-6"></a>

## 1.6 Найти число в матрице

```java
static boolean exists(int[][] matrix, int target) {
    if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
        return false;
    }

    int rows = matrix.length;
    int cols = matrix[0].length;
    int left = 0;
    int right = rows * cols - 1;

    while (left <= right) {
        // ВАЖНО: МАТРИЦА ПО УСЛОВИЮ = ОДИН ОТСОРТИРОВАННЫЙ МАССИВ
        int mid = (left + right) >>> 1;
        int value = matrix[mid / cols][mid % cols];

        if (value == target) return true;
        if (value < target) left = mid + 1;
        else right = mid - 1;
    }

    return false;
}
```

---

<a id="alg-1-7"></a>

## 1.7 Количество вхождений числа

```java
static long countOccurrences(int[] arr, int target) {
    return Arrays.stream(arr)
        // ВАЖНО: ДЛЯ НЕОТСОРТИРОВАННОГО МАССИВА НУЖЕН ПОЛНЫЙ ПРОХОД O(N)
        .filter(x -> x == target)
        .count();
}
```

---

<a id="alg-1-8"></a>

## 1.8 Числа 1–100: делятся на 2, но не на 4

```java
static void printNumbers() {
    // ВАЖНО: ЭТО ЧИСЛА ВИДА 4K + 2, СРАЗУ ИДЁМ ШАГОМ 4
    for (int i = 2; i <= 100; i += 4) {
        System.out.println(i);
    }
}
```

---

<a id="alg-1-9"></a>

## 1.9 Наибольший общий элемент двух массивов

```java
static OptionalInt maxCommon(int[] a, int[] b) {
    // ВАЖНО: SET ДАЁТ БЫСТРУЮ ПРОВЕРКУ НАЛИЧИЯ
    Set<Integer> values = Arrays.stream(a)
        .boxed()
        .collect(Collectors.toSet());

    return Arrays.stream(b)
        .filter(values::contains)
        .max(); // ВАЖНО: OPTIONALINT, ПОТОМУ ЧТО ОБЩЕГО ЭЛЕМЕНТА МОЖЕТ НЕ БЫТЬ
}
```

---

<a id="alg-1-10"></a>

## 1.10 Топ-5 значений с повторами

```java
static List<Integer> top5WithDuplicates(List<Integer> nums) {
    return nums.stream()
        // ВАЖНО: СНАЧАЛА СЧИТАЕМ ЧАСТОТЫ ЗНАЧЕНИЙ
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
        .limit(5) // ВАЖНО: ТОП-5 РАЗНЫХ ЗНАЧЕНИЙ, ПОТОМ ВОССТАНАВЛИВАЕМ ПОВТОРЫ
        .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
        .toList();
}
```

---

<a id="alg-1-11"></a>

## 1.11 Первый неповторяющийся символ

```java
static char firstUnique(String s) {
    // ВАЖНО: LINKEDHASHMAP СОХРАНЯЕТ ПОРЯДОК ПЕРВОГО ПОЯВЛЕНИЯ
    Map<Character, Integer> count = new LinkedHashMap<>();

    for (char c : s.toCharArray()) {
        count.merge(c, 1, Integer::sum);
    }

    for (var e : count.entrySet()) {
        if (e.getValue() == 1) {
            return e.getKey();
        }
    }

    return '\0';
}
```

---

<a id="alg-1-12"></a>

## 1.12 Мин/макс температура за сутки

```java
record MinMax(double min, double max) {}

static MinMax minMax(List<Double> temperatures) {
    if (temperatures == null || temperatures.isEmpty()) {
        throw new IllegalArgumentException("temperatures must not be empty");
    }

    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    for (Double t : temperatures) {
        if (t == null) {
            throw new IllegalArgumentException("temperature must not be null");
        }

        // ВАЖНО: ОДИН ПРОХОД, ОДНОВРЕМЕННО ОБНОВЛЯЕМ MIN И MAX
        min = Math.min(min, t);
        max = Math.max(max, t);
    }

    return new MinMax(min, max);
}
```

---

<a id="alg-1-13"></a>

## 1.13 Рейтинг поста

```java
public class PostRating {

    private long count;
    private long sum;

    public synchronized void addRating(int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("rating must be 0..5");
        }

        // ВАЖНО: ХРАНИМ SUM + COUNT, НЕ СПИСОК ВСЕХ ОЦЕНОК
        sum += rating;
        count++;
    }

    public synchronized double getAverage() {
        // ВАЖНО: SYNCHRONIZED НА ОБОИХ МЕТОДАХ ДАЁТ СОГЛАСОВАННУЮ ПАРУ SUM/COUNT
        return count == 0 ? 0.0 : (double) sum / count;
    }
}
```

---

<a id="alg-1-14"></a>

## 1.14 Три стека в одном массиве

```java
public class ThreeStacks {

    private final int[] data;
    private final int[] top = {-1, -1, -1};
    private final int capacityPerStack;

    public ThreeStacks(int capacityPerStack) {
        if (capacityPerStack <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.capacityPerStack = capacityPerStack;
        this.data = new int[capacityPerStack * 3];
    }

    public void push(int stack, int value) {
        checkStack(stack);
        if (top[stack] + 1 == capacityPerStack) {
            throw new IllegalStateException("stack is full");
        }

        // ВАЖНО: КАЖДОМУ СТЕКУ ВЫДЕЛЕН СВОЙ СЕГМЕНТ МАССИВА
        data[stack * capacityPerStack + ++top[stack]] = value;
    }

    public int pop(int stack) {
        checkStack(stack);
        if (top[stack] < 0) {
            throw new NoSuchElementException("stack is empty");
        }

        return data[stack * capacityPerStack + top[stack]--];
    }

    public int peek(int stack) {
        checkStack(stack);
        if (top[stack] < 0) {
            throw new NoSuchElementException("stack is empty");
        }

        return data[stack * capacityPerStack + top[stack]];
    }

    private void checkStack(int stack) {
        if (stack < 0 || stack > 2) {
            throw new IllegalArgumentException("stack must be 0..2");
        }
    }
}
```

---

<a id="alg-1-15"></a>

## 1.15 Обход дерева

```java
static void inOrder(TreeNode node, List<Integer> out) {
    if (node == null) {
        return;
    }

    // ВАЖНО: IN-ORDER ДЛЯ BST ВЕРНЁТ ЗНАЧЕНИЯ ПО ВОЗРАСТАНИЮ
    inOrder(node.left, out);
    out.add(node.val);
    inOrder(node.right, out);
}
```

---

<a id="spring-2-1"></a>

## 2.1 Группировка заказов по статусам

```java
public Map<OrderStatus, List<Order>> groupByStatus(List<Order> orders) {
    return orders.stream()
        // ВАЖНО: GROUPINGBY СРАЗУ СОБИРАЕТ MAP<STATUS, ORDERS>
        .collect(Collectors.groupingBy(Order::getStatus));
}
```

---

<a id="spring-2-2"></a>

## 2.2 Можно ли выполнить заказ по остаткам

```java
public boolean canFulfill(Order order, Map<Long, Integer> stock) {
    Map<Long, Integer> required = order.getItems()
        .stream()
        // ВАЖНО: ОДИН ТОВАР МОЖЕТ БЫТЬ НЕСКОЛЬКО РАЗ, ПОЭТОМУ СНАЧАЛА СУММИРУЕМ QUANTITY
        .collect(Collectors.groupingBy(
            OrderItem::productId,
            Collectors.summingInt(OrderItem::quantity)
        ));

    return required.entrySet()
        .stream()
        .allMatch(e -> stock.getOrDefault(e.getKey(), 0) >= e.getValue());
}
```

---

<a id="spring-2-3"></a>

## 2.3 Проверка остатков через БД

```java
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductIdIn(Collection<Long> productIds);
}

@Transactional(readOnly = true)
public boolean canFulfill(Order order) {
    Map<Long, Integer> required = order.getItems()
        .stream()
        .collect(Collectors.groupingBy(
            OrderItem::productId,
            Collectors.summingInt(OrderItem::quantity)
        ));

    Map<Long, Integer> available = stockRepository
        // ВАЖНО: ОДИН IN-ЗАПРОС, НЕ ЗАПРОС В ЦИКЛЕ НА КАЖДЫЙ ТОВАР
        .findByProductIdIn(required.keySet())
        .stream()
        .collect(Collectors.toMap(Stock::getProductId, Stock::getQuantity));

    return required.entrySet()
        .stream()
        .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
}
```

---

<a id="spring-2-4"></a>

## 2.4 Заказы дешевле лимита

```java
public List<Order> ordersBelow(List<Order> orders, BigDecimal limit) {
    return orders.stream()
        // ВАЖНО: BIGDECIMAL СРАВНИВАЕМ ЧЕРЕЗ COMPARETO, НЕ ЧЕРЕЗ EQUALS
        .filter(order -> order.getTotal().compareTo(limit) < 0)
        .toList();
}
```

---

<a id="spring-2-5"></a>

## 2.5 Средняя цена позиций выше target

```java
public List<Order> withAvgItemPriceAbove(List<Order> orders, BigDecimal target) {
    return orders.stream()
        // ВАЖНО: ПУСТЫЕ ITEMS ИНАЧЕ ДАДУТ ДЕЛЕНИЕ НА НОЛЬ
        .filter(order -> !order.getItems().isEmpty())
        .filter(order -> avgItemPrice(order).compareTo(target) > 0)
        .toList();
}

private BigDecimal avgItemPrice(Order order) {
    BigDecimal sum = order.getItems()
        .stream()
        .map(OrderItem::price)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(
        BigDecimal.valueOf(order.getItems().size()),
        2,
        RoundingMode.HALF_UP // ВАЖНО: ДЛЯ BIGDECIMAL DIVIDE НУЖЕН SCALE + ROUNDINGMODE
    );
}
```

---

<a id="spring-2-6"></a>

## 2.6 Средняя стоимость UNPAID-заказов

```java
public BigDecimal avgUnpaidCost(Map<OrderStatus, List<Order>> ordersByStatus) {
    // ВАЖНО: GETORDEFAULT, ЧТОБЫ НЕ ПОЛУЧИТЬ NPE, ЕСЛИ UNPAID НЕТ В MAP
    List<Order> unpaid = ordersByStatus.getOrDefault(OrderStatus.UNPAID, List.of());

    if (unpaid.isEmpty()) {
        return BigDecimal.ZERO;
    }

    BigDecimal sum = unpaid.stream()
        .map(Order::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(BigDecimal.valueOf(unpaid.size()), 2, RoundingMode.HALF_UP);
}
```

---

<a id="spring-2-7"></a>

## 2.7 Остатки склада с пагинацией

```java
public Page<Stock> getStockWithPagination(int page, int size) {
    if (page < 0 || size <= 0 || size > 200) {
        throw new IllegalArgumentException("invalid pagination");
    }

    // ВАЖНО: SORT ОБЯЗАТЕЛЕН, ИНАЧЕ СТРАНИЦЫ МОГУТ ДУБЛИРОВАТЬ/ПРОПУСКАТЬ ДАННЫЕ
    Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
    return stockRepository.findAll(pageable);
}
```

---

<a id="spring-2-8"></a>

## 2.8 Заказы по статусу через БД / Specification

```java
public interface OrderRepository extends JpaRepository<Order, Long>,
                                         JpaSpecificationExecutor<Order> {
    List<Order> findByStatus(OrderStatus status);
}

@Transactional(readOnly = true)
public List<Order> getOrdersByStatus(OrderStatus status) {
    // ВАЖНО: ФИЛЬТРУЕМ В БД, НЕ ДЕЛАЕМ FINDALL().STREAM().FILTER(...)
    return orderRepository.findByStatus(status);
}

public static Specification<Order> hasStatus(OrderStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
}
```

---

<a id="spring-2-9"></a>

## 2.9 POST создание заказа

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
            // ВАЖНО: ДЛЯ CREATE ВОЗВРАЩАЕМ 201 CREATED + LOCATION
            .created(URI.create("/orders/" + created.id()))
            .body(created);
    }
}

public record CreateOrderRequest(
    @NotNull Long customerId,
    @NotEmpty List<@Valid OrderItemDto> items // ВАЖНО: ВАЛИДИРУЕМ И СПИСОК, И ЭЛЕМЕНТЫ СПИСКА
) {}
```

---

<a id="spring-2-10"></a>

## 2.10 POST search с несколькими параметрами в body

```java
public record OrderSearchRequest(
    OrderStatus status,
    LocalDate from,
    LocalDate to,
    @Positive BigDecimal minTotal
) {}

@PostMapping("/orders/search")
public List<OrderDto> search(@Valid @RequestBody OrderSearchRequest request) {
    // ВАЖНО: НЕ ПИХАЕМ МНОГО @REQUESTPARAM, ДЕЛАЕМ ОДИН REQUEST DTO
    return orderService.search(request);
}
```

---

<a id="spring-2-11"></a>

## 2.11 Успешные платежи: count + total

```java
public record PaymentSummary(long count, BigDecimal total) {}

public PaymentSummary summarize(List<Transaction> transactions) {
    return transactions.stream()
        .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
        // ВАЖНО: TEEING СЧИТАЕТ COUNT И SUM ЗА ОДИН ПРОХОД
        .collect(Collectors.teeing(
            Collectors.counting(),
            Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add),
            PaymentSummary::new
        ));
}
```

---

<a id="spring-2-12"></a>

## 2.12 Итоговая стоимость со скидками

```java
public interface DiscountPolicy {
    boolean isApplicable(Purchase purchase);
    BigDecimal apply(BigDecimal currentPrice, Purchase purchase);
}

public BigDecimal totalPrice(List<Purchase> purchases, List<DiscountPolicy> policies) {
    BigDecimal total = BigDecimal.ZERO;

    for (Purchase purchase : purchases) {
        BigDecimal price = purchase.getPrice();

        for (DiscountPolicy policy : policies) {
            if (policy.isApplicable(purchase)) {
                // ВАЖНО: STRATEGY — КАЖДАЯ СКИДКА ИНКАПСУЛИРОВАНА ОТДЕЛЬНО
                price = policy.apply(price, purchase);
            }
        }

        total = total.add(price);
    }

    return total.setScale(2, RoundingMode.HALF_UP);
}
```

---

<a id="review-4-1"></a>

## 4.1 Циклические зависимости Spring

**Что заметили и правим:**

1. Цикл `OrderService ↔ PaymentService` — разрываем зависимость.
2. Field injection скрывает проблему — переходим на constructor injection.
3. `@Lazy` / `allow-circular-references=true` — только временный костыль.
4. Минимальный acceptable-вариант — событие между сервисами.

```java
@Service
public class PaymentService {

    private final ApplicationEventPublisher events;

    public PaymentService(ApplicationEventPublisher events) {
        // FIX #2: CONSTRUCTOR INJECTION, ЗАВИСИМОСТЬ ОБЯЗАТЕЛЬНА И ВИДНА
        this.events = events;
    }

    public void onPaymentFailed(Long orderId) {
        // FIX #1: PAYMENT SERVICE НЕ ЗНАЕТ ПРО ORDER SERVICE, ОН ТОЛЬКО ПУБЛИКУЕТ СОБЫТИЕ
        events.publishEvent(new PaymentFailedEvent(orderId));
    }
}

public record PaymentFailedEvent(Long orderId) {}

@Component
public class OrderPaymentListener {

    private final OrderService orderService;

    public OrderPaymentListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handle(PaymentFailedEvent event) {
        // FIX #1: ORDER SERVICE РЕАГИРУЕТ НА СОБЫТИЕ, ПРЯМОГО ЦИКЛА БОЛЬШЕ НЕТ
        orderService.cancelOrder(event.orderId());
    }
}
```

---

<a id="review-4-2"></a>

## 4.2 updateOrderStatus

**Что заметили и правим:**

1. Строки-статусы и `if/else` — заменяем на `enum` + allowed transitions.
2. `findById(...).get()` — заменяем на `orElseThrow` с бизнес-исключением.
3. Нет проверки владельца/прав — проверяем `userId`.
4. Нет транзакции — добавляем `@Transactional`.
5. Lost update — добавляем `@Version`.
6. Field injection — constructor injection.

```java
public enum OrderStatus {
    NEW, PAID, SHIPPED, CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        NEW, Set.of(PAID, CANCELLED),
        PAID, Set.of(SHIPPED, CANCELLED),
        SHIPPED, Set.of(),
        CANCELLED, Set.of()
    );

    public boolean canMoveTo(OrderStatus target) {
        // FIX #1: ВАЛИДНЫЕ ПЕРЕХОДЫ ХРАНЯТСЯ В ОДНОМ МЕСТЕ, А НЕ В IF/ELSE ПО СТРОКАМ
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }
}

@Entity
public class Order {
    @Id
    private Long id;

    @Version
    private Long version; // FIX #5: ЗАЩИТА ОТ LOST UPDATE

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // FIX #1: ENUM СТРОКОЙ В БД, НЕ ORDINAL И НЕ MAGIC STRING

    private Long userId;

    public boolean belongsTo(Long userId) {
        return Objects.equals(this.userId, userId);
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!status.canMoveTo(newStatus)) {
            throw new InvalidStatusTransitionException(status, newStatus);
        }
        this.status = newStatus;
    }
}

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository; // FIX #6: CONSTRUCTOR INJECTION
    }

    @Transactional // FIX #4: READ-MODIFY-WRITE В ОДНОЙ ТРАНЗАКЦИИ
    public OrderDto updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId)); // FIX #2: НЕ OPTIONAL.GET()

        if (!order.belongsTo(userId)) {
            throw new AccessDeniedException("order does not belong to user"); // FIX #3: ПРОВЕРКА ПРАВ
        }

        order.changeStatus(newStatus); // FIX #1: ВАЛИДАЦИЯ ПЕРЕХОДА В ДОМЕНЕ
        return OrderDto.from(order); // FIX #4: SAVE НЕ ОБЯЗАТЕЛЕН, СРАБОТАЕТ DIRTY CHECKING
    }
}
```

---

<a id="review-4-3"></a>

## 4.3 Подтверждение заказа с проверкой остатков

**Что заметили и правим:**

1. Нет транзакции — списание остатков и статус заказа должны быть атомарны.
2. Нельзя частично списывать — сначала проверяем ВСЁ, потом меняем данные.
3. Oversell при параллельных заказах — берём lock на остатки.
4. N+1 по складу — один запрос `IN`.
5. Один товар может быть в заказе несколько раз — агрегируем required по `productId`.
6. Повторный confirm — валидируем текущий статус.

**Кусок 1 — грузим заказ и проверяем статус:**

```java
@Transactional // FIX #1: ВСЁ ИЛИ НИЧЕГО
public void confirmOrder(Long orderId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (order.getStatus() != OrderStatus.PAID) {
        throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.CONFIRMED); // FIX #6
    }
```

**Кусок 2 — считаем, сколько реально нужно товара:**

```java
    Map<Long, Integer> required = order.getItems().stream()
        // FIX #5: ОДИН PRODUCTID МОЖЕТ БЫТЬ В НЕСКОЛЬКИХ ПОЗИЦИЯХ
        .collect(Collectors.groupingBy(
            OrderItem::getProductId,
            Collectors.summingInt(OrderItem::getQuantity)
        ));
```

**Кусок 3 — одним запросом берём остатки под lock:**

```java
    List<Long> productIds = required.keySet().stream().sorted().toList(); // FIX #3: СТАБИЛЬНЫЙ ПОРЯДОК LOCK'ОВ

    Map<Long, Stock> stocks = stockRepository.findByProductIdInForUpdate(productIds)
        .stream()
        .collect(Collectors.toMap(Stock::getProductId, Function.identity())); // FIX #4: ОДИН IN-ЗАПРОС
```

```java
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId in :productIds")
    List<Stock> findByProductIdInForUpdate(Collection<Long> productIds); // FIX #3: ЗАЩИТА ОТ OVERSELL
}
```

**Кусок 4 — сначала проверяем всё, потом списываем:**

```java
    for (var e : required.entrySet()) {
        Stock stock = stocks.get(e.getKey());
        if (stock == null || stock.getQuantity() < e.getValue()) {
            throw new InsufficientStockException(e.getKey(), e.getValue()); // FIX #2: НЕ ДЕЛАЕМ ЧАСТИЧНЫЙ CONFIRM
        }
    }

    required.forEach((productId, qty) -> stocks.get(productId).decrease(qty));
    order.setStatus(OrderStatus.CONFIRMED);
}
```

---

<a id="review-4-4"></a>

## 4.4 processPayment

**Что заметили и правим:**

1. `double` для денег — только `BigDecimal`.
2. Повтор запроса может списать деньги дважды — нужен `idempotencyKey`.
3. Внешний `charge` внутри БД-транзакции — плохо; держим транзакции короткими.
4. Если charge прошёл, а save упал — нужен сохранённый `PENDING` payment до вызова.
5. Строки `"success"/"fail"` — возвращаем DTO/status.

```java
public PaymentDto processPayment(Long orderId, BigDecimal amount, String idempotencyKey) { // FIX #1, #2
    Payment payment = tx.execute(status -> createOrGetPendingPayment(orderId, amount, idempotencyKey));

    if (payment.isFinalStatus()) {
        return PaymentDto.from(payment); // FIX #2: ПОВТОРНЫЙ ЗАПРОС НЕ ДЕЛАЕТ НОВЫЙ CHARGE
    }

    PaymentGatewayResponse response = paymentGateway.charge(
        payment.getCustomerId(),
        amount,
        idempotencyKey // FIX #2: RETRY CHARGE БЕЗ КЛЮЧА ОПАСЕН
    ); // FIX #3: HTTP-ВЫЗОВ ВНЕ DB-ТРАНЗАКЦИИ

    return tx.execute(status -> finishPayment(payment.getId(), response));
}

private Payment createOrGetPendingPayment(Long orderId, BigDecimal amount, String idempotencyKey) {
    return paymentRepository.findByIdempotencyKey(idempotencyKey)
        .orElseGet(() -> {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

            if (order.getStatus() == OrderStatus.PAID) {
                throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.PAID);
            }

            // FIX #4: СНАЧАЛА ФИКСИРУЕМ ПЛАТЁЖНОЕ НАМЕРЕНИЕ В БД
            return paymentRepository.save(Payment.pending(order.getId(), amount, idempotencyKey));
        });
}

private PaymentDto finishPayment(Long paymentId, PaymentGatewayResponse response) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    if (response.success()) {
        payment.markSuccess(response.externalId());
    } else {
        payment.markFailed(response.errorCode());
    }

    return PaymentDto.from(payment); // FIX #5: ВОЗВРАЩАЕМ ПОНЯТНЫЙ DTO, НЕ STRING
}
```

---

<a id="review-4-5"></a>

## 4.5 Перевод денег

**Что заметили и правим:**

1. Нет `@Transactional` — списание и зачисление должны быть атомарны.
2. `double` для денег — `BigDecimal`.
3. Check-then-act гонка по балансу — lock или atomic update.
4. Deadlock при встречных переводах — lock в стабильном порядке.
5. Молчаливый return при нехватке средств — кидаем бизнес-исключение.

```java
@Transactional // FIX #1: ДВА БАЛАНСА МЕНЯЮТСЯ В ОДНОЙ ТРАНЗАКЦИИ
public void transfer(Long fromId, Long toId, BigDecimal amount) { // FIX #2: BIGDECIMAL
    if (amount.signum() <= 0) {
        throw new InvalidAmountException(amount);
    }
    if (Objects.equals(fromId, toId)) {
        throw new SameAccountTransferException(fromId);
    }

    Long firstId = Math.min(fromId, toId);
    Long secondId = Math.max(fromId, toId);

    Account first = lockById(firstId);
    Account second = lockById(secondId); // FIX #4: ОДИНАКОВЫЙ ПОРЯДОК LOCK'ОВ ПРОТИВ DEADLOCK

    Account from = first.getId().equals(fromId) ? first : second;
    Account to = first.getId().equals(toId) ? first : second;

    if (from.getBalance().compareTo(amount) < 0) {
        throw new InsufficientFundsException(fromId, amount); // FIX #5: НЕ МОЛЧА RETURN
    }

    from.setBalance(from.getBalance().subtract(amount));
    to.setBalance(to.getBalance().add(amount));
}

private Account lockById(Long id) {
    return accountRepository.findByIdForUpdate(id)
        .orElseThrow(() -> new AccountNotFoundException(id));
}
```

```java
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id); // FIX #3: НЕ ДАЁМ ДВУМ ТРАНЗАКЦИЯМ СПИСАТЬ ОДИН БАЛАНС
}
```

---

<a id="review-4-6"></a>

## 4.6 Возврат статуса из БД

**Что заметили и правим:**

1. `.get()` ломает отсутствие заказа до `if (order != null)`.
2. `""` как статус отсутствия — плохой контракт.
3. Для чтения статуса не надо грузить всю entity.
4. Если JDBC — никаких SQL-конкатенаций.

```java
@Transactional(readOnly = true)
public OrderStatus getOrderStatus(Long orderId) {
    return orderRepository.findStatusById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId)); // FIX #1, #2
}

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o.status from Order o where o.id = :id")
    Optional<OrderStatus> findStatusById(Long id); // FIX #3: ЧИТАЕМ ТОЛЬКО STATUS
}
```

```java
String sql = "select status from orders where id = ?";
return jdbcTemplate.queryForObject(sql, OrderStatus.class, id); // FIX #4: PARAMETERIZED QUERY
```

---

<a id="review-4-7"></a>

## 4.7 Получение имени пользователя через REST

**Что заметили и правим:**

1. Нет timeout — поток может зависнуть.
2. URL hardcode/конкатенация — выносим в config и используем path variable.
3. `Map` вместо DTO — NPE/cast problems.
4. Нет обработки 4xx/5xx.
5. Field injection — constructor injection.

```java
@ConfigurationProperties(prefix = "clients.user-service")
public record UserClientProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {}

public record UserResponse(Long id, String name) {}

@Service
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder, UserClientProperties props) {
        this.restClient = builder
            .baseUrl(props.baseUrl()) // FIX #2: URL ИЗ CONFIG
            .requestFactory(ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                    .withConnectTimeout(props.connectTimeout()) // FIX #1: CONNECT TIMEOUT
                    .withReadTimeout(props.readTimeout())       // FIX #1: READ TIMEOUT
            ))
            .build(); // FIX #5: СОЗДАЁМ КЛИЕНТ ЧЕРЕЗ CONSTRUCTOR
    }

    public String getUserName(Long userId) {
        UserResponse response = restClient.get()
            .uri(uri -> uri.path("/users/{id}").build(userId)) // FIX #2: НЕ СКЛЕИВАЕМ URL РУКАМИ
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                (request, resp) -> { throw new UserNotFoundException(userId); }) // FIX #4
            .onStatus(HttpStatusCode::is5xxServerError,
                (request, resp) -> { throw new ExternalServiceException("user-service failed"); }) // FIX #4
            .body(UserResponse.class); // FIX #3: DTO ВМЕСТО MAP

        if (response == null || response.name() == null) {
            throw new ExternalServiceException("invalid user-service response");
        }

        return response.name();
    }
}
```

---

<a id="review-4-8"></a>

## 4.8 Внешняя интеграция и @Async

**Что заметили и правим:**

1. Self-invocation `this.export()` — `@Async` и `@Transactional` не сработают.
2. `@Async void` теряет ошибки — возвращаем `CompletableFuture`.
3. Нужен ограниченный executor.
4. Внешний API внутри транзакции — держит DB connection и ломает консистентность.

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("export-");
        executor.initialize();
        return executor; // FIX #3: ОГРАНИЧЕННЫЙ POOL
    }
}
```

```java
@Service
public class ExportFacade {
    private final ExportService exportService;

    public List<CompletableFuture<Void>> exportAll(List<Long> ids) {
        return ids.stream()
            .map(exportService::export) // FIX #1: ВЫЗОВ ЧЕРЕЗ ДРУГОЙ BEAN, НЕ THIS.EXPORT()
            .toList();
    }
}
```

```java
@Service
public class ExportService {
    @Async("exportExecutor")
    public CompletableFuture<Void> export(Long orderId) { // FIX #2: НЕ VOID
        OrderExportDto dto = tx.execute(status -> loadExportDto(orderId)); // FIX #4: КОРОТКАЯ DB-ТРАНЗАКЦИЯ
        externalApi.send(dto); // FIX #4: ВНЕ DB-ТРАНЗАКЦИИ, С TIMEOUT/IDEMPOTENCY
        tx.executeWithoutResult(status -> markExported(orderId));
        return CompletableFuture.completedFuture(null);
    }
}
```

---

<a id="review-4-9"></a>

## 4.9 Kafka listener

**Что заметили и правим:**

1. Kafka обычно at-least-once — нужны idempotency/dedup.
2. Poison message не должен бесконечно блокировать партицию — нужен DLT/error handler.
3. `Map<String,Object>` и касты — заменяем на typed event.
4. `new ObjectMapper()` в listener — не надо.
5. Строки event type — enum/handlers.

```java
public record OrderEvent(UUID eventId, Long orderId, OrderEventType type) {}

@KafkaListener(topics = "orders", groupId = "order-service")
@Transactional // FIX #1: DEDUP И БИЗНЕС-ИЗМЕНЕНИЕ В ОДНОЙ ТРАНЗАКЦИИ
public void listen(OrderEvent event) { // FIX #3: TYPED EVENT, НЕ MAP
    if (!processedEventRepository.tryMarkProcessed(event.eventId())) {
        return; // FIX #1: ДУБЛЬ СООБЩЕНИЯ ПРОПУСКАЕМ
    }

    OrderEventHandler handler = handlers.get(event.type()); // FIX #5: DISPATCH ПО ENUM/HANDLER
    if (handler == null) {
        throw new UnsupportedEventTypeException(event.type());
    }

    handler.handle(event);
}
```

```java
@Bean
DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
    return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3)); // FIX #2: RETRY + DLT
}
```

---

<a id="review-4-10"></a>

## 4.10 Интеграция с внешним API

**Что заметили и правим:**

1. Entity уходит наружу — нужен DTO.
2. Нет timeout/error handling/retry policy.
3. Нет idempotency key — возможны дубли во внешней системе.
4. БД и внешний HTTP нельзя делать одной «магической» транзакцией.
5. Минимальный acceptable-вариант — outbox + worker.

```java
@Transactional
public void scheduleExport(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.canBeExported()) {
        throw new InvalidOrderStateException(orderId, order.getStatus());
    }

    // FIX #4, #5: В ТРАНЗАКЦИИ ТОЛЬКО ПИШЕМ OUTBOX-СОБЫТИЕ, HTTP НЕ ВЫЗЫВАЕМ
    outboxRepository.save(OutboxEvent.externalOrderExport(orderId));
}
```

```java
public void handle(OutboxEvent event) {
    ExternalOrderDto dto = toDto(event.payload()); // FIX #1: НАРУЖУ УХОДИТ DTO, НЕ ENTITY
    client.sendOrder(dto, event.id().toString()); // FIX #2, #3: TIMEOUT + IDEMPOTENCY KEY В CLIENT
    outboxRepository.markSent(event.id());
}
```

---

<a id="review-4-11"></a>

## 4.11 Потоконебезопасный singleton-бин

**Что заметили и правим:**

1. Spring singleton вызывается параллельно — `HashMap` нельзя менять конкурентно.
2. `containsKey + put` не атомарны.
3. `SimpleDateFormat` не thread-safe.
4. Кэшу нужны TTL/limit, иначе устареет или разрастётся.

```java
@Service
public class RateService {

    private final ConcurrentMap<String, BigDecimal> cache = new ConcurrentHashMap<>(); // FIX #1
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // FIX #3

    public BigDecimal getRate(String currency) {
        return cache.computeIfAbsent(currency, this::loadRate); // FIX #2: АТОМАРНО
    }

    public String today() {
        return LocalDate.now().format(DATE_FORMAT); // FIX #3: IMMUTABLE FORMATTER
    }

    private BigDecimal loadRate(String currency) {
        // FIX #4: В PROD ЛУЧШЕ CAFFEINE/@CACHEABLE С TTL И MAX SIZE
        return BigDecimal.ONE;
    }
}
```

---

<a id="review-4-12"></a>

## 4.12 JPA N+1

**Что заметили и правим:**

1. `findAll()` без пагинации — риск OOM.
2. Lazy `customer/items` в цикле — N+1.
3. Для отчёта лучше DTO projection, не entity.
4. Count/sum лучше считать в БД.

```java
public record OrderReportRow(Long orderId, String customerName, long itemsCount) {}

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        select new com.example.OrderReportRow(o.id, c.name, count(i))
        from Order o
        join o.customer c
        left join o.items i
        where o.createdAt >= :from and o.createdAt < :to
        group by o.id, c.name
        """)
    Page<OrderReportRow> buildReport(Instant from, Instant to, Pageable pageable);
    // FIX #2, #3, #4: DTO PROJECTION СРАЗУ ИЗ БД, БЕЗ LAZY ACCESS В ЦИКЛЕ
}

@Transactional(readOnly = true)
public Page<OrderReportRow> buildReport(Instant from, Instant to, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id")); // FIX #1: PAGINATION + STABLE SORT
    return orderRepository.buildReport(from, to, pageable);
}
```

---

<a id="review-4-13"></a>

## 4.13 @Scheduled job

**Что заметили и правим:**

1. В кластере job запустится на каждом инстансе — нужен distributed lock.
2. Нельзя грузить всю таблицу — batch/limit.
3. Нужны retry/логирование/метрики по зависшим платежам.
4. Каждую запись лучше обрабатывать изолированно.

```java
@Scheduled(fixedDelayString = "${jobs.payment-reconciliation.delay}")
public void reconcile() {
    lockProvider.executeWithLock("payment-reconciliation", Duration.ofMinutes(5), () -> { // FIX #1
        List<Payment> stuck = paymentRepository.findStuckPendingPayments(
            Instant.now().minus(Duration.ofMinutes(10)),
            PageRequest.of(0, 100) // FIX #2: BATCH, НЕ ВСЯ ТАБЛИЦА
        );

        for (Payment payment : stuck) {
            reconcileOne(payment); // FIX #4: ОДИН ПЛАТЁЖ = ОДНА КОРОТКАЯ ОПЕРАЦИЯ
        }
    });
}

@Transactional
void reconcileOne(Payment payment) {
    GatewayStatus status = gatewayClient.getStatus(payment.getExternalId()); // FIX #3: CLIENT С TIMEOUT/RETRY

    if (status == GatewayStatus.SUCCESS) payment.markSuccess();
    if (status == GatewayStatus.FAILED) payment.markFailed();
}
```

---

<a id="review-4-14"></a>

## 4.14 Почему @Transactional не работает

**Что заметили и правим:**

1. Self-invocation `this.createOne()` — вызов мимо Spring proxy.
2. `@Transactional` на private/final method обычно не сработает через proxy.
3. Решение — вынести транзакционный метод в отдельный bean.
4. Проверить rollback rules для checked exceptions.

```java
@Service
public class OrderService {

    private final OrderTxService orderTxService;

    public void importOrders(List<CreateOrderRequest> requests) {
        for (CreateOrderRequest request : requests) {
            orderTxService.createOne(request); // FIX #1, #3: ВЫЗОВ ЧЕРЕЗ ДРУГОЙ SPRING BEAN
        }
    }
}

@Service
public class OrderTxService {

    private final OrderRepository orderRepository;

    @Transactional // FIX #2: PUBLIC METHOD НА SPRING BEAN ПРОХОДИТ ЧЕРЕЗ PROXY
    public void createOne(CreateOrderRequest request) {
        orderRepository.save(Order.from(request));
    }
}
```
