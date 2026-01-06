# Паттерны проектирования: Задачи для практики

## Форматы задач

**Формат A** — Реализация с нуля (как на Яндекс собеседовании)
**Формат B** — Рефакторинг плохого кода

---

## Чеклист перед решением

```
НАЧАЛО:
□ Определи какой паттерн нужен
□ Нарисуй UML/схему на бумаге
□ Определи участников (интерфейсы, классы)

КОД:
□ Начни с интерфейсов
□ Потом конкретные реализации
□ Проверь edge cases

ЗАВЕРШЕНИЕ:
□ Объясни почему выбрал этот паттерн
□ Назови альтернативы
□ Расскажи плюсы/минусы выбора
```

---

## Сводная таблица

| # | Паттерн | Формат | Сложность |
|---|---------|--------|-----------|
| 1 | Singleton | A + B | Легко |
| 2 | Factory Method | A + B | Средне |
| 3 | Builder | A | Легко |
| 4 | Strategy | A | Легко |
| 5 | Observer | A | Средне |
| 6 | Decorator | B | Средне |
| 7 | Proxy | A | Средне |
| 8 | Chain of Responsibility | A | Средне |
| 9 | State | A | Сложно |
| 10 | Adapter + Facade | A | Сложно |
| 11 | Command | A | Средне |
| 12 | Composite | A | Средне |
| 13 | Iterator | A | Средне |

---

# Задача 1: Singleton — Конфигурация приложения

## Формат A: Реализация

**Условие:**
Реализуй класс `AppConfig` для хранения настроек приложения.

**Требования:**
1. Единственный экземпляр на приложение
2. Ленивая инициализация (загрузка конфига из файла — дорогая операция)
3. Thread-safe (приложение многопоточное)
4. Методы: `getString(key)`, `getInt(key)`, `reload()`
5. При reload() все потоки должны видеть новые значения

```java
public class AppConfig {
    // ???

    public static AppConfig getInstance() { /* ??? */ }

    public String getString(String key) { /* ??? */ }

    public int getInt(String key) { /* ??? */ }

    public void reload() { /* ??? */ }
}
```

**Edge cases:**
- [ ] Ключ не найден → что возвращать?
- [ ] Некорректный тип (запросили int, а там string)
- [ ] Параллельный reload() и getString()
- [ ] Reflection атака

**Пример использования:**
```java
AppConfig config = AppConfig.getInstance();
String dbUrl = config.getString("database.url");
int poolSize = config.getInt("database.pool.size");
```

---

## Формат B: Рефакторинг

**Плохой код:**
```java
public class Config {
    public static Config instance;
    public Map<String, String> settings = new HashMap<>();

    public Config() {
        loadFromFile();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();  // Race condition!
        }
        return instance;
    }

    private void loadFromFile() {
        // долгое чтение файла
        settings.put("app.name", "MyApp");
        settings.put("app.version", "1.0");
    }

    public String get(String key) {
        return settings.get(key);
    }
}
```

**Задание:**
1. Найди все проблемы (минимум 5)
2. Исправь с сохранением функциональности
3. Сделай thread-safe

**Чеклист проблем:**
- [ ] Race condition в getInstance()
- [ ] Публичное поле settings
- [ ] Нет volatile для instance
- [ ] Публичный конструктор
- [ ] Нет защиты от Reflection
- [ ] HashMap не thread-safe для concurrent read/write

---

# Задача 2: Factory Method — Генератор уведомлений

## Формат A: Реализация

**Контекст:**
Система уведомлений. Нужно отправлять уведомления разными каналами.

**Требования:**
1. Каналы: EMAIL, SMS, PUSH, TELEGRAM
2. Каждый канал имеет свою логику отправки
3. Легко добавлять новые каналы без изменения существующего кода
4. Клиентский код не должен знать о конкретных реализациях

```java
// Дано
enum NotificationChannel { EMAIL, SMS, PUSH, TELEGRAM }

record Notification(
    String recipient,
    String message,
    NotificationChannel channel
) {}

// Реализовать
interface NotificationSender {
    void send(Notification notification);
    NotificationChannel getChannel();
}

// Фабрика
interface NotificationSenderFactory {
    NotificationSender createSender(NotificationChannel channel);
}
```

**Пример использования:**
```java
Notification n = new Notification("user@mail.com", "Hello", EMAIL);
NotificationSender sender = factory.createSender(n.channel());
sender.send(n);
```

**Edge cases:**
- [ ] Неизвестный канал
- [ ] Невалидный recipient для канала (email без @, телефон без +)
- [ ] Ошибка отправки — как обработать?

