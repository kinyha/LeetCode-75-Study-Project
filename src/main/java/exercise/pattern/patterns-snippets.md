# Паттерны проектирования: Код-сниппеты

## Быстрый поиск

| Паттерн | Ссылка |
|---------|--------|
| Singleton | [#singleton](#singleton) |
| Factory Method | [#factory-method](#factory-method) |
| Abstract Factory | [#abstract-factory](#abstract-factory) |
| Builder | [#builder](#builder) |
| Adapter | [#adapter](#adapter) |
| Decorator | [#decorator](#decorator) |
| Proxy | [#proxy](#proxy) |
| Facade | [#facade](#facade) |
| Composite | [#composite](#composite) |
| Strategy | [#strategy](#strategy) |
| Observer | [#observer](#observer) |
| State | [#state](#state) |
| Chain of Responsibility | [#chain-of-responsibility](#chain-of-responsibility) |
| Template Method | [#template-method](#template-method) |
| Command | [#command](#command) |
| Iterator | [#iterator](#iterator) |

---

# Порождающие паттерны

## Singleton

### Вариант 1: Eager (простой)
```java
// Инициализация при загрузке класса
// Используется: простые случаи, лёгкие объекты
public class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    private EagerSingleton() {}

    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}
```

### Вариант 2: Double-Checked Locking
```java
// Ленивая инициализация + thread-safety
// ВАЖНО: volatile обязателен из-за reordering!
public class DCLSingleton {
    private static volatile DCLSingleton instance;

    private DCLSingleton() {}

    public static DCLSingleton getInstance() {
        if (instance == null) {                     // 1-я проверка (без блокировки)
            synchronized (DCLSingleton.class) {
                if (instance == null) {             // 2-я проверка (с блокировкой)
                    instance = new DCLSingleton();
                }
            }
        }
        return instance;
    }
}
```

### Вариант 3: Holder (Bill Pugh) — РЕКОМЕНДУЕТСЯ
```java
// Ленивая + thread-safe без synchronized
// JVM гарантирует инициализацию при первом обращении к Holder
public class HolderSingleton {
    private HolderSingleton() {}

    private static class Holder {
        static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

### Вариант 4: Enum — ЛУЧШИЙ
```java
// Защита от Reflection и Serialization из коробки
// Joshua Bloch рекомендует (Effective Java)
public enum EnumSingleton {
    INSTANCE;

    private final Map<String, String> config = new HashMap<>();

    public void loadConfig(String path) {
        // загрузка конфигурации
    }

    public String get(String key) {
        return config.get(key);
    }
}

// Использование:
EnumSingleton.INSTANCE.get("app.name");
```

---

## Factory Method

### Базовый пример
```java
// Продукт
interface Notification {
    void send(String message);
}

class EmailNotification implements Notification {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

class SmsNotification implements Notification {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// Создатель с фабричным методом
abstract class NotificationService {

    // Factory Method — подклассы определяют какой объект создать
    protected abstract Notification createNotification();

    public void notify(String message) {
        Notification notification = createNotification();
        notification.send(message);
    }
}

class EmailNotificationService extends NotificationService {
    @Override
    protected Notification createNotification() {
        return new EmailNotification();
    }
}

class SmsNotificationService extends NotificationService {
    @Override
    protected Notification createNotification() {
        return new SmsNotification();
    }
}

// Использование:
NotificationService service = new EmailNotificationService();
service.notify("Hello!");
```

### Simple Factory (НЕ паттерн, но часто путают)
```java
// Статический метод — проще, но менее гибко
public class NotificationFactory {

    public static Notification create(String type) {
        return switch (type) {
            case "email" -> new EmailNotification();
            case "sms" -> new SmsNotification();
            case "push" -> new PushNotification();
            default -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }
}

// Использование:
Notification n = NotificationFactory.create("email");
```

---

## Abstract Factory

```java
// Семейство продуктов
interface Button { void render(); }
interface Checkbox { void render(); }

// Конкретные продукты — Windows
class WindowsButton implements Button {
    public void render() { System.out.println("[Windows Button]"); }
}
class WindowsCheckbox implements Checkbox {
    public void render() { System.out.println("[Windows Checkbox]"); }
}

// Конкретные продукты — Mac
class MacButton implements Button {
    public void render() { System.out.println("[Mac Button]"); }
}
class MacCheckbox implements Checkbox {
    public void render() { System.out.println("[Mac Checkbox]"); }
}

// Abstract Factory
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

class WindowsFactory implements GUIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

class MacFactory implements GUIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

// Клиент работает с абстракцией
class Application {
    private Button button;
    private Checkbox checkbox;

    public Application(GUIFactory factory) {
        button = factory.createButton();
        checkbox = factory.createCheckbox();
    }

    public void render() {
        button.render();
        checkbox.render();
    }
}

// Использование:
GUIFactory factory = System.getProperty("os.name").contains("Mac")
    ? new MacFactory()
    : new WindowsFactory();
Application app = new Application(factory);
app.render();
```

---

## Builder

### Классический Builder
```java
// Immutable объект с множеством параметров
public class HttpRequest {
    private final String method;      // обязательный
    private final String url;         // обязательный
    private final Map<String, String> headers;  // опциональный
    private final String body;        // опциональный
    private final Duration timeout;   // опциональный

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
        this.timeout = builder.timeout;
    }

    // Getters...

    public static Builder builder(String method, String url) {
        return new Builder(method, url);
    }

    public static class Builder {
        // Обязательные
        private final String method;
        private final String url;
        // Опциональные с дефолтами
        private Map<String, String> headers = new HashMap<>();
        private String body = null;
        private Duration timeout = Duration.ofSeconds(30);

        private Builder(String method, String url) {
            this.method = Objects.requireNonNull(method);
            this.url = Objects.requireNonNull(url);
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public HttpRequest build() {
            // Валидация
            if (body != null && (method.equals("GET") || method.equals("DELETE"))) {
                throw new IllegalStateException("GET/DELETE cannot have body");
            }
            return new HttpRequest(this);
        }
    }
}

// Использование:
HttpRequest request = HttpRequest.builder("POST", "https://api.example.com/users")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token123")
    .body("{\"name\": \"John\"}")
    .timeout(Duration.ofSeconds(10))
    .build();
```

### Lombok Builder
```java
// Компактный вариант с Lombok
@Builder
@Value  // immutable + getters
public class User {
    String firstName;
    String lastName;
    @Builder.Default int age = 0;
    @Builder.Default String email = "";
}

// Использование:
User user = User.builder()
    .firstName("John")
    .lastName("Doe")
    .age(25)
    .build();
```

---

# Структурные паттерны

## Adapter

### Object Adapter (композиция — рекомендуется)
```java
// Целевой интерфейс (то, что ожидает клиент)
interface MediaPlayer {
    void play(String filename);
}

// Адаптируемый класс (legacy или внешняя библиотека)
class VlcPlayer {
    void playVlc(String filename) {
        System.out.println("Playing VLC: " + filename);
    }
}

class Mp4Player {
    void playMp4(String filename) {
        System.out.println("Playing MP4: " + filename);
    }
}

// Адаптер
class MediaAdapter implements MediaPlayer {
    private final VlcPlayer vlcPlayer;
    private final Mp4Player mp4Player;

    public MediaAdapter() {
        this.vlcPlayer = new VlcPlayer();
        this.mp4Player = new Mp4Player();
    }

    @Override
    public void play(String filename) {
        if (filename.endsWith(".vlc")) {
            vlcPlayer.playVlc(filename);
        } else if (filename.endsWith(".mp4")) {
            mp4Player.playMp4(filename);
        }
    }
}

// Использование:
MediaPlayer player = new MediaAdapter();
player.play("movie.mp4");
```

### Пример из Java
```java
// Arrays.asList — адаптер массива к List
String[] array = {"a", "b", "c"};
List<String> list = Arrays.asList(array);  // view, не копия!

// InputStreamReader — адаптер InputStream к Reader
InputStream is = new FileInputStream("file.txt");
Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
```

---

## Decorator

```java
// Компонент
interface DataSource {
    void writeData(String data);
    String readData();
}

// Конкретный компонент
class FileDataSource implements DataSource {
    private final String filename;

    public FileDataSource(String filename) {
        this.filename = filename;
    }

    public void writeData(String data) {
        // запись в файл
    }

    public String readData() {
        // чтение из файла
        return "data";
    }
}

// Базовый декоратор
abstract class DataSourceDecorator implements DataSource {
    protected final DataSource wrappee;

    protected DataSourceDecorator(DataSource source) {
        this.wrappee = source;
    }

    public void writeData(String data) {
        wrappee.writeData(data);
    }

    public String readData() {
        return wrappee.readData();
    }
}

// Конкретные декораторы
class EncryptionDecorator extends DataSourceDecorator {
    public EncryptionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        String encrypted = encrypt(data);
        super.writeData(encrypted);
    }

    @Override
    public String readData() {
        return decrypt(super.readData());
    }

    private String encrypt(String data) { return "encrypted:" + data; }
    private String decrypt(String data) { return data.replace("encrypted:", ""); }
}

class CompressionDecorator extends DataSourceDecorator {
    public CompressionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        String compressed = compress(data);
        super.writeData(compressed);
    }

    @Override
    public String readData() {
        return decompress(super.readData());
    }

    private String compress(String data) { return "compressed:" + data; }
    private String decompress(String data) { return data.replace("compressed:", ""); }
}

// Использование — комбинирование декораторов:
DataSource source = new FileDataSource("file.txt");
source = new EncryptionDecorator(source);
source = new CompressionDecorator(source);
source.writeData("secret data");  // сжатие -> шифрование -> запись
```

### Пример из Java I/O
```java
// Цепочка декораторов
InputStream is = new BufferedInputStream(
    new GZIPInputStream(
        new FileInputStream("data.gz")
    )
);
```

---

## Proxy

### Virtual Proxy (ленивая загрузка)
```java
interface Image {
    void display();
}

// Реальный объект (тяжёлая загрузка)
class RealImage implements Image {
    private final String filename;

    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();  // долгая операция
    }

    private void loadFromDisk() {
        System.out.println("Loading " + filename);
    }

    public void display() {
        System.out.println("Displaying " + filename);
    }
}

// Proxy — откладывает загрузку
class ProxyImage implements Image {
    private final String filename;
    private RealImage realImage;  // ленивая инициализация

    public ProxyImage(String filename) {
        this.filename = filename;
    }

    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);  // загрузка по требованию
        }
        realImage.display();
    }
}

// Использование:
Image image = new ProxyImage("photo.jpg");  // нет загрузки
// ... позже
image.display();  // загрузка происходит здесь
image.display();  // уже загружено, повторной загрузки нет
```

### Caching Proxy
```java
interface UserRepository {
    User findById(String id);
}

class DatabaseUserRepository implements UserRepository {
    public User findById(String id) {
        // медленный запрос к БД
        return queryDatabase(id);
    }
}

class CachingUserRepository implements UserRepository {
    private final UserRepository delegate;
    private final Map<String, User> cache = new ConcurrentHashMap<>();

    public CachingUserRepository(UserRepository delegate) {
        this.delegate = delegate;
    }

    public User findById(String id) {
        return cache.computeIfAbsent(id, delegate::findById);
    }

    public void invalidate(String id) {
        cache.remove(id);
    }
}
```

---

## Facade

```java
// Сложная подсистема
class VideoFile { /* ... */ }
class CodecFactory {
    static Codec extract(VideoFile file) { return null; }
}
class Codec { /* ... */ }
class BitrateReader {
    static byte[] read(String filename, Codec codec) { return null; }
}
class AudioMixer {
    byte[] fix(byte[] data) { return data; }
}

// Фасад — упрощённый интерфейс
class VideoConverter {

    public byte[] convert(String filename, String format) {
        VideoFile file = new VideoFile(filename);
        Codec codec = CodecFactory.extract(file);
        byte[] data = BitrateReader.read(filename, codec);
        byte[] result = new AudioMixer().fix(data);
        return result;
    }
}

// Использование — клиент не знает о сложной подсистеме:
VideoConverter converter = new VideoConverter();
byte[] mp4 = converter.convert("movie.ogg", "mp4");
```

### Пример из Spring
```java
// JdbcTemplate — фасад к JDBC API
@Autowired
JdbcTemplate jdbc;

// Вместо Connection, Statement, ResultSet, close()...
List<User> users = jdbc.query(
    "SELECT * FROM users WHERE age > ?",
    (rs, rowNum) -> new User(rs.getString("name"), rs.getInt("age")),
    18
);
```

---

## Composite

```java
// Компонент — общий интерфейс для листьев и контейнеров
interface FileSystemComponent {
    String getName();
    long getSize();
    void print(String indent);
}

// Лист — файл (не содержит детей)
class File implements FileSystemComponent {
    private final String name;
    private final long size;

    public File(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() { return name; }
    public long getSize() { return size; }

    public void print(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " bytes)");
    }
}

// Composite — директория (содержит детей)
class Directory implements FileSystemComponent {
    private final String name;
    private final List<FileSystemComponent> children = new ArrayList<>();

    public Directory(String name) {
        this.name = name;
    }

    public void add(FileSystemComponent component) {
        children.add(component);
    }

    public void remove(FileSystemComponent component) {
        children.remove(component);
    }

    public String getName() { return name; }

    // Рекурсивный подсчёт размера
    public long getSize() {
        return children.stream()
            .mapToLong(FileSystemComponent::getSize)
            .sum();
    }

    public void print(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : children) {
            child.print(indent + "  ");
        }
    }
}

// Использование:
Directory root = new Directory("project");
root.add(new File("README.md", 1024));
root.add(new File("pom.xml", 2048));

Directory src = new Directory("src");
src.add(new File("Main.java", 4096));
src.add(new File("Utils.java", 2048));
root.add(src);

root.print("");
// 📁 project/
//   📄 README.md (1024 bytes)
//   📄 pom.xml (2048 bytes)
//   📁 src/
//     📄 CountryTbamk.java (4096 bytes)
//     📄 Utils.java (2048 bytes)

System.out.println("Total size: " + root.getSize());  // 9216 bytes
```

### Пример: Меню ресторана
```java
interface MenuComponent {
    default void add(MenuComponent c) { throw new UnsupportedOperationException(); }
    default void remove(MenuComponent c) { throw new UnsupportedOperationException(); }
    void print();
}

class MenuItem implements MenuComponent {
    private final String name;
    private final int price;

    public MenuItem(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public void print() {
        System.out.println("  " + name + " - " + price + "₽");
    }
}

class Menu implements MenuComponent {
    private final String name;
    private final List<MenuComponent> items = new ArrayList<>();

    public Menu(String name) { this.name = name; }

    public void add(MenuComponent c) { items.add(c); }
    public void remove(MenuComponent c) { items.remove(c); }

    public void print() {
        System.out.println("\n=== " + name + " ===");
        items.forEach(MenuComponent::print);
    }
}
```

---

# Поведенческие паттерны

## Strategy

```java
// Стратегия
interface PaymentStrategy {
    void pay(int amount);
}

class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void pay(int amount) {
        System.out.println("Paid " + amount + " via Credit Card: " + cardNumber);
    }
}

class PayPalPayment implements PaymentStrategy {
    private final String email;

    public PayPalPayment(String email) {
        this.email = email;
    }

    public void pay(int amount) {
        System.out.println("Paid " + amount + " via PayPal: " + email);
    }
}

// Контекст
class ShoppingCart {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public void checkout(int amount) {
        paymentStrategy.pay(amount);
    }
}

// Использование:
ShoppingCart cart = new ShoppingCart();
cart.setPaymentStrategy(new CreditCardPayment("1234-5678"));
cart.checkout(100);

cart.setPaymentStrategy(new PayPalPayment("user@mail.com"));
cart.checkout(200);
```

### Strategy через лямбды
```java
// Функциональный интерфейс — можно использовать лямбды
@FunctionalInterface
interface DiscountStrategy {
    double apply(double price);
}

class PriceCalculator {
    private DiscountStrategy discount = price -> price;  // default: no discount

    public void setDiscount(DiscountStrategy discount) {
        this.discount = discount;
    }

    public double calculate(double price) {
        return discount.apply(price);
    }
}

// Использование:
PriceCalculator calc = new PriceCalculator();
calc.setDiscount(price -> price * 0.9);  // 10% скидка
calc.setDiscount(price -> price - 50);   // фиксированная скидка
```

---

## Observer

```java
// Observer
interface EventListener {
    void update(String eventType, String data);
}

// Subject
class EventManager {
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(String eventType, EventListener listener) {
        listeners.getOrDefault(eventType, List.of()).remove(listener);
    }

    public void notify(String eventType, String data) {
        for (EventListener listener : listeners.getOrDefault(eventType, List.of())) {
            listener.update(eventType, data);
        }
    }
}

// Конкретный Subject
class Editor {
    private final EventManager events = new EventManager();
    private String file;

    public void subscribe(String eventType, EventListener listener) {
        events.subscribe(eventType, listener);
    }

    public void openFile(String path) {
        this.file = path;
        events.notify("open", file);
    }

    public void saveFile() {
        events.notify("save", file);
    }
}

// Конкретные Observer'ы
class LoggingListener implements EventListener {
    public void update(String eventType, String data) {
        System.out.println("Log: " + eventType + " - " + data);
    }
}

class EmailAlertsListener implements EventListener {
    private final String email;

    public EmailAlertsListener(String email) {
        this.email = email;
    }

    public void update(String eventType, String data) {
        System.out.println("Email to " + email + ": " + eventType + " - " + data);
    }
}

// Использование:
Editor editor = new Editor();
editor.subscribe("save", new LoggingListener());
editor.subscribe("save", new EmailAlertsListener("admin@example.com"));
editor.openFile("test.txt");
editor.saveFile();  // оба listener'а получат уведомление
```

### Spring Event
```java
// Event
record OrderCreatedEvent(String orderId, BigDecimal total) {}

// Publisher
@Service
class OrderService {
    @Autowired
    ApplicationEventPublisher publisher;

    public void createOrder(Order order) {
        // ... создание заказа
        publisher.publishEvent(new OrderCreatedEvent(order.getId(), order.getTotal()));
    }
}

// Listeners
@Component
class EmailNotifier {
    @EventListener
    void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("Sending email for order: " + event.orderId());
    }
}

@Component
class InventoryUpdater {
    @EventListener
    void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("Updating inventory for order: " + event.orderId());
    }
}
```

---

## State

```java
// State интерфейс
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void deliver(Order order);
    void cancel(Order order);
}

// Конкретные состояния
class NewState implements OrderState {
    public void pay(Order order) {
        System.out.println("Payment processed");
        order.setState(new PaidState());
    }
    public void ship(Order order) {
        throw new IllegalStateException("Cannot ship unpaid order");
    }
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver unpaid order");
    }
    public void cancel(Order order) {
        System.out.println("Order cancelled");
        order.setState(new CancelledState());
    }
}

class PaidState implements OrderState {
    public void pay(Order order) {
        throw new IllegalStateException("Already paid");
    }
    public void ship(Order order) {
        System.out.println("Order shipped");
        order.setState(new ShippedState());
    }
    public void deliver(Order order) {
        throw new IllegalStateException("Must ship first");
    }
    public void cancel(Order order) {
        System.out.println("Refund processed, order cancelled");
        order.setState(new CancelledState());
    }
}

class ShippedState implements OrderState {
    public void pay(Order order) {
        throw new IllegalStateException("Already paid");
    }
    public void ship(Order order) {
        throw new IllegalStateException("Already shipped");
    }
    public void deliver(Order order) {
        System.out.println("Order delivered");
        order.setState(new DeliveredState());
    }
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel shipped order");
    }
}

class DeliveredState implements OrderState {
    public void pay(Order order) { throw new IllegalStateException("Order completed"); }
    public void ship(Order order) { throw new IllegalStateException("Order completed"); }
    public void deliver(Order order) { throw new IllegalStateException("Order completed"); }
    public void cancel(Order order) { throw new IllegalStateException("Order completed"); }
}

class CancelledState implements OrderState {
    public void pay(Order order) { throw new IllegalStateException("Order cancelled"); }
    public void ship(Order order) { throw new IllegalStateException("Order cancelled"); }
    public void deliver(Order order) { throw new IllegalStateException("Order cancelled"); }
    public void cancel(Order order) { throw new IllegalStateException("Order cancelled"); }
}

// Context
class Order {
    private OrderState state = new NewState();

    void setState(OrderState state) {
        this.state = state;
    }

    public void pay() { state.pay(this); }
    public void ship() { state.ship(this); }
    public void deliver() { state.deliver(this); }
    public void cancel() { state.cancel(this); }
}

// Использование:
Order order = new Order();
order.pay();     // -> PaidState
order.ship();    // -> ShippedState
order.deliver(); // -> DeliveredState
```

---

## Chain of Responsibility

```java
// Handler
abstract class AuthHandler {
    protected AuthHandler next;

    public AuthHandler setNext(AuthHandler handler) {
        this.next = handler;
        return handler;  // для fluent API
    }

    public abstract boolean handle(Request request);

    protected boolean handleNext(Request request) {
        if (next == null) return true;  // конец цепочки
        return next.handle(request);
    }
}

// Конкретные обработчики
class RateLimitHandler extends AuthHandler {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    public boolean handle(Request request) {
        int count = requestCounts.merge(request.ip(), 1, Integer::sum);
        if (count > 100) {
            System.out.println("Rate limit exceeded for " + request.ip());
            return false;
        }
        return handleNext(request);
    }
}

class AuthenticationHandler extends AuthHandler {
    public boolean handle(Request request) {
        if (request.token() == null || !isValidToken(request.token())) {
            System.out.println("Authentication failed");
            return false;
        }
        return handleNext(request);
    }

    private boolean isValidToken(String token) {
        return token.startsWith("Bearer ");
    }
}

class AuthorizationHandler extends AuthHandler {
    public boolean handle(Request request) {
        if (!hasPermission(request.token(), request.resource())) {
            System.out.println("Access denied to " + request.resource());
            return false;
        }
        return handleNext(request);
    }

    private boolean hasPermission(String token, String resource) {
        return true;  // simplified
    }
}

record Request(String ip, String token, String resource) {}

// Использование:
AuthHandler chain = new RateLimitHandler();
chain.setNext(new AuthenticationHandler())
     .setNext(new AuthorizationHandler());

Request request = new Request("192.168.1.1", "Bearer token123", "/api/users");
boolean allowed = chain.handle(request);
```

### Servlet Filter — пример из Java EE
```java
public class LoggingFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Before request");
        chain.doFilter(req, res);  // передать дальше по цепочке
        System.out.println("After request");
    }
}
```

---

## Template Method

```java
// Абстрактный класс со скелетом алгоритма
abstract class DataMiner {

    // Template Method — определяет скелет алгоритма
    public final void mine(String path) {
        openFile(path);
        String rawData = extractData();
        String data = parseData(rawData);
        String analysis = analyzeData(data);
        sendReport(analysis);
        closeFile();
    }

    // Конкретные шаги — одинаковы для всех
    private void openFile(String path) {
        System.out.println("Opening: " + path);
    }

    private void closeFile() {
        System.out.println("Closing file");
    }

    private void sendReport(String analysis) {
        System.out.println("Sending report: " + analysis);
    }

    // Абстрактные шаги — подклассы реализуют
    protected abstract String extractData();
    protected abstract String parseData(String rawData);

    // Hook — можно переопределить, но необязательно
    protected String analyzeData(String data) {
        return "Analyzed: " + data;
    }
}

// Конкретные реализации
class PDFDataMiner extends DataMiner {
    @Override
    protected String extractData() {
        return "PDF raw data";
    }

    @Override
    protected String parseData(String rawData) {
        return "Parsed PDF: " + rawData;
    }
}

class CSVDataMiner extends DataMiner {
    @Override
    protected String extractData() {
        return "CSV raw data";
    }

    @Override
    protected String parseData(String rawData) {
        return "Parsed CSV: " + rawData;
    }

    @Override
    protected String analyzeData(String data) {
        return "CSV Analysis with charts: " + data;
    }
}

// Использование:
DataMiner miner = new PDFDataMiner();
miner.mine("report.pdf");
```

### Пример из Java
```java
// AbstractList — шаблон для коллекций
public abstract class AbstractList<E> {

    // Template Method
    public boolean addAll(int index, Collection<? extends E> c) {
        // ... общий код
        for (E e : c) {
            add(index++, e);  // вызывает абстрактный метод
        }
        // ... общий код
    }

    // Абстрактный метод — подклассы реализуют
    public abstract void add(int index, E element);
}
```

---

## Command

```java
// Command интерфейс
interface Command {
    void execute();
    void undo();
}

// Receiver — знает как выполнять операции
class TextEditor {
    private StringBuilder text = new StringBuilder();

    public void insertText(String str, int position) {
        text.insert(position, str);
    }

    public void deleteText(int start, int length) {
        text.delete(start, start + length);
    }

    public String getText() { return text.toString(); }
}

// Конкретные команды
class InsertCommand implements Command {
    private final TextEditor editor;
    private final String text;
    private final int position;

    public InsertCommand(TextEditor editor, String text, int position) {
        this.editor = editor;
        this.text = text;
        this.position = position;
    }

    public void execute() {
        editor.insertText(text, position);
    }

    public void undo() {
        editor.deleteText(position, text.length());
    }
}

// Invoker — хранит историю команд
class CommandHistory {
    private final Deque<Command> history = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd) {
        cmd.execute();
        history.push(cmd);
        redoStack.clear();  // после новой команды redo недоступен
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command cmd = history.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            history.push(cmd);
        }
    }
}

// Использование:
TextEditor editor = new TextEditor();
CommandHistory history = new CommandHistory();

history.execute(new InsertCommand(editor, "Hello", 0));
System.out.println(editor.getText());  // "Hello"

history.execute(new InsertCommand(editor, " World", 5));
System.out.println(editor.getText());  // "Hello World"

history.undo();
System.out.println(editor.getText());  // "Hello"

history.redo();
System.out.println(editor.getText());  // "Hello World"
```

### Макрос — композиция команд
```java
class MacroCommand implements Command {
    private final List<Command> commands = new ArrayList<>();

    public void add(Command cmd) { commands.add(cmd); }

    public void execute() {
        commands.forEach(Command::execute);
    }

    public void undo() {
        // Отменяем в обратном порядке
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
}
```

### Command через лямбды
```java
@FunctionalInterface
interface SimpleCommand {
    void execute();
}

// Использование:
SimpleCommand print = () -> System.out.println("Hello");
SimpleCommand save = () -> saveToFile();

List<SimpleCommand> commands = List.of(print, save);
commands.forEach(SimpleCommand::execute);
```

---

## Iterator

```java
// Кастомная коллекция с итератором
class NumberRange implements Iterable<Integer> {
    private final int start;
    private final int end;

    public NumberRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RangeIterator();
    }

    // Внутренний класс итератора
    private class RangeIterator implements Iterator<Integer> {
        private int current = start;

        @Override
        public boolean hasNext() {
            return current < end;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current++;
        }
    }
}

// Использование:
NumberRange range = new NumberRange(1, 5);

// Через for-each (использует iterator())
for (int n : range) {
    System.out.print(n + " ");  // 1 2 3 4
}

// Явно через Iterator
Iterator<Integer> it = range.iterator();
while (it.hasNext()) {
    System.out.print(it.next() + " ");
}
```

### ListIterator — двунаправленный
```java
List<String> list = new ArrayList<>(List.of("A", "B", "C"));
ListIterator<String> it = list.listIterator();

// Вперёд
while (it.hasNext()) {
    System.out.print(it.next());  // ABC
}

// Назад
while (it.hasPrevious()) {
    System.out.print(it.previous());  // CBA
}

// Модификация во время итерации
it = list.listIterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("B")) {
        it.set("X");      // заменить текущий
        it.add("Y");      // добавить после текущего
    }
}
// list = [A, X, Y, C]
```

### Fail-fast vs Fail-safe
```java
// Fail-fast (ArrayList, HashMap) — бросает ConcurrentModificationException
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    list.remove(s);  // ConcurrentModificationException!
}

// Правильно — через Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    it.next();
    it.remove();  // OK
}

// Fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) — работает на копии
List<String> safeList = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));
for (String s : safeList) {
    safeList.remove(s);  // OK, но может не увидеть все изменения
}
```

---

## Шпаргалка по созданию

```
Singleton:      private static instance + private constructor + getInstance()
Factory:        abstract createProduct() + возврат Product
Abstract Factory: createProductA() + createProductB() + семейства
Builder:        inner static class Builder + fluent API + build()
Adapter:        implements Target + содержит Adaptee
Decorator:      implements Component + содержит Component + super.method()
Proxy:          implements Subject + содержит RealSubject
Facade:         методы-упрощения + делегирование к подсистемам
Composite:      interface Component + Leaf + Composite(List<Component>)
Strategy:       interface Algorithm + setStrategy(Algorithm) + execute()
Observer:       subscribe() + unsubscribe() + notify() + List<Observer>
State:          interface State + setState(State) + делегирование state.method()
Chain:          setNext(Handler) + handle() + handleNext()
Template Method: final templateMethod() + abstract step() + hook()
Command:        interface Command(execute/undo) + Invoker + Receiver
Iterator:       Iterable<T> + Iterator<T>(hasNext/next) + внутренний класс
```
