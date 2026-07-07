# ТИ-1 IBS — подготовка по фактической структуре собеса

Источники: скоркарты двух коллег (Вадим — задача «Получение имени пользователя через REST», Миша — «Обновление статуса заказа updateOrderStatus») + фидбек Миши. Дополняется после инфы от второго.

---

## 0. Разведданные: формат и выводы

**Формат:** 3 этапа за ~1 час. (1) live code review маленького Spring-сервиса, (2) теория поверхностно, но широко — по всем 10 секциям скоркарты, (3) рассказ о проекте. **Фулл-тайм шаринг экрана** — никаких шпаргалок на втором мониторе, всё из головы, проговаривая вслух.

**Скоринг:** 10 секций, 0–4 каждая (insufficient / junior / regular / senior), отдельно средняя по задаче и по теории. Цель: 3.0+ по всем секциям, 3.5+ по коду. Вадим: задача 4.00, теория 3.72 → почти все секции senior. Миша: задача 3.33, теория 3.15.

**Где просели коллеги (→ туда будут копать и у тебя):**

| Секция | Вадим | Миша | Вывод |
|---|---|---|---|
| 8. Многопоточность (+ GC, JVM) | 3.00 | 3.00 | Оба просели. Интервьюер копает CAS и сборщики кроме G1 — раздел 8 этого дока учить в первую очередь |
| 3. Алгоритмы, структуры, паттерны, ООП | 4.00 | 2.67 | У Миши «не хватало глубины по паттернам» — раздел 3 |
| 9. Тестирование | 3.00 | 3.25 | Подтянуть формулировки |
| 1. Coding task | 4.00 | 3.33 | Формат — code review, известны конкретные баги (раздел 1) |

**Конкретные темы, которые Миша запомнил как «докопались»:** CAS, типы индексов в БД, сборщики мусора кроме G1, паттерны проектирования. Всё разобрано ниже глубоко.

**Что оценили позитивно у Миши (копируй поведение):** обосновывал решения, уточнял бизнес-требования перед реализацией. Т.е. на code review сначала вопросы («какие статусы валидны? может ли заказ менять любой юзер?»), потом правки.

---

## 1. Coding task: live code review

### 1.1 Методика (проговаривай в этом порядке)

1. **Прочитать код целиком молча 30–60 сек**, потом: «Пройдусь сверху вниз: сначала корректность и ошибки, потом дизайн, потом что бы добавил».
2. **Уточнить бизнес-контекст** (то, за что похвалили Мишу): допустимые переходы статусов, кто имеет право менять, что при конкурентном изменении.
3. Порядок разбора: корректность/NPE → инжекция зависимостей → типизация (enum vs строки) → обработка ошибок → валидация существования сущностей → транзакции → конкурентность → API-слой → тесты.
4. Каждую правку — с обоснованием «почему», не просто «так лучше».

### 1.2 Задача Миши: updateOrderStatus — реконструкция и разбор