---

## Формат B: Рефакторинг

**Плохой код:**
```java
public class NotificationService {

    public void send(String recipient, String message, String type) {
        if (type.equals("email")) {
            // 20 строк кода для email
            SmtpClient smtp = new SmtpClient("smtp.server.com");
            smtp.connect();
            smtp.setFrom("noreply@app.com");
            smtp.setTo(recipient);
            smtp.setSubject("Notification");
            smtp.setBody(message);
            smtp.send();
            smtp.disconnect();
        } else if (type.equals("sms")) {
            // 15 строк кода для SMS
            SmsGateway gateway = new SmsGateway("api-key-123");
            gateway.sendSms(recipient, message);
        } else if (type.equals("push")) {
            // 10 строк кода для Push
            PushService push = new PushService();
            push.sendPush(recipient, message);
        } else if (type.equals("telegram")) {
            // 12 строк для Telegram
            TelegramBot bot = new TelegramBot("bot-token");
            bot.sendMessage(recipient, message);
        }
        // TODO: скоро добавят Viber, WhatsApp, Slack...
    }
}
```

**Задание:**
1. Примени Factory Method паттерн
2. Сделай код расширяемым (Open/Closed Principle)
3. Убери дублирование и if-else цепочку

**Чеклист проблем:**
- [ ] Нарушение Open/Closed — для нового канала нужно менять метод
- [ ] Нарушение SRP — один метод делает всё
- [ ] Дублирование — каждый if содержит похожую логику
- [ ] Магические строки вместо enum
- [ ] Сложность тестирования

---

# Задача 3: Builder — HTTP Request

## Формат A: Реализация

**Условие:**
Реализуй builder для HTTP запроса.

**Требования:**
1. Метод (GET/POST/PUT/DELETE) — обязательный
2. URL — обязательный
3. Headers — опциональные (может быть несколько)
4. Query params — опциональные
5. Body — опциональный (только для POST/PUT)
6. Timeout — опциональный (по умолчанию 30 сек)
7. Объект immutable после создания

```java
// Сигнатура
public class HttpRequest {
    // fields...

    private HttpRequest(Builder builder) { /* ??? */ }

    // getters...

    public static Builder builder() { /* ??? */ }

    public static class Builder {
        // ???

        public Builder method(Method method) { /* ??? */ }
        public Builder url(String url) { /* ??? */ }
        public Builder header(String name, String value) { /* ??? */ }
        public Builder queryParam(String name, String value) { /* ??? */ }
        public Builder body(String body) { /* ??? */ }
        public Builder timeout(Duration timeout) { /* ??? */ }
        public HttpRequest build() { /* ??? */ }
    }

    public enum Method { GET, POST, PUT, DELETE }
}
```

**Пример использования:**
```java
HttpRequest request = HttpRequest.builder()
    .method(Method.POST)
    .url("https://api.example.com/users")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token123")
    .queryParam("version", "2")
    .body("{\"name\": \"John\"}")
    .timeout(Duration.ofSeconds(10))
    .build();
```

**Валидация в build():**
- [ ] URL не может быть пустым
- [ ] Method обязателен
- [ ] GET/DELETE не могут иметь body
- [ ] Timeout должен быть положительным

**Edge cases:**
- [ ] Дублирующиеся headers с одним ключом
- [ ] Null значения в headers/params
- [ ] URL с уже существующими query params

---

# Задача 4: Strategy — Расчёт стоимости доставки

## Формат A: Реализация

**Контекст:**
Интернет-магазин с разными способами доставки.

**Требования:**
1. Стратегии: STANDARD, EXPRESS, PICKUP, COURIER
2. STANDARD: 300₽ фиксированно
3. EXPRESS: 500₽ + 15₽/км
4. PICKUP: бесплатно
5. COURIER: 200₽ + 20₽/км, но минимум 400₽
6. Легко добавлять новые стратегии
7. Возможность менять стратегию в runtime

```java
// Дано
record Order(List<Product> items, Address address, int distanceKm) {}
record Product(String name, int price, int weight) {}
record Address(String city, String street) {}

// Реализовать
interface DeliveryStrategy {
    int calculateCost(Order order);
    String getName();
}

class DeliveryCalculator {
    // ???

    public void setStrategy(DeliveryStrategy strategy) { /* ??? */ }
    public int calculate(Order order) { /* ??? */ }
}
```

