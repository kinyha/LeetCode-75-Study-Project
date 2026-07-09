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

```java
// 1. 🔴 ЦИКЛ ORDERSERVICE ↔ PAYMENTSERVICE
// 2. 🔴 CONSTRUCTOR INJECTION ВМЕСТО FIELD INJECTION
// 3. 🔴 РАЗРЫВАЕМ ЗАВИСИМОСТЬ ЧЕРЕЗ EVENT / ТРЕТИЙ СЕРВИС
// 4. 🟡 @LAZY И ALLOW-CIRCULAR-REFERENCES=TRUE — КОСТЫЛИ, НЕ РЕШЕНИЕ

@Service
class PaymentService {
    private final ApplicationEventPublisher events; // 🔴 FIX #2 CONSTRUCTOR INJECTION

    PaymentService(ApplicationEventPublisher events) {
        this.events = events;
    }

    void onPaymentFailed(Long orderId) {
        // 🔴 FIX #1/#3 PAYMENT SERVICE БОЛЬШЕ НЕ ВЫЗЫВАЕТ ORDERSERVICE НАПРЯМУЮ
        events.publishEvent(new PaymentFailedEvent(orderId));
    }
}

record PaymentFailedEvent(Long orderId) {}

@Component
class OrderPaymentListener {
    private final OrderService orderService; // 🔴 FIX #3 ЗАВИСИМОСТЬ ТОЛЬКО В ОДНУ СТОРОНУ

    OrderPaymentListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    void handle(PaymentFailedEvent event) {
        orderService.cancelOrder(event.orderId());
    }
}
```

---

<a id="review-4-2"></a>

## 4.2 updateOrderStatus

```java
// 1. 🔴 CONSTRUCTOR INJECTION
// 2. 🔴 OPTIONAL.ORELSETHROW()
// 3. 🔴 ENUM ВМЕСТО STRING
// 4. 🔴 ПРОВЕРКА ПЕРЕХОДОВ СТАТУСОВ
// 5. 🔴 @TRANSACTIONAL
// 6. 🔴 @VERSION, ГОНКИ / LOST UPDATE
// 7. 🟡 CUSTOM EXCEPTION
// 8. 🟡 @RESTCONTROLLERADVICE

public enum OrderStatus {
    NEW, PAID, SHIPPED, CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        NEW, Set.of(PAID, CANCELLED),
        PAID, Set.of(SHIPPED, CANCELLED),
        SHIPPED, Set.of(),
        CANCELLED, Set.of()
    );

    boolean canMoveTo(OrderStatus target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target); // 🔴 FIX #4 ПРОВЕРКА ПЕРЕХОДОВ СТАТУСОВ
    }
}

@Entity
class Order {
    @Id Long id;

    @Version
    Long version; // 🔴 FIX #6 ЗАЩИТА ОТ LOST UPDATE ПРИ ДВУХ ПАРАЛЛЕЛЬНЫХ UPDATE

    @Enumerated(EnumType.STRING)
    OrderStatus status; // 🔴 FIX #3 ENUM ВМЕСТО STRING, STRING В БД ВМЕСТО ORDINAL

    Long userId;

    void changeStatus(OrderStatus newStatus) {
        if (!status.canMoveTo(newStatus)) {
            throw new InvalidStatusTransitionException(status, newStatus); // 🟡 FIX #7 CUSTOM EXCEPTION
        }
        this.status = newStatus;
    }
}

@Service
class OrderService {
    private final OrderRepository orderRepository; // 🔴 FIX #1 FINAL DEPENDENCY

    OrderService(OrderRepository orderRepository) { // 🔴 FIX #1 CONSTRUCTOR INJECTION
        this.orderRepository = orderRepository;
    }

    @Transactional // 🔴 FIX #5 READ-MODIFY-WRITE В ОДНОЙ ТРАНЗАКЦИИ
    OrderDto updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId)); // 🔴 FIX #2 НЕ OPTIONAL.GET()

        if (!Objects.equals(order.userId, userId)) {
            throw new AccessDeniedException("not owner"); // 🟡 FIX #7 БИЗНЕС-ОШИБКА, НЕ NPE/ILLEGALARGUMENT В РАНДОМНОМ МЕСТЕ
        }

        order.changeStatus(newStatus); // 🔴 FIX #3/#4 ENUM + VALID TRANSITION
        return OrderDto.from(order);   // 🔴 FIX #5 DIRTY CHECKING, SAVE() НЕ ОБЯЗАТЕЛЕН
    }
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage()); // 🟡 FIX #8 404
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    ProblemDetail conflict(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage()); // 🟡 FIX #8 409
    }
}
```

