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
    Map<Integer, Long> cnt = new HashMap<>();
    for (int x : arr) {
        cnt.merge(x, 1L, Long::sum);
    }

    long pairs = 0;
    for (var e : cnt.entrySet()) {
        int x = e.getKey();
        long c = e.getValue();

        if (x > 0) {
            pairs += c * cnt.getOrDefault(-x, 0L);
        } else if (x == 0) {
            pairs += c * (c - 1) / 2; // пары нулей: C(n, 2)
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
        result[k++] = a[i] <= b[j] ? a[i++] : b[j++];
    }
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
        int mid = (left + right) >>> 1; // безопаснее, чем (left + right) / 2
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
        .filter(x -> x == target)
        .count();
}
```

---

<a id="alg-1-8"></a>

## 1.8 Числа 1–100: делятся на 2, но не на 4

```java
static void printNumbers() {
    for (int i = 2; i <= 100; i += 4) {
        System.out.println(i); // 2, 6, 10, ..., 98
    }
}
```

---

<a id="alg-1-9"></a>

## 1.9 Наибольший общий элемент двух массивов

```java
static OptionalInt maxCommon(int[] a, int[] b) {
    Set<Integer> values = Arrays.stream(a)
        .boxed()
        .collect(Collectors.toSet());

    return Arrays.stream(b)
        .filter(values::contains)
        .max();
}
```

---

<a id="alg-1-10"></a>

## 1.10 Топ-5 значений с повторами

```java
static List<Integer> top5WithDuplicates(List<Integer> nums) {
    return nums.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
        .limit(5) // топ-5 разных значений
        .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
        .toList();
}
```

---

<a id="alg-1-11"></a>

## 1.11 Первый неповторяющийся символ

```java
static char firstUnique(String s) {
    Map<Character, Integer> count = new LinkedHashMap<>(); // сохраняет порядок

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

        sum += rating;
        count++;
    }

    public synchronized double getAverage() {
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
        .findByProductIdIn(required.keySet()) // один запрос, не N запросов в цикле
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
        .filter(order -> order.getTotal().compareTo(limit) < 0) // BigDecimal сравниваем через compareTo
        .toList();
}
```

---

<a id="spring-2-5"></a>

## 2.5 Средняя цена позиций выше target

```java
public List<Order> withAvgItemPriceAbove(List<Order> orders, BigDecimal target) {
    return orders.stream()
        .filter(order -> !order.getItems().isEmpty()) // иначе деление на 0
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
        RoundingMode.HALF_UP
    );
}
```

---

<a id="spring-2-6"></a>

## 2.6 Средняя стоимость UNPAID-заказов

```java
public BigDecimal avgUnpaidCost(Map<OrderStatus, List<Order>> ordersByStatus) {
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

    Pageable pageable = PageRequest.of(page, size, Sort.by("id")); // стабильный порядок страниц
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
    return orderRepository.findByStatus(status); // фильтр в БД, не findAll().stream()
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
            .created(URI.create("/orders/" + created.id())) // 201 + Location
            .body(created);
    }
}