**Пример использования:**
```java
Order order = new Order(
    List.of(new Product("Phone", 10000, 200)),
    new Address("Moscow", "Main St"),
    15  // km
);

DeliveryCalculator calc = new DeliveryCalculator();

calc.setStrategy(new ExpressDelivery());
int expressCost = calc.calculate(order);  // 500 + 15*15 = 725₽

calc.setStrategy(new CourierDelivery());
int courierCost = calc.calculate(order);  // max(400, 200 + 20*15) = 500₽
```

**Edge cases:**
- [ ] Дистанция = 0
- [ ] Отрицательная дистанция
- [ ] Пустой заказ

**Бонус:** Добавь стратегию с учётом веса товаров.

---

# Задача 5: Observer — Система мониторинга

## Формат A: Реализация

**Контекст:**
Мониторинг сервера. При изменении метрик уведомлять подписчиков.

**Требования:**
1. Метрики: CPU, MEMORY, DISK, NETWORK
2. Подписчики: EmailAlerter, SlackNotifier, LogWriter, DashboardUpdater
3. Подписчик получает уведомление при изменении метрики
4. Можно подписаться на конкретные типы метрик
5. Можно подписаться/отписаться в runtime
6. Thread-safe

```java
// Дано
enum MetricType { CPU, MEMORY, DISK, NETWORK }

record MetricEvent(
    MetricType type,
    double value,
    double threshold,
    Instant timestamp
) {}

// Реализовать
interface MetricObserver {
    void onMetricChanged(MetricEvent event);
    Set<MetricType> getSubscribedMetrics();  // какие метрики интересуют
}

class MetricMonitor {
    void subscribe(MetricObserver observer) { /* ??? */ }
    void unsubscribe(MetricObserver observer) { /* ??? */ }
    void updateMetric(MetricType type, double value) { /* ??? */ }
}
```

**Пример использования:**
```java
MetricMonitor monitor = new MetricMonitor();

// Подписывается только на CPU и MEMORY
MetricObserver emailer = new EmailAlerter("admin@example.com",
    Set.of(MetricType.CPU, MetricType.MEMORY));
monitor.subscribe(emailer);

// Подписывается на все метрики
MetricObserver logger = new LogWriter(Set.of(MetricType.values()));
monitor.subscribe(logger);

monitor.updateMetric(MetricType.CPU, 95.5);  // emailer и logger получат
monitor.updateMetric(MetricType.DISK, 80.0); // только logger получит
```

**Edge cases:**
- [ ] Повторная подписка того же observer
- [ ] Отписка несуществующего observer
- [ ] Observer бросает исключение — не должно влиять на других
- [ ] Параллельные subscribe/unsubscribe/update

---

# Задача 6: Decorator — Обработка данных

## Формат B: Рефакторинг

**Плохой код:**
```java
public class DataProcessor {
    private boolean compress;
    private boolean encrypt;
    private boolean base64;
    private boolean addChecksum;
    private boolean log;

    public DataProcessor(boolean compress, boolean encrypt, boolean base64,
                         boolean addChecksum, boolean log) {
        this.compress = compress;
        this.encrypt = encrypt;
        this.base64 = base64;
        this.addChecksum = addChecksum;
        this.log = log;
    }

    public byte[] process(byte[] data) {
        byte[] result = data;

        if (log) {
            System.out.println("Processing " + data.length + " bytes");
        }

        if (compress) {
            result = compressData(result);
            if (log) System.out.println("After compress: " + result.length);
        }

        if (encrypt) {
            result = encryptData(result);
            if (log) System.out.println("After encrypt: " + result.length);
        }

        if (base64) {
            result = base64Encode(result);
            if (log) System.out.println("After base64: " + result.length);
        }

        if (addChecksum) {
            result = appendChecksum(result);
            if (log) System.out.println("After checksum: " + result.length);
        }

        return result;
    }

    // TODO: скоро добавят: validation, sanitization, caching, signing...
    // Комбинаций будет 2^N — взрыв конструкторов

    private byte[] compressData(byte[] data) { return data; /* stub */ }
    private byte[] encryptData(byte[] data) { return data; /* stub */ }
    private byte[] base64Encode(byte[] data) { return data; /* stub */ }
    private byte[] appendChecksum(byte[] data) { return data; /* stub */ }
}
```

**Задание:**
1. Примени Decorator паттерн
2. Сделай обработчики комбинируемыми
3. Порядок обработки должен быть гибким
4. Убери boolean флаги

**Ожидаемый результат:**
```java
DataProcessor processor = new LoggingDecorator(
    new CompressionDecorator(
        new EncryptionDecorator(
            new Base64Decorator(
                new BaseProcessor()
            )
        )
    )
);

byte[] result = processor.process(data);
```

