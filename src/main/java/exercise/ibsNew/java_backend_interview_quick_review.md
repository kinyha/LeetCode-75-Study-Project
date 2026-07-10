# Java Backend Interview — быстрый повтор

> Конспект для устного собеседования: сначала **короткий ответ**, затем детали, пример и типичная ловушка.
>
> Актуальность раздела о версиях Java: **10 июля 2026 года**.

## Как пользоваться конспектом

На большинство вопросов отвечай в четыре шага:

1. Дай определение одним предложением.
2. Объясни, как это работает внутри или на практике.
3. Назови компромиссы и типичные проблемы.
4. Приведи короткий пример из проекта.

Формула хорошего ответа: **«что это → как работает → когда применять → чего опасаться»**.

## Содержание

1. [Java Core](#1-java-core)
2. [Паттерны проектирования](#2-паттерны-проектирования)
3. [Spring и Spring Boot](#3-spring-и-spring-boot)
4. [Базы данных, JPA и Hibernate](#4-базы-данных-jpa-и-hibernate)
5. [REST, SOAP, gRPC и сети](#5-rest-soap-grpc-и-сети)
6. [Kafka и RabbitMQ](#6-kafka-и-rabbitmq)
7. [Многопоточность](#7-многопоточность)
8. [Тестирование](#8-тестирование)
9. [Docker, Kubernetes, безопасность и IDE](#9-docker-kubernetes-безопасность-и-ide)
10. [Чек-лист перед интервью](#10-чек-лист-перед-интервью)

---

# 1. Java Core

## 1.1. HashMap: устройство, коллизии и плохой `hashCode()`

**Короткий ответ:** `HashMap` хранит пары ключ–значение в массиве корзин. По хэшу ключа вычисляется индекс корзины. Если несколько ключей попали в одну корзину, возникает коллизия: элементы хранятся сначала в связанном списке, а при достаточном числе элементов — в красно-чёрном дереве. Средняя сложность `get()` и `put()` — `O(1)`, худшая — `O(n)`, либо `O(log n)` после treeification.

### Что происходит в `put(key, value)`

1. Для `null`-ключа используется хэш `0`; `HashMap` допускает один `null`-ключ.
2. Для обычного ключа вызывается `hashCode()`, затем старшие биты подмешиваются к младшим: примерно `h ^ (h >>> 16)`.
3. Индекс корзины считается как `(capacity - 1) & hash`. Поэтому capacity поддерживается степенью двойки.
4. Если корзина пуста — создаётся узел.
5. Иначе сравниваются хэш и ключ: сначала ссылка/хэш, затем `equals()`.
6. Совпавший ключ обновляет значение; новый ключ добавляется в список или дерево.
7. Когда `size > capacity * loadFactor`, таблица расширяется. По умолчанию load factor равен `0.75`, начальная capacity обычно `16` при первой фактической аллокации.

### Коллизии и treeification

- Коллизия означает одинаковый **индекс корзины**, а не обязательно одинаковый `hashCode()`.
- При длине корзины около `8` список может превратиться в красно-чёрное дерево.
- Treeification выполняется только при capacity не меньше `64`; иначе выгоднее расширить таблицу.
- При уменьшении корзины дерево может снова стать списком.
- Красно-чёрное дерево ограничивает поиск примерно `O(log n)`, но не делает плохой хэш хорошим: сравнения, память и ветвления всё равно дороже.

### Почему плохой хэш опасен

```java
final class BadKey {
    private final long id;

    @Override public int hashCode() { return 1; } // все ключи в одной корзине
    @Override public boolean equals(Object o) {
        return o instanceof BadKey other && id == other.id;
    }
}
```

При равномерном распределении `get()` почти сразу находит нужную корзину. При постоянном хэше он обходит список или дерево и многократно вызывает `equals()`.

### Влияние на разных уровнях

| Уровень | Что происходит | Что оптимизировать |
|---|---|---|
| Метод | `map.get()` вместо почти `O(1)` выполняет много сравнений; растут CPU и latency | Исправить контракт `equals/hashCode`, использовать стабильные и хорошо распределённые поля, задать разумную initial capacity |
| Сервис | Медленный lookup вызывается тысячи раз; увеличиваются p95/p99, очередь запросов и GC из-за лишних объектов | Профилировать JFR/async-profiler, ограничить размер кэша, подобрать структуру данных, добавить метрики latency/hit rate |
| Архитектура | Один экземпляр не успевает, появляются таймауты, ретраи и каскадная нагрузка на другие сервисы | Партиционирование, внешний распределённый кэш, rate limit/backpressure, устранение retry storm; но сначала исправить локальную причину |

### Контракт ключа

- Если `a.equals(b) == true`, то `a.hashCode() == b.hashCode()` обязательно.
- Обратное неверно: одинаковый hash не означает равенство.
- Поля, участвующие в `equals/hashCode`, нельзя менять, пока объект является ключом. Иначе запись физически останется в старой корзине и логически «потеряется».
- Для ключей удобны immutable-типы, например `record`.
- `HashMap` не потокобезопасна; для конкурентного доступа обычно нужен `ConcurrentHashMap`.

**Ответ на 30 секунд:** «`HashMap` — массив корзин. Индекс получается из хэша ключа, а равенство внутри корзины проверяет `equals`. Коллизии сначала образуют список, а при больших корзинах и capacity не меньше 64 — красно-чёрное дерево. В среднем операции `O(1)`, при плохом распределении деградируют до `O(n)` или `O(log n)` после treeification. Ключ должен быть неизменяемым и соблюдать контракт `equals/hashCode`».

**Ловушка:** resize не обязан пересчитывать пользовательский `hashCode`; при удвоении capacity элементы старой корзины обычно разделяются на две группы по одному дополнительному биту хэша.

## 1.2. Generics и PECS

**Короткий ответ:** generics дают типобезопасность на этапе компиляции. Java реализует их в основном через стирание типов: `List<String>` и `List<Integer>` в runtime — один raw-класс `List`. PECS означает **Producer Extends, Consumer Super**.

```java
static double sum(List<? extends Number> source) {
    double result = 0;
    for (Number n : source) result += n.doubleValue();
    return result;
}

static void addDefaults(List<? super Integer> target) {
    target.add(1);
    target.add(2);
}
```

- `? extends T`: коллекция **производит** значения как `T`. Читать безопасно, добавлять нельзя, кроме `null`.
- `? super T`: коллекция **принимает** `T`. Добавлять можно, читать можно только как `Object`.
- Если параметр и читается, и изменяется как один точный тип, wildcard обычно не нужен: `List<T>`.

### Инвариантность

`List<Integer>` не является подтипом `List<Number>`. Иначе в список целых можно было бы добавить `Double`. Wildcard выражает нужную вариативность: `List<? extends Number>`.

### Стирание типов

Из-за type erasure нельзя:

- написать `new T()` или `new T[10]`;
- проверить `obj instanceof List<String>`;
- перегрузить методы только различием `List<String>` и `List<Integer>`;
- использовать примитивы как type argument: нужен `Integer`, а не `int`.

Можно передать `Class<T>`, `Supplier<T>` или type token, когда runtime-тип действительно нужен.

**Ответ на 30 секунд:** «Generics переносят проверку типов в compile time и уменьшают касты. Они инвариантны и в runtime в основном стираются. PECS: если параметр только отдаёт `T`, использую `? extends T`; если принимает — `? super T`. Например, `Collections.copy(List<? super T> dest, List<? extends T> src)`».

## 1.3. `Optional`

**Короткий ответ:** `Optional<T>` явно моделирует возможное отсутствие результата и помогает строить цепочки преобразований без ручных проверок `null`.

```java
return userRepository.findById(id)
        .filter(User::isActive)
        .map(User::email)
        .orElseThrow(() -> new UserNotFoundException(id));
```

Основные методы:

- `of(value)` — значение обязано быть не `null`;
- `ofNullable(value)` — допускает `null`;
- `empty()` — пустое значение;
- `map()` — преобразует непустое значение;
- `flatMap()` — когда функция уже возвращает `Optional`;
- `orElse(defaultValue)` вычисляет аргумент **всегда**;
- `orElseGet(supplier)` вычисляет fallback лениво;
- `orElseThrow()` — завершает цепочку исключением.

```java
// expensiveFallback() вызовется даже при наличии user
user.orElse(expensiveFallback());

// вызовется только для empty
user.orElseGet(this::expensiveFallback);
```

### Где применять

Хорошо — как возвращаемый тип lookup-метода. Обычно плохо:

- в полях JPA entity и DTO;
- в параметрах метода;
- в коллекции `List<Optional<T>>` вместо пустого списка;
- `Optional.get()` без проверки — это замаскированный `null`-подход;
- возвращать `null` вместо `Optional.empty()` нельзя.

**Ловушка:** `Optional` не делает код автоматически безопасным и не предназначен для сериализации/ORM-модели.

## 1.4. Big O: сложность алгоритма

**Короткий ответ:** Big O описывает верхнюю асимптотическую границу роста затрат при увеличении размера входа `n`, отбрасывая константы и младшие члены.

| Сложность | Пример | Интуиция |
|---|---|---|
| `O(1)` | доступ по индексу массива | размер входа почти не влияет |
| `O(log n)` | бинарный поиск, сбалансированное дерево | на шаге отбрасываем часть данных |
| `O(n)` | один проход по списку | работа растёт линейно |
| `O(n log n)` | эффективная сортировка сравнением | типичный хороший результат сортировки |
| `O(n²)` | два вложенных прохода по всем элементам | удвоили вход — примерно в 4 раза больше работы |
| `O(2ⁿ)`, `O(n!)` | полный перебор вариантов | быстро становится непрактично |

### Как оценивать

```java
for (int i = 0; i < n; i++) {       // n
    for (int j = 0; j < n; j++) {   // n на каждую итерацию
        work();                       // O(1)
    }
}
// O(n²)
```

- Последовательные участки складываются: `O(n) + O(n²) = O(n²)`.
- Вложенные независимые циклы умножаются.
- Деление задачи пополам даёт логарифм.
- Для рекурсии учитывай количество вызовов и глубину.
- Отдельно оценивай время и память.
- У структуры могут отличаться average, amortized и worst case. Например, `ArrayList.add()` обычно `O(1)`, иногда resize `O(n)`, амортизированно `O(1)`.

**Ловушка:** Big O не показывает реальные миллисекунды, константы, cache locality, I/O и распределение данных. Для production нужны ещё профилирование и benchmark, например JMH.

## 1.5. Последняя версия Java и что нового

**По состоянию на 10 июля 2026 года:** последняя GA-версия — **Java 26**, выпущенная 17 марта 2026 года. Она не LTS. Последняя LTS — **Java 25**; предыдущие LTS — 21, 17, 11 и 8. Java 27 в этот момент ещё не GA.

### Что важно назвать про Java 26

- Улучшена производительность G1 за счёт снижения синхронизации между application- и GC-потоками (JEP 522).
- AOT-кэш объектов стал независимым от конкретного GC и работает в том числе с ZGC (JEP 516).
- HTTP Client получил HTTP/3 (JEP 517).
- Structured Concurrency — шестой preview (JEP 525), а не финальная возможность.
- Lazy Constants — второй preview (JEP 526).
- Primitive types в patterns/`instanceof`/`switch` — четвёртый preview (JEP 530).
- Vector API остаётся incubator (JEP 529).
- Applet API удалён (JEP 504).

### Что именно изменилось в GC в Java 26

Главный ответ: **G1 стал меньше синхронизировать mutator-потоки приложения с потоками refinement GC**. Введена работа с двумя card tables: приложение обновляет одну, фоновые GC-потоки обрабатывают другую, после чего таблицы меняются. Это упрощает write barrier и повышает throughput; архитектура и пользовательские флаги G1 при этом не меняются.

Дополнительно:

- G1 умеет раньше освобождать подходящие humongous-объекты со ссылками;
- G1 поддерживает `UseGCOverheadLimit` и может бросить `OutOfMemoryError`, если почти всё время уходит на GC, а свободной памяти почти нет;
- AOT object cache стал GC-agnostic, поэтому оптимизации старта доступны и для ZGC.

### Если спрашивают про Java 25 LTS

Полезно назвать 4–5 вещей, не перечисляя все JEP:

- финальные module import declarations;
- compact source files и instance `main`;
- flexible constructor bodies;
- Scoped Values стали финальным API;
- compact object headers — opt-in;
- Generational Shenandoah;
- улучшения JFR и AOT.

**Хорошая формулировка:** «В production я работал с Java X. Знаю, что текущая GA — 26, а актуальная LTS — 25. В 26 из GC-изменений главное — оптимизация G1 через уменьшение синхронизации и более дешёвые write barriers. Preview-функции я отдельно не называю стабильными».

Официальные источники: [JDK 26 release](https://blogs.oracle.com/java/the-arrival-of-java-26), [JEP 522](https://openjdk.org/jeps/522), [JDK 25](https://openjdk.org/projects/jdk/25/), [Oracle Java roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html).

---

# 2. Паттерны проектирования

## 2.1. Три группы паттернов GoF

- **Порождающие:** управляют созданием объектов — Factory Method, Abstract Factory, Builder, Prototype, Singleton.
- **Структурные:** собирают классы и объекты в более крупные структуры — Adapter, Decorator, Facade, Proxy, Composite, Bridge, Flyweight.
- **Поведенческие:** описывают взаимодействие и распределение обязанностей — Strategy, Observer, Command, State, Template Method, Chain of Responsibility, Mediator, Iterator, Visitor, Memento, Interpreter.

На собеседовании лучше назвать по два примера и один разобрать на реальном кейсе.

## 2.2. Стратегия (`Strategy`)

**Короткий ответ:** Strategy инкапсулирует взаимозаменяемые алгоритмы за общим интерфейсом. Клиент зависит от абстракции и выбирает реализацию конфигурацией или по данным запроса, а не большим `if/else`.

```java
interface PriceStrategy {
    Money calculate(Order order);
}

final class RegularPrice implements PriceStrategy { /* ... */ }
final class VipPrice implements PriceStrategy { /* ... */ }

final class PricingService {
    private final Map<CustomerType, PriceStrategy> strategies;

    Money price(Order order) {
        return strategies.get(order.customerType()).calculate(order);
    }
}
```

### Когда оправдана

- есть несколько алгоритмов одного назначения;
- алгоритмы меняются независимо от клиента;
- новые варианты будут добавляться;
- нужно тестировать каждый алгоритм отдельно;
- выбор происходит в runtime.

### Когда лишняя

Если вариантов два, логика в одну строку и расширение не ожидается, отдельная иерархия создаст только шум.

**Любимый паттерн — пример ответа:** «Часто использую Strategy для выбора провайдера оплаты или правила расчёта. Spring внедряет список/Map реализаций, а сервис выбирает нужную по типу операции. Это убирает растущий `switch`, изолирует интеграции и позволяет независимо тестировать каждую стратегию. Но простой `if` не заменяю паттерном без причины».

## 2.3. Декоратор и Адаптер

| | Decorator | Adapter |
|---|---|---|
| Цель | Добавить поведение | Согласовать несовместимые интерфейсы |
| Интерфейс наружу | Обычно тот же, что у оборачиваемого объекта | Тот, который ожидает клиент |
| Число обёрток | Часто можно строить цепочку | Обычно одна граница преобразования |
| Пример JDK | `BufferedInputStream(InputStream)` | `Arrays.asList(array)` как адаптация представления |

```java
// Decorator: контракт тот же, добавили метрики
final class MetricsPaymentClient implements PaymentClient {
    private final PaymentClient delegate;

    public Receipt pay(Command c) {
        long started = System.nanoTime();
        try { return delegate.pay(c); }
        finally { recordLatency(started); }
    }
}

// Adapter: внешний SDK преобразован в наш доменный контракт
final class VendorPaymentAdapter implements PaymentClient {
    private final VendorSdk sdk;

    public Receipt pay(Command c) {
        return map(sdk.execute(toVendorRequest(c)));
    }
}
```

**Ключевая фраза:** «Decorator сохраняет интерфейс и наращивает обязанности; Adapter меняет представление интерфейса, чтобы две части системы смогли работать вместе».

**Не путать с Proxy:** Proxy обычно контролирует доступ к тому же объекту — lazy loading, remote access, security. Структурно похож, но намерение другое.

---

# 3. Spring и Spring Boot

## 3.1. IoC и Dependency Injection

**Короткий ответ:** Inversion of Control означает, что управление созданием объектов, их связями и жизненным циклом передаётся фреймворку. В Spring эту роль выполняет IoC-контейнер (`ApplicationContext`), который создаёт bean definitions, инстанцирует бины, внедряет зависимости и запускает lifecycle callbacks.

```java
@Service
final class OrderService {
    private final OrderRepository repository;

    OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Класс не создаёт `new JpaOrderRepository()` и не ищет зависимость сам. Он объявляет потребность в интерфейсе, а Spring передаёт подходящий bean.

### Упрощённый жизненный цикл бина

1. Spring читает конфигурацию: component scan, `@Bean`, imports и auto-configuration.
2. Создаёт `BeanDefinition` — метаданные о классе, scope, factory method и т. д.
3. `BeanFactoryPostProcessor` может изменить definitions.
4. Бин создаётся, зависимости внедряются.
5. Вызываются aware-callbacks, `BeanPostProcessor` до/после initialization, `@PostConstruct`.
6. На этапе post-processing бин может быть обёрнут proxy, например для транзакций или AOP.
7. При закрытии контекста вызываются `@PreDestroy`/destroy methods для управляемых экземпляров.

**DI — способ реализации IoC**, но IoC шире: контейнер контролирует не только внедрение, но и жизненный цикл, конфигурацию, события, прокси.

**Почему constructor injection предпочтительнее:** обязательные зависимости видны, поле можно сделать `final`, объект легче тестировать без Spring, невозможен частично инициализированный объект, циклическая зависимость обнаруживается явно.

## 3.2. AOP в Spring

**Короткий ответ:** AOP выносит сквозную логику — транзакции, аудит, метрики, безопасность — из бизнес-кода. Spring AOP обычно создаёт proxy вокруг bean и перехватывает вызовы подходящих методов.

Термины:

- **Aspect** — модуль сквозной логики;
- **Join point** — точка выполнения; в Spring AOP это вызов метода bean;
- **Pointcut** — правило выбора методов;
- **Advice** — код до, после или вокруг вызова;
- **Weaving** — связывание аспекта с кодом/объектом.

```java
@Aspect
@Component
class TimingAspect {
    @Around("@annotation(Measured)")
    Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try { return pjp.proceed(); }
        finally { record(pjp.getSignature(), System.nanoTime() - start); }
    }
}
```

### Как устроены proxy

- JDK dynamic proxy работает через интерфейсы.
- CGLIB создаёт подкласс целевого класса.
- В современных Spring-приложениях детали выбора зависят от конфигурации, но ключевой принцип один: внешний вызов должен пройти через proxy.

### Главная ловушка — self-invocation

```java
@Transactional
public void outer() {
    inner(); // вызов this.inner(), proxy обходится
}

@Transactional(propagation = REQUIRES_NEW)
public void inner() { /* ... */ }
```

Ожидаемый `REQUIRES_NEW` может не сработать, потому что вызов не прошёл через proxy. Решения: вынести метод в отдельный bean, изменить границу транзакции или при необходимости использовать AspectJ weaving.

### Когда использовать

Подходит для действительно сквозной, ортогональной логики. Не стоит прятать в аспектах основную бизнес-логику: ухудшаются явность, отладка и понимание порядка выполнения. Также помни про ограничения `final`/`private` методов для subclass-proxy.

## 3.3. Автоконфигурация Spring Boot

**Короткий ответ:** Spring Boot смотрит на classpath, существующие бины и properties, после чего условно импортирует конфигурации и создаёт разумные default beans. Пользовательский bean обычно заставляет автоконфигурацию «отступить» через `@ConditionalOnMissingBean`.

`@SpringBootApplication` объединяет:

- `@Configuration`;
- `@EnableAutoConfiguration`;
- `@ComponentScan`.

Пример логики автоконфигурации:

```java
@AutoConfiguration
@ConditionalOnClass(MyClient.class)
@EnableConfigurationProperties(MyClientProperties.class)
public class MyClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MyClient myClient(MyClientProperties p) {
        return new MyClient(p.baseUrl(), p.timeout());
    }
}
```

Кандидаты регистрируются в:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### Что даёт Boot

- starters с согласованным набором зависимостей;
- dependency management;
- embedded server;
- auto-configuration;
- externalized configuration (`application.yml`, env variables);
- Actuator и production-ready endpoints;
- единый packaging и простой запуск.

Boot не «генерирует магию»: это обычные конфигурационные классы с условиями. Диагностировать выбор можно через condition evaluation report и запуск с `--debug`.

### Как написать собственный starter

Обычно разделяют:

1. `*-autoconfigure` — properties, auto-configuration, conditions и beans.
2. `*-starter` — почти пустой модуль, который подтягивает autoconfigure и нужные зависимости.

Хороший starter:

- использует `@ConfigurationProperties` вместо разрозненных `@Value`;
- создаёт bean только при наличии нужного класса/свойства;
- применяет `@ConditionalOnMissingBean`, чтобы пользователь мог переопределить default;
- не сканирует чужие пакеты неожиданно;
- содержит тесты через `ApplicationContextRunner`;
- добавляет configuration metadata для подсказок IDE.

**Ответ на 30 секунд:** «Starter отвечает за удобное подключение зависимостей, а autoconfigure-модуль — за условное создание beans. Я регистрирую `@AutoConfiguration` в `AutoConfiguration.imports`, связываю properties, добавляю `OnClass`, `OnProperty`, `OnMissingBean` и проверяю контекст через `ApplicationContextRunner`».

## 3.4. `@Component`, `@Service`, `@Repository`

Все три — stereotype-аннотации; `@Service` и `@Repository` мета-аннотированы `@Component`, поэтому обнаруживаются component scan.

| Аннотация | Смысл |
|---|---|
| `@Component` | общий Spring-managed компонент |
| `@Service` | бизнес-сервис; прежде всего семантика и читаемость архитектуры |
| `@Repository` | слой доступа к данным; также участвует в exception translation в иерархию `DataAccessException` |

```java
@Repository class JpaOrderRepository { /* persistence */ }
@Service class OrderService { /* business rules */ }
@Component class CsvParser { /* generic infrastructure component */ }
```

**Ловушка:** аннотации сами по себе не обеспечивают слоистую архитектуру. `@Service` на классе не запрещает ему делать SQL, а `@Repository` не создаёт транзакцию автоматически.

## 3.5. `@Autowired`, `@Inject`, `@Resource` и виды autowiring

### Constructor, setter и field injection

- **Constructor injection** — для обязательных зависимостей; рекомендуется.
- **Setter injection** — для необязательной или изменяемой конфигурации.
- **Field injection** — кратко, но скрывает зависимости, мешает `final` и unit-тестам; обычно не рекомендуется.

Если у Spring bean один конструктор, `@Autowired` на нём не нужна.

### Различия аннотаций

| Аннотация | Стандарт | Базовое разрешение | Особенности |
|---|---|---|---|
| `@Autowired` | Spring | по типу | `@Qualifier`, `@Primary`, `required=false`, `Optional`, коллекции |
| `@Inject` | Jakarta DI | по типу | похожа на `@Autowired`, qualifier через стандартные/поддерживаемые qualifiers; нет `required` |
| `@Resource` | Jakarta Annotation | прежде всего по имени, затем по типу в типичных сценариях Spring | удобно для явного имени; обычно field/setter, не constructor |

При нескольких beans одного типа:

```java
OrderService(@Qualifier("fastPaymentClient") PaymentClient client) { ... }
```

Другие способы:

- пометить default как `@Primary`;
- внедрить `List<PaymentClient>` или `Map<String, PaymentClient>`;
- использовать имя параметра, но явный `@Qualifier` надёжнее для важного выбора.

**Ловушка:** автосвязывание «по имени» и «по типу» — это не разные контейнеры. Это стратегии разрешения кандидатов. Не следует полагаться на случайное совпадение имени поля.

## 3.6. `@Conditional`

**Короткий ответ:** `@Conditional` регистрирует конфигурацию или bean только когда реализация интерфейса `Condition` вернула `true`.

```java
final class ProductionCondition implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        return "prod".equals(ctx.getEnvironment().getProperty("app.mode"));
    }
}

@Bean
@Conditional(ProductionCondition.class)
PaymentClient realClient() { return new RealPaymentClient(); }
```

В Boot чаще применяют готовые варианты:

- `@ConditionalOnClass` / `@ConditionalOnMissingClass`;
- `@ConditionalOnBean` / `@ConditionalOnMissingBean`;
- `@ConditionalOnProperty`;
- `@ConditionalOnWebApplication`;
- `@ConditionalOnResource`.

`@Profile` решает близкую, но более узкую задачу выбора по активному окружению. Для library/starter-конфигурации обычно лучше конкретные `On...` conditions.

**Ловушка:** условия оцениваются во время построения контекста, и порядок регистрации definitions иногда важен. Не используй condition как динамический `if` на каждый запрос.

## 3.7. Scope бинов и prototype внутри singleton

Основные scopes:

- `singleton` — один экземпляр на `ApplicationContext`, default;
- `prototype` — новый экземпляр при каждом запросе у контейнера;
- `request` — один на HTTP-запрос;
- `session` — один на HTTP-сессию;
- `application` — один на `ServletContext`;
- `websocket` — один на WebSocket-сессию.

### Почему простой injection prototype не работает «каждый раз»

Singleton создаётся один раз. Если при его создании внедрить prototype напрямую, контейнер запросит prototype один раз — и singleton навсегда сохранит эту ссылку.

Правильные варианты:

```java
@Service
class ReportService {
    private final ObjectProvider<ReportBuilder> builders;

    ReportService(ObjectProvider<ReportBuilder> builders) {
        this.builders = builders;
    }

    Report build() {
        return builders.getObject().build();
    }
}
```

Также возможны `Provider<T>`, scoped proxy (`proxyMode`) или `@Lookup`. `ObjectProvider` обычно самый явный Spring-вариант.

**Важная деталь:** Spring создаёт prototype, но не управляет полным уничтожением каждого такого объекта. Освобождение внешних ресурсов должен организовать клиент.

---

# 4. Базы данных, JPA и Hibernate

## 4.1. Индексы в реляционных БД

**Короткий ответ:** индекс — дополнительная структура данных, которая ускоряет чтение ценой диска и удорожания `INSERT/UPDATE/DELETE`. Чаще всего используется B-tree; индекс должен соответствовать реальным фильтрам, сортировкам и соединениям.

### Основные виды

- **B-tree/B+tree** — equality, ranges, prefix сортировки; основной универсальный тип.
- **Hash** — equality lookup; возможности и полезность зависят от СУБД.
- **Unique** — обеспечивает уникальность и ускоряет поиск.
- **Composite** — индекс по нескольким столбцам; важен порядок.
- **Partial/filtered** — индексирует только строки по условию.
- **Functional/expression** — индекс по выражению, например `lower(email)`.
- **Covering** — содержит все нужные запросу данные; в некоторых СУБД используются included columns.
- Специализированные: full-text, bitmap, GiST/GIN, spatial — зависят от СУБД и типа данных.
- **Clustered** определяет физический порядок/организацию строк; обычно один. **Non-clustered** хранится отдельно и ссылается на строку. Конкретная семантика зависит от СУБД.

### Составной индекс и leftmost prefix

Для индекса `(tenant_id, status, created_at)` хорошо подходят условия по:

- `tenant_id`;
- `tenant_id + status`;
- `tenant_id + status + range(created_at)`.

Запрос только по `created_at` обычно не использует такой индекс эффективно. Equality-столбцы часто ставят перед range/sort, но финальное решение подтверждают планом.

### Практический алгоритм

1. Найти медленный реальный запрос и его частоту.
2. Посмотреть `EXPLAIN (ANALYZE, BUFFERS)` или аналог.
3. Проверить cardinality/selectivity, объём данных и статистику.
4. Создать минимально нужный индекс.
5. Повторно измерить план, latency и цену записи.
6. Удалять дублирующие/неиспользуемые индексы осторожно.

**Ловушки:** функция над колонкой без expression index, `LIKE '%text'`, неявное приведение типов, низкая селективность, слишком много индексов, deep pagination. Индекс не гарантирует использование — оптимизатор выбирает план по стоимости.

## 4.2. Materialized View

**Короткий ответ:** обычное view хранит запрос, а materialized view хранит результат запроса физически. Чтение быстрее, но данные могут быть устаревшими и требуют refresh.

| | View | Materialized View |
|---|---|---|
| Хранит строки | Нет | Да |
| Свежесть | На момент запроса | На момент последнего refresh |
| Скорость сложной агрегации | Считает заново | Обычно быстрее |
| Цена | Нагрузка при чтении | Диск и стоимость обновления |

Подходит для тяжёлых отчётов, денормализованных read models, агрегаций, которые допустимо обновлять периодически. Не подходит, если требуется строго актуальный баланс сразу после транзакции.

Refresh может быть полным, инкрементальным или конкурентным — зависит от СУБД. Частоту выбирают по допустимой stale window. Индексы на materialized view часто нужны отдельно.

## 4.3. Lazy loading в Hibernate

**Короткий ответ:** lazy association загружается не вместе с entity, а при первом обращении через proxy или persistent collection. Это экономит ненужные запросы, но может породить N+1 или `LazyInitializationException`.

```java
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private List<OrderLine> lines;
```

По умолчанию JPA обычно задаёт `LAZY` для to-many и `EAGER` для to-one, но `EAGER` редко является хорошим глобальным решением. Для to-one фактическая ленивость может зависеть от enhancement/proxy и конкретной модели.

`LazyInitializationException` возникает, когда код обращается к незагруженной связи после закрытия persistence context/session.

Правильный подход: загрузить ровно нужный граф на границе use case — fetch join, EntityGraph, DTO projection, batch fetching. Не маскировать проблему включением Open Session in View: он переносит случайные SQL-запросы в web/serialization слой и усложняет контроль транзакций.

## 4.4. N+1 в Hibernate и способы решения

**Проблема:** один запрос получает `N` родительских сущностей, затем обращение к связи выполняет ещё по запросу на каждую сущность. Итого `1 + N` round trips.

```java
List<Order> orders = orderRepository.findAll(); // 1 query
orders.forEach(o -> o.getCustomer().getName()); // до N queries
```

### Решения кроме `join fetch`

- **EntityGraph** — декларативно задать associations для конкретного запроса.
- **DTO projection** — выбрать только необходимые поля одним запросом.
- **Batch fetching** — `@BatchSize` или `hibernate.default_batch_fetch_size`; связи догружаются группами через `IN (...)`.
- **`FetchMode.SUBSELECT`** — Hibernate загружает коллекции для набора родителей одним subselect; применять осознанно.
- Явный query/агрегация вместо обхода object graph.
- Second-level cache может уменьшить запросы для повторного доступа, но не является основным исправлением плохого query shape.

### Почему `EAGER` не универсальное решение

Он может загрузить слишком много данных и всё равно породить дополнительные select в зависимости от запроса. Fetch strategy нужно выбирать под use case.

### Fetch join и пагинация

Fetch join коллекции размножает строки родителя. Hibernate дедуплицирует entities, но SQL limit/offset с collection fetch может дать неправильную/дорогую пагинацию или обработку в памяти. Надёжный шаблон: сначала page IDs родителей, затем вторым запросом загрузить граф по этим IDs.

**Как обнаруживать:** включить SQL/statistics в тестах, считать число запросов, смотреть APM/trace. N+1 — это проблема round trips, а не только «много строк».

## 4.5. Оптимистическая и пессимистическая блокировка

### Optimistic locking

```java
@Version
private long version;
```

При update Hibernate добавляет version в `WHERE` и увеличивает её. Если обновлено 0 строк, кто-то изменил запись раньше — возникает optimistic lock exception.

Подходит, когда конфликты редки, чтений больше, транзакции хочется держать короткими. Нужна политика retry или сообщение пользователю. Retry должен повторять всю бизнес-транзакцию и быть безопасным.

### Pessimistic locking

Обычно соответствует `SELECT ... FOR UPDATE`: СУБД блокирует строку до конца транзакции.

Подходит, когда конфликт вероятен и нельзя позволить конкурентам выполнить работу на устаревших данных. Цена: ожидания, меньший throughput, deadlock и timeout. Транзакции должны быть короткими; порядок захвата ресурсов — стабильным.

| | Optimistic | Pessimistic |
|---|---|---|
| Конфликт | обнаруживается при записи | предотвращается блокировкой |
| Хорошо при | редких конфликтах | частых/дорогих конфликтах |
| Цена | retry/ошибка пользователю | ожидания/deadlock |

**Ловушка:** in-memory Java lock не защищает от другого экземпляра сервиса. Для распределённой системы нужна координация на уровне БД/внешнего lock service или другой дизайн.

## 4.6. Пагинация в Spring Data JPA

```java
Page<Order> page = repository.findByStatus(
        Status.NEW,
        PageRequest.of(0, 20, Sort.by("createdAt").descending().and(Sort.by("id")))
);
```

- `Page<T>` содержит content, total pages/elements и обычно требует дополнительный `count` query.
- `Slice<T>` знает только наличие следующей страницы и может избежать дорогого total count.
- `List<T>` с `Pageable` — минимум метаданных.

### Offset pagination

`LIMIT 20 OFFSET 100000` проста, поддерживает переход на страницу, но БД должна пройти/отбросить много строк. При конкурентных вставках возможны пропуски и дубли между страницами.

### Keyset/cursor pagination

```sql
WHERE (created_at, id) < (:lastCreatedAt, :lastId)
ORDER BY created_at DESC, id DESC
LIMIT 20
```

Она быстрее на глубоких страницах и стабильнее при изменениях, но не позволяет дешёво перейти сразу на произвольный номер страницы. Нужен уникальный детерминированный порядок; `id` часто добавляют как tie-breaker.

### Частые проблемы

- пагинация с fetch join коллекции;
- нестабильный sort только по неуникальному полю;
- тяжёлый `count` с joins — иногда нужен отдельный `countQuery`;
- N+1 при преобразовании page content в DTO;
- отсутствие индекса под `WHERE + ORDER BY`.

## 4.7. Миграции: Flyway и Liquibase

**Короткий ответ:** schema migration хранится в version control и применяется автоматически в строго контролируемом порядке. Hibernate `ddl-auto=update` не подходит как основной production-механизм.

### Flyway

- простая модель versioned migrations: `V1__init.sql`, `V2__add_index.sql`;
- repeatable migrations: `R__refresh_view.sql`;
- хорошо подходит командам, которые предпочитают явный SQL.

### Liquibase

- changelog в XML/YAML/JSON/SQL;
- changesets, preconditions, labels/contexts;
- умеет генерировать rollback для части change types, но rollback всё равно нужно проверять.

### Production-практика

- одна миграция после применения не редактируется; добавляется новая;
- миграции проверяются на копии production-подобных данных;
- большие DDL/индексы планируются с учётом lock и online/concurrent возможностей СУБД;
- для zero-downtime используется expand-and-contract: добавить совместимую схему → выкатить код → перенести данные → удалить старое позже;
- миграцию запускает один контролируемый job/процесс, а не гонка всех replicas;
- backup — не замена rollback-плану, но обязателен для рискованных операций.

## 4.8. Criteria API

**Короткий ответ:** JPA Criteria API строит типизированный запрос объектами вместо строки JPQL. Главная польза — динамические фильтры, когда набор условий формируется во время выполнения.

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Order> query = cb.createQuery(Order.class);
Root<Order> order = query.from(Order.class);

List<Predicate> predicates = new ArrayList<>();
if (status != null) predicates.add(cb.equal(order.get("status"), status));
if (from != null) predicates.add(cb.greaterThanOrEqualTo(order.get("createdAt"), from));

query.where(predicates.toArray(Predicate[]::new));
return em.createQuery(query).getResultList();
```

Плюсы: композиция условий, интеграция со `Specification`, меньше string concatenation. Минусы: многословность, сложнее читать SQL-смысл, строковые имена полей без metamodel всё ещё ломаются при refactoring.

Использовать вместо статичного JPQL/SQL стоит при реальной динамике: search form с десятком необязательных фильтров. Для сложной аналитики или СУБД-специфичных функций native SQL, jOOQ или QueryDSL часто читаемее.

**Ловушка:** Criteria API не работает «вместо Hibernate» — это API построения JPA-запроса, который всё равно выполняет JPA provider, например Hibernate.

---

# 5. REST, SOAP, gRPC и сети

## 5.1. REST и его принципы

**Короткий ответ:** REST — архитектурный стиль для распределённых систем, в котором ресурсы адресуются URI, а операции выполняются через единообразный интерфейс. HTTP API может быть RESTful, но «JSON поверх HTTP» сам по себе ещё не гарантирует REST.

Ограничения REST:

- **client–server** — UI/клиент отделён от хранения и бизнес-логики сервера;
- **stateless** — каждый запрос содержит всё нужное для обработки; сервер не хранит conversational state клиента между запросами;
- **cacheable** — ответы явно определяют возможность кэширования;
- **uniform interface** — ресурсы, стандартные методы и самодостаточные сообщения;
- **layered system** — клиент может не знать о proxy/gateway между ним и сервером;
- **code on demand** — необязательное ограничение.

### Практика HTTP API

- URI описывает ресурс существительным: `/orders/{id}`, а не `/getOrder`.
- `GET` — безопасный и идемпотентный; `PUT` и `DELETE` задуманы как идемпотентные; `POST` обычно не идемпотентен.
- `PUT` обычно заменяет полное представление по известному URI, `PATCH` частично изменяет.
- Используются корректные status codes, media types, headers, caching и content negotiation.
- Stateless не означает «нет состояния вообще»: бизнес-состояние хранится на сервере, но каждый запрос аутентифицируется/обрабатывается самостоятельно.

**Ловушка:** идемпотентность означает одинаковый наблюдаемый итог повторного запроса, а не обязательно одинаковый status code и отсутствие любых побочных логов.

## 5.2. Обработка ошибок в REST API

**Цель:** стабильный машинно-читаемый контракт, корректный HTTP status, безопасное сообщение клиенту и trace/correlation id для диагностики.

```java
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(OrderNotFoundException ex) {
        ProblemDetail p = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        p.setTitle("Order not found");
        p.setDetail(ex.getMessage());
        p.setProperty("code", "ORDER_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(p);
    }
}
```

Полезные средства Spring:

- `@RestControllerAdvice` — глобальная обработка для REST-контроллеров;
- `@ExceptionHandler` — конкретное сопоставление exception → response;
- `@ResponseStatus` / `ResponseStatusException` — для простых случаев;
- `ProblemDetail` — представление ошибки в стиле RFC 9457;
- Bean Validation: `@Valid`, `@NotNull`, `@Size` и обработка validation errors.

### Рекомендуемая форма ошибки

```json
{
  "type": "https://api.example.com/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "Order 42 does not exist",
  "instance": "/orders/42",
  "code": "ORDER_NOT_FOUND",
  "traceId": "..."
}
```

### Выбор статуса

- `400` — синтаксически/структурно неверный запрос;
- `401` — нет корректной аутентификации;
- `403` — личность известна, но доступ запрещён;
- `404` — ресурс не найден;
- `409` — конфликт состояния/уникальности/version;
- `422` — запрос понятен, но нарушает семантические правила;
- `429` — rate limit;
- `500` — непредвиденная ошибка сервера;
- `503` — временная недоступность.

Не отдавай stack trace, SQL, токены и внутренние имена. Логируй exception один раз на правильной границе; ожидаемые 4xx не всегда должны быть ERROR.

## 5.3. SOAP и отличие от REST

**SOAP** — протокол обмена XML-сообщениями с формальным контрактом WSDL и расширениями WS-* для security, reliable messaging и транзакционных enterprise-сценариев.

| | SOAP | REST over HTTP |
|---|---|---|
| Модель | операции/сообщения | ресурсы и единый интерфейс |
| Формат | XML envelope | часто JSON, но не обязан |
| Контракт | WSDL, XSD | обычно OpenAPI |
| Транспорт | не только HTTP | чаще HTTP |
| Enterprise standards | богатый WS-* стек | чаще проще и легче |

SOAP уместен для legacy enterprise-интеграций, строгого contract-first, WS-Security/message-level security или обязательных отраслевых стандартов. Для публичного CRUD/API и web/mobile обычно проще REST.

**Ловушка:** REST не является протоколом, а SOAP не обязательно «плохой и старый» — выбор определяется требованиями интеграции.

## 5.4. OpenAPI / Swagger

**OpenAPI** — спецификация описания HTTP API: endpoints, параметры, schemas, ответы, security. **Swagger** — экосистема инструментов вокруг спецификации: UI, editor, code generation и др.

Использование:

- **code-first:** аннотации/код → генерируется OpenAPI;
- **design-first:** сначала `openapi.yaml`, затем server stubs и clients.

Что документировать: операции, DTO schemas, validation constraints, auth schemes, все значимые status codes, примеры, pagination, idempotency keys.

Польза: интерактивная документация, генерация клиентов, contract testing, review API до реализации. Риск code-first: схема может формально генерироваться, но оставаться непонятной; generated spec нужно проверять в CI на breaking changes.

## 5.5. gRPC и отличие от REST

**Короткий ответ:** gRPC — RPC framework, обычно использующий Protocol Buffers как IDL/serialization и HTTP/2 как transport. Клиент вызывает типизированный метод сгенерированного stub, будто удалённый сервис — локальный объект, хотя сеть всё равно остаётся ненадёжной.

```proto
service UserService {
  rpc GetUser(GetUserRequest) returns (UserResponse);
  rpc WatchUsers(WatchRequest) returns (stream UserResponse);
}
```

Виды вызовов: unary, server streaming, client streaming, bidirectional streaming.

| | gRPC | REST/JSON |
|---|---|---|
| Контракт | `.proto`, строгая схема | OpenAPI необязателен |
| Данные | компактный binary protobuf | обычно читаемый JSON |
| Transport | HTTP/2 | HTTP/1.1, HTTP/2, HTTP/3 |
| Streaming | встроенные режимы | возможен, но менее унифицирован |
| Browser/public API | требует дополнительной поддержки, например gRPC-Web | естественно поддерживается |

gRPC хорош для внутреннего низколатентного межсервисного API, полиглотной генерации клиентов и streaming. REST удобнее для внешнего API, браузеров, простого debugging и HTTP caching.

### Production-нюансы

- всегда задавать deadline; иначе зависший вызов живёт слишком долго;
- cancellation распространять вниз;
- retries делать только для подходящих status codes и идемпотентных операций;
- учитывать backward compatibility protobuf: не переиспользовать удалённые field numbers, добавлять поля совместимо;
- использовать TLS, auth metadata, health checks, load balancing, tracing;
- не забывать, что быстрый binary format не устраняет сетевые сбои и partial failure.

## 5.6. RPC и REST

**RPC** моделирует удалённые **действия**: `createInvoice`, `approvePayment`. **REST** моделирует **ресурсы** и их представления: `POST /invoices`, `POST /payments/{id}/approvals`.

RPC удобен, когда домен естественно операционный, нужен строгий IDL, высокое performance/streaming и обе стороны контролируются. REST удобен для resource-oriented публичного API, слабой связанности, стандартной HTTP-семантики и кэширования.

**Ловушка «локального вызова»:** RPC syntax скрывает сеть. У remote call есть latency, timeout, retry, duplicate, unavailable и schema evolution — их нужно проектировать явно.

## 5.7. TCP и UDP

| | TCP | UDP |
|---|---|---|
| Соединение | connection-oriented | connectionless |
| Доставка | надёжный упорядоченный поток байтов | отдельные datagrams без гарантии |
| Потери/повторы | retransmission и duplicate suppression | обрабатывает приложение/верхний протокол |
| Flow/congestion control | есть | базовый UDP не предоставляет |
| Границы сообщений | нет, это byte stream | сохраняются |
| Типичные кейсы | HTTP/1.1–2, БД, SSH | DNS, realtime, игры, media; QUIC работает поверх UDP |

TCP гарантирует порядок байтов **внутри соединения**, но не даёт application-level exactly-once. После timeout клиент не всегда знает, выполнил ли сервер операцию, поэтому нужны idempotency keys.

UDP быстрее не «по определению»: у него меньше встроенных гарантий. QUIC/HTTP/3 поверх UDP реализует надёжность, congestion control и streams на пользовательском уровне.

---

# 6. Kafka и RabbitMQ

## 6.1. Kafka offset

**Короткий ответ:** offset — монотонно растущая позиция записи внутри конкретной partition. Он уникален только в паре `(topic, partition)`, а не во всём topic.

Consumer читает с позиции, а consumer group хранит committed offset — откуда продолжить после restart/rebalance. Важно различать:

- current position — следующая запись текущего consumer;
- committed offset — сохранённая точка восстановления;
- log end offset — конец partition;
- consumer lag — насколько consumer отстаёт от конца.

Обычно commit означает **offset следующей записи**, а не «номер уже обработанной».

### Когда commit делать

- Commit до обработки → возможна потеря при падении: at-most-once.
- Обработка, затем commit → при падении между ними запись будет обработана повторно: at-least-once.
- Auto-commit удобен, но легко коммитит не ту бизнес-границу; для важной обработки чаще нужен осознанный manual/container-managed commit.

Offset можно reset на earliest/latest или конкретную позицию, пока данные ещё сохранены retention. Commit offset сам по себе не удаляет message из Kafka.

## 6.2. Consumer groups

**Короткий ответ:** в одной consumer group каждая partition в конкретный момент назначена максимум одному consumer. Поэтому consumers группы делят работу, а разные группы получают сообщения независимо.

Если partitions = 6:

- 3 consumers → примерно по 2 partitions;
- 6 consumers → по 1;
- 10 consumers → 4 простаивают.

Максимальный параллелизм одной группы ограничен количеством partitions. Увеличение partitions помогает масштабированию, но влияет на ordering, ресурсы брокера и распределение ключей.

При входе/выходе consumers или изменении metadata происходит rebalance. На время перераспределения возможна пауза и повторная обработка незакоммиченных сообщений. Cooperative rebalancing и static membership могут уменьшить disruption.

**Ловушка:** две replicas с разными `group.id` не балансируют нагрузку — обе прочитают весь topic.

## 6.3. Retention в Kafka

**Короткий ответ:** Kafka хранит записи независимо от того, прочитаны они или нет. Retention удаляет старые log segments по времени/размеру, а compaction сохраняет последнее значение для каждого ключа.

Основные политики topic:

- `cleanup.policy=delete` — удаление сегментов по `retention.ms` и/или `retention.bytes`;
- `cleanup.policy=compact` — log compaction по ключу;
- `compact,delete` — обе политики.

### Важные нюансы

- Удаляются сегменты, не отдельные сообщения мгновенно, поэтому фактическое время может быть больше порога.
- `retention.bytes` обычно применяется на partition; общий объём topic зависит от числа partitions и replication factor.
- Consumer lag не останавливает retention. Если consumer отстал дальше сохранённого диапазона, старые данные уже не прочитать из Kafka.
- Retention — не backup: удаление, ошибка producer или corruption могут потребовать отдельного архива/репликации.
- Replication factor увеличивает фактически занятый диск.

### Compaction

Для одинакового key Kafka со временем стремится оставить последнюю запись. Tombstone — запись с ключом и `null` value — означает удаление ключа; tombstone тоже хранится ограниченное время. Compaction не гарантирует, что в partition в каждый момент физически осталась ровно одна запись на key, и не сохраняет глобальный порядок между keys.

**Ответ на 30 секунд:** «Retention относится к журналу, а не к факту чтения. `delete` очищает старые segments по времени/размеру, `compact` сохраняет актуальное состояние по key, их можно комбинировать. Медленный consumer может потерять доступ к данным, если lag превысил retention window».

## 6.4. Порядок и гарантии доставки Kafka

### Порядок

Kafka гарантирует порядок только **внутри одной partition**. Чтобы события одного entity шли последовательно, producer отправляет их с одинаковым key, например `orderId`, и partitioner выбирает одну partition.

Глобальный порядок всего topic возможен практически только с одной partition, что ограничивает масштабирование. После увеличения числа partitions mapping key → partition может измениться, поэтому архитектуру ordering нужно продумывать заранее.

Producer idempotence и корректные настройки retries/in-flight защищают порядок при повторной отправке. Consumer внутри partition не должен бездумно передавать записи в unordered thread pool; иначе завершение бизнес-обработки перестанет быть упорядоченным.

### Семантика обработки

- **At-most-once:** commit до обработки. Дублей меньше, но сообщение можно потерять.
- **At-least-once:** обработка до commit. Потери минимизируются, но дубли возможны.
- **Exactly-once:** результат каждой записи наблюдаем ровно один раз в определённых границах. В Kafka read–process–write это достигается idempotent producer + transactions + чтение `read_committed` + атомарный commit offsets с output records.

Exactly-once Kafka не делает атомарной запись в произвольную внешнюю БД. Для DB + Kafka используют idempotent consumer, inbox/outbox, unique business key, CDC или координированный дизайн.

```text
DB transaction: изменить бизнес-данные + записать событие в outbox
CDC publisher: outbox → Kafka
Consumer: eventId + UNIQUE constraint → безопасная повторная обработка
```

**Ловушка:** `acks=all` — durability acknowledgement, а не exactly-once. Идемпотентность producer убирает дубли его retries в рамках протокола, но не исправляет двойной бизнес-вызов клиента.

## 6.5. Durable Queue в RabbitMQ

**Короткий ответ:** durable queue переживает restart broker как объявленная очередь, но для сохранности сообщений этого недостаточно: сообщения должны быть persistent, а producer — получить publisher confirm.

Нужны все уровни:

1. Queue объявлена `durable=true`.
2. Exchange тоже durable, если он должен пережить restart.
3. Message публикуется как persistent.
4. Publisher confirm подтверждает, что broker принял публикацию согласно своей гарантии.
5. Consumer использует manual ack только после успешной обработки.

При падении consumer до ack сообщение может быть redelivered — обработчик должен быть идемпотентным. При постоянной ошибке нужны retry policy, dead-letter exchange/queue и ограничение числа попыток.

Для более сильной доступности в кластере применяют quorum queues. Durable не означает «никогда не потеряется при любой катастрофе»: важны replication, fsync/confirm semantics, backup и отказоустойчивость инфраструктуры.

---

# 7. Многопоточность

## 7.1. Типы Executors и их проблемы

**Короткий ответ:** `Executor` отделяет отправку задачи от политики выполнения, `ExecutorService` добавляет lifecycle и `Future`. Тип пула выбирают по характеру нагрузки, очереди и требуемому backpressure.

### Основные варианты

- `newFixedThreadPool(n)` — фиксированное число workers, но стандартная очередь фактически неограниченная: при перегрузке растут память и latency.
- `newCachedThreadPool()` — переиспользует idle threads и может создать очень много потоков: опасно при медленном I/O/downstream.
- `newSingleThreadExecutor()` — последовательное выполнение; одна зависшая задача блокирует остальные.
- `newScheduledThreadPool(n)` — delayed/periodic tasks; исключение может прекратить дальнейшие запуски конкретной periodic task.
- `ForkJoinPool` / work-stealing — CPU-рекурсивные и мелкие независимые задачи; blocking I/O может исчерпать workers.
- virtual-thread-per-task executor — дешёвый поток на задачу для большого числа blocking I/O операций; это не ускоряет CPU и не отменяет лимиты downstream.

### Главные production-проблемы

- неограниченная очередь → OOM и огромный queueing delay;
- неограниченное число platform threads → memory/context switching exhaustion;
- отсутствие timeout/cancellation;
- исключения теряются внутри `Future`, если результат никто не проверил;
- пул не закрывается (`shutdown`/structured lifecycle);
- deadlock/starvation: задача в пуле ждёт другую задачу из того же исчерпанного пула;
- `ThreadLocal` протекает между задачами в pooled threads, если не очищать;
- один общий pool для независимых workloads создаёт noisy neighbor.

### Предпочтительная конфигурация platform pool

```java
ExecutorService executor = new ThreadPoolExecutor(
        8, 16,
        30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(500),
        new ThreadPoolExecutor.CallerRunsPolicy()
);
```

Bounded queue + rejection policy создают контролируемое поведение при saturation. `CallerRunsPolicy` может замедлить producer и дать backpressure, но применять её нужно с пониманием request thread latency.

Для CPU-bound приблизительная отправная точка — число cores; для I/O-bound потоков может быть больше, но размер определяется измерениями, временем ожидания и capacity downstream. Bulkhead-пулы полезны для изоляции внешних систем.

### Virtual threads

Подходят, когда код в основном блокируется на сетевом/дисковом I/O. Не надо превращать их в фиксированный pool ради ограничения concurrency — лимит внешнего ресурса задают semaphore/rate limiter/connection pool. Следить за pinning в отдельных сценариях `synchronized`/native calls и измерять.

## 7.2. Блокировки и lock-free алгоритмы в Java

### Что дают locks

Lock обеспечивает mutual exclusion и отношение happens-before: изменения до unlock видимы потоку после последующего lock того же монитора/lock.

- `synchronized` — intrinsic monitor, автоматическое освобождение при выходе/exception, reentrant.
- `ReentrantLock` — `tryLock`, timeout, interruptible lock, несколько `Condition`, optional fairness; обязательно `unlock()` в `finally`.
- `ReadWriteLock` — параллельные readers и exclusive writer; полезен только при подходящем профиле чтений и достаточно длинных critical sections.
- `StampedLock` — optimistic read, но не reentrant; требует аккуратной validation.
- `Semaphore` ограничивает число одновременных пользователей ресурса.

```java
lock.lock();
try {
    updateState();
} finally {
    lock.unlock();
}
```

### `volatile`

Обеспечивает visibility и ordering для чтения/записи переменной, но составная операция `count++` не становится атомарной. Для неё нужен lock или atomic.

### Lock-free

Алгоритм lock-free гарантирует системный прогресс: хотя бы один поток продвинется, даже если другой задержан. Обычно используется CAS (`compareAndSet`).

```java
AtomicInteger counter = new AtomicInteger();
counter.incrementAndGet();
```

Плюсы: нет блокировки OS monitor и convoy при коротких операциях. Минусы: retry under contention, CPU spinning, сложность доказательства корректности, ABA problem. `LongAdder` лучше масштабируется для счётчиков под высокой конкуренцией, но `sum()` не является атомарным snapshot для бизнес-инварианта.

Concurrent collections не означают атомарность нескольких вызовов:

```java
map.computeIfAbsent(key, this::load); // лучше, чем containsKey + put
```

**Ловушка:** lock-free не означает wait-free и не означает «всегда быстрее». Измерять нужно на реальной contention-нагрузке.

## 7.3. `Thread` и `Runnable`

- `Thread` — механизм/контекст выполнения: имя, priority, state, interruption.
- `Runnable` — задача без возвращаемого результата и checked exception в сигнатуре.
- `Callable<V>` возвращает результат и может бросить exception; выполняется через executor и даёт `Future<V>`.

```java
Runnable task = () -> processOrder();
executor.submit(task);
```

Предпочтительно реализовать задачу и передать её executor, а не наследовать `Thread`: задача отделена от планирования, легче тестируется, пул переиспользует ресурсы.

### Состояния потока

`NEW → RUNNABLE → BLOCKED/WAITING/TIMED_WAITING → TERMINATED`. Java `RUNNABLE` объединяет реально выполняемый и готовый к выполнению OS state.

### Interruption

Interrupt — кооперативный сигнал, не принудительное убийство. Blocking methods могут бросить `InterruptedException` и очистить flag. Если метод не может пробросить исключение, обычно восстанавливают флаг:

```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

`start()` запускает новый поток, прямой вызов `run()` выполняет код в текущем потоке. Один `Thread` нельзя `start()` дважды.

---

# 8. Тестирование

## 8.1. Unit-тестирование и принципы

**Короткий ответ:** unit test быстро и детерминированно проверяет небольшую единицу поведения в изоляции от медленных внешних систем. Он должен подтверждать наблюдаемое поведение, а не внутреннюю реализацию.

Хороший тест:

- быстрый и повторяемый;
- независим от порядка запуска;
- имеет ясную причину падения;
- использует Arrange–Act–Assert или Given–When–Then;
- проверяет один сценарий, но может содержать несколько assertions одного результата;
- покрывает happy path, boundaries и значимые errors;
- не mock-ает value objects и сам тестируемый класс.

```java
@Test
void appliesVipDiscount() {
    // given
    var service = new PricingService(orderRepository);
    given(orderRepository.find(42L)).willReturn(vipOrder());

    // when
    Money result = service.price(42L);

    // then
    assertThat(result).isEqualTo(money("90.00"));
}
```

Mocks полезны на границах для проверки результата/взаимодействия, но чрезмерные mocks делают тест связанным со структурой метода. Чаще проверяй output/state; interaction — когда сам вызов внешней границы и есть важное поведение.

## 8.2. Unit и Integration tests

| | Unit | Integration |
|---|---|---|
| Scope | класс/небольшой модуль | несколько реальных компонентов |
| Внешние системы | заменены fake/mock | реальная БД/broker/HTTP boundary |
| Скорость | миллисекунды | обычно медленнее |
| Что ловит | бизнес-ветки и edge cases | mapping, wiring, SQL, serialization, конфигурацию |

В Spring полезны slice tests:

- `@WebMvcTest` — web layer;
- `@DataJpaTest` — JPA/repository;
- `@JsonTest` — serialization;
- `@SpringBootTest` — полный контекст; web environment выбирается по задаче.

Не заменяй production PostgreSQL in-memory H2, если важны dialect, locks, JSONB, indexes или native SQL. Реальная совместимая БД через Testcontainers надёжнее.

Хорошая пирамида: много быстрых unit, меньше component/integration, ещё меньше end-to-end. Точные пропорции не догма — важны risk и feedback time.

## 8.3. Testcontainers

**Короткий ответ:** Testcontainers запускает одноразовые Docker-контейнеры зависимостей для integration tests: PostgreSQL, Kafka, Redis и т. д. Тест работает с настоящим продуктом нужной версии, а не приблизительной embedded-заменой.

```java
@Testcontainers
@SpringBootTest
class OrderRepositoryIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");
}
```

Преимущества:

- production-like dialect/protocol;
- изоляция и воспроизводимость локально/CI;
- можно тестировать migrations, constraints, transaction behavior;
- container lifecycle автоматизирован.

Практика:

- pin image version, не использовать `latest`;
- переиспользовать static container внутри test suite, чтобы не запускать на каждый метод;
- ждать readiness через корректный wait strategy, а не `sleep`;
- очищать данные между тестами через transaction rollback/truncate/schema strategy;
- migrations применять тем же механизмом, что production;
- CI runner должен иметь поддерживаемую container runtime среду.

**Ловушки:** Testcontainers — integration, не unit; он не моделирует всю production topology/нагрузку. Подмена Kafka generic container без официального модуля требует правильно рекламировать listeners.

## 8.4. TDD и BDD

### TDD

Цикл **Red → Green → Refactor**:

1. Написать минимальный падающий тест желаемого поведения.
2. Реализовать минимум для прохождения.
3. Улучшить дизайн, сохранив зелёные тесты.

TDD — техника разработки и design feedback, а не «сначала написать все тесты проекта». Она помогает создавать тестируемый API и маленькие шаги, но не заменяет архитектурное мышление и integration tests.

### BDD

BDD фокусируется на поведении, общем языке бизнеса и сценариях:

```gherkin
Given у VIP-клиента заказ на 100 евро
When рассчитывается цена
Then итоговая цена равна 90 евро
```

Given–When–Then можно применять и без Cucumber. BDD полезен, когда примеры требований обсуждают dev, QA и product. Плохой BDD копирует детали UI и создаёт хрупкие длинные сценарии.

## 8.5. Покрытие тестами и CI

Для JVM обычно используют **JaCoCo**; отчёты показывают instruction, branch, line, method и class coverage. SonarQube/SonarCloud могут агрегировать coverage и quality gates. Для mutation testing применяют PIT: он меняет код и проверяет, убьют ли тесты мутацию.

### Что означает coverage

Высокое покрытие доказывает только, что код выполнялся, но не что результат корректно проверен. `100% line coverage` может не проверить ветки, границы и assertions.

Хорошая политика:

- quality gate на **new/changed code**, чтобы legacy не блокировал улучшения;
- разумный branch coverage для бизнес-логики;
- исключения только для действительно технического кода, а не ради зелёного отчёта;
- отчёт публикуется в CI, PR получает status check;
- unit и integration tests могут быть разными jobs/stages; долгие наборы запускаются параллельно или по расписанию дополнительно.

**Ответ про проект:** «Да, мы пишем unit для бизнес-логики, slice/integration для Spring, БД и сообщений, а критичные flows закрываем end-to-end/contract tests. JaCoCo генерирует отчёт в CI, quality gate проверяет новое покрытие. Процент не является целью сам по себе — смотрим branch coverage и качество assertions».

---

# 9. Docker, Kubernetes, безопасность и IDE

## 9.1. Docker-контейнер и виртуальная машина

**Короткий ответ:** контейнер — изолированный процесс, использующий kernel хоста; VM виртуализирует аппаратную среду и запускает собственную гостевую ОС.

| | Container | Virtual machine |
|---|---|---|
| Kernel | общий с host | свой guest kernel |
| Старт | обычно быстро | обычно медленнее |
| Размер | image приложения и user space | образ целой ОС |
| Изоляция | namespaces/cgroups и security mechanisms | сильная гипервизорная граница |

Docker image — immutable layered template, container — запущенный экземпляр image с writable layer. Данные, которые должны пережить удаление container, хранят в volume/внешнем storage.

Контейнер не «лёгкая VM» в строгом смысле. Он разделяет kernel и требует security hardening: non-root user, минимальный image, read-only filesystem где возможно, limits, seccomp/capabilities, scanning и обновление base image.

## 9.2. Dockerfile и Docker Compose

**Dockerfile** — декларативный рецепт сборки image.

```dockerfile
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY app.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Каждая значимая инструкция формирует cacheable layer. Хорошая практика:

- multi-stage build, если компиляция происходит внутри Docker;
- точный и небольшой trusted base image;
- `.dockerignore`;
- non-root user;
- exec-form `ENTRYPOINT`, чтобы signals доходили процессу;
- секреты не помещать через `COPY`/`ARG` в image layers;
- сначала копировать редко меняющиеся dependency manifests, затем source — для cache.

**Docker Compose** описывает несколько services, networks, volumes, ports, env и dependencies в YAML. Полезен для локальной среды и простых multi-container запусков.

`depends_on` задаёт порядок/условия запуска в доступных режимах, но сам факт старта не всегда означает готовность приложения. Нужны healthcheck и retry подключения. Compose не заменяет Kubernetes для сложной orchestration production-кластера.

## 9.3. Kubernetes Pod

**Короткий ответ:** Pod — минимальная deployable единица Kubernetes: один или несколько тесно связанных containers, которые разделяют network namespace/IP и могут разделять volumes. Обычно один Pod содержит один основной application container плюс sidecar/init containers при необходимости.

Containers одного Pod общаются через `localhost`. Pod эфемерен: при пересоздании получает новую identity/IP, поэтому стабильный доступ даёт Service, а устойчивые данные — PersistentVolume/внешнее хранилище.

Важные элементы:

- requests/limits влияют на scheduling и resource control;
- readiness probe решает, получает ли Pod traffic;
- liveness probe решает, перезапустить ли container;
- startup probe защищает медленно стартующее приложение от ранней liveness;
- init containers выполняются до app containers.

**Ловушка:** container restart и Pod replacement — разные события. Pod не является долговечной «машиной».

## 9.4. ReplicaSet

**Короткий ответ:** ReplicaSet через reconciliation loop поддерживает заданное число Pod replicas, выбранных label selector. Если Pod исчез, ReplicaSet создаёт новый; если лишний — удаляет.

Напрямую ReplicaSet обычно не создают. Им управляет Deployment, который добавляет declarative rollout, rolling update, rollback и историю revisions.

Упрощённо:

```text
Deployment → ReplicaSet новой версии → Pods
           → старый ReplicaSet с 0 replicas для rollback history
```

ReplicaSet не реплицирует данные приложения и сам по себе не гарантирует, что Pod готов принимать traffic. Для этого нужны readiness probes и Service. Selector Deployment/ReplicaSet должен совпадать с labels template и не пересекаться случайно с чужими Pods.

## 9.5. SQL Injection

**Короткий ответ:** SQL injection возникает, когда непроверенные данные меняют структуру SQL-команды. Главная защита — parameterized queries/prepared statements, где код и данные передаются отдельно.

```java
// плохо
String sql = "SELECT * FROM users WHERE email = '" + email + "'";

// хорошо
jdbcTemplate.query("SELECT * FROM users WHERE email = ?", mapper, email);
```

JPA parameters тоже безопасны:

```java
em.createQuery("select u from User u where u.email = :email", User.class)
  .setParameter("email", email);
```

### Defense in depth

- allowlist для динамических identifiers/sort columns: placeholders обычно не параметризуют имя таблицы/колонки;
- минимальные DB privileges для application user;
- разные accounts для migrations и runtime;
- validation полезна для бизнес-правил, но не заменяет parameters;
- stored procedures безопасны только если внутри не собирают SQL строковой конкатенацией;
- логирование/monitoring и secret management;
- ORM не спасает, если native/JPQL query строится конкатенацией.

**Ловушка:** escaping вручную зависит от dialect/encoding и ненадёжнее binding parameters.

## 9.6. IDE и горячие клавиши

Хороший ответ должен быть личным и конкретным:

> «Основная IDE — IntelliJ IDEA. Использую keyboard-first navigation и refactoring, а не запоминаю расположение файлов. Мои частые сочетания: Search Everywhere, Find Action, Go to Class/File, Rename, Extract Method/Variable, Find Usages, Recent Files, Generate и Reformat Code. Keymap может отличаться между Windows/Linux и macOS».

Популярные сочетания IntelliJ IDEA для Windows/Linux default keymap:

| Действие | Клавиши |
|---|---|
| Search Everywhere | `Shift` дважды |
| Find Action | `Ctrl+Shift+A` |
| Go to Class | `Ctrl+N` |
| Go to File | `Ctrl+Shift+N` |
| Recent Files | `Ctrl+E` |
| Find Usages | `Alt+F7` |
| Rename | `Shift+F6` |
| Extract Variable | `Ctrl+Alt+V` |
| Extract Method | `Ctrl+Alt+M` |
| Generate | `Alt+Insert` |
| Reformat Code | `Ctrl+Alt+L` |
| Optimize Imports | `Ctrl+Alt+O` |
| Quick Fix | `Alt+Enter` |

Не обязательно перечислять много. Назови 5–7, которыми действительно пользуешься, и один пример: «через Find Usages проверяю влияние изменения контракта, затем Rename делает безопасный refactoring».

---

# 10. Чек-лист перед интервью

## 10.1. Самые вероятные follow-up вопросы

- Почему mutable key ломает поиск в `HashMap`?
- Почему treeification не происходит сразу при 8 элементах?
- Чем `orElse` отличается от `orElseGet`?
- Почему `List<Integer>` — не `List<Number>`?
- Почему `@Transactional` не срабатывает при self-invocation?
- Как пользователь переопределяет bean автоконфигурации starter?
- Как получить новый prototype при каждом вызове singleton?
- Почему `EAGER` не лечит N+1?
- Какие есть варианты кроме fetch join?
- Почему fetch join коллекции конфликтует с pagination?
- Когда выбрать optimistic, а когда pessimistic lock?
- Чем `Slice` дешевле `Page`?
- Где заканчивается exactly-once Kafka?
- Почему `acks=all` не означает exactly-once?
- Что будет, если consumers больше, чем partitions?
- Чем retention отличается от compaction?
- Почему durable queue недостаточно без persistent messages и confirms?
- Чем virtual threads отличаются от platform thread pool?
- Почему высокий coverage не доказывает качество тестов?
- Чем Pod отличается от container, а ReplicaSet — от Deployment?

## 10.2. Шаблон ответа о проектном опыте

> «На проекте задача была ___. Мы выбрали ___, потому что ___. Главный компромисс был ___. Чтобы контролировать риск, мы добавили ___. Результат измеряли по ___».

Пример:

> «Для событий заказов использовали Kafka с `orderId` как key, поэтому порядок сохранялся внутри заказа. Обработку сделали at-least-once, а consumer — идемпотентным через `eventId` и unique constraint. Это проще и надёжнее, чем обещать exactly-once между Kafka и PostgreSQL. Контролировали consumer lag, число retries и DLQ».

## 10.3. Что проговорить честно

- Разделяй «использовал сам» и «знаю концептуально».
- Называй конкретную production-версию Java, Spring Boot, БД и Kafka, с которой работал.
- Preview/incubator Java features не выдавай за стабильные.
- Не обещай абсолютных гарантий: «exactly once», «никогда не потеряется», «индекс всегда ускоряет» требуют границ и условий.
- Если не помнишь флаг/аннотацию, объясни принцип и способ проверки. Это лучше уверенной ошибки.

## 10.4. Быстрый прогон за 15 минут

1. За 2 минуты расскажи `HashMap`: hash → bucket → collision → tree → resize → contract.
2. За 1 минуту — PECS и `Optional.orElseGet`.
3. За 2 минуты — IoC, constructor injection, AOP proxy/self-invocation.
4. За 2 минуты — Boot autoconfiguration и собственный starter.
5. За 3 минуты — indexes, N+1, locks, pagination.
6. За 2 минуты — Kafka partition/order/offset/group/retention/guarantees.
7. За 1 минуту — executors и bounded queue.
8. За 1 минуту — unit/integration/Testcontainers/coverage.
9. За 1 минуту — Docker vs VM, Pod, ReplicaSet.

---

## Короткая памятка в одну строку

**HashMap:** хороший immutable key → **PECS:** producer extends, consumer super → **Spring:** constructor DI, proxy-boundaries → **Hibernate:** fetch под use case, не EAGER везде → **Kafka:** порядок в partition, at-least-once требует идемпотентности → **Executors:** bounded resources → **Tests:** поведение важнее процента → **Containers/K8s:** container — процесс, Pod — единица размещения, Deployment управляет rollout.
