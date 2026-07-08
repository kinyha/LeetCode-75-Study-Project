# Дополнительные разъяснения по вопросам, которые были непонятны

Этот файл для тем, где основного короткого ответа оказалось мало. Формат: что именно путалось, потом нормальное объяснение и короткая фраза для собеса.

---

## 1. `map` и `collect` в Stream API

### Что путалось

Чем отличается `map` от `collect`.

### Объяснение

`map` преобразует каждый элемент стрима.

```java
List<String> names = users.stream()
    .map(User::getName)
    .toList();
```

Было:

```text
User -> User -> User
```

Стало:

```text
String -> String -> String
```

`collect` собирает результат стрима в конкретную структуру: `List`, `Set`, `Map`, строку и т.д.

```java
List<String> names = users.stream()
    .map(User::getName)
    .collect(Collectors.toList());
```

Пример с `Map`:

```java
Map<Long, User> usersById = users.stream()
    .collect(Collectors.toMap(
        User::getId,
        Function.identity()
    ));
```

Если ключи могут повторяться, нужен merge:

```java
Map<Long, User> usersById = users.stream()
    .collect(Collectors.toMap(
        User::getId,
        Function.identity(),
        (oldValue, newValue) -> newValue
    ));
```

### Коротко

```text
map     = во что превратить каждый элемент
collect = куда собрать итог
```

---

## 2. Spring: циклическая зависимость сервисов

### Что путалось

Код выглядел логично: `placeOrder()` вызывает `paymentService.charge()`, а при ошибке платежа `PaymentService` вызывает `orderService.cancelOrder()`. Почему это плохо?

### Объяснение

Проблема не в самом вызове метода, а в зависимостях Spring-бинов:

```text
OrderService -> PaymentService
PaymentService -> OrderService
```

Spring создаёт и связывает бины до вызова бизнес-методов. Поэтому приложение может упасть уже на старте контекста.

Java-код может скомпилироваться, но Spring Boot обычно не поднимет приложение и покажет ошибку про cycle или `BeanCurrentlyInCreationException`.

Плохие обходы:

```java
@Lazy
```

или:

```properties
spring.main.allow-circular-references=true
```

Это не исправляет архитектуру, а только маскирует проблему.

### Как лучше

Вынести сценарий в отдельный сервис:

```java
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public void placeOrder(Order order) {
        orderService.create(order);

        try {
            paymentService.charge(order);
        } catch (PaymentException e) {
            orderService.cancelOrder(order.getId());
        }
    }
}
```

Теперь зависимости односторонние:

```text
OrderProcessingService -> OrderService
OrderProcessingService -> PaymentService
```

Альтернатива: `PaymentService` публикует событие `PaymentFailedEvent`, а отдельный listener отменяет заказ.

### Коротко

```text
Проблема не в вызове метода, а в том, что два Spring-бина зависят друг от друга по кругу.
```

---

## 3. `updateOrderStatus`: какую задачу могли дать

### Что путалось

На скрине была уже отрефакторенная попытка решения, а не исходная задача. Нужно было восстановить, какой плохой код могли дать на вход.