**Чеклист проблем:**
- [ ] Экспоненциальный рост комбинаций (2^N)
- [ ] Нарушение Open/Closed — новая обработка = изменение класса
- [ ] Порядок обработки захардкожен
- [ ] Сложно тестировать отдельные шаги
- [ ] Код log дублируется

---

# Задача 7: Proxy — Кэширующий репозиторий

## Формат A: Реализация

**Условие:**
Репозиторий пользователей. Добавить кэширование без изменения оригинала.

```java
// Дано
record User(String id, String name, String email) {}

interface UserRepository {
    Optional<User> findById(String id);
    List<User> findAll();
    List<User> findByName(String name);
    void save(User user);
    void delete(String id);
}

class DatabaseUserRepository implements UserRepository {
    // Медленные запросы к БД
    public Optional<User> findById(String id) {
        simulateSlowQuery();
        return Optional.of(new User(id, "User" + id, id + "@mail.com"));
    }

    public List<User> findAll() {
        simulateSlowQuery();
        return List.of(/* ... */);
    }

    // ... остальные методы
}

// Реализовать
class CachingUserRepository implements UserRepository {
    // Кэширует результаты findById
    // findAll и findByName НЕ кэшируются (слишком большие результаты)
    // При save() и delete() инвалидирует кэш для этого id
}
```

**Требования:**
1. findById кэшируется по id
2. TTL кэша — 5 минут
3. При save(user) — удалить user.id из кэша
4. При delete(id) — удалить из кэша
5. Thread-safe
6. Максимум 1000 записей в кэше (LRU eviction)

**Пример использования:**
```java
UserRepository repo = new CachingUserRepository(
    new DatabaseUserRepository(),
    Duration.ofMinutes(5),
    1000  // max cache size
);

repo.findById("123");  // запрос к БД
repo.findById("123");  // из кэша (быстро)

repo.save(new User("123", "Updated", "new@mail.com"));
repo.findById("123");  // снова запрос к БД (кэш инвалидирован)
```

**Edge cases:**
- [ ] Null user в save()
- [ ] findById несуществующего — кэшировать ли Optional.empty()?
- [ ] Параллельные запросы одного id — только один должен идти в БД

---

# Задача 8: Chain of Responsibility — Валидация заказа

## Формат A: Реализация

**Контекст:**
Система заказов. Нужно валидировать заказ по цепочке проверок.

**Требования:**
1. Проверки: StockCheck, PaymentCheck, FraudCheck, AddressCheck, LimitCheck
2. Если одна проверка не прошла — остальные не выполняются
3. Порядок проверок настраиваемый
4. Легко добавлять новые проверки
5. Каждая проверка возвращает причину отказа

```java
// Дано
record Order(
    String orderId,
    String customerId,
    List<OrderItem> items,
    PaymentInfo payment,
    Address address
) {}

record OrderItem(String productId, int quantity, int price) {}
record PaymentInfo(String cardNumber, int amount) {}
record Address(String country, String city, String street) {}

record ValidationResult(boolean valid, String error) {
    static ValidationResult ok() { return new ValidationResult(true, null); }
    static ValidationResult fail(String error) { return new ValidationResult(false, error); }
}

// Реализовать
abstract class OrderValidator {
    protected OrderValidator next;

    public void setNext(OrderValidator next) { this.next = next; }

    public abstract ValidationResult validate(Order order);

    protected ValidationResult validateNext(Order order) {
        // ???
    }
}

// Конкретные валидаторы
class StockValidator extends OrderValidator {
    // Проверяет наличие товаров на складе
}

class PaymentValidator extends OrderValidator {
    // Проверяет валидность карты и достаточность средств
}

class FraudValidator extends OrderValidator {
    // Проверяет на мошенничество (сумма > 100000, страна в blacklist)
}

class AddressValidator extends OrderValidator {
    // Проверяет возможность доставки по адресу
}

class LimitValidator extends OrderValidator {
    // Проверяет лимит заказов в день для пользователя
}
```

**Пример использования:**
```java
OrderValidator chain = new StockValidator();
chain.setNext(new PaymentValidator())
     .setNext(new FraudValidator())
     .setNext(new AddressValidator());

Order order = new Order(/* ... */);
ValidationResult result = chain.validate(order);

if (!result.valid()) {
    System.out.println("Order rejected: " + result.error());
}
```

**Edge cases:**
- [ ] Пустая цепочка
- [ ] Пустой заказ
- [ ] Null в полях заказа

---

# Задача 9: State — Заказ в интернет-магазине