Код на входе был примерно такой (по описанию: if-else со стрингами, field injection, нет Optional, нет custom exception, нет проверок существования):

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    public void updateOrderStatus(Long orderId, Long userId, String status) {
        Order order = orderRepository.findById(orderId).get();
        if (status.equals("NEW")) {
            order.setStatus("NEW");
        } else if (status.equals("PAID")) {
            order.setStatus("PAID");
        } else if (status.equals("SHIPPED")) {
            order.setStatus("SHIPPED");
        } else {
            throw new IllegalArgumentException("Unknown status");
        }
        orderRepository.save(order);
    }
}
```

**Полный список замечаний (проговори все, даже очевидные):**

1. **Field injection через @Autowired** → constructor injection: поля final, зависимости обязательны и видны, тестируется без Spring, циклы падают на старте. С одним конструктором аннотация не нужна.
2. **`findById(...).get()`** — NoSuchElementException на пустом Optional. → `orElseThrow(() -> new OrderNotFoundException(orderId))`.
3. **Нет проверки существования user** и того, что заказ принадлежит этому пользователю / у пользователя есть право менять статус.
4. **Строки вместо enum** — magic strings, опечатки не ловятся компилятором. → `enum OrderStatus`, в entity `@Enumerated(EnumType.STRING)` (ORDINAL ломается при изменении порядка констант).
5. **if-else цепочка** → как минимум switch по enum; правильнее — **валидация переходов** (стейт-машина): из NEW можно в PAID, из PAID в SHIPPED, из SHIPPED — никуда. Сейчас код позволяет SHIPPED → NEW.
6. **IllegalArgumentException** как бизнес-ошибка → кастомные исключения (OrderNotFoundException, InvalidStatusTransitionException) + маппинг в HTTP-статусы через @RestControllerAdvice (404 / 409 / 400).
7. **Нет @Transactional** — read-modify-write должен быть атомарным.
8. **Конкурентность**: два параллельных запроса на один заказ → lost update. → `@Version` (optimistic lock) или пессимистичная блокировка при горячих заказах.
9. **`status.equals(...)` упадёт NPE при status == null** — если оставлять строки, то `"NEW".equals(status)`; но правильный фикс — enum на уровне DTO + Bean Validation.
10. Мелочи: нет логирования, метод void — стоит вернуть обновлённое состояние/DTO, нет тестов.

**Целевой вид (напиши по памяти минимум дважды до собеса):**

```java
public enum OrderStatus {
    NEW, PAID, SHIPPED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        NEW, Set.of(PAID),
        PAID, Set.of(SHIPPED),
        SHIPPED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED.get(this).contains(target);
    }
}

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(order.getStatus(), newStatus);
        }
        order.setStatus(newStatus);
        return OrderDto.from(order); // dirty checking сохранит сам, save не обязателен
    }
}

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(InvalidStatusTransitionException.class)
    ProblemDetail conflict(InvalidStatusTransitionException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
```

Бонус-фразы на senior: «в entity добавил бы @Version против lost update», «на контроллере enum в DTO + @Valid — невалидный статус отсечётся до сервиса», «переходы статусов покрыл бы параметризованным unit-тестом».

### 1.3 Задача Вадима: «Получение имени пользователя через REST»

Скорее всего ревью REST-клиента или контроллера. Чек-лист под этот вариант:

- **RestTemplate без таймаутов** — дефолт бесконечный: настроить connect/read timeout через factory; упомянуть, что RestTemplate в maintenance, новый код — на RestClient/WebClient.
- Нет обработки ошибок HTTP (4xx/5xx → RestClientException): try/catch либо errorHandler; ретраи только на идемпотентные вызовы, с backoff.
- URL конкатенацией + без энкодинга → UriComponentsBuilder / uriVariables.
- Ответ в Map/String вместо типизированного DTO.
- NPE на отсутствующих полях ответа → Optional/валидация.
- Хардкод URL → конфигурация @ConfigurationProperties.
- Блокирующий вызов внутри @Transactional — держит соединение БД (см. основной док, R2).
- Контроллер: @PathVariable без валидации, отдача entity наружу вместо DTO, 200 вместо 404 при отсутствии.

### 1.4 Универсальный чек-лист review (держи в голове как скелет)

Инжекция → Optional/NPE → enum вместо строк → кастомные исключения + advice → проверки существования и прав → валидация входа (@Valid) → @Transactional → конкурентность (@Version) → DTO на границе → логирование → тесты. Одиннадцать пунктов — прогони любой код по ним, и 4.00 по задаче реален.

### 1.5 Прогноз: какие ещё варианты дадут (та же рука)

Обе известные задачи — один шаблон: Spring-сервис на 15–30 строк, один метод, 5–8 заложенных багов из фиксированного набора (DI, Optional, строки vs enum, исключения, транзакции, конкурентность). Ниже — 5 вариантов, закрывающих вероятное пространство. Уверенность: перевод денег и @Transactional-фрагменты — высокая (финтех-заказчик + такие дают чаще всего); listener и @Scheduled — средняя (ровно профиль вакансии: event-ы и шедулеры); N+1 и потоконебезопасный синглтон — классика жанра, средняя.

### 1.6 Вариант «перевод денег» — @Transactional-минное поле (учить первым)

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

### 1.7 Вариант «найди, почему @Transactional не работает» — 5 мини-фрагментов

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

### 1.8 Вариант «Kafka listener» (профиль вакансии: обработка event-ов)

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

### 1.9 Вариант «JPA-сервис с N+1» (твоя коронка — расскажешь боевой кейс)

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

### 1.10 Вариант «потоконебезопасный singleton-бин»

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

### 1.11 Вариант «@Scheduled джоба» (профиль вакансии: шедулеры)

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

### 1.12 Банк багов: признак → диагноз (быстрый скан любого кода)

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

## 2. Java Core (секция 2 скоркарты)

Полные ответы — в основном доке (interview_prep_ibs_cfa.md, раздел 2). Топ, что реально спрашивают на этом ТИ, — уровень «поверхностно, но чётко»:

- equals/hashCode контракт + что сломается в HashMap.
- String: почему immutable, pool, StringBuilder.
- Integer cache, автобоксинг-ловушки.
- Checked vs unchecked, try-with-resources.
- final/static; интерфейс vs абстрактный класс.
- Java 8: лямбды, Stream (map/flatMap, groupingBy), Optional, default methods.
- Иммутабельный класс / records.
- Generics + erasure (PECS — если докопаются).
- Передача по значению.

Новое к добавлению (JVM-часть у них живёт в секции 8 — см. раздел 8 ниже: class loading, JIT, память).

---

## 3. Алгоритмы, структуры данных, паттерны, ООП (у Миши 2.67 — приоритет)

### 3.1 Сложность операций (зазубрить таблицу)

| Структура | Доступ | Поиск | Вставка | Удаление |
|---|---|---|---|---|
| ArrayList | O(1) | O(n) | O(1) амортиз. в конец / O(n) в середину | O(n) |
| LinkedList | O(n) | O(n) | O(1) у известного узла | O(1) у узла |
| HashMap/HashSet | — | O(1) avg, O(log n) worst (tree bins) | O(1) avg | O(1) avg |
| TreeMap/TreeSet | — | O(log n) | O(log n) | O(log n) |
| ArrayDeque | O(1) с концов | O(n) | O(1) с концов | O(1) с концов |
| PriorityQueue | O(1) peek | O(n) | O(log n) | O(log n) poll |

Плюс: бинарный поиск O(log n) на отсортированном, сортировки O(n log n) (Arrays.sort: primitives — dual-pivot quicksort, objects — TimSort, стабильная).

### 3.2 ООП — формулировки без воды

- **Инкапсуляция** — состояние скрыто, доступ через контракт (private поля + инварианты в методах).
- **Наследование** — переиспользование через is-a. **Композиция предпочтительнее**: наследование жёстко связывает с реализацией родителя (fragile base class), композиция гибче и тестируемее.
- **Полиморфизм** — один контракт, разные реализации; динамическая диспетчеризация по фактическому типу.
- **Абстракция** — интерфейс отделён от реализации.
- Overloading vs overriding: перегрузка резолвится в compile-time по статическим типам аргументов; переопределение — в runtime; при override можно сузить checked-исключения и вернуть ковариантный тип, нельзя сузить видимость.

### 3.3 Паттерны — минимум с примерами из JDK/Spring (сюда докапывались)

**Порождающие:**
- **Singleton** — один экземпляр. Spring beans (per-container). Реализации руками — спросят, напиши обе:

```java
// 1) Holder idiom — ленивый, потокобезопасный за счёт class loading
class Singleton {
    private Singleton() {}
    private static class Holder { static final Singleton I = new Singleton(); }
    public static Singleton getInstance() { return Holder.I; }
}

// 2) Double-checked locking — обязательно volatile
class Singleton {
    private static volatile Singleton instance;
    private Singleton() {}
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) instance = new Singleton();
            }
        }
        return instance;
    }
}
```
Почему volatile: без него возможна публикация недоконструированного объекта (переупорядочивание записи ссылки и инициализации полей). Ещё вариант — enum (защита от рефлексии/сериализации).

- **Factory Method** — создание делегировано подклассам/методу: `Collections.unmodifiableList`, BeanFactory. **Abstract Factory** — семейство связанных объектов (ConnectionFactory). Разница — один продукт vs семейство.
- **Builder** — пошаговая сборка сложного/иммутабельного объекта: StringBuilder, HttpRequest.newBuilder(), Lombok @Builder.
- **Prototype** — клонирование (clone/copy-конструктор). Редко нужен, знать имя.

**Структурные:**
- **Adapter** — привести чужой интерфейс к нужному: `Arrays.asList`, InputStreamReader (byte→char).
- **Decorator** — обернуть, добавив поведение, интерфейс тот же: `new BufferedInputStream(new FileInputStream(...))` — классика.
- **Proxy** — тот же интерфейс, контроль доступа/ленивость/добавочная логика без изменения объекта: Spring AOP-прокси (@Transactional), Hibernate lazy proxies.
- **Facade** — простой вход в сложную подсистему: твой сервисный слой над репозиториями и клиентами.
- **Composite** — дерево «часть-целое» с единым интерфейсом (UI-компоненты, файловая система).

Вопрос-ловушка «Decorator vs Proxy vs Adapter»: Adapter меняет интерфейс; Decorator сохраняет интерфейс и наращивает функциональность (обёртки компонуются); Proxy сохраняет интерфейс и контролирует доступ к объекту (клиент может не знать о прокси).

**Поведенческие:**
- **Strategy** — взаимозаменяемые алгоритмы: Comparator, твоя Map<EventType, Handler>.
- **State** — поведение зависит от внутреннего состояния, объект «меняет класс»: стейт-машина статусов заказа из задачи. Разница со Strategy: стратегию выбирает клиент, состояние переключается само по ходу жизни объекта.
- **Observer** — подписка на события: Spring ApplicationEvent/@EventListener, listeners.
- **Template Method** — скелет алгоритма в базовом классе, шаги в наследниках: JdbcTemplate (шаблон + твои callback'и — строго говоря, гибрид со Strategy, так и скажи).
- **Chain of Responsibility** — цепочка обработчиков: Spring Security filter chain, servlet filters.
- **Command** — запрос как объект: Runnable/Callable.
- **Iterator** — обход без раскрытия структуры: java.util.Iterator.

Готовь 2–3 «из своего кода»: Strategy для обработчиков auction events, Facade — сервисный слой, Observer — доменные события + RabbitMQ.

---

## 4. Spring (+ Boot)

База — основной док, раздел 7. Что добавить/акцентировать под этот собес:

**Q: Почему field injection плохо? (всплывёт после code review)**
Нельзя сделать поле final; зависимости скрыты (класс легко раздувается); тест без контейнера требует рефлексии; NPE при ручном создании. Constructor injection решает всё; Spring сам рекомендует.

**Q: ApplicationContext vs BeanFactory?**
BeanFactory — базовый контейнер (ленивое создание). ApplicationContext — надстройка: eager-инициализация singleton'ов, события, i18n, ресурсы, интеграция с AOP. На практике всегда ApplicationContext.

**Q: Как летит HTTP-запрос в Spring MVC?**
DispatcherServlet → HandlerMapping (какой контроллер) → HandlerAdapter (вызов) → ArgumentResolver'ы (@PathVariable, @RequestBody через HttpMessageConverter/Jackson) → метод → возврат → MessageConverter → ответ. Фильтры сервлета (в т.ч. Security) — до DispatcherServlet, интерцепторы — вокруг хендлера.

**Q: @Controller vs @RestController?**
@RestController = @Controller + @ResponseBody: возврат сериализуется в тело, а не резолвится как view.

**Q: Жизненный цикл бина, прокси, @Transactional-ловушки, автоконфигурация** — рассказать по основному доку без запинки; это ядро секции 4.

**Q: Spring Data JPA (живёт в секции 5, но готовь со Spring):**
- JpaRepository ⊃ PagingAndSortingRepository ⊃ CrudRepository.
- Query methods: findByStatusAndCreatedAtAfter — парсинг имени; @Query для сложного (JPQL/native); @Modifying для update/delete.
- Pageable/Sort; проекции (интерфейсные/DTO) — не тянуть entity, где не нужно.
- deleteById vs deleteAllInBatch; saveAll и батчинг (hibernate.jdbc.batch_size).

---

## 5. Базы данных, SQL, Hibernate, JPA (докапывались до типов индексов)

### 5.1 Типы индексов PostgreSQL — развёрнуто (это спрашивали)

| Тип | Что внутри | Когда |
|---|---|---|
| **B-tree** (дефолт) | сбалансированное дерево | =, <, >, BETWEEN, ORDER BY, LIKE 'abc%' (по префиксу). 95% случаев |
| **Hash** | хэш-таблица | только =. Почти всегда проигрывает B-tree, ниша |
| **GIN** | инвертированный индекс | массивы, jsonb (@>, ?), полнотекст (tsvector), pg_trgm для LIKE '%x%'. Быстрый поиск, дорогая запись |
| **GiST** | обобщённое дерево | геометрия, диапазоны (range types), KNN «ближайшие», exclusion constraints |
| **SP-GiST** | префиксные/квадродеревья | несбалансированные структуры: тексты по префиксу, IP |
| **BRIN** | min/max по блокам | огромные append-only таблицы с корреляцией по порядку (timestamp в логах): крошечный размер |

Разновидности поверх (обязательно назвать): **unique**, **составной** (работает по leading-колонкам), **partial** (`WHERE status = 'ACTIVE'` — маленький и горячий), **covering** (`INCLUDE` → index-only scan), **функциональный** (`ON lower(email)`), создание **CONCURRENTLY** (без блокировки записи на проде).

Когда индекс НЕ используется: функция над колонкой без функционального индекса, неявный каст, низкая селективность (планировщик выберет Seq Scan — и будет прав), LIKE '%x', не-leading колонка составного. Проверка: EXPLAIN (ANALYZE, BUFFERS).

Минусы индексов: замедляют запись (поддержка на каждый INSERT/UPDATE), место, bloat.

### 5.2 Остальное по секции — из основного дока, разделы 5–6

Изоляция + специфика PG, MVCC/VACUUM, оптимистичные/пессимистичные блокировки, N+1 (твоя история 8–11с → 180мс — расскажи обязательно), entity states, LazyInitializationException, кэши Hibernate, каскады.

---

## 6. REST, микросервисы, межсетевое взаимодействие

**Q: Идемпотентность HTTP-методов?**
GET/HEAD — safe + идемпотентны; PUT/DELETE — идемпотентны (повтор = тот же результат); POST — нет; PATCH — не гарантированно. Поэтому ретраи из коробки — только на идемпотентные.

**Q: PUT vs PATCH vs POST?**
POST — создать (сервер выдаёт id) / произвольное действие; PUT — полная замена ресурса по известному URI; PATCH — частичное обновление. updateOrderStatus — семантически PATCH /orders/{id}/status.

**Q: Коды ответов — назвать без запинки:**
200 OK, 201 Created (+Location), 204 No Content; 400 (невалидный запрос), 401 (не аутентифицирован), 403 (нет прав), 404, 409 Conflict (конфликт состояния — недопустимый переход статуса!), 422 (семантика); 500, 502/503/504 (шлюз/недоступность/таймаут).

**Q: Уровни зрелости REST (Richardson)?**
0 — один endpoint-«свалка» (RPC over HTTP); 1 — ресурсы/URI; 2 — HTTP-глаголы + коды (норма индустрии); 3 — HATEOAS (гиперссылки в ответах, редко на практике).

**Q: Версионирование API?**
URI (/v1/... — проще всего), заголовок, media type. Главное — обратная совместимость: добавлять поля можно, удалять/переименовывать — версия.

**Q: REST vs gRPC vs события?**
REST — универсально, человекочитаемо; gRPC — бинарный protobuf, HTTP/2, стриминг, контракт-first, ниже латентность — внутренняя синхронка; события (Kafka/Rabbit) — асинхронная развязка. Выбор: команды с немедленным ответом — sync; факты — события.

**Q: Таймауты/ретраи/устойчивость межсервисного вызова?**
Всегда connect+read timeout; retry с exponential backoff + jitter и только на идемпотентное; circuit breaker (resilience4j); fallback. Каскадные отказы — почему таймаут обязателен.

**Q: HTTP базово?**
Stateless, keep-alive; HTTP/1.1 — текстовый, head-of-line blocking; HTTP/2 — мультиплексирование по одному соединению, бинарные фреймы. HTTPS = TLS-рукопожатие → симметричный сеансовый ключ.

---

## 7. Брокеры сообщений (Kafka, Rabbit, JMS)

Kafka — глубоко в основном доке, раздел 8 (партиции, consumer groups, семантики, offset'ы, идемпотентный консюмер, outbox, ребаланс). Дополнение по RabbitMQ и JMS, раз они в названии секции:

**RabbitMQ (у тебя боевой — спросят):**
- AMQP: producer → **exchange** → binding'и → очереди → consumer.
- Типы exchange: **direct** (точный routing key), **topic** (маски orders.*.paid), **fanout** (всем), headers.
- Подтверждения: consumer ack/nack (+requeue), publisher confirms; **prefetch** (QoS) — сколько неподтверждённых на консюмера, защита от перегруза.
- Надёжность: durable queue + persistent message; **DLX** (dead letter exchange) при nack/TTL/переполнении; TTL, приоритеты, delayed messages (плагин).
- На AuctionPort: bid-события через RabbitMQ — вспомни топологию (какой exchange, как роутились).

**JMS — уровень определения:**
Спецификация Java API для брокеров (Jakarta Messaging). Две модели: point-to-point (Queue — одно сообщение одному консюмеру) и pub/sub (Topic — всем подписчикам). Реализации: ActiveMQ Artemis, IBM MQ. Kafka — не JMS. Этого достаточно.

**Kafka vs RabbitMQ** — сравнение из основного дока: лог с retention/replay vs очереди с роутингом; порядок по партициям vs гибкая маршрутизация.

---

## 8. Многопоточность + GC + JVM (оба коллеги 3.00 — главный кандидат на «докопаться»)

Треды/пулы/synchronized/volatile/CompletableFuture — основной док, раздел 4. Здесь — то, чем добивали: **CAS** и **сборщики кроме G1**.

### 8.1 CAS — развёрнутый ответ (спрашивали дословно)

Compare-And-Swap — атомарная процессорная инструкция (x86: cmpxchg): «запиши new, если в ячейке всё ещё expected; верни, получилось ли». На ней построены атомики и почти вся lock-free синхронизация (включая захват локов внутри AQS/ReentrantLock).

```java
// как устроен incrementAndGet — retry-цикл
public final int incrementAndGet() {
    int prev, next;
    do {
        prev = get();          // volatile read
        next = prev + 1;
    } while (!compareAndSet(prev, next)); // CAS, при проигрыше гонки — повтор
    return next;
}
```

Свойства: без блокировок → нет дедлоков и переключений контекста; под низкой конкуренцией быстрее локов. Минусы: под высокой конкуренцией потоки крутятся в retry (contention) → для счётчиков **LongAdder** (страйпинг: массив ячеек, сумма при чтении). **ABA-проблема**: значение A→B→A, CAS не заметит подмены — AtomicStampedReference (версия рядом со значением). Доступ в JDK: раньше Unsafe, сейчас VarHandle.

### 8.2 Сборщики мусора — все, не только G1 (спрашивали дословно)

База: generational hypothesis — большинство объектов умирает молодыми. Heap: young (eden + 2 survivor) и old. **Minor GC** — young, быстрый; **Major/Full GC** — old/весь heap. Любой сборщик имеет STW-паузы (хотя бы короткие), потоки останавливаются в safepoint'ах.

| Сборщик | Флаг | Суть | Когда |
|---|---|---|---|
| **Serial** | -XX:+UseSerialGC | один поток, полный STW | крошечные heap'ы, однопроцессорные контейнеры |
| **Parallel** | -XX:+UseParallelGC | STW, но многопоточно; максимальный throughput | батчи/оффлайн-джобы, где паузы не важны. Дефолт Java 8 |
| **CMS** | — | конкурентная очистка old gen | deprecated в 9, удалён в 14 — знать судьбу; страдал фрагментацией |
| **G1** | -XX:+UseG1GC | дефолт с Java 9. Heap = регионы (~1–32 МБ); young + mixed collections; конкурентная маркировка; цель по паузе (MaxGCPauseMillis, дефолт 200 мс); эвакуация регионов = компактификация; humongous-объекты (>50% региона) — отдельная боль | универсальный дефолт, средние/большие heap'ы |
| **ZGC** | -XX:+UseZGC | паузы < 1 мс независимо от размера heap (до ТБ): почти всё конкурентно, colored pointers + load barriers. С Java 21 — generational ZGC | low-latency сервисы, большие heap'ы; трейд-офф — CPU/throughput |
| **Shenandoah** | -XX:+UseShenandoahGC | аналог ZGC по целям (конкурентная компактификация), RedHat; нет в Oracle-сборках | то же, в OpenJDK-дистрибутивах |
| **Epsilon** | -XX:+UseEpsilonGC | не собирает вообще | бенчмарки, короткоживущие процессы |

Как выбрать (итоговая фраза): «дефолтный G1 покрывает большинство сервисов; жёсткие требования к латентности и большой heap — ZGC/Shenandoah; чистый throughput без требований к паузам — Parallel; маленький контейнер — Serial».

### 8.3 JVM-минимум к этой же секции

- **Память**: heap (young/old), stack на поток, Metaspace (метаданные классов, нативная память; PermGen удалён в 8), code cache (JIT). OOM-варианты: Java heap space / Metaspace / unable to create native thread.
- **Class loading**: Bootstrap → Platform → Application loader, **parent delegation** (сначала спросить родителя — защита от подмены java.lang.*). Этапы: loading → linking (verify, prepare, resolve) → initialization (static-блоки).
- **JIT**: старт в интерпретаторе → горячие методы компилируются (C1/C2, tiered), инлайнинг, эскейп-анализ (аллокация на стеке/скаляризация).
- **Диагностика**: jstack (thread dump — дедлоки, что делают потоки), jmap/jcmd (heap dump → Eclipse MAT), jstat -gc, GC-логи (-Xlog:gc*). Высокий CPU → thread dump ×3 и смотреть, где крутятся потоки.
- Флаги, которые стоит знать: -Xms/-Xmx, -XX:MaxMetaspaceSize, -XX:+HeapDumpOnOutOfMemoryError.

---

## 9. Тестирование

База — основной док, раздел 10 (пирамида, mock vs spy, slices, Testcontainers). Дополнить формулировками:

- Структура теста: **given / when / then** (arrange-act-assert); имя теста описывает сценарий: `updateStatus_shouldThrow_whenTransitionNotAllowed`.
- Что мокать: внешние границы (репозитории в unit-тесте сервиса, HTTP-клиенты, часы через Clock). Не мокать тестируемый класс и value-объекты.
- ArgumentCaptor: проверить, что ушло в зависимость (payload события).
- Параметризованные тесты (@ParameterizedTest + @CsvSource/@EnumSource) — идеально для матрицы переходов статусов из coding task, скажи об этом — свяжешь секции.
- Интеграционные: @DataJpaTest на реальном PostgreSQL через Testcontainers (H2 ≠ PG: типы, диалект); @WebMvcTest + MockMvc для контроллеров и advice.
- Покрытие: метрика, а не цель; ветвления бизнес-логики важнее геттеров.
- Тест транзакционности/конкурентности — честно: сложно, чаще проверяется дизайном (идемпотентность, @Version) + интеграционно.

---

## 10. Прочее: CI/CD, безопасность, Docker, UNIX, Git

### 10.1 Docker (Миша: спрашивали)

- **Image vs container**: образ — иммутабельные read-only слои; контейнер — запущенный экземпляр + writable-слой.
- **Слои и кэш**: каждая инструкция Dockerfile — слой; порядок важен — зависимости раньше кода:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Multi-stage** (выше): собрали с JDK+Maven, в рантайм — только JRE + jar. Меньше образ, меньше поверхность атаки.
- **ENTRYPOINT vs CMD**: entrypoint — что запускать, cmd — аргументы по умолчанию (переопределяются). Exec-форма `["java","-jar",...]` — процесс получает сигналы (graceful shutdown), shell-форма — нет.
- **JVM в контейнере**: JVM видит лимиты cgroups (UseContainerSupport, дефолт с 10+); задавать -XX:MaxRAMPercentage=75.0 вместо жёсткого -Xmx; лимит памяти контейнера = heap + metaspace + стеки + native.
- docker-compose — локальный стенд (app + postgres + kafka); volumes — персистентность; networks — сервисы видят друг друга по имени.
- Твой опыт: Docker на всех проектах, деплой в Kubernetes (AKS) — скажи.

### 10.2 CI/CD (твоя сильная зона — GitHub Actions + ArgoCD)

Пайплайн: push → build → unit/integration тесты → статанализ → сборка образа → push в registry → деплой. У тебя: GitHub Actions (CI) + **ArgoCD (GitOps: желаемое состояние кластера в git-репо, ArgoCD синхронизирует — деплой через PR, откат через revert)** — расскажи именно так, это звучит на senior. Стратегии выката: rolling (дефолт K8s), blue-green, canary — по одному предложению.

Kubernetes-минимум: pod → deployment (реплики, rolling update) → service (стабильный адрес) → ingress; liveness (перезапустить) vs readiness (не слать трафик) пробы; configmap/secret; requests/limits.

### 10.3 Безопасность (банковский заказчик — спросят)

- **SQL injection**: конкатенация SQL со входом → PreparedStatement/параметры JPA. Никогда строки в запрос.
- **Хранение секретов**: не в коде и не в git; env/Vault/K8s secrets; ротация. (Твой личный урок с токеном бота — не рассказывай, просто знай тему глубже других.)
- **JWT**: header.payload.signature; подпись HS256 (общий секрет) vs RS256 (пара ключей); проверять подпись, exp, issuer; в payload нет секретов (это base64, не шифрование); короткоживущий access + refresh.
- **Пароли**: только hash + salt, BCrypt/Argon2, никогда plain/MD5.
- **OWASP топ, назвать 4–5**: injection, broken authentication, broken access control (проверка прав на КАЖДЫЙ ресурс — привет проверке «заказ принадлежит юзеру» из coding task), sensitive data exposure, SSRF.
- **CSRF** — актуален для cookie-сессий; stateless JWT API — отключают; **CORS** — какие origin'ы могут дёргать API из браузера.
- Валидация входа на границе, rate limiting, принцип наименьших привилегий (учётка сервиса к БД — не superuser).

### 10.4 UNIX/Linux — практический минимум

```bash
tail -f app.log                     # хвост лога в реальном времени
grep -rn "OrderNotFound" logs/      # поиск по файлам, -i без регистра
less +F app.log                     # просмотр с follow
ps aux | grep java                  # процессы
top / htop                          # CPU/память живьём
free -h ; df -h                     # память / диск
ss -tlnp | grep 8080                # кто слушает порт (или lsof -i :8080)
kill -15 PID                        # SIGTERM (graceful), -9 только если завис
curl -v -X POST -H "Content-Type: application/json" -d '{"a":1}' url
journalctl -u myservice -f          # логи systemd-сервиса
find . -name "*.log" -mtime -1      # файлы за сутки
chmod 755 script.sh ; chown user:group f
```
Сценарий «на порту 8080 что-то висит, найди и убей»: `ss -tlnp | grep 8080` → PID → `kill -15`.

### 10.5 Git

- merge vs rebase: merge сохраняет историю с merge-коммитом; rebase переписывает поверх — линейная история; **не ребейзить опубликованные ветки**.
- reset (двигает ветку: --soft/--mixed/--hard) vs revert (новый коммит-отмена — для общих веток только revert).
- cherry-pick — перенос коммита; stash; feature branch → PR → review → squash/merge.

---

## 11. План на 3 дня до собеса

1. **День 1:** Раздел 1 (code review) — написать целевой OrderService по памяти 2 раза; раздел 8 (CAS + таблица GC) — до автоматизма. Это два известных «места раскопок».
2. **День 2:** Раздел 3 (паттерны: оба синглтона руками, Decorator/Proxy/Adapter, Strategy/State) + раздел 5.1 (таблица индексов).
3. **День 3:** Разделы 6, 7, 10 (REST-коды, RabbitMQ exchanges, Docker multi-stage, Linux-команды) + прогон слабых мест из основного дока (@Transactional-ловушки, ThreadPoolExecutor).

Во время собеса: экран шарится постоянно → проговаривай мысль вслух непрерывно, уточняй бизнес-требования до правок кода (за это хвалили), обосновывай каждое решение.