### Вероятный исходный код

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
            notificationService.notifyUser(order.getUserId(), "Your order is completed");
        } else if (newStatus.equals("CANCELLED")) {
            order.setStatus("CANCELLED");
            order.setReason("CANCELLED reason");
            orderRepository.save(order);
            notificationService.notifyUser(order.getUserId(), "Your order is cancelled");
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

### Что не так

```text
field injection
findById(...).get()
статусы строками
длинный if-else
дублирование save/notify
нет @Transactional
нет проверки userId
уведомление может уйти до успешного commit
нет валидации переходов статусов
```

### Коротко

Нормальный рефакторинг: `OrderStatus enum`, constructor injection, `orElseThrow`, `switch`, custom exceptions, `@Transactional`, уведомление через событие после commit или хотя бы аккуратно вынесенный метод.

---

## 4. Generics и PECS

### Что путалось

Фраза `Producer Extends, Consumer Super` звучит наоборот: если я читаю, почему это producer, а если пишу, почему consumer?

### Объяснение

Слова producer/consumer относятся к роли коллекции, а read/write - к твоему коду.

Если ты читаешь из коллекции:

```java
Number n = numbers.get(0);
```

то коллекция отдаёт тебе значение. Значит, она producer.

```java
List<? extends Number> numbers
```

Если ты пишешь в коллекцию:

```java
numbers.add(10);
```

то коллекция принимает значение. Значит, она consumer.

```java
List<? super Integer> numbers
```

### `extends` и `super` по иерархии

```text
? extends T = T и наследники T
? super T   = T и родители T
```

Пример:

```java
List<? extends Number>
```

Может быть:

```text
List<Number>
List<Integer>
List<Double>
List<BigDecimal>
```

Читать можно как `Number`:

```java
Number n = list.get(0);
```

Добавлять нельзя, потому что реальный список может быть `List<Double>`, а ты попробуешь положить `Integer`.

```java
list.add(1); // нельзя
```

Пример:

```java
List<? super Integer>
```

Может быть:

```text
List<Integer>
List<Number>
List<Object>
```

Добавлять `Integer` можно:

```java
list.add(1);
```

Читать безопасно можно только как `Object`:

```java
Object value = list.get(0);
```

Потому что если реальный список `List<Object>`, там могут лежать разные объекты.

### Про `Integer`

`Integer` - `final`, у него нет наследников. Поэтому:

```java
List<? extends Integer>
```

практически означает неизвестный список `Integer`, но добавлять туда всё равно нельзя.

А:

```java
List<? super Integer>
```

означает:

```text
Integer или родители: Number, Object
```

### Пример из JDK

```java
Collections.copy(List<? super T> dest, List<? extends T> src)
```

`src` - источник, из него читают:

```text
src -> extends
```

`dest` - назначение, туда пишут:

```text
dest -> super
```

### Коротко

```text
extends = коллекция отдаёт мне элементы = я читаю
super   = коллекция принимает мои элементы = я пишу
```

---

## 5. AutoCloseable

### Что путалось

Что вообще спрашивают в вопросе про `AutoCloseable` и при чём тут `try-with-resources`, файлы и БД.

### Объяснение

`AutoCloseable` - интерфейс для объектов, которые надо закрывать после использования.

```java
public interface AutoCloseable {
    void close() throws Exception;
}
```

Пример:

```java
try (FileInputStream in = new FileInputStream("a.txt")) {
    // читаем файл
}
```

После выхода из `try` Java сама вызовет:

```java
in.close();
```

Даже если внутри была ошибка.

Компилятор примерно разворачивает это в `try-finally`:

```java
FileInputStream in = new FileInputStream("a.txt");
try {
    // работа
} finally {
    in.close();
}
```

Если ошибка произошла и в основном коде, и в `close()`, основная ошибка остаётся главной, а ошибка из `close()` добавляется в suppressed:

```java
exception.getSuppressed();
```

### `Closeable`

`Closeable` - подтип `AutoCloseable` для IO:

```java
public interface Closeable extends AutoCloseable {
    void close() throws IOException;
}
```

Разница:

```text
AutoCloseable.close() throws Exception
Closeable.close()     throws IOException
```

### Файлы

Если не закрыть файл или writer, данные могут остаться в буфере и не попасть на диск. Ещё останется открытый файловый дескриптор.

Правильно:

```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter("out.txt"))) {
    writer.write("hello");
}
```

### БД

```java
try (Connection connection = dataSource.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql);
     ResultSet resultSet = statement.executeQuery()) {

    while (resultSet.next()) {
        // читаем данные
    }
}
```

Закрытие идёт в обратном порядке:

```text
ResultSet -> Statement -> Connection
```

Если `Connection` взят из пула, то `connection.close()` обычно не закрывает физическое соединение, а возвращает его в пул.

### Коротко

`AutoCloseable` нужен для автоматического освобождения ресурсов через `try-with-resources`. Файлы закрываем, чтобы сбросить буферы и освободить дескрипторы. БД-ресурсы закрываем, чтобы вернуть connection в пул и не держать statement/resultset/cursor.

---

## 6. ClassLoader и `ClassNotFoundException`

### Что путалось

Что такое ClassLoader, зачем иерархия загрузчиков, и чем `ClassNotFoundException` отличается от `NoClassDefFoundError`.

### Объяснение

`ClassLoader` загружает `.class`-файлы в JVM, когда класс понадобился.

Иерархия:

```text
Bootstrap ClassLoader
    ->
Platform ClassLoader
    ->
Application ClassLoader
```

Примерно:

```text
Bootstrap   -> String, Object, Integer
Platform    -> классы платформы Java
Application -> твой код и зависимости из classpath
```

### Parent delegation

Правило:

```text
сначала спросить родителя, потом искать самому
```

Если нужен `java.lang.String`, application classloader сначала спросит родителей, и настоящий `String` загрузится bootstrap-загрузчиком. Это защищает от подмены системных классов.

### Классы грузятся лениво

Класс не обязан грузиться при старте приложения. JVM загрузит его, когда он реально понадобится.

### Идентичность класса

Для JVM класс - это не только имя:

```text
com.example.User
```

а:

```text
имя класса + ClassLoader
```

Одинаковый класс, загруженный разными ClassLoader-ами, для JVM является разными классами.

Из-за этого возможна странная ошибка:

```text
ClassCastException: com.example.User cannot be cast to com.example.User
```

Смысл: `User` из ClassLoader A нельзя привести к `User` из ClassLoader B.

### `ClassNotFoundException`

Возникает, когда ты явно просишь загрузить класс по имени, а JVM не находит его в classpath.

```java
Class.forName("com.example.PaymentService");
```

или:

```java
classLoader.loadClass("com.example.PaymentService");
```

Это checked exception.

### `NoClassDefFoundError`

Класс был при компиляции, но во время запуска его нет или он не смог инициализироваться.

Пример:

```java
ObjectMapper mapper = new ObjectMapper();
```

Компиляция прошла, но jar с Jackson не попал в runtime classpath:

```text
NoClassDefFoundError
```

Ещё вариант - упал static initializer:

```java
class Config {
    static String value = loadConfig();

    static String loadConfig() {
        throw new RuntimeException("bad config");
    }
}
```

Тогда можно получить:

```text
NoClassDefFoundError: Could not initialize class Config
```

### Коротко

```text
ClassNotFoundException = явно попросил Class.forName/loadClass, класса нет
NoClassDefFoundError   = при компиляции был, в runtime нет или не инициализировался
```

Что проверять:

```text
classpath
зависимости
конфликты версий
fat-jar/Docker image
runtime dependencies
иерархии ClassLoader в app server
```

---

## 7. Рефлексия

### Что путалось

Что такое рефлексия, почему она медленнее и опаснее, и при чём тут `setAccessible`, JPMS и singleton.

### Объяснение

Обычный вызов:

```java
user.getName();
```

Рефлексия:

```java
Method method = User.class.getDeclaredMethod("getName");
Object result = method.invoke(user);
```

То есть метод берётся по имени в runtime и вызывается динамически.

Рефлексию используют Spring, Jackson, Hibernate: находят аннотации, создают объекты, читают и заполняют поля.

### Проблема 1: обход инкапсуляции

```java
Field field = User.class.getDeclaredField("password");
field.setAccessible(true);
field.set(user, "new value");
```

Так можно лезть в private-поля. Поэтому `private` - это не абсолютная защита от кода, который уже выполняется в твоём процессе.

### Проблема 2: ошибки уходят в runtime

Прямой код:

```java
user.getAge();
```

Если метода нет, не скомпилируется.

Рефлексия:

```java
User.class.getDeclaredMethod("getAge");
```

Если метода нет, ошибка будет только в runtime:

```text
NoSuchMethodException
```

### Проблема 3: производительность

`Method.invoke()` дороже прямого вызова:

```text
проверки доступа
универсальный вызов через Object
упаковка аргументов
хуже JIT-оптимизации
```

Смягчают так:

```text
кэшируют Method/Field
используют MethodHandle
не ставят рефлексию в горячий путь без причины
```

### JPMS

В модульной Java пакет должен быть открыт для deep reflection:

```java
module app {
    opens com.example.domain to spring.core, com.fasterxml.jackson.databind;
}
```

Разница:

```text
exports = другим можно использовать public API
opens   = другим можно лезть рефлексией
```

### Singleton

Обычный singleton с private constructor можно попытаться сломать рефлексией:

```java
Constructor<MySingleton> c = MySingleton.class.getDeclaredConstructor();
c.setAccessible(true);
MySingleton second = c.newInstance();
```

Надёжнее:

```java
public enum MySingleton {
    INSTANCE
}
```

Enum singleton обычной рефлексией не создаётся.

### Коротко

Рефлексия позволяет работать с классами, методами и полями в runtime. Минусы: можно обойти инкапсуляцию, ошибки проявляются в runtime, вызовы медленнее прямых, есть ограничения JPMS. Защита: не открывать лишние пакеты, не использовать `setAccessible(true)` без необходимости, кэшировать metadata, для singleton использовать enum.

---

## 8. Variable shadowing

### Что путалось

Что означает shadowing и почему в конструкторе пишут `this.name = name`.

### Объяснение

Shadowing - это когда переменная из ближней области видимости скрывает переменную с таким же именем снаружи.

Плохой пример:

```java
class User {
    private String name;

    public User(String name) {
        name = name;
    }
}
```

В конструкторе параметр:

```java
String name
```

скрывает поле:

```java
private String name;
```

Поэтому:

```java
name = name;
```

это параметр присваивается самому себе. Поле объекта не меняется.

Правильно:

```java
class User {
    private String name;

    public User(String name) {
        this.name = name;
    }
}
```

```text
this.name = поле объекта
name      = параметр
```

### Правило поиска имени

Java берёт ближайшую переменную:

```text
локальная переменная / параметр
поле класса
внешняя область
```

Пример:

```java
class Example {
    private int value = 10;

    void print(int value) {
        System.out.println(value);      // параметр
        System.out.println(this.value); // поле
    }
}
```

### Вложенные блоки и лямбды

Локальную переменную метода нельзя переобъявить во вложенном блоке:

```java
void method() {
    int count = 1;

    if (true) {
        int count = 2; // ошибка компиляции
    }
}
```

В лямбде тоже нельзя переобъявить локальную переменную метода:

```java
void method() {
    int value = 10;

    Runnable r = () -> {
        int value = 20; // ошибка компиляции
    };
}
```

Но параметр лямбды может скрывать поле класса:

```java
class Example {
    private String name = "field";

    void method() {
        Consumer<String> consumer = name -> {
            System.out.println(name);      // параметр лямбды
            System.out.println(this.name); // поле
        };
    }
}
```

### Коротко

`Variable shadowing` - это когда параметр или локальная переменная с тем же именем скрывает поле класса. В конструкторах и сеттерах поэтому пишут `this.field = field`.

---

## 9. Memory leaks в Java

### Что путалось

Почему в Java вообще бывают утечки памяти, если есть GC.

### Объяснение

GC удаляет не все "ненужные" объекты, а только те, до которых нельзя добраться от GC roots.

GC roots - это точки, откуда JVM начинает искать живые объекты:

```text
static поля
локальные переменные активных потоков
сами Thread-объекты
JNI/native ссылки
```

Если объект достижим от GC roots, GC считает его живым. Даже если бизнес-логике он уже не нужен.

Утечка памяти в Java:

```text
объект уже не нужен, но ссылка на него всё ещё где-то хранится
```

Пример:

```java
class UserCache {
    private static final List<User> users = new ArrayList<>();

    void add(User user) {
        users.add(user);
    }
}
```

Если постоянно добавлять пользователей и никогда не удалять, список будет расти. Для GC все `User` живые, потому что на них ссылается static-коллекция.

### Частые источники

```text
static коллекции
кэши без ограничения размера или TTL
listeners/callbacks без unsubscribe
ThreadLocal в пулах потоков без remove()
незакрытые ресурсы
inner class держит ссылку на outer class
ClassLoader leaks при redeploy
```

### `ThreadLocal`

```java
private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

void handleRequest() {
    context.set(loadContext());

    try {
        // обработка запроса
    } finally {
        context.remove();
    }
}
```

`remove()` важен, потому что в пуле поток не умирает после запроса. Если не очистить `ThreadLocal`, значение может жить вместе с потоком очень долго.

### Кэши

Плохой кэш:

```java
private final Map<Long, User> cache = new HashMap<>();
```

Если он только растёт, это потенциальная утечка.

Нормальный кэш должен иметь:

```text
max size
TTL
eviction
```

Например через Caffeine.

### Ресурсы

Незакрытый ресурс - это не всегда heap leak, но всё равно утечка ресурса.

```java
try (Connection connection = dataSource.getConnection()) {
    // работа с БД
}
```

Если не закрыть connection из пула, он может не вернуться в пул.

### Как искать

```text
GC logs
рост old gen
heap dump
Eclipse MAT
dominator tree
JFR
```

### Коротко

В Java утечка памяти возникает, когда объект уже не нужен, но остаётся достижимым от GC roots, поэтому GC не может его собрать. Частые причины: static коллекции, кэши без eviction, listeners без отписки, `ThreadLocal` в пулах потоков без `remove`, незакрытые ресурсы. Избежать можно ограниченными кэшами, `try-with-resources`, отпиской listeners и `threadLocal.remove()` в `finally`.

---

## 10. Красно-чёрное дерево

### Что путалось

Что это вообще за дерево и почему оно "красно-чёрное".

### Объяснение

Красно-чёрное дерево - это не про настоящий цвет. Цвет - это служебная метка у узла:

```text
узел может быть red или black
```

Эти метки нужны, чтобы дерево оставалось примерно сбалансированным.

Обычное бинарное дерево поиска устроено так:

```text
слева  - значения меньше
справа - значения больше
```

Пример:

```text
        10
       /  \
      5    20
```

Искать в таком дереве удобно: если ищем `20`, идём вправо; если `5`, идём влево.

Но обычное дерево может превратиться в список, если добавлять элементы в плохом порядке:

```text
1
 \
  2
   \
    3
     \
      4
```

Тогда поиск становится `O(n)`, почти как в обычном списке.

Красно-чёрное дерево не даёт дереву так перекоситься. После вставки или удаления оно может:

```text
перекрасить узлы
сделать поворот
```

Поворот - это локальная перестановка узлов, чтобы дерево стало ровнее.

### Зачем цвета

Цвета нужны как простые правила баланса. Главные идеи:

```text
красный узел не должен иметь красного ребёнка
на всех путях вниз должно быть одинаковое число чёрных узлов
```

Фраза "на всех путях вниз" означает: если из одного и того же узла идти к любому концу дерева, количество чёрных узлов должно совпадать.

Например:

```text
    5(B)
   /    \
 1(R)  10(R)
 / \    /  \
N(B) N(B) N(B) N(B)
```

`N(B)` - это пустой leaf/null-узел, в правилах красно-чёрного дерева он считается чёрным.

Смотрим пути от `5` вниз:

```text
5 -> 1  -> N
5 -> 10 -> N
```

На каждом таком пути одинаковое число чёрных узлов. Это правило не даёт одной стороне дерева стать сильно длиннее другой.

Из-за этих правил дерево не становится слишком высоким.

Гарантия:

```text
поиск     O(log n)
вставка   O(log n)
удаление  O(log n)
```

### Где используется

В Java красно-чёрное дерево используется:

```text
TreeMap
TreeSet
бакеты HashMap при большом числе коллизий
```

`TreeMap` хранит ключи отсортированными. Поэтому ему нужна структура, где можно быстро искать и при этом поддерживать порядок.

`HashMap` обычно использует массив бакетов и списки внутри бакетов. Но если в одном бакете стало слишком много элементов из-за коллизий, Java 8+ может заменить список на красно-чёрное дерево, чтобы поиск был не `O(n)`, а `O(log n)`.

### Наглядная жизнь дерева

Допустим, добавляем числа в обычное дерево поиска:

```text
10, потом 5, потом 1
```

Обычное дерево стало бы перекошенным:

```text
    10
   /
  5
 /
1
```

Это уже похоже на список. Красно-чёрное дерево после вставки видит проблему: красный узел оказался под красным узлом или дерево стало слишком неровным. Тогда оно делает поворот и перекраску.

Упрощённо результат может стать таким:

```text
    5(B)
   /    \
 1(R)  10(R)
```

`B` - black, `R` - red. Смысл не в цветах как данных, а в том, что дерево стало ниже и ровнее.

Ещё пример с ростом:

```text
put(10)

10(B)
```

```text
put(5)

   10(B)
   /
 5(R)
```

```text
put(1)

до балансировки:

    10(B)
    /
  5(R)
  /
1(R)

после поворота и перекраски:

    5(B)
   /    \
 1(R)  10(R)
```

Дальше дерево живёт так постоянно:

```text
добавили элемент
нашли место как в обычном BST
проверили правила цветов
если правила нарушены - перекрасили узлы и/или сделали поворот
```

Для пользователя `TreeMap` это скрыто:

```java
Map<Integer, String> map = new TreeMap<>();
map.put(10, "ten");
map.put(5, "five");
map.put(1, "one");
```

Снаружи ты просто получаешь отсортированные ключи:

```text
1, 5, 10
```

А внутри `TreeMap` поддерживает красно-чёрное дерево, чтобы операции оставались быстрыми.

### Коротко

Красно-чёрное дерево - это самобалансирующееся бинарное дерево поиска. "Красный" и "чёрный" - это метки узлов, которые помогают поддерживать баланс. Благодаря этому операции поиска, вставки и удаления работают за `O(log n)`. В Java оно используется в `TreeMap`, `TreeSet` и внутри `HashMap` при большом количестве коллизий.

---

## 11. Visitor

### Что путалось

Что такое "посетитель", зачем он нужен и что значит `element.accept(visitor) -> visitor.visit(this)`.

### Объяснение

Visitor нужен, когда есть иерархия объектов, и над ней надо делать разные операции.

Например, есть фигуры:

```java
interface Shape {
}

class Circle implements Shape {
    double radius;
}

class Rectangle implements Shape {
    double width;
    double height;
}
```

И нам нужны разные операции:

```text
посчитать площадь
экспортировать в JSON
нарисовать
проверить валидность
```

Можно засунуть всё внутрь классов:

```java
class Circle {
    double area() { ... }
    String toJson() { ... }
    void draw() { ... }
}
```

Но тогда классы фигур разрастаются. Visitor предлагает другое: сами объекты остаются объектами, а операции выносятся в отдельные visitor-классы.

### Как выглядит

```java
interface Shape {
    void accept(ShapeVisitor visitor);
}

class Circle implements Shape {
    double radius;

    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}

class Rectangle implements Shape {
    double width;
    double height;

    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}
```

Visitor:

```java
interface ShapeVisitor {
    void visit(Circle circle);
    void visit(Rectangle rectangle);
}
```

Операция "посчитать площадь":

```java
class AreaVisitor implements ShapeVisitor {
    public void visit(Circle circle) {
        System.out.println(Math.PI * circle.radius * circle.radius);
    }

    public void visit(Rectangle rectangle) {
        System.out.println(rectangle.width * rectangle.height);
    }
}
```

Использование:

```java
Shape shape = new Circle();
shape.accept(new AreaVisitor());
```

### Что тут происходит

Строка:

```java
shape.accept(visitor);
```

говорит объекту:

```text
прими посетителя
```

А внутри конкретного класса вызывается:

```java
visitor.visit(this);
```

Если объект реально `Circle`, вызовется:

```java
visit(Circle circle)
```

Если объект реально `Rectangle`, вызовется:

```java
visit(Rectangle rectangle)
```

Это и называют double dispatch:

```text
сначала выбирается реальный тип элемента через accept
потом выбирается нужный visit(...) по этому типу
```

### Плюсы и минусы

Плюс:

```text
легко добавить новую операцию
```

Например, хочешь экспорт в JSON - создаёшь `JsonExportVisitor`, сами `Circle` и `Rectangle` почти не трогаешь.

Минус:

```text
тяжело добавить новый тип элемента
```

Если добавишь `Triangle`, придётся добавить:

```java
void visit(Triangle triangle);
```

и реализовать этот метод во всех visitor-классах.

### Где применяется

```text
AST в компиляторах
обход дерева выражений
экспорт документов в разные форматы
операции над сложной иерархией объектов
```

### Современная альтернатива

В новых Java часто можно проще:

```java
sealed interface Shape permits Circle, Rectangle {}

double area(Shape shape) {
    return switch (shape) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
    };
}
```

То есть `sealed` + pattern matching `switch` иногда заменяют Visitor, потому что компилятор знает все варианты.

### Коротко

Visitor выносит операции над иерархией объектов в отдельные классы. Объект принимает visitor через `accept`, а потом вызывает `visitor.visit(this)`, чтобы сработал метод для конкретного типа. Удобно добавлять новые операции, но неудобно добавлять новые типы объектов.

---

## 12. Spring `@Transactional`

### Что путалось

Как настроить транзакционность в Spring и что значат параметры `@Transactional`.

### Объяснение

`@Transactional` говорит Spring:

```text
перед методом открыть транзакцию
если всё нормально - commit
если ошибка - rollback
```

Пример:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow();

        order.setStatus(OrderStatus.PAID);
    }
}
```

В JPA внутри транзакции можно не вызывать `save`, если entity уже managed. Hibernate увидит изменение и сохранит при commit через dirty checking.

### Как это работает в Spring

Обычно через proxy:

```text
твой код вызывает proxy
proxy открывает транзакцию
proxy вызывает реальный метод
proxy делает commit или rollback
```

Поэтому важная ловушка:

```java
public void outer() {
    inner(); // this.inner(), proxy не участвует
}