## Формат A: Реализация

**Контекст:**
Жизненный цикл заказа с различными состояниями и переходами.

**Диаграмма состояний:**
```
                    ┌──────────────────┐
                    │                  │
    ┌───────┐  pay  │  ┌──────┐  ship  │  ┌─────────┐  deliver  ┌───────────┐
    │  NEW  ├───────┼─►│ PAID ├────────┼─►│ SHIPPED ├──────────►│ DELIVERED │
    └───┬───┘       │  └──┬───┘        │  └─────────┘           └───────────┘
        │           │     │            │
        │ cancel    │     │ cancel     │
        ▼           │     ▼            │
    ┌───────────────┴─────────────┐    │
    │         CANCELLED           │    │
    └─────────────────────────────┘    │
                                       │
    ┌─────────────────────────────┐    │
    │         REFUNDED            │◄───┘ refund (только из DELIVERED)
    └─────────────────────────────┘
```

**Требования:**
1. Состояния: NEW, PAID, SHIPPED, DELIVERED, CANCELLED, REFUNDED
2. Переходы валидируются (нельзя ship() из NEW)
3. Поведение зависит от состояния
4. Недопустимый переход → IllegalStateException с понятным сообщением
5. Логирование переходов

```java
// Реализовать
interface OrderState {
    void pay(OrderContext order);
    void ship(OrderContext order);
    void deliver(OrderContext order);
    void cancel(OrderContext order);
    void refund(OrderContext order);
    String getName();
}

class OrderContext {
    private OrderState state;
    private String orderId;
    private List<String> history = new ArrayList<>();

    public void setState(OrderState state) { /* ??? */ }

    public void pay() { state.pay(this); }
    public void ship() { state.ship(this); }
    public void deliver() { state.deliver(this); }
    public void cancel() { state.cancel(this); }
    public void refund() { state.refund(this); }

    public String getStateName() { return state.getName(); }
    public List<String> getHistory() { return List.copyOf(history); }
}
```

**Пример использования:**
```java
OrderContext order = new OrderContext("ORD-123");

System.out.println(order.getStateName());  // NEW
order.pay();
System.out.println(order.getStateName());  // PAID
order.ship();
System.out.println(order.getStateName());  // SHIPPED

try {
    order.pay();  // IllegalStateException: Cannot pay in SHIPPED state
} catch (IllegalStateException e) {
    System.out.println(e.getMessage());
}

order.deliver();
System.out.println(order.getStateName());  // DELIVERED

order.refund();
System.out.println(order.getStateName());  // REFUNDED

System.out.println(order.getHistory());
// [NEW -> PAID, PAID -> SHIPPED, SHIPPED -> DELIVERED, DELIVERED -> REFUNDED]
```

**Edge cases:**
- [ ] Повторный вызов того же действия
- [ ] Все переходы из каждого состояния

---

# Задача 10: Adapter + Facade — Интеграция платежей

## Формат A: Реализация

**Контекст:**
Интеграция с разными платёжными системами. Каждая имеет свой API.

**Требования:**
1. Платёжные системы: Stripe, PayPal, YooKassa (разные API)
2. Единый интерфейс для всех систем (Adapter)
3. Упрощённый API для клиентского кода (Facade)
4. Автоматический выбор системы по типу карты/метода

