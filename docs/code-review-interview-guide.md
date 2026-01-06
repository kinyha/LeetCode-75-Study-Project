# Code Review на собеседовании: Полный гайд

> Структурированный подход к задачам типа "найди ошибки", "сделай ревью", "отрефактори код"

---

## 1. Формат и ожидания интервьюера

### 1.1 Что оценивают

| Критерий | Что смотрят | Как проявляется |
|----------|-------------|-----------------|
| **Системность** | Есть ли у вас методичный подход | Последовательный обход кода, чеклист в голове |
| **Глубина** | Понимаете ли причины проблем | Объясняете "почему", а не просто "что" |
| **Приоритизация** | Умеете ли отличать критичное от minor | Сначала security/bugs, потом code style |
| **Коммуникация** | Как объясняете проблемы | Чётко, без воды, с примерами |
| **Решения** | Предлагаете ли исправления | Не просто "плохо", а "вот как надо" |

### 1.2 Типичный тайминг

```
┌─────────────────────────────────────────────────────┐
│  5-7 мин   │   Простая задача (1-2 проблемы)       │
│  10-15 мин │   Средняя задача (3-5 проблем)        │
│  15-20 мин │   Комплексная задача (5+ проблем)     │
└─────────────────────────────────────────────────────┘
```

### 1.3 Как структурировать ответ

**Формула ответа на каждую найденную проблему:**

```
1. ПРОБЛЕМА   → Что конкретно не так (цитата из кода)
2. ПОСЛЕДСТВИЯ → Что может случиться (конкретный сценарий)
3. РЕШЕНИЕ    → Как исправить (пример кода или подход)
```

**Пример хорошего ответа:**
> "Здесь строка формируется через конкатенацию с пользовательским вводом — это SQL injection.
> Злоумышленник может передать `'; DROP TABLE users; --` и удалить данные.
> Нужно использовать PreparedStatement с параметрами."

**Пример плохого ответа:**
> "Тут какая-то проблема с SQL, надо переписать."

---

## 2. Мега-чеклист: куда смотреть

### Приоритет проверки

```
🔴 КРИТИЧЕСКИЕ (блокеры)     → Ищем первыми
🟡 ФУНКЦИОНАЛЬНЫЕ (баги)     → Ищем вторыми
🟢 КАЧЕСТВО (code smells)    → Ищем в конце
```

---

### 2.1 🔴 Критические проблемы (блокеры)

#### Безопасность

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **SQL Injection** | `"SELECT * FROM x WHERE id = " + id` | `PreparedStatement` с `?` |
| **XSS** | `response.write(userInput)` | Экранирование, `StringEscapeUtils` |
| **Path Traversal** | `new File(baseDir + fileName)` | Валидация, `Path.normalize()` |
| **Command Injection** | `Runtime.exec("cmd " + param)` | Whitelist параметров |
| **Hardcoded secrets** | `password = "qwerty123"` | Environment variables, Vault |
| **Логирование паролей** | `log.info("User: " + user)` | Маскирование sensitive данных |

```java
// ❌ SQL Injection
String query = "SELECT * FROM users WHERE name = '" + name + "'";
jdbc.execute(query);

// ✅ Правильно
String query = "SELECT * FROM users WHERE name = ?";
jdbc.query(query, new Object[]{name}, rowMapper);
```

#### Ресурсы и утечки

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **Connection leak** | `conn = getConnection()` без close | `try-with-resources` |
| **Stream не закрыт** | `Files.lines(path)` без close | `try-with-resources` |
| **InputStream leak** | `new FileInputStream(f)` | `try-with-resources` |
| **ExecutorService leak** | `Executors.newFixedThreadPool()` | `shutdown()` в finally |

```java
// ❌ Resource leak
BufferedReader reader = new BufferedReader(new FileReader(file));
String line = reader.readLine();
// reader никогда не закрывается!

// ✅ Правильно
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    String line = reader.readLine();
}
```

#### Concurrency

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **Race condition** | `if (!map.contains(k)) map.put(k,v)` | `putIfAbsent()` или synchronized |
| **Non-atomic increment** | `counter++` в многопоточке | `AtomicInteger.incrementAndGet()` |
| **Double-checked locking** | `if (x == null) { synchronized... }` | + `volatile` |
| **Shared mutable state** | Поле без синхронизации | `volatile`, `Atomic*`, `synchronized` |
| **HashMap в многопоточке** | `private Map<> cache = new HashMap<>()` | `ConcurrentHashMap` |
| **Deadlock** | Разный порядок блокировок | Единый порядок lock |

```java
// ❌ Race condition (check-then-act)
if (!cache.containsKey(key)) {
    cache.put(key, computeValue()); // Два потока могут войти сюда одновременно
}

// ✅ Правильно
cache.computeIfAbsent(key, k -> computeValue());
```