---

<a id="review-4-3"></a>

## 4.3 Подтверждение заказа с проверкой остатков

```java
// 1. 🔴 @TRANSACTIONAL — СПИСАНИЕ ОСТАТКОВ И СТАТУС ЗАКАЗА АТОМАРНЫ
// 2. 🔴 НЕЛЬЗЯ ЧАСТИЧНО СПИСЫВАТЬ — СНАЧАЛА ПРОВЕРЯЕМ ВСЁ
// 3. 🔴 OVERSALE ПРИ ПАРАЛЛЕЛЬНЫХ ЗАКАЗАХ — LOCK НА STOCK
// 4. 🔴 N+1 ПО СКЛАДУ — ОДИН ЗАПРОС IN
// 5. 🟡 ОДИН PRODUCTID МОЖЕТ БЫТЬ В НЕСКОЛЬКИХ ПОЗИЦИЯХ — АГРЕГИРУЕМ
// 6. 🟡 ПОВТОРНЫЙ CONFIRM — ПРОВЕРЯЕМ ТЕКУЩИЙ СТАТУС

@Transactional // 🔴 FIX #1 ВСЁ ИЛИ НИЧЕГО
void confirmOrder(Long orderId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (order.status != OrderStatus.PAID) {
        throw new InvalidStatusTransitionException(order.status, OrderStatus.CONFIRMED); // 🟡 FIX #6 НЕ СПИСЫВАЕМ ПОВТОРНО
    }

    Map<Long, Integer> required = order.items.stream()
        .collect(groupingBy(OrderItem::productId, summingInt(OrderItem::quantity))); // 🟡 FIX #5 СУММИРУЕМ ДУБЛИ PRODUCTID

    List<Long> productIds = required.keySet().stream().sorted().toList(); // 🔴 FIX #3 СТАБИЛЬНЫЙ ПОРЯДОК LOCK'ОВ

    Map<Long, Stock> stockByProduct = stockRepository.findByProductIdInForUpdate(productIds)
        .stream()
        .collect(toMap(Stock::productId, identity())); // 🔴 FIX #4 ОДИН IN-ЗАПРОС, НЕ ЗАПРОС В ЦИКЛЕ

    for (var item : required.entrySet()) {
        Stock stock = stockByProduct.get(item.getKey());
        if (stock == null || stock.quantity < item.getValue()) {
            throw new InsufficientStockException(item.getKey(), item.getValue()); // 🔴 FIX #2 ПАДАЕМ ДО ЛЮБОГО СПИСАНИЯ
        }
    }

    required.forEach((productId, qty) -> stockByProduct.get(productId).decrease(qty)); // 🔴 FIX #2 СПИСЫВАЕМ ТОЛЬКО ПОСЛЕ ПОЛНОЙ ПРОВЕРКИ
    order.status = OrderStatus.CONFIRMED;
}

interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId in :ids")
    List<Stock> findByProductIdInForUpdate(Collection<Long> ids); // 🔴 FIX #3 LOCK ПРОТИВ OVERSALE
}
```

---

<a id="review-4-4"></a>

## 4.4 processPayment