public record CreateOrderRequest(
    @NotNull Long customerId,
    @NotEmpty List<@Valid OrderItemDto> items
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
        .collect(Collectors.teeing(
            Collectors.counting(),
            Collectors.reducing(
                BigDecimal.ZERO,
                Transaction::getAmount,
                BigDecimal::add
            ),
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
@Service
public class PaymentService {

    private final ApplicationEventPublisher events;

    public PaymentService(ApplicationEventPublisher events) {
        this.events = events; // FIX: нет зависимости PaymentService -> OrderService
    }

    public void onPaymentFailed(Long orderId) {
        events.publishEvent(new PaymentFailedEvent(orderId)); // FIX: связь через событие
    }
}

public record PaymentFailedEvent(Long orderId) {}

@Component
public class OrderPaymentListener {

    private final OrderService orderService;

    public OrderPaymentListener(OrderService orderService) {
        this.orderService = orderService; // FIX: OrderService вызывается только из listener
    }

    @EventListener
    public void handle(PaymentFailedEvent event) {
        orderService.cancelOrder(event.orderId());
    }
}
```

---

<a id="review-4-2"></a>

## 4.2 updateOrderStatus

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
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }
}

@Entity
public class Order {
    @Id
    private Long id;

    @Version
    private Long version; // FIX: защита от lost update при параллельных запросах

    @Enumerated(EnumType.STRING) // FIX: enum строкой, не ORDINAL и не magic string
    private OrderStatus status;

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
        this.orderRepository = orderRepository; // FIX: constructor injection вместо field injection
    }

    @Transactional // FIX: read-modify-write должен быть атомарным
    public OrderDto updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId)); // FIX: без Optional.get()

        if (!order.belongsTo(userId)) {
            throw new AccessDeniedException("order does not belong to user"); // FIX: проверка прав
        }

        order.changeStatus(newStatus); // FIX: переходы валидируются в домене

        return OrderDto.from(order); // save не обязателен: dirty checking в транзакции
    }
}
```

---

<a id="review-4-3"></a>

## 4.3 Подтверждение заказа с проверкой остатков

```java
@Service
public class OrderConfirmationService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    public OrderConfirmationService(OrderRepository orderRepository, StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional // FIX: списание остатков и смена статуса — одна атомарная операция
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.CONFIRMED);
        }

        Map<Long, Integer> required = order.getItems().stream()
            .collect(Collectors.groupingBy(
                OrderItem::getProductId,
                Collectors.summingInt(OrderItem::getQuantity)
            )); // FIX: одинаковый товар может быть в нескольких позициях

        List<Long> productIds = required.keySet().stream().sorted().toList(); // FIX: стабильный порядок lock'ов
        Map<Long, Stock> stocks = stockRepository.findByProductIdInForUpdate(productIds)
            .stream()
            .collect(Collectors.toMap(Stock::getProductId, Function.identity())); // FIX: один запрос + lock

        for (var e : required.entrySet()) {
            Stock stock = stocks.get(e.getKey());

            if (stock == null || stock.getQuantity() < e.getValue()) {
                throw new InsufficientStockException(e.getKey(), e.getValue()); // FIX: не подтверждаем частично
            }
        }

        required.forEach((productId, qty) -> stocks.get(productId).decrease(qty));
        order.setStatus(OrderStatus.CONFIRMED);
    }
}

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId in :productIds")
    List<Stock> findByProductIdInForUpdate(Collection<Long> productIds);
}
```

---

<a id="review-4-4"></a>

## 4.4 processPayment

```java
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final TransactionTemplate tx;

    public PaymentService(
        PaymentRepository paymentRepository,
        OrderRepository orderRepository,
        PaymentGateway paymentGateway,
        TransactionTemplate tx
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.tx = tx;
    }

    public PaymentDto processPayment(Long orderId, BigDecimal amount, String idempotencyKey) {
        Payment payment = tx.execute(status -> createOrGetPendingPayment(orderId, amount, idempotencyKey));

        if (payment.isFinalStatus()) {
            return PaymentDto.from(payment); // FIX: повторный запрос не списывает деньги второй раз
        }

        PaymentGatewayResponse response = paymentGateway.charge(
            payment.getCustomerId(),
            amount,
            idempotencyKey // FIX: ретрай charge безопасен только с idempotency key
        ); // FIX: внешний вызов не держит DB-транзакцию

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

                return paymentRepository.save(
                    Payment.pending(order.getId(), order.getCustomerId(), amount, idempotencyKey)
                ); // FIX: сначала сохраняем намерение платежа
            });
    }

    private PaymentDto finishPayment(Long paymentId, PaymentGatewayResponse response) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        Order order = orderRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(payment.getOrderId()));

        if (response.success()) {
            payment.markSuccess(response.externalId());
            order.setStatus(OrderStatus.PAID);
        } else {
            payment.markFailed(response.errorCode());
        }

        return PaymentDto.from(payment);
    }
}
```

---

<a id="review-4-5"></a>

## 4.5 Перевод денег

```java
@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository; // FIX: constructor injection
    }

    @Transactional // FIX: списание и зачисление должны коммититься вместе
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new InvalidAmountException(amount); // FIX: сумма должна быть положительной
        }
        if (Objects.equals(fromId, toId)) {
            throw new SameAccountTransferException(fromId);
        }

        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Account first = lockById(firstId);
        Account second = lockById(secondId); // FIX: стабильный порядок lock'ов против deadlock

        Account from = first.getId().equals(fromId) ? first : second;
        Account to = first.getId().equals(toId) ? first : second;

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromId, amount); // FIX: не молча return
        }

        from.setBalance(from.getBalance().subtract(amount)); // FIX: BigDecimal вместо double
        to.setBalance(to.getBalance().add(amount));
    }

    private Account lockById(Long id) {
        return accountRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
    }
}

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id);
}
```

---

<a id="review-4-6"></a>

## 4.6 Возврат статуса из БД

```java
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public OrderStatus getOrderStatus(Long orderId) {
        return orderRepository.findStatusById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId)); // FIX: не Optional.get() и не ""
    }
}

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o.status from Order o where o.id = :id")
    Optional<OrderStatus> findStatusById(Long id); // FIX: читаем только нужное поле
}
```

---

<a id="review-4-7"></a>

## 4.7 Получение имени пользователя через REST

```java
@ConfigurationProperties(prefix = "clients.user-service")
public record UserClientProperties(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout
) {}

public record UserResponse(Long id, String name) {}

@Service
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder, UserClientProperties props) {
        this.restClient = builder
            .baseUrl(props.baseUrl()) // FIX: URL из конфига, не hardcode
            .requestFactory(ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                    .withConnectTimeout(props.connectTimeout()) // FIX: connect timeout
                    .withReadTimeout(props.readTimeout())       // FIX: read timeout
            ))
            .build();
    }

    public String getUserName(Long userId) {
        UserResponse response = restClient.get()
            .uri(uri -> uri.path("/users/{id}").build(userId)) // FIX: path variable экранируется
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                (request, resp) -> { throw new UserNotFoundException(userId); })
            .onStatus(HttpStatusCode::is5xxServerError,
                (request, resp) -> { throw new ExternalServiceException("user-service failed"); })
            .body(UserResponse.class); // FIX: DTO вместо Map

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
        return executor; // FIX: ограниченный pool вместо бесконечных потоков
    }
}

@Service
public class ExportFacade {

    private final ExportService exportService;

    public ExportFacade(ExportService exportService) {
        this.exportService = exportService;
    }

    public List<CompletableFuture<Void>> exportAll(List<Long> ids) {
        return ids.stream()
            .map(exportService::export) // FIX: вызов через другой bean, не this.export()
            .toList();
    }
}

@Service
public class ExportService {

    private final ExternalApi externalApi;
    private final TransactionTemplate tx;

    public ExportService(ExternalApi externalApi, TransactionTemplate tx) {
        this.externalApi = externalApi;
        this.tx = tx;
    }

    @Async("exportExecutor")
    public CompletableFuture<Void> export(Long orderId) {
        OrderExportDto dto = tx.execute(status -> loadExportDto(orderId)); // FIX: БД-транзакция короткая

        externalApi.send(dto); // FIX: внешний вызов не держит DB connection

        tx.executeWithoutResult(status -> markExported(orderId)); // FIX: результат фиксируем отдельно

        return CompletableFuture.completedFuture(null); // FIX: exception попадёт в future, не потеряется как в void
    }
}
```

---

<a id="review-4-9"></a>

## 4.9 Kafka listener

```java
public record OrderEvent(
    UUID eventId,
    Long orderId,
    OrderEventType type
) {}

@Component
public class OrderEventListener {

    private final ProcessedEventRepository processedEventRepository;
    private final Map<OrderEventType, OrderEventHandler> handlers;

    public OrderEventListener(
        ProcessedEventRepository processedEventRepository,
        List<OrderEventHandler> handlers
    ) {
        this.processedEventRepository = processedEventRepository;
        this.handlers = handlers.stream()
            .collect(Collectors.toMap(OrderEventHandler::type, Function.identity()));
    }

    @KafkaListener(topics = "orders", groupId = "order-service")
    @Transactional // FIX: dedup и бизнес-изменение в одной транзакции
    public void listen(OrderEvent event) {
        if (!processedEventRepository.tryMarkProcessed(event.eventId())) {
            return; // FIX: at-least-once даёт дубли, повтор не обрабатываем
        }

        OrderEventHandler handler = handlers.get(event.type());
        if (handler == null) {
            throw new UnsupportedEventTypeException(event.type());
        }

        handler.handle(event); // FIX: типизированный handler вместо Map/cast/string if
    }
}

public interface ProcessedEventRepository {

    // Реализация: insert into processed_events(event_id) values (?) on conflict do nothing
    boolean tryMarkProcessed(UUID eventId);
}
```

---

<a id="review-4-10"></a>

## 4.10 Интеграция с внешним API

```java
@Service
public class ExternalOrderExportService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public ExternalOrderExportService(
        OrderRepository orderRepository,
        OutboxRepository outboxRepository
    ) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void scheduleExport(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId)); // FIX: без Optional.get()

        if (!order.canBeExported()) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        outboxRepository.save(OutboxEvent.externalOrderExport(orderId)); // FIX: БД и внешний API не смешиваем
    }
}

@Component
public class ExternalOrderWorker {

    private final ExternalOrderClient client;
    private final OutboxRepository outboxRepository;

    public void handle(OutboxEvent event) {
        client.sendOrder(event.payload(), event.id().toString()); // FIX: idempotency key для внешнего API
        outboxRepository.markSent(event.id());
    }
}
```

---

<a id="review-4-11"></a>

## 4.11 Потоконебезопасный singleton-бин

```java
@Service
public class RateService {

    private final ConcurrentMap<String, BigDecimal> cache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public BigDecimal getRate(String currency) {
        return cache.computeIfAbsent(currency, this::loadRate); // FIX: атомарно, без containsKey + put
    }

    public String today() {
        return LocalDate.now().format(DATE_FORMAT); // FIX: DateTimeFormatter immutable, SimpleDateFormat нет
    }

    private BigDecimal loadRate(String currency) {
        // В проде лучше Caffeine/@Cacheable с TTL, иначе курс может устареть.
        return BigDecimal.ONE;
    }
}
```

---

<a id="review-4-12"></a>

## 4.12 JPA N+1

```java
public record OrderReportRow(
    Long orderId,
    String customerName,
    long itemsCount
) {}

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select new com.example.OrderReportRow(
            o.id,
            c.name,
            count(i)
        )
        from Order o
        join o.customer c
        left join o.items i
        where o.createdAt >= :from and o.createdAt < :to
        group by o.id, c.name
        """)
    Page<OrderReportRow> buildReport(
        Instant from,
        Instant to,
        Pageable pageable
    ); // FIX: DTO projection + pagination, не findAll + lazy access
}

@Service
public class ReportService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<OrderReportRow> buildReport(Instant from, Instant to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return orderRepository.buildReport(from, to, pageable);
    }
}
```

---

<a id="review-4-13"></a>

## 4.13 @Scheduled job

```java
@Component
public class PaymentReconciliationJob {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;
    private final LockProvider lockProvider;

    public PaymentReconciliationJob(
        PaymentRepository paymentRepository,
        PaymentGatewayClient gatewayClient,
        LockProvider lockProvider
    ) {
        this.paymentRepository = paymentRepository;
        this.gatewayClient = gatewayClient;
        this.lockProvider = lockProvider;
    }

    @Scheduled(fixedDelayString = "${jobs.payment-reconciliation.delay}")
    public void reconcile() {
        lockProvider.executeWithLock("payment-reconciliation", Duration.ofMinutes(5), () -> {
            List<Payment> stuckPayments = paymentRepository.findStuckPendingPayments(
                Instant.now().minus(Duration.ofMinutes(10)),
                PageRequest.of(0, 100)
            ); // FIX: limit/batch вместо загрузки всей таблицы

            for (Payment payment : stuckPayments) {
                reconcileOne(payment);
            }
        }); // FIX: в кластере job должен выполняться одним инстансом
    }

    @Transactional
    void reconcileOne(Payment payment) {
        GatewayStatus status = gatewayClient.getStatus(payment.getExternalId());

        if (status == GatewayStatus.SUCCESS) {
            payment.markSuccess();
        } else if (status == GatewayStatus.FAILED) {
            payment.markFailed();
        }
    }
}
```

---

<a id="review-4-14"></a>

## 4.14 Почему @Transactional не работает

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTxService orderTxService;

    public OrderService(OrderRepository orderRepository, OrderTxService orderTxService) {
        this.orderRepository = orderRepository;
        this.orderTxService = orderTxService;
    }

    public void importOrders(List<CreateOrderRequest> requests) {
        for (CreateOrderRequest request : requests) {
            orderTxService.createOne(request); // FIX: вызов через другой Spring bean, не this.createOne()
        }
    }
}

@Service
public class OrderTxService {

    private final OrderRepository orderRepository;

    public OrderTxService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void createOne(CreateOrderRequest request) {
        orderRepository.save(Order.from(request));
    }
}
```