```java
// Внешние API (имитация — уже даны)
class StripeApi {
    record StripeResult(boolean success, String chargeId, String error) {}

    StripeResult charge(String cardNumber, int amountCents, String currency) {
        // API Stripe работает с центами
        return new StripeResult(true, "ch_123", null);
    }

    void refund(String chargeId) { }
}

class PayPalApi {
    record PayPalRequest(String email, double amount, String currency) {}
    record PayPalResponse(String status, String transactionId, String message) {}

    PayPalResponse makePayment(PayPalRequest request) {
        // PayPal работает с email и дробными суммами
        return new PayPalResponse("COMPLETED", "tx_456", null);
    }
}

class YooKassaClient {
    record YooKassaPayment(String shopId, int amount, String description) {}
    enum PaymentStatus { PENDING, SUCCEEDED, CANCELED }

    String createPayment(YooKassaPayment payment) {
        // Возвращает paymentId, нужно проверять статус отдельно
        return "yoo_789";
    }

    PaymentStatus getStatus(String paymentId) {
        return PaymentStatus.SUCCEEDED;
    }
}

// Реализовать

// 1. Единый интерфейс (Target для Adapter)
record PaymentRequest(
    String customerId,
    int amount,          // в рублях
    String currency,
    PaymentMethod method,
    String credentials   // номер карты или email
) {}

record PaymentResult(
    boolean success,
    String transactionId,
    String error
) {
    static PaymentResult ok(String txId) { return new PaymentResult(true, txId, null); }
    static PaymentResult fail(String error) { return new PaymentResult(false, null, error); }
}

enum PaymentMethod { CARD_VISA, CARD_MASTERCARD, PAYPAL, YOOKASSA }

interface PaymentGateway {
    PaymentResult pay(PaymentRequest request);
    void refund(String transactionId);
    Set<PaymentMethod> getSupportedMethods();
}

// 2. Адаптеры для каждой системы
class StripeAdapter implements PaymentGateway { /* ??? */ }
class PayPalAdapter implements PaymentGateway { /* ??? */ }
class YooKassaAdapter implements PaymentGateway { /* ??? */ }

// 3. Фасад — упрощённый интерфейс
class PaymentService {
    // Автоматически выбирает нужный gateway по методу оплаты

    PaymentResult processPayment(PaymentRequest request) { /* ??? */ }

    // Упрощённые методы
    PaymentResult payByCard(String customerId, int amount, String cardNumber) { /* ??? */ }
    PaymentResult payByPayPal(String customerId, int amount, String email) { /* ??? */ }
}
```

**Пример использования:**
```java
PaymentService payments = new PaymentService();

// Через фасад — просто
PaymentResult result = payments.payByCard("cust_1", 1000, "4242424242424242");

// Или полный контроль
PaymentRequest request = new PaymentRequest(
    "cust_1",
    1000,
    "RUB",
    PaymentMethod.PAYPAL,
    "user@gmail.com"
);
PaymentResult result2 = payments.processPayment(request);
```

**Edge cases:**
- [ ] Неподдерживаемый метод оплаты
- [ ] Ошибка платёжной системы
- [ ] Конвертация валют (руб -> центы для Stripe)
- [ ] Timeout при ожидании статуса (YooKassa)

---

# Задача 11: Command — Текстовый редактор с Undo/Redo

## Формат A: Реализация

**Контекст:**
Простой текстовый редактор с поддержкой отмены и повтора операций.

**Требования:**
1. Операции: вставка текста, удаление текста, замена текста
2. Undo — отмена последней операции
3. Redo — повтор отменённой операции
4. История команд (можно отменить несколько операций подряд)
5. После новой операции Redo-стек очищается

```java
// Дано
interface Command {
    void execute();
    void undo();
}

// Реализовать
class TextEditor {
    private StringBuilder content = new StringBuilder();

    public void insert(int position, String text) { /* ??? */ }
    public void delete(int position, int length) { /* ??? */ }
    public void replace(int position, int length, String text) { /* ??? */ }

    public String getContent() { return content.toString(); }
}

class EditorController {
    private final TextEditor editor;
    // история команд

    public void executeCommand(Command cmd) { /* ??? */ }
    public void undo() { /* ??? */ }
    public void redo() { /* ??? */ }
}

// Команды
class InsertCommand implements Command { /* ??? */ }
class DeleteCommand implements Command { /* ??? */ }
class ReplaceCommand implements Command { /* ??? */ }
```

**Пример использования:**
```java
TextEditor editor = new TextEditor();
EditorController controller = new EditorController(editor);

controller.executeCommand(new InsertCommand(editor, 0, "Hello"));
// content: "Hello"

controller.executeCommand(new InsertCommand(editor, 5, " World"));
// content: "Hello World"

controller.undo();
// content: "Hello"

controller.undo();
// content: ""

controller.redo();
// content: "Hello"

controller.executeCommand(new InsertCommand(editor, 5, "!"));
// content: "Hello!"
// Redo-стек очищен, нельзя redo " World"
```

**Edge cases:**
- [ ] Undo при пустой истории
- [ ] Redo при пустом redo-стеке
- [ ] Позиция за пределами текста
- [ ] Удаление с длиной больше, чем осталось текста

**Бонус:** Добавь `MacroCommand` — выполняет несколько команд как одну (с возможностью undo всего макроса).

---

# Задача 12: Composite — Организационная структура

## Формат A: Реализация

**Контекст:**
Структура компании с отделами и сотрудниками. Нужно считать общую зарплату.

**Требования:**
1. Сотрудник (Leaf) — имеет имя, должность, зарплату
2. Отдел (Composite) — содержит сотрудников и подотделы
3. Метод `getSalary()` — для сотрудника возвращает его зарплату, для отдела — сумму зарплат всех
4. Метод `print()` — красиво выводит структуру с отступами
5. Метод `count()` — количество сотрудников (для отдела — рекурсивно)