```java
// 1. 🔴 BIGDECIMAL ВМЕСТО DOUBLE
// 2. 🔴 IDEMPOTENCY-KEY — ПОВТОР ЗАПРОСА НЕ ДОЛЖЕН СПИСАТЬ ДВАЖДЫ
// 3. 🔴 PENDING PAYMENT В БД ДО ВЫЗОВА ШЛЮЗА
// 4. 🔴 HTTP CHARGE НЕ ДЕРЖИМ ВНУТРИ DB-ТРАНЗАКЦИИ
// 5. 🟡 DTO/STATUS ВМЕСТО "SUCCESS"/"FAIL"

PaymentDto processPayment(Long orderId, BigDecimal amount, String idempotencyKey) { // 🔴 FIX #1/#2
    Payment payment = tx(() -> paymentRepository.findByIdempotencyKey(idempotencyKey)
        .orElseGet(() -> createPendingPayment(orderId, amount, idempotencyKey))); // 🔴 FIX #3

    if (payment.isFinal()) {
        return PaymentDto.from(payment); // 🔴 FIX #2 ПОВТОРНЫЙ ЗАПРОС ВЕРНЁТ СТАРЫЙ РЕЗУЛЬТАТ, НЕ НОВЫЙ CHARGE
    }

    PaymentResponse response = paymentGateway.charge(payment.customerId, amount, idempotencyKey); // 🔴 FIX #4 ВНЕ DB-ТРАНЗАКЦИИ + IDEMPOTENCY

    return tx(() -> finishPayment(payment.id, response)); // 🔴 FIX #4 КОРОТКАЯ ТРАНЗАКЦИЯ ТОЛЬКО НА UPDATE СТАТУСА
}

@Transactional
Payment createPendingPayment(Long orderId, BigDecimal amount, String key) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (order.status == OrderStatus.PAID) {
        throw new InvalidStatusTransitionException(order.status, OrderStatus.PAID);
    }

    return paymentRepository.save(Payment.pending(order.id, amount, key)); // 🔴 FIX #3 СНАЧАЛА ФИКСИРУЕМ НАМЕРЕНИЕ
}

PaymentDto finishPayment(Long paymentId, PaymentResponse response) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    if (response.success()) payment.markSuccess(response.externalId());
    else payment.markFailed(response.errorCode());

    return PaymentDto.from(payment); // 🟡 FIX #5 НЕ STRING
}
```

---

<a id="review-4-5"></a>

## 4.5 Перевод денег

```java
// 1. 🔴 @TRANSACTIONAL — СПИСАНИЕ И ЗАЧИСЛЕНИЕ АТОМАРНЫ
// 2. 🔴 BIGDECIMAL ВМЕСТО DOUBLE
// 3. 🔴 LOCK / ATOMIC UPDATE ПРОТИВ ГОНКИ ПО БАЛАНСУ
// 4. 🔴 LOCK В СТАБИЛЬНОМ ПОРЯДКЕ ПРОТИВ DEADLOCK
// 5. 🟡 INSUFFICIENTFUNDSEXCEPTION ВМЕСТО МОЛЧАЛИВОГО RETURN

@Transactional // 🔴 FIX #1
void transfer(Long fromId, Long toId, BigDecimal amount) { // 🔴 FIX #2
    if (amount.signum() <= 0) throw new InvalidAmountException(amount);
    if (Objects.equals(fromId, toId)) throw new SameAccountTransferException(fromId);

    Account first = accountRepository.findByIdForUpdate(Math.min(fromId, toId)).orElseThrow();
    Account second = accountRepository.findByIdForUpdate(Math.max(fromId, toId)).orElseThrow(); // 🔴 FIX #4 LOCK ORDER

    Account from = Objects.equals(first.id, fromId) ? first : second;
    Account to = Objects.equals(first.id, toId) ? first : second;

    if (from.balance.compareTo(amount) < 0) {
        throw new InsufficientFundsException(fromId, amount); // 🟡 FIX #5 НЕ МОЛЧИМ
    }

    from.balance = from.balance.subtract(amount);
    to.balance = to.balance.add(amount);
}

interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id); // 🔴 FIX #3 ДВА ПОТОКА НЕ СПИШУТ ОДИН БАЛАНС ОДНОВРЕМЕННО
}
```

---

<a id="review-4-6"></a>

## 4.6 Возврат статуса из БД

```java
// 1. 🔴 OPTIONAL.ORELSETHROW() ВМЕСТО FINDBYID().GET()
// 2. 🔴 НЕ ВОЗВРАЩАЕМ "" КАК ПРИЗНАК ОТСУТСТВИЯ
// 3. 🟡 PROJECTION — ДЛЯ СТАТУСА НЕ ГРУЗИМ ВСЮ ENTITY
// 4. 🟡 JDBC: PARAMETERIZED QUERY, НЕ SQL-КОНКАТЕНАЦИЯ

@Transactional(readOnly = true)
OrderStatus getOrderStatus(Long orderId) {
    return orderRepository.findStatusById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId)); // 🔴 FIX #1/#2
}

interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o.status from Order o where o.id = :id")
    Optional<OrderStatus> findStatusById(Long id); // 🟡 FIX #3 ЧИТАЕМ ТОЛЬКО STATUS
}

String sql = "select status from orders where id = ?";
OrderStatus status = jdbcTemplate.queryForObject(sql, OrderStatus.class, orderId); // 🟡 FIX #4 НЕ КОНКАТЕНИРУЕМ SQL
```