#### NPE и исключения

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **NPE** | `obj.method()` без проверки | `Objects.requireNonNull()`, Optional |
| **Optional.get()** | `.get()` без проверки | `.orElse()`, `.orElseThrow()` |
| **Пустой catch** | `catch (Exception e) { }` | Логирование, rethrow |
| **Catch Exception** | `catch (Exception e)` | Конкретные типы исключений |
| **Throws Exception** | `public void foo() throws Exception` | Конкретные типы |

```java
// ❌ Пустой catch — маскирует ошибки
try {
    process();
} catch (Exception e) {
    // Ничего
}

// ✅ Правильно
try {
    process();
} catch (ProcessingException e) {
    log.error("Processing failed: {}", e.getMessage(), e);
    throw new ServiceException("Unable to process", e);
}
```

---

### 2.2 🟡 Функциональные баги

#### Сравнение объектов

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **== вместо equals** | `str1 == str2` | `Objects.equals(str1, str2)` |
| **Integer cache** | `Integer a == Integer b` (> 127) | `.equals()` или `.intValue()` |
| **equals без hashCode** | Переопределён только equals | Переопределить оба |
| **Mutable key в HashMap** | `map.put(mutableObj, val)` | Immutable ключи |

```java
// ❌ Работает только для -128..127
Integer a = 200;
Integer b = 200;
if (a == b) { // false!
    // ...
}

// ✅ Правильно
if (a.equals(b)) { // true
    // ...
}
```

#### Граничные случаи

| Проблема | Что проверять |
|----------|---------------|
| **Пустая коллекция** | `list.isEmpty()` перед `list.get(0)` |
| **null параметры** | Валидация в начале метода |
| **Off-by-one** | `<` vs `<=`, `i = 0` vs `i = 1` |
| **Отрицательные числа** | Деление, массивы с отрицательным индексом |
| **Переполнение** | `int` арифметика → `long` |
| **Пустая строка** | `""` vs `null` |

```java
// ❌ ArrayIndexOutOfBoundsException при numbers.length == 0
public int findMax(int[] numbers) {
    int max = numbers[0]; // Упадёт на пустом массиве!
    for (int n : numbers) {
        if (n > max) max = n;
    }
    return max;
}

// ✅ Правильно
public int findMax(int[] numbers) {
    if (numbers == null || numbers.length == 0) {
        throw new IllegalArgumentException("Array must not be empty");
    }
    // ...
}
```

#### Коллекции

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **ConcurrentModification** | `for (x : list) list.remove(x)` | `Iterator.remove()` или `removeIf()` |
| **Arrays.asList immutable** | `Arrays.asList(...).add(x)` | `new ArrayList<>(Arrays.asList(...))` |
| **Unmodifiable возврат** | `return Collections.unmodifiableList(list)` | Документация или копия |

```java
// ❌ ConcurrentModificationException
for (String item : list) {
    if (item.startsWith("a")) {
        list.remove(item);
    }
}

// ✅ Правильно
list.removeIf(item -> item.startsWith("a"));
```

---

### 2.3 🟢 Code Smells и рефакторинг

#### Структурные проблемы

| Code Smell | Признаки | Решение |
|------------|----------|---------|
| **if-else chain** | 5+ веток if-else на типах | Strategy/Polymorphism |
| **Switch на enum** | `switch(type) { case A: ... }` | Метод в enum |
| **God Method** | Метод > 50 строк | Extract Method |
| **Magic Numbers** | `if (status == 3)` | Константы с именами |
| **Feature Envy** | Метод использует чужие поля | Переместить метод |
| **Deep Nesting** | 4+ уровней вложенности | Early return, extract |

```java
// ❌ if-else chain
public void process(Code code) {
    if (code.getType() == CodeType.A) {
        doA();
    } else if (code.getType() == CodeType.B) {
        doB();
    } else if (code.getType() == CodeType.C) {
        doC();
    }
    // ... ещё 10 веток
}

// ✅ Strategy pattern
public void process(Code code) {
    CodeHandler handler = handlers.get(code.getType());
    handler.handle(code);
}

// Или метод в enum
public enum CodeType {
    A { void handle() { doA(); } },
    B { void handle() { doB(); } };
    abstract void handle();
}
```

#### Нейминг и стиль

| Проблема | Пример | Правильно |
|----------|--------|-----------|
| **Неинформативные имена** | `int x`, `String s` | `int count`, `String userName` |
| **Boolean методы** | `status()` | `isActive()`, `hasPermission()` |
| **Венгерская нотация** | `strName`, `intCount` | `name`, `count` |
| **Класс с маленькой буквы** | `class user` | `class User` |