```java
// Реализовать
interface OrganizationComponent {
    String getName();
    int getSalary();
    int count();
    void print(String indent);
}

class Employee implements OrganizationComponent {
    // имя, должность, зарплата
}

class Department implements OrganizationComponent {
    // название, список компонентов (сотрудники и подотделы)

    void add(OrganizationComponent component) { /* ??? */ }
    void remove(OrganizationComponent component) { /* ??? */ }
}
```

**Пример использования:**
```java
Department company = new Department("Company");

Department dev = new Department("Development");
dev.add(new Employee("Alice", "Senior Dev", 150000));
dev.add(new Employee("Bob", "Junior Dev", 80000));

Department qa = new Department("QA");
qa.add(new Employee("Charlie", "QA Lead", 120000));

company.add(dev);
company.add(qa);
company.add(new Employee("Eve", "CEO", 300000));

company.print("");
// Company
//   Development
//     Alice (Senior Dev) - 150000₽
//     Bob (Junior Dev) - 80000₽
//   QA
//     Charlie (QA Lead) - 120000₽
//   Eve (CEO) - 300000₽

System.out.println("Total salary: " + company.getSalary());  // 650000
System.out.println("Employee count: " + company.count());    // 4
```

**Edge cases:**
- [ ] Пустой отдел
- [ ] Отдел с только подотделами (без прямых сотрудников)
- [ ] Глубокая вложенность (отдел в отделе в отделе)

**Бонус:** Добавь метод `findByName(String name)` — поиск сотрудника по имени во всей структуре.

---

# Задача 13: Iterator — Коллекция с несколькими итераторами

## Формат A: Реализация

**Контекст:**
Playlist песен с разными способами обхода.

**Требования:**
1. Playlist содержит список песен
2. Обычный итератор — по порядку добавления
3. Shuffle итератор — в случайном порядке
4. Filter итератор — только песни определённого жанра
5. Все итераторы должны работать через стандартный `Iterator<Song>`

```java
// Дано
record Song(String title, String artist, String genre, int durationSec) {}

// Реализовать
class Playlist implements Iterable<Song> {
    private final List<Song> songs = new ArrayList<>();

    public void add(Song song) { songs.add(song); }
    public void remove(Song song) { songs.remove(song); }

    // Обычный итератор (по порядку)
    @Override
    public Iterator<Song> iterator() { /* ??? */ }

    // Shuffle итератор
    public Iterator<Song> shuffleIterator() { /* ??? */ }

    // Filter итератор
    public Iterator<Song> genreIterator(String genre) { /* ??? */ }

    // Iterable для filter (для for-each)
    public Iterable<Song> byGenre(String genre) { /* ??? */ }
}
```

**Пример использования:**
```java
Playlist playlist = new Playlist();
playlist.add(new Song("Song A", "Artist 1", "Rock", 180));
playlist.add(new Song("Song B", "Artist 2", "Pop", 200));
playlist.add(new Song("Song C", "Artist 1", "Rock", 240));
playlist.add(new Song("Song D", "Artist 3", "Jazz", 300));

// Обычный обход
for (Song song : playlist) {
    System.out.println(song.title());
}
// Song A, Song B, Song C, Song D

// Shuffle
Iterator<Song> shuffled = playlist.shuffleIterator();
while (shuffled.hasNext()) {
    System.out.println(shuffled.next().title());
}
// Random order

// По жанру
for (Song song : playlist.byGenre("Rock")) {
    System.out.println(song.title());
}
// Song A, Song C
```

**Edge cases:**
- [ ] Пустой плейлист
- [ ] Жанр, которого нет в плейлисте
- [ ] Shuffle одной песни
- [ ] Модификация плейлиста во время итерации

**Бонус:** Добавь `ReverseIterator` — обход с конца.

---

# Подсказки (раскрывай после попытки решения)

<details>
<summary>Задача 1: Singleton</summary>

**Формат A:**
- Используй Holder (Bill Pugh) или Enum
- Для reload() нужен volatile Map или ConcurrentHashMap
- getInt() можно через Integer.parseInt(getString())

**Формат B — проблемы:**
1. Race condition → DCL или Holder
2. Публичное поле → private + getter
3. Нет volatile → добавить volatile
4. Публичный конструктор → private
5. Reflection → проверка в конструкторе или Enum
6. HashMap → ConcurrentHashMap

</details>

<details>
<summary>Задача 2: Factory Method</summary>