---

<a id="review-4-7"></a>

## 4.7 Получение имени пользователя через REST

```java
// 1. 🔴 TIMEOUT — ВНЕШНИЙ СЕРВИС НЕ ДОЛЖЕН ВЕШАТЬ ПОТОК НАВСЕГДА
// 2. 🔴 TYPED DTO ВМЕСТО MAP
// 3. 🔴 ОБРАБОТКА 4XX/5XX
// 4. 🟡 URL В CONFIG, НЕ HARDCODE
// 5. 🟡 CONSTRUCTOR INJECTION

@ConfigurationProperties("clients.user-service")
record UserClientProps(String baseUrl, Duration connectTimeout, Duration readTimeout) {}

record UserResponse(Long id, String name) {} // 🔴 FIX #2 DTO ВМЕСТО MAP

@Service
class UserClient {
    private final RestClient restClient;

    UserClient(RestClient.Builder builder, UserClientProps props) { // 🟡 FIX #5 CONSTRUCTOR INJECTION
        this.restClient = builder
            .baseUrl(props.baseUrl()) // 🟡 FIX #4 CONFIG
            .requestFactory(timeoutFactory(props.connectTimeout(), props.readTimeout())) // 🔴 FIX #1 TIMEOUT
            .build();
    }

    String getUserName(Long userId) {
        UserResponse response = restClient.get()
            .uri(uri -> uri.path("/users/{id}").build(userId)) // 🟡 FIX #4 НЕ СКЛЕИВАЕМ URL СТРОКОЙ
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw new UserNotFoundException(userId); }) // 🔴 FIX #3
            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> { throw new ExternalServiceException("user-service failed"); }) // 🔴 FIX #3
            .body(UserResponse.class); // 🔴 FIX #2

        if (response == null || response.name() == null) throw new ExternalServiceException("bad response");
        return response.name();
    }
}
```

---

<a id="review-4-8"></a>

## 4.8 Внешняя интеграция и @Async

```java
// 1. 🔴 SELF-INVOCATION THIS.EXPORT() — @ASYNC/@TRANSACTIONAL НЕ СРАБОТАЮТ
// 2. 🔴 @ASYNC VOID ТЕРЯЕТ ОШИБКИ — COMPLETABLEFUTURE
// 3. 🔴 BOUNDED EXECUTOR, НЕ БЕСКОНЕЧНЫЕ THREADS
// 4. 🔴 ВНЕШНИЙ HTTP НЕ ДЕРЖИМ В DB-ТРАНЗАКЦИИ

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean
    Executor exportExecutor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(4);
        e.setMaxPoolSize(16);
        e.setQueueCapacity(500);
        e.initialize();
        return e; // 🔴 FIX #3 ОГРАНИЧЕННЫЙ POOL
    }
}

@Service
class ExportFacade {
    private final ExportService exportService;

    List<CompletableFuture<Void>> exportAll(List<Long> ids) {
        return ids.stream()
            .map(exportService::export) // 🔴 FIX #1 ВЫЗОВ ЧЕРЕЗ ДРУГОЙ BEAN, НЕ THIS.EXPORT()
            .toList();
    }
}

@Service
class ExportService {
    @Async("exportExecutor")
    CompletableFuture<Void> export(Long orderId) { // 🔴 FIX #2 НЕ VOID
        OrderExportDto dto = tx(() -> loadDto(orderId)); // 🔴 FIX #4 КОРОТКО ЧИТАЕМ ИЗ БД
        externalApi.send(dto);                          // 🔴 FIX #4 HTTP ВНЕ DB-ТРАНЗАКЦИИ
        tx(() -> markExported(orderId));                // 🔴 FIX #4 КОРОТКО ФИКСИРУЕМ РЕЗУЛЬТАТ
        return completedFuture(null);
    }
}
```

---

<a id="review-4-9"></a>

## 4.9 Kafka listener