---

### 2.4 🔵 Spring-специфичные проблемы

#### @Transactional pitfalls

| Проблема | Паттерн в коде | Почему не работает | Как исправить |
|----------|----------------|--------------------| --------------|
| **Private метод** | `@Transactional private void...` | Proxy не перехватывает | Сделать public |
| **Self-invocation** | `this.transactionalMethod()` | Вызов идёт мимо proxy | Инжектить себя или выделить класс |
| **Внешний вызов** | HTTP/file внутри транзакции | Блокирует соединение | Вынести за транзакцию |
| **Checked exception** | `throws IOException` | Не откатывает по умолчанию | `rollbackFor = Exception.class` |
| **REQUIRES_NEW в том же классе** | `@Transactional(propagation=REQUIRES_NEW)` | Self-invocation | Выделить в другой сервис |

```java
// ❌ Self-invocation — @Transactional не работает
@Service
public class UserService {

    public void createUser(User user) {
        this.saveWithTransaction(user); // Вызов идёт мимо proxy!
    }

    @Transactional
    public void saveWithTransaction(User user) {
        userRepository.save(user);
    }
}

// ✅ Решение 1: Выделить в отдельный сервис
@Service
public class UserService {
    @Autowired
    private UserTransactionService txService;

    public void createUser(User user) {
        txService.saveWithTransaction(user);
    }
}

// ✅ Решение 2: Инжектить себя (self-injection)
@Service
public class UserService {
    @Autowired
    private UserService self;

    public void createUser(User user) {
        self.saveWithTransaction(user);
    }
}
```

#### Bean lifecycle

| Проблема | Паттерн в коде | Как исправить |
|----------|----------------|---------------|
| **Prototype в Singleton** | `@Autowired Prototype proto` | Provider, ObjectFactory, @Lookup |
| **Circular dependency** | A → B → A | @Lazy, рефакторинг |
| **@Async без @EnableAsync** | `@Async void process()` | Добавить `@EnableAsync` |
| **@PostConstruct + @Transactional** | Транзакция в PostConstruct | ApplicationRunner, @EventListener |

```java
// ❌ Prototype инжектится в Singleton один раз
@Service  // Singleton по умолчанию
public class OrderService {
    @Autowired
    private OrderProcessor processor; // Prototype — но создастся только 1 раз!

    public void process(Order order) {
        processor.process(order); // Всегда один и тот же объект
    }
}

// ✅ Правильно — Provider/ObjectFactory
@Service
public class OrderService {
    @Autowired
    private ObjectFactory<OrderProcessor> processorFactory;

    public void process(Order order) {
        OrderProcessor processor = processorFactory.getObject(); // Новый каждый раз
        processor.process(order);
    }
}
```

---

## 3. Топ-20 типичных проблем

| # | Категория | Проблема | Сложность |
|---|-----------|----------|-----------|
| 1 | Security | SQL Injection через конкатенацию | ⭐⭐ |
| 2 | Security | Пароли/токены в логах | ⭐⭐ |
| 3 | Resources | Незакрытые Connection/Stream | ⭐⭐ |
| 4 | Resources | Пустой catch блок | ⭐ |
| 5 | Concurrency | Race condition (check-then-act) | ⭐⭐⭐ |
| 6 | Concurrency | HashMap в многопоточке | ⭐⭐ |
| 7 | Concurrency | Double-checked locking без volatile | ⭐⭐⭐ |
| 8 | NPE | Optional.get() без проверки | ⭐ |
| 9 | NPE | Null параметр без валидации | ⭐ |
| 10 | Comparison | == вместо equals для объектов | ⭐ |
| 11 | Comparison | Integer сравнение через == | ⭐⭐ |
| 12 | Collections | ConcurrentModificationException | ⭐⭐ |
| 13 | Collections | equals без hashCode | ⭐⭐ |
| 14 | Spring | @Transactional на private | ⭐⭐ |
| 15 | Spring | Self-invocation в @Transactional | ⭐⭐⭐ |
| 16 | Spring | Prototype в Singleton | ⭐⭐⭐ |
| 17 | Spring | Внешние вызовы в транзакции | ⭐⭐ |
| 18 | Refactoring | if-else chain → Strategy | ⭐⭐ |
| 19 | Refactoring | Magic numbers | ⭐ |
| 20 | Refactoring | God method (>50 строк) | ⭐⭐ |

---

## 4. Паттерны ответов

### 4.1 Формула структурированного ответа

```
"Вижу [ПРОБЛЕМУ] в строке X.
 Это приведёт к [ПОСЛЕДСТВИЮ].
 Решение: [КАК ИСПРАВИТЬ]."
```

### 4.2 Примеры хороших ответов

**Задача:** Найти проблемы в коде