@Transactional
public void inner() {
}
```

Это self-invocation. Вызов внутри того же класса идёт мимо proxy, и транзакция может не открыться.

### Основные параметры

#### `propagation`

Отвечает на вопрос:

```text
что делать, если транзакция уже есть?
```

Самые важные варианты:

```java
@Transactional(propagation = Propagation.REQUIRED)
```

`REQUIRED` - дефолт. Если транзакция уже есть, метод присоединится к ней. Если нет - создаст новую.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

`REQUIRES_NEW` - всегда создаёт новую транзакцию, а старую временно приостанавливает.

Пример:

```java
@Transactional
public void createOrder() {
    orderRepository.save(order);
    auditService.writeAudit(); // REQUIRES_NEW
    throw new RuntimeException();
}
```

Если `writeAudit()` в `REQUIRES_NEW`, аудит может сохраниться, даже если основной заказ откатился.

Но `REQUIRES_NEW` надо использовать аккуратно: можно получить лишние соединения, блокировки и странное поведение.

#### `isolation`

Отвечает на вопрос:

```text
насколько транзакция изолирована от параллельных транзакций?
```

Обычно оставляют дефолт БД:

```java
@Transactional(isolation = Isolation.DEFAULT)
```

Частые уровни:

```text
READ_COMMITTED     - не читаем незакоммиченные данные
REPEATABLE_READ    - повторное чтение той же строки даёт тот же результат
SERIALIZABLE       - самый строгий, но самый дорогой
```

Чем строже isolation, тем меньше аномалий, но выше риск блокировок и хуже производительность.

#### `readOnly`

Подсказка, что метод только читает данные:

```java
@Transactional(readOnly = true)
public OrderDto getOrder(Long id) {
    return orderRepository.findById(id)
        .map(OrderDto::from)
        .orElseThrow();
}
```

Это может помочь Hibernate и БД оптимизировать работу. Но это не железная защита от записи во всех случаях. Лучше воспринимать как настройку и сигнал намерения.

#### `rollbackFor`

По умолчанию Spring откатывает транзакцию на:

```text
RuntimeException
Error
```

Но checked exception не откатывает по умолчанию.

```java
@Transactional(rollbackFor = Exception.class)
public void importOrders() throws IOException {
    // если IOException - тоже rollback
}
```

Можно точечно:

```java
@Transactional(rollbackFor = IOException.class)
```

#### `noRollbackFor`

Говорит, что на конкретную ошибку rollback не нужен:

```java
@Transactional(noRollbackFor = NotificationException.class)
public void completeOrder(Long id) {
    // заказ можно сохранить, даже если уведомление упало
}
```

Но с внешними вызовами обычно лучше не так, а через событие после commit или outbox.

#### `timeout`

Ограничение времени транзакции:

```java
@Transactional(timeout = 5)
public void recalculate() {
}
```

Если метод работает слишком долго, транзакция может быть прервана. Полезно, чтобы не держать соединение и блокировки бесконечно.

#### `transactionManager`

Нужно, если в приложении несколько менеджеров транзакций:

```java
@Transactional(transactionManager = "orderTransactionManager")
```

Например, две базы данных или JPA + Kafka/JMS.

### Типичные проблемы

```text
self-invocation - вызов метода внутри того же класса мимо proxy
private/final method - proxy может не перехватить
checked exception - нет rollback без rollbackFor
exception поймали и не пробросили - proxy думает, что всё хорошо, делает commit
длинная транзакция - долго держит connection и locks
HTTP-вызов внутри транзакции - плохо, внешняя система тормозит, а БД ждёт
LazyInitializationException - пытаемся читать lazy-связь после закрытия транзакции
уведомление до commit - пользователь получил сообщение, а транзакция потом откатилась
```

### Как отвечать на собесе

`@Transactional` в Spring обычно работает через proxy и `PlatformTransactionManager`: proxy открывает транзакцию перед public-методом и делает commit/rollback после него. Основные параметры: `propagation` - как вести себя при существующей транзакции, `isolation` - уровень изоляции, `readOnly` - транзакция только для чтения, `rollbackFor` - на какие checked exceptions откатываться, `timeout` - лимит времени. Главные ловушки: self-invocation, checked exceptions без rollback, проглоченные исключения, длинные транзакции и внешние вызовы внутри транзакции.

---

## 13. Change Data Capture

### Что путалось

Что значит "отслеживать изменения в базе" и какие есть варианты: CDC, Debezium, триггеры, polling, outbox.

### Объяснение

Change Data Capture, или CDC, - это механизм:

```text
узнать, что в БД появилась/изменилась/удалилась запись
и передать это изменение дальше
```

Например:

```text
в таблице orders заказ стал PAID
другой сервис должен узнать об этом
```

Наивно можно сделать так:

```java
orderRepository.save(order);
notificationService.send(...);
```

Но тут есть риск: заказ ещё не закоммитился, а уведомление уже ушло. Или приложение упало между `save` и `send`.

CDC решает похожую задачу надёжнее: изменения читаются из самой БД или из специальной outbox-таблицы.

### Вариант 1: polling по `updated_at`

Самый простой способ:

```sql
select * from orders
where updated_at > :lastSeenTime
```

То есть отдельная джоба раз в N секунд спрашивает БД:

```text
что изменилось после прошлого раза?
```

Плюсы:

```text
просто понять
просто реализовать
```

Минусы:

```text
нагружает БД постоянными запросами
можно пропустить изменения из-за времени/часов/одинакового timestamp
не всегда понятно, что именно изменилось
задержка зависит от интервала polling
```

Подходит для простых случаев, где не нужна строгая надёжность.

### Вариант 2: триггеры

В БД можно повесить trigger:

```text
после INSERT/UPDATE/DELETE в orders записать строку в order_changes
```

Примерно:

```text
orders изменился
trigger сработал
в audit/change table появилась запись
приложение читает эту таблицу
```

Плюсы:

```text
изменение фиксируется прямо внутри БД
можно сохранить OLD/NEW значения
```

Минусы:

```text
логика спрятана в БД, а не в коде приложения
сложнее тестировать и деплоить
каждая запись становится дороже
```

Хорошо для аудита и точечных задач, но не всегда хочется строить на этом интеграцию сервисов.

### Вариант 3: чтение transaction log

Это промышленный CDC.

БД и так пишет журнал транзакций:

```text
PostgreSQL -> WAL
MySQL      -> binlog
```

Инструмент типа Debezium читает этот журнал и публикует изменения дальше, часто в Kafka:

```text
PostgreSQL WAL -> Debezium -> Kafka topic -> другие сервисы
```

Например:

```text
orders INSERT
orders UPDATE status=PAID
orders DELETE
```

превращаются в события.

Плюсы:

```text
не надо постоянно опрашивать таблицы
видно реальные изменения из журнала БД
хорошо подходит для интеграции сервисов
можно передавать события в Kafka
```

Минусы:

```text
сложнее инфраструктура
нужно следить за lag
нужна настройка Debezium/Kafka Connect
нужно думать про порядок, повторы и идемпотентность
```

### Вариант 4: Transactional Outbox

Outbox - очень частый паттерн для микросервисов.

Идея:

```text
в той же транзакции сохранить бизнес-данные и событие в outbox-таблицу
```

Пример:

```java
@Transactional
public void payOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(OrderStatus.PAID);

    outboxRepository.save(new OutboxEvent(
        "OrderPaid",
        order.getId()
    ));
}
```

Теперь либо сохранятся и заказ, и событие, либо не сохранится ничего.

Потом отдельный relay читает outbox и публикует в Kafka/RabbitMQ:

```text
outbox table -> relay -> Kafka
```

Outbox можно читать polling-ом или через Debezium:

```text
outbox table -> Debezium -> Kafka
```

Плюсы:

```text
нет проблемы "БД сохранилась, а событие не отправилось"
событие связано с той же транзакцией
хорошо для микросервисов
```

Минусы:

```text
нужна outbox-таблица
нужен отдельный publisher/relay
нужна идемпотентность, потому что событие может прийти повторно
```

### LISTEN/NOTIFY

В PostgreSQL есть лёгкий механизм:

```sql
LISTEN channel;
NOTIFY channel, 'payload';
```

Он подходит для простых уведомлений внутри PostgreSQL-мира.

Но это не полноценная очередь сообщений:

```text
нет долговременного хранения как в Kafka
не лучший вариант для сложной интеграции сервисов
```

### Как выбрать

```text
простая админская синхронизация -> polling
аудит внутри БД -> trigger + audit table
интеграция сервисов и поток изменений -> Debezium/WAL/binlog
надёжная публикация бизнес-событий -> Transactional Outbox
лёгкий сигнал в PostgreSQL -> LISTEN/NOTIFY
```

### Коротко

CDC - это способ отслеживать изменения в БД и передавать их дальше. Промышленный вариант - читать transaction log БД: PostgreSQL WAL или MySQL binlog, часто через Debezium и Kafka. Более простые варианты - polling по `updated_at` или триггеры. Для бизнес-событий часто используют Transactional Outbox: в одной транзакции сохраняют данные и событие, а отдельный relay публикует событие наружу.