```java
// 1. 🔴 AT-LEAST-ONCE — НУЖНА ИДЕМПОТЕНТНОСТЬ / DEDUP
// 2. 🔴 POISON MESSAGE — RETRY + DLT, НЕ БЕСКОНЕЧНО БЛОКИРУЕМ PARTITION
// 3. 🔴 TYPED EVENT ВМЕСТО MAP<STRING,OBJECT>
// 4. 🟡 ENUM/HANDLER ВМЕСТО СТРОК И IF'ОВ
// 5. 🟡 OBJECTMAPPER/DESERIALIZER КАК BEAN, НЕ NEW В LISTENER

record OrderEvent(UUID eventId, Long orderId, OrderEventType type) {} // 🔴 FIX #3

enum OrderEventType { CREATED, PAID, CANCELLED } // 🟡 FIX #4

@KafkaListener(topics = "orders", groupId = "order-service")
@Transactional
void listen(OrderEvent event) { // 🔴 FIX #3 TYPED EVENT
    if (!processedEventRepository.tryMarkProcessed(event.eventId())) {
        return; // 🔴 FIX #1 ДУБЛЬ НЕ ОБРАБАТЫВАЕМ ПОВТОРНО
    }

    handlers.get(event.type()).handle(event); // 🟡 FIX #4 DISPATCH ПО ENUM, НЕ IF(TYPE.EQUALS("..."))
}

@Bean
DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
    var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
    return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000, 3)); // 🔴 FIX #2 RETRY + DLT
}
```

---

<a id="review-4-10"></a>

## 4.10 Интеграция с внешним API

```java
// 1. 🔴 ENTITY НЕ ОТПРАВЛЯЕМ НАРУЖУ — DTO
// 2. 🔴 TIMEOUT + ERROR HANDLING
// 3. 🔴 IDEMPOTENCY KEY ПРОТИВ ДУБЛЕЙ ВО ВНЕШНЕЙ СИСТЕМЕ
// 4. 🔴 OUTBOX — НЕ СМЕШИВАЕМ DB TRANSACTION И HTTP
// 5. 🟡 RETRY/DLQ/METRICS В WORKER

@Transactional
void scheduleExternalExport(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.canBeExported()) throw new InvalidOrderStateException(orderId, order.status);

    outboxRepository.save(OutboxEvent.externalOrderExport(orderId)); // 🔴 FIX #4 В DB-ТРАНЗАКЦИИ ТОЛЬКО OUTBOX, HTTP НЕ ДЕЛАЕМ
}

void handleOutbox(OutboxEvent event) {
    ExternalOrderDto dto = toDto(event.payload()); // 🔴 FIX #1 DTO, НЕ JPA ENTITY
    externalClient.send(dto, event.id().toString()); // 🔴 FIX #2/#3 TIMEOUT + IDEMPOTENCY KEY
    outboxRepository.markSent(event.id()); // 🟡 FIX #5 ЕСЛИ УПАЛО — RETRY/DLQ ПО OUTBOX
}
```

---

<a id="review-4-11"></a>

## 4.11 Потоконебезопасный singleton-бин

```java
// 1. 🔴 SPRING SINGLETON ВЫЗЫВАЕТСЯ ПАРАЛЛЕЛЬНО — HASHMAP НЕ ОК
// 2. 🔴 CONTAINSKEY + PUT НЕ АТОМАРНЫ
// 3. 🔴 SIMPLEDATEFORMAT НЕ THREAD-SAFE
// 4. 🟡 CACHE НУЖЕН TTL/MAX SIZE

@Service
class RateService {
    private final ConcurrentMap<String, BigDecimal> cache = new ConcurrentHashMap<>(); // 🔴 FIX #1
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // 🔴 FIX #3

    BigDecimal getRate(String currency) {
        return cache.computeIfAbsent(currency, this::loadRate); // 🔴 FIX #2 АТОМАРНО, БЕЗ CONTAINSKEY + PUT
    }

    String today() {
        return LocalDate.now().format(DATE_FORMAT); // 🔴 FIX #3 JAVA.TIME IMMUTABLE
    }

    BigDecimal loadRate(String currency) {
        return externalRateClient.load(currency); // 🟡 FIX #4 В PROD ЛУЧШЕ CAFFEINE/@CACHEABLE С TTL/MAX SIZE
    }
}
```

---

<a id="review-4-12"></a>

## 4.12 JPA N+1