```java
@Transactional
public void processOrder(Long orderId) {
    Order order = orderRepo.findById(orderId).get();
    emailService.sendConfirmation(order.getEmail());
    paymentService.charge(order.getTotal());
    order.setStatus("COMPLETED");
    orderRepo.save(order);
}
```

**Хороший ответ:**

> **Проблема 1:** `findById().get()` — бросит `NoSuchElementException` если заказ не найден.
> **Последствие:** Необработанное исключение, 500 ошибка для клиента.
> **Решение:** `orElseThrow(() -> new OrderNotFoundException(orderId))`

> **Проблема 2:** Внешние вызовы (`emailService`, `paymentService`) внутри транзакции.
> **Последствие:** Если email отправится, а payment упадёт — транзакция откатится, но письмо уже ушло.
> **Решение:** Вынести отправку email после коммита транзакции (через `@TransactionalEventListener`).

> **Проблема 3:** Magic string `"COMPLETED"`.
> **Последствие:** Опечатка не поймается компилятором, сложнее рефакторить.
> **Решение:** Использовать enum `OrderStatus.COMPLETED`.

### 4.3 Как показать уровень Senior

1. **Глубина анализа**: Не просто "тут баг", а объяснение механизма
2. **Альтернативы**: "Можно решить так или так, я бы выбрал X потому что..."
3. **Production опыт**: "Мы столкнулись с похожей проблемой, когда..."
4. **Системное мышление**: "Это может повлиять на..."

---

## 5. Чеклист для самопроверки

### Перед ответом пройдись по списку:

```
□ SECURITY
  □ SQL/XSS/Command injection?
  □ Hardcoded credentials?
  □ Sensitive data в логах?
  □ Path traversal?

□ RESOURCES
  □ Все ресурсы закрываются? (try-with-resources)
  □ Нет пустых catch блоков?

□ CONCURRENCY
  □ Shared state защищён?
  □ Нет check-then-act без синхронизации?
  □ Правильные коллекции (ConcurrentHashMap)?

□ NULL SAFETY
  □ Параметры валидируются?
  □ Optional используется правильно?

□ COMPARISON
  □ equals вместо ==?
  □ hashCode если есть equals?

□ COLLECTIONS
  □ Нет модификации при итерации?
  □ Границы проверяются?

□ SPRING (если есть)
  □ @Transactional на public методах?
  □ Нет self-invocation?
  □ Правильные scope бинов?

□ CODE QUALITY
  □ Нет magic numbers?
  □ Нет god methods?
  □ Понятный нейминг?
```

---

## 6. Частые ошибки кандидатов

| Ошибка | Почему плохо | Как правильно |
|--------|--------------|---------------|
| Начинать с code style | Показывает неправильные приоритеты | Сначала security и bugs |
| Говорить "тут плохо" без объяснения | Не показывает понимания | Объяснять последствия |
| Не предлагать решения | Критиковать легко, исправлять — ценно | Всегда давать решение |
| Придумывать проблемы | Показывает неуверенность | Говорить только о реальных |
| Молчать долго | Интервьюер не знает, что вы думаете | Думать вслух |

---

## 7. Полезные шаблоны кода

### Безопасный SQL
```java
// PreparedStatement
String sql = "SELECT * FROM users WHERE id = ? AND status = ?";
jdbcTemplate.query(sql, new Object[]{id, status}, rowMapper);
```

### try-with-resources
```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // ...
}
```

### Atomic operations
```java
private final AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();

// Или ConcurrentHashMap
cache.computeIfAbsent(key, k -> expensiveCompute(k));
```

### Optional правильно
```java
// ❌ Плохо
Optional<User> opt = findUser();
User user = opt.get(); // NoSuchElementException

// ✅ Хорошо
User user = findUser()
    .orElseThrow(() -> new UserNotFoundException(id));
```

### equals и hashCode
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

---

## 8. Заключение

### Приоритеты при ревью (запомни!)

```
1. 🔴 SECURITY    → Injection, credentials, data exposure
2. 🔴 RESOURCES   → Leaks, unclosed connections
3. 🔴 CONCURRENCY → Race conditions, thread safety
4. 🟡 BUGS        → NPE, wrong logic, edge cases
5. 🟢 QUALITY     → Code smells, naming, structure
```

### Ключевые фразы для ответа

- "Здесь потенциальная уязвимость..."
- "Это приведёт к..."
- "Рекомендую заменить на..."
- "В production это вызовет..."
- "Альтернативное решение..."

---

> 💡 **Совет**: Практикуйся находить проблемы в реальном коде — читай Pull Requests на GitHub, делай ревью кода коллег. Чем больше практики, тем быстрее ты будешь находить проблемы на собеседовании.