**Формат A:**
```java
interface NotificationSenderFactory {
    default NotificationSender createSender(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> new EmailSender();
            case SMS -> new SmsSender();
            // ...
        };
    }
}
```

**Формат B:**
- Создай интерфейс NotificationSender
- Каждый канал — отдельный класс
- Фабрика возвращает нужную реализацию

</details>

<details>
<summary>Задача 4: Strategy</summary>

```java
interface DeliveryStrategy {
    int calculateCost(Order order);
}

class ExpressDelivery implements DeliveryStrategy {
    public int calculateCost(Order order) {
        return 500 + order.distanceKm() * 15;
    }
}

class CourierDelivery implements DeliveryStrategy {
    public int calculateCost(Order order) {
        return Math.max(400, 200 + order.distanceKm() * 20);
    }
}
```

</details>

<details>
<summary>Задача 6: Decorator</summary>

```java
interface DataProcessor {
    byte[] process(byte[] data);
}

class BaseProcessor implements DataProcessor {
    public byte[] process(byte[] data) { return data; }
}

abstract class ProcessorDecorator implements DataProcessor {
    protected final DataProcessor wrapped;
    ProcessorDecorator(DataProcessor wrapped) { this.wrapped = wrapped; }
}

class CompressionDecorator extends ProcessorDecorator {
    CompressionDecorator(DataProcessor wrapped) { super(wrapped); }

    public byte[] process(byte[] data) {
        byte[] compressed = compress(data);
        return wrapped.process(compressed);
    }
}
```

</details>

<details>
<summary>Задача 9: State</summary>

```java
class NewState implements OrderState {
    public void pay(OrderContext ctx) {
        // бизнес-логика оплаты
        ctx.setState(new PaidState());
    }

    public void ship(OrderContext ctx) {
        throw new IllegalStateException("Cannot ship unpaid order");
    }

    // ...
}
```

Совет: вынеси общие проверки в абстрактный базовый класс.

</details>

<details>
<summary>Задача 11: Command</summary>

```java
class InsertCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final String text;

    // Для undo нужно запомнить что вставили
    public void execute() {
        editor.insert(position, text);
    }

    public void undo() {
        editor.delete(position, text.length());
    }
}

class DeleteCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final int length;
    private String deletedText;  // запоминаем для undo

    public void execute() {
        deletedText = editor.getContent().substring(position, position + length);
        editor.delete(position, length);
    }

    public void undo() {
        editor.insert(position, deletedText);
    }
}
```

Для EditorController используй два стека: `Deque<Command> history` и `Deque<Command> redoStack`.

</details>

<details>
<summary>Задача 12: Composite</summary>

```java
class Employee implements OrganizationComponent {
    private final String name;
    private final String position;
    private final int salary;

    public int getSalary() { return salary; }
    public int count() { return 1; }

    public void print(String indent) {
        System.out.println(indent + name + " (" + position + ") - " + salary + "₽");
    }
}

class Department implements OrganizationComponent {
    private final String name;
    private final List<OrganizationComponent> children = new ArrayList<>();

    public int getSalary() {
        return children.stream()
            .mapToInt(OrganizationComponent::getSalary)
            .sum();
    }

    public int count() {
        return children.stream()
            .mapToInt(OrganizationComponent::count)
            .sum();
    }

    public void print(String indent) {
        System.out.println(indent + name);
        children.forEach(c -> c.print(indent + "  "));
    }
}
```

</details>

<details>
<summary>Задача 13: Iterator</summary>

```java
// Обычный итератор — делегируй к List.iterator()
public Iterator<Song> iterator() {
    return songs.iterator();
}

// Shuffle итератор
public Iterator<Song> shuffleIterator() {
    List<Song> shuffled = new ArrayList<>(songs);
    Collections.shuffle(shuffled);
    return shuffled.iterator();
}

// Filter итератор
public Iterator<Song> genreIterator(String genre) {
    return songs.stream()
        .filter(s -> s.genre().equals(genre))
        .iterator();
}

// Iterable для for-each
public Iterable<Song> byGenre(String genre) {
    return () -> genreIterator(genre);
}
```

Или реализуй свой класс итератора:
```java
private class GenreIterator implements Iterator<Song> {
    private final String genre;
    private int cursor = 0;

    public boolean hasNext() {
        while (cursor < songs.size()) {
            if (songs.get(cursor).genre().equals(genre)) return true;
            cursor++;
        }
        return false;
    }

    public Song next() {
        if (!hasNext()) throw new NoSuchElementException();
        return songs.get(cursor++);
    }
}
```

</details>