```java
// 1. 🔴 FINDALL() БЕЗ ПАГИНАЦИИ — OOM
// 2. 🔴 LAZY CUSTOMER/ITEMS В ЦИКЛЕ — N+1
// 3. 🔴 ДЛЯ ОТЧЁТА ЛУЧШЕ DTO PROJECTION, НЕ ENTITY
// 4. 🟡 COUNT/SUM СЧИТАЕТ БД, НЕ JAVA В ЦИКЛЕ

record OrderReportRow(Long orderId, String customerName, long itemsCount) {}

interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        select new com.example.OrderReportRow(o.id, c.name, count(i))
        from Order o
        join o.customer c
        left join o.items i
        where o.createdAt >= :from and o.createdAt < :to
        group by o.id, c.name
        """)
    Page<OrderReportRow> buildReport(Instant from, Instant to, Pageable pageable); // 🔴 FIX #2/#3 + 🟡 FIX #4
}

@Transactional(readOnly = true)
Page<OrderReportRow> buildReport(Instant from, Instant to, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id")); // 🔴 FIX #1 PAGINATION + STABLE SORT
    return orderRepository.buildReport(from, to, pageable);
}
```

---

<a id="review-4-13"></a>

## 4.13 @Scheduled job

```java
// 1. 🔴 В КЛАСТЕРЕ @SCHEDULED ЗАПУСТИТСЯ НА КАЖДОМ ИНСТАНСЕ — НУЖЕН DISTRIBUTED LOCK
// 2. 🔴 НЕ ГРУЗИМ ВСЮ ТАБЛИЦУ — BATCH/LIMIT
// 3. 🟡 ОДНА ЗАПИСЬ = ОДНА КОРОТКАЯ ТРАНЗАКЦИЯ
// 4. 🟡 TIMEOUT/RETRY/METRICS ДЛЯ ВНЕШНЕГО CLIENT

@Scheduled(fixedDelayString = "${jobs.payment-reconcile.delay}")
void reconcileJob() {
    lockProvider.executeWithLock("payment-reconcile", Duration.ofMinutes(5), () -> { // 🔴 FIX #1
        List<Payment> stuck = paymentRepository.findStuckPending(
            Instant.now().minus(Duration.ofMinutes(10)),
            PageRequest.of(0, 100) // 🔴 FIX #2 BATCH, НЕ FINDALL()
        );

        stuck.forEach(this::reconcileOne); // 🟡 FIX #3 НЕ ОДНА ДЛИННАЯ ТРАНЗАКЦИЯ НА ВСЁ
    });
}

@Transactional
void reconcileOne(Payment payment) {
    GatewayStatus status = gatewayClient.getStatus(payment.externalId); // 🟡 FIX #4 CLIENT С TIMEOUT/RETRY
    if (status == SUCCESS) payment.markSuccess();
    if (status == FAILED) payment.markFailed();
}
```

---

<a id="review-4-14"></a>

## 4.14 Почему @Transactional не работает

```java
// 1. 🔴 SELF-INVOCATION THIS.CREATEONE() — ВЫЗОВ МИМО SPRING PROXY
// 2. 🔴 PRIVATE/FINAL METHOD НЕ ПОДХОДИТ ДЛЯ PROXY-TRANSACTION
// 3. 🔴 ВЫНОСИМ ТРАНЗАКЦИОННЫЙ МЕТОД В ОТДЕЛЬНЫЙ BEAN
// 4. 🟡 CHECKED EXCEPTION НЕ ДАЁТ ROLLBACK ПО УМОЛЧАНИЮ

@Service
class OrderImportService {
    private final OrderTxService orderTxService;

    void importOrders(List<CreateOrderRequest> requests) {
        for (CreateOrderRequest request : requests) {
            orderTxService.createOne(request); // 🔴 FIX #1/#3 ВЫЗОВ ЧЕРЕЗ ДРУГОЙ SPRING BEAN, НЕ THIS.CREATEONE()
        }
    }
}

@Service
class OrderTxService {
    @Transactional(rollbackFor = Exception.class) // 🔴 FIX #2 PUBLIC METHOD НА BEAN + 🟡 FIX #4 ЕСЛИ НУЖЕН ROLLBACK НА CHECKED
    public void createOne(CreateOrderRequest request) {
        orderRepository.save(Order.from(request));
    }
}
```
