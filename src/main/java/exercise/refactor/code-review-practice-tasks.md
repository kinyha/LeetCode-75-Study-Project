# Практические задачи: Code Review на собеседовании

> 42 задачи для подготовки к ревью кода на собеседованиях

---

## Как использовать

1. **Прочитай код** и найди все проблемы
2. **Открой подсказки** если застрял (🔍)
3. **Проверь себя** по критериям (✅)
4. **Изучи полный разбор** (📝)

**Сложность:**
- ⭐ — Junior (1-2 проблемы)
- ⭐⭐ — Middle- (2-3 проблемы)
- ⭐⭐⭐ — Middle/Senior (4+ проблем)

---

# Категория 1: Простые баги

---

### Задача 1.1: Поиск дубликата

**Компания**: Газпромбанк | **Сложность**: ⭐⭐

```java
/**
 * Возвращает индекс первого дубликата в массиве.
 * Пример: [1,2,3,4,4,5] → 4
 */
public int findDuplicateIndex(int... numbers) {
    int[] countArray = new int[nubmers.length];
    for (int i = 0; i < numbers.length; i++) {
        int current = numbers[i];
        if (countArray[current] > 0) {
            return i;
        } else {
            countArray[current] += 1;
        }
    }
    throw new CustomException("Duplicate not found!");
}
```

<details>
<summary>🔍 Подсказки</summary>

- Внимательно посмотри на имена переменных
- Что если `numbers[i]` больше длины массива?
- Что если `numbers[i]` отрицательный?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум (Junior)**: Найти опечатку

**Хорошо (Middle)**:
- Найти опечатку
- Найти ArrayIndexOutOfBoundsException
- Предложить исправление

**Отлично (Senior)**:
- Все вышеперечисленное
- Предложить альтернативный алгоритм (HashSet)
- Обсудить сложность O(n) vs O(n²)

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1: Опечатка**
```java
int[] countArray = new int[nubmers.length]; // nubmers вместо numbers
```
Код не скомпилируется.

**Проблема 2: ArrayIndexOutOfBoundsException**
```java
countArray[current] // Если current >= numbers.length — упадёт
```
Пример: `[100, 100]` — массив длины 2, но пытаемся обратиться к индексу 100.

**Проблема 3: Отрицательные числа**
```java
countArray[current] // Если current < 0 — ArrayIndexOutOfBoundsException
```

**Решение:**
```java
public int findDuplicateIndex(int... numbers) {
    Set<Integer> seen = new HashSet<>();
    for (int i = 0; i < numbers.length; i++) {
        if (!seen.add(numbers[i])) {
            return i;
        }
    }
    throw new IllegalStateException("Duplicate not found");
}
```

</details>

---

### Задача 1.2: Optional.get()

**Компания**: Тинькофф | **Сложность**: ⭐

```java
public UserDto getUser(Long userId) {
    Optional<User> user = userRepository.findById(userId);
    User entity = user.get();
    return mapper.toDto(entity);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что вернёт `findById()` если пользователя нет?
- Какое исключение бросит `get()` на пустом Optional?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Указать на `.get()` без проверки

**Хорошо**: Предложить `orElseThrow()`

**Отлично**: Обсудить разницу между `orElse`, `orElseGet`, `orElseThrow`

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `user.get()` бросит `NoSuchElementException` если пользователь не найден.

**Решение:**
```java
public UserDto getUser(Long userId) {
    return userRepository.findById(userId)
        .map(mapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(userId));
}
```

</details>

---

### Задача 1.3: Integer сравнение

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
public boolean isVipUser(User user) {
    Integer vipThreshold = 128;
    Integer userLevel = user.getLevel(); // может быть null

    if (userLevel == vipThreshold) {
        return true;
    }
    if (userLevel == 127) {
        return false; // обычный пользователь
    }
    return userLevel > vipThreshold;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Как работает кеш Integer в Java?
- Что если `userLevel` равен null?
- Почему `127` работает, а `128` может не работать?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Указать на `==` vs `equals`

**Хорошо**: Объяснить Integer cache (-128..127)

**Отлично**:
- Integer cache
- NPE при unboxing
- Правильное решение

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1: Integer cache**
```java
if (userLevel == vipThreshold) // Сравнение ссылок!
```
Java кеширует Integer от -128 до 127. Для 128 создаются новые объекты, `==` вернёт `false`.

**Проблема 2: NPE**
```java
return userLevel > vipThreshold; // NPE если userLevel == null
```
При unboxing `null` в `int` происходит NullPointerException.

**Решение:**
```java
public boolean isVipUser(User user) {
    Integer userLevel = user.getLevel();
    if (userLevel == null) {
        return false;
    }
    return userLevel >= 128;
}
```

</details>

---

### Задача 1.4: StringBuilder в цикле

**Компания**: Альфа-банк | **Сложность**: ⭐

```java
public String formatUsers(List<User> users) {
    String result = "";
    for (User user : users) {
        result = result + user.getName() + ", ";
    }
    return result;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Как работает конкатенация строк в цикле?
- Сколько объектов String создаётся?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Предложить StringBuilder

**Хорошо**: Объяснить почему конкатенация неэффективна

**Отлично**: Предложить Stream + Collectors.joining()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Каждая операция `+` создаёт новый StringBuilder, копирует строку, создаёт новый String. O(n²) по памяти.

**Решение 1:**
```java
public String formatUsers(List<User> users) {
    StringBuilder sb = new StringBuilder();
    for (User user : users) {
        sb.append(user.getName()).append(", ");
    }
    return sb.toString();
}
```

**Решение 2:**
```java
public String formatUsers(List<User> users) {
    return users.stream()
        .map(User::getName)
        .collect(Collectors.joining(", "));
}
```

</details>

---

### Задача 1.5: Off-by-one

**Компания**: ВТБ | **Сложность**: ⭐

```java
public int sumArray(int[] arr) {
    int sum = 0;
    for (int i = 1; i <= arr.length; i++) {
        sum += arr[i];
    }
    return sum;
}
```

<details>
<summary>🔍 Подсказки</summary>

- С какого индекса начинаются массивы в Java?
- Какой максимальный валидный индекс для массива длины n?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Найти обе ошибки (начало и конец)

**Хорошо**: Объяснить off-by-one как класс ошибок

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** `i = 1` — пропускает первый элемент `arr[0]`

**Проблема 2:** `i <= arr.length` — ArrayIndexOutOfBoundsException на `arr[arr.length]`

**Решение:**
```java
for (int i = 0; i < arr.length; i++) {
    sum += arr[i];
}
// Или
for (int num : arr) {
    sum += num;
}
```

</details>

---

### Задача 1.6: Null в equals

**Компания**: Яндекс | **Сложность**: ⭐⭐

```java
public boolean checkUser(String role, User user) {
    if (role.equals(user.getRole())) {
        return true;
    }
    return user.getName().startsWith("Admin");
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если `role` равен null?
- Что если `user.getRole()` вернёт null?
- Что если `user.getName()` вернёт null?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Найти хотя бы один NPE

**Хорошо**: Найти все три потенциальных NPE

**Отлично**: Предложить defensive programming подход

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** `role.equals(...)` — NPE если role == null

**Проблема 2:** `user.getName().startsWith(...)` — NPE если getName() == null

**Решение:**
```java
public boolean checkUser(String role, User user) {
    Objects.requireNonNull(user, "User must not be null");

    if (Objects.equals(role, user.getRole())) {
        return true;
    }

    String name = user.getName();
    return name != null && name.startsWith("Admin");
}
```

</details>

---

# Категория 2: Ресурсы и исключения

---

### Задача 2.1: Чтение CSV файла

**Компания**: Точка банк | **Сложность**: ⭐⭐

```java
boolean containsStringInData(String csvFile, String str) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
    ArrayList<String> list = new ArrayList();

    String line;
    while ((line = br.readLine()) != null) {
        list.add(line);
    }

    boolean result;
    for (String s : list) {
        if (s == str) {
            result = true;
        }
    }

    return result;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Закрывается ли reader?
- Посмотри на имя переменной в while
- Как сравниваются строки?
- Инициализирована ли переменная result?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Найти 2 проблемы

**Хорошо**: Найти все 5 проблем

**Отлично**: Предложить современное решение через Files.lines()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Resource leak — reader никогда не закрывается

**Проблема 2:** `br` вместо `reader` — не скомпилируется

**Проблема 3:** `s == str` — сравнение ссылок вместо содержимого

**Проблема 4:** `result` не инициализирована — ошибка компиляции "variable might not have been initialized"

**Проблема 5:** Raw type `ArrayList` вместо `ArrayList<String>`

**Решение:**
```java
boolean containsStringInData(String csvFile, String str) throws IOException {
    try (Stream<String> lines = Files.lines(Path.of(csvFile))) {
        return lines.anyMatch(line -> line.equals(str));
    }
}
```

</details>

---

### Задача 2.2: Connection leak

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
public List<User> getUsers(String status) {
    Connection conn = dataSource.getConnection();
    String sql = "SELECT * FROM users WHERE status = '" + status + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);

    List<User> users = new ArrayList<>();
    while (rs.next()) {
        users.add(mapUser(rs));
    }
    return users;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что происходит с Connection, Statement, ResultSet?
- Как формируется SQL запрос?
- Что если выбросится исключение?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Resource leak или SQL injection

**Хорошо**: Обе проблемы

**Отлично**: + throws declaration, try-with-resources

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Resource leak — Connection, Statement, ResultSet не закрываются

**Проблема 2:** SQL Injection через конкатенацию

**Проблема 3:** Метод не объявляет `throws SQLException`

**Решение:**
```java
public List<User> getUsers(String status) throws SQLException {
    String sql = "SELECT * FROM users WHERE status = ?";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, status);

        try (ResultSet rs = stmt.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
        }
    }
}
```

</details>

---

### Задача 2.3: Пустой catch

**Компания**: ОТП банк | **Сложность**: ⭐

```java
public void processFile(String path) {
    try {
        List<String> lines = Files.readAllLines(Path.of(path));
        for (String line : lines) {
            processLine(line);
        }
    } catch (Exception e) {
        // Логируем и продолжаем
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что происходит при исключении?
- Узнает ли вызывающий код об ошибке?
- Достаточно ли информации для debug?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Указать на пустой catch

**Хорошо**: Объяснить почему это плохо

**Отлично**: Предложить правильную обработку

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Пустой catch — ошибки молча игнорируются

**Проблема 2:** Ловится `Exception` вместо конкретных типов

**Проблема 3:** Комментарий "Логируем" — но логирования нет

**Решение:**
```java
public void processFile(String path) {
    try {
        List<String> lines = Files.readAllLines(Path.of(path));
        for (String line : lines) {
            processLine(line);
        }
    } catch (IOException e) {
        log.error("Failed to process file: {}", path, e);
        throw new FileProcessingException("Unable to process " + path, e);
    }
}
```

</details>

---

### Задача 2.4: Files.lines без закрытия

**Компания**: Альфа-банк | **Сложность**: ⭐⭐

```java
public long countLines(String path) {
    return Files.lines(Path.of(path)).count();
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что возвращает Files.lines()?
- Нужно ли закрывать Stream?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Указать на незакрытый Stream

**Хорошо**: Объяснить что Files.lines() держит file handle

**Отлично**: Показать try-with-resources для Stream

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `Files.lines()` возвращает Stream, который держит открытый file handle. Без закрытия — утечка ресурсов.

**Решение:**
```java
public long countLines(String path) throws IOException {
    try (Stream<String> lines = Files.lines(Path.of(path))) {
        return lines.count();
    }
}
```

</details>

---

### Задача 2.5: Неправильный порядок закрытия

**Компания**: Иннотех | **Сложность**: ⭐⭐

```java
public void writeData(String file, String data) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    ObjectOutputStream oos = new ObjectOutputStream(bos);

    oos.writeObject(data);

    fos.close();
    bos.close();
    oos.close();
}
```

<details>
<summary>🔍 Подсказки</summary>

- В каком порядке нужно закрывать потоки?
- Что если exception в writeObject()?
- Есть ли потеря данных?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Неправильный порядок close

**Хорошо**: + Нет flush + exception handling

**Отлично**: try-with-resources решение

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Неправильный порядок — нужно закрывать снаружи внутрь (oos → bos → fos)

**Проблема 2:** Если writeObject() бросит исключение — потоки не закроются

**Проблема 3:** Нет flush() — данные могут не записаться

**Решение:**
```java
public void writeData(String file, String data) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file);
         BufferedOutputStream bos = new BufferedOutputStream(fos);
         ObjectOutputStream oos = new ObjectOutputStream(bos)) {

        oos.writeObject(data);
        oos.flush();
    }
}
```

</details>

---

# Категория 3: Concurrency

---

### Задача 3.1: Race condition в кеше

**Компания**: Яндекс | **Сложность**: ⭐⭐⭐

```java
public class UserCache {
    private Map<Long, User> cache = new HashMap<>();

    public User getUser(Long id) {
        if (!cache.containsKey(id)) {
            User user = loadFromDatabase(id);
            cache.put(id, user);
        }
        return cache.get(id);
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если два потока одновременно запросят одного пользователя?
- Сколько раз будет вызван loadFromDatabase()?
- Что происходит с HashMap при параллельной записи?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Race condition в check-then-act

**Хорошо**: + HashMap не thread-safe

**Отлично**: ConcurrentHashMap.computeIfAbsent()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Check-then-act race condition
```java
if (!cache.containsKey(id)) {  // Thread A: true
    // Thread B: тоже проверяет, тоже true
    User user = loadFromDatabase(id);  // Оба загружают
    cache.put(id, user);
}
```

**Проблема 2:** HashMap не thread-safe — при параллельной записи может corrupted state, бесконечный цикл в resize

**Решение:**
```java
public class UserCache {
    private final Map<Long, User> cache = new ConcurrentHashMap<>();

    public User getUser(Long id) {
        return cache.computeIfAbsent(id, this::loadFromDatabase);
    }
}
```

</details>

---

### Задача 3.2: Double-checked locking

**Компания**: Сбер | **Сложность**: ⭐⭐⭐

```java
public class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что такое instruction reordering?
- Может ли поток увидеть частично инициализированный объект?
- Какое ключевое слово отсутствует?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Отсутствует volatile

**Хорошо**: Объяснить почему нужен volatile

**Отлично**: Альтернативы (holder pattern, enum)

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Без `volatile` JVM может переупорядочить инструкции:
1. Выделить память
2. Присвоить ссылку в instance
3. Вызвать конструктор

Другой поток может увидеть instance != null, но получить не до конца инициализированный объект.

**Решение 1: volatile**
```java
private static volatile Singleton instance;
```

**Решение 2: Holder pattern (лучше)**
```java
public class Singleton {
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

</details>

---

### Задача 3.3: Non-atomic counter

**Компания**: Тинькофф | **Сложность**: ⭐⭐

```java
public class Counter {
    private int count = 0;

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Является ли `count++` атомарной операцией?
- Сколько операций выполняется на уровне JVM?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: count++ не атомарен

**Хорошо**: Read-modify-write, race condition

**Отлично**: AtomicInteger, synchronized, LongAdder

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `count++` это 3 операции: read → increment → write

Два потока:
```
Thread A: read count (0)
Thread B: read count (0)
Thread A: increment (1)
Thread B: increment (1)
Thread A: write (1)
Thread B: write (1)
// Результат: 1 вместо 2
```

**Решение 1:**
```java
private final AtomicInteger count = new AtomicInteger(0);

public void increment() {
    count.incrementAndGet();
}
```

**Решение 2 (для высокой конкурентности):**
```java
private final LongAdder count = new LongAdder();

public void increment() {
    count.increment();
}
```

</details>

---

### Задача 3.4: ConcurrentModificationException

**Компания**: ВТБ | **Сложность**: ⭐⭐

```java
public void removeInactive(List<User> users) {
    for (User user : users) {
        if (!user.isActive()) {
            users.remove(user);
        }
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что происходит при модификации коллекции во время итерации?
- Какое исключение будет выброшено?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: ConcurrentModificationException

**Хорошо**: Iterator.remove() или removeIf()

**Отлично**: Объяснить fail-fast итераторы

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Модификация коллекции во время итерации вызывает `ConcurrentModificationException` (fail-fast поведение).

**Решение 1:**
```java
public void removeInactive(List<User> users) {
    users.removeIf(user -> !user.isActive());
}
```

**Решение 2:**
```java
Iterator<User> iterator = users.iterator();
while (iterator.hasNext()) {
    if (!iterator.next().isActive()) {
        iterator.remove();
    }
}
```

</details>

---

### Задача 3.5: Volatile не достаточно

**Компания**: Озон | **Сложность**: ⭐⭐⭐

```java
public class RangeChecker {
    private volatile int min = 0;
    private volatile int max = 100;

    public void setRange(int newMin, int newMax) {
        min = newMin;
        max = newMax;
    }

    public boolean isInRange(int value) {
        return value >= min && value <= max;
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- volatile гарантирует visibility, но что с atomicity?
- Может ли isInRange увидеть min=50, max=10?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Два volatile не атомарны вместе

**Хорошо**: Пример неконсистентного состояния

**Отлично**: Решение через immutable object

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** setRange не атомарен. Между записью min и max другой поток может прочитать неконсистентное состояние.

```
Thread A: setRange(50, 150)
    - min = 50
    Thread B: isInRange(75)
        - min = 50, max = 100 (старый!)
        - 75 >= 50 && 75 <= 100 = true (но должно быть true для нового диапазона по другой причине)
    - max = 150
```

**Решение:**
```java
public class RangeChecker {
    private volatile Range range = new Range(0, 100);

    public void setRange(int newMin, int newMax) {
        range = new Range(newMin, newMax); // Атомарная замена ссылки
    }

    public boolean isInRange(int value) {
        Range r = range; // Локальная копия
        return value >= r.min && value <= r.max;
    }

    private record Range(int min, int max) {}
}
```

</details>

---

### Задача 3.6: HashMap в многопоточке

**Компания**: Авито | **Сложность**: ⭐⭐

```java
@Service
public class SessionCache {
    private Map<String, Session> sessions = new HashMap<>();

    public void addSession(String token, Session session) {
        sessions.put(token, session);
    }

    public Session getSession(String token) {
        return sessions.get(token);
    }

    public void removeSession(String token) {
        sessions.remove(token);
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Spring бины по умолчанию singleton
- HashMap thread-safe?
- Что происходит при параллельном resize?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: HashMap не thread-safe

**Хорошо**: ConcurrentHashMap

**Отлично**: Обсудить выбор между synchronized и ConcurrentHashMap

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** HashMap не thread-safe. При параллельных операциях:
- Потеря данных
- Corrupted state
- Бесконечный цикл при resize (в старых версиях Java)

**Решение:**
```java
@Service
public class SessionCache {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // методы остаются те же
}
```

</details>

---

# Категория 4: Безопасность

---

### Задача 4.1: SQL Injection

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
@Transactional
public void process(String oldName, String newName) {
    Long id = exec("select id from file where name='" + oldName + "'");
    processFile(oldName, newName);
    exec("update file set name='" + newName + "' where id = " + id);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Как формируются SQL запросы?
- Что если oldName = "'; DROP TABLE file; --"?
- Что ещё не так с этим методом?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: SQL injection

**Хорошо**: + Файловая операция в транзакции

**Отлично**: + Порядок операций (что если processFile упадёт?)

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** SQL Injection — параметры вставляются через конкатенацию

**Проблема 2:** Файловая операция внутри транзакции — если БД откатится, файл уже переименован

**Проблема 3:** Порядок — если processFile упадёт, id уже получен, но update не выполнится

**Решение:**
```java
@Transactional
public void process(String oldName, String newName) {
    // 1. Сначала проверяем и готовим данные
    Long id = jdbcTemplate.queryForObject(
        "SELECT id FROM file WHERE name = ?",
        Long.class,
        oldName
    );

    // 2. Обновляем БД
    jdbcTemplate.update(
        "UPDATE file SET name = ? WHERE id = ?",
        newName, id
    );

    // 3. После коммита переименовываем файл
}

// Переименование файла через @TransactionalEventListener
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onCommit(FileRenamedEvent event) {
    processFile(event.getOldName(), event.getNewName());
}
```

</details>

---

### Задача 4.2: Path Traversal

**Компания**: Тинькофф | **Сложность**: ⭐⭐

```java
@GetMapping("/download")
public ResponseEntity<Resource> downloadFile(@RequestParam String filename) {
    Path path = Paths.get("/uploads/" + filename);
    Resource resource = new FileSystemResource(path);
    return ResponseEntity.ok().body(resource);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если filename = "../../../etc/passwd"?
- Как можно выйти за пределы /uploads/?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Path traversal уязвимость

**Хорошо**: Пример эксплуатации

**Отлично**: Path.normalize() + проверка

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Злоумышленник может передать `../../../etc/passwd` и получить доступ к любым файлам на сервере.

**Решение:**
```java
@GetMapping("/download")
public ResponseEntity<Resource> downloadFile(@RequestParam String filename) {
    Path basePath = Paths.get("/uploads").toAbsolutePath().normalize();
    Path filePath = basePath.resolve(filename).normalize();

    // Проверяем что путь не вышел за пределы базовой директории
    if (!filePath.startsWith(basePath)) {
        throw new SecurityException("Invalid file path");
    }

    if (!Files.exists(filePath)) {
        return ResponseEntity.notFound().build();
    }

    Resource resource = new FileSystemResource(filePath);
    return ResponseEntity.ok().body(resource);
}
```

</details>

---

### Задача 4.3: Пароли в логах

**Компания**: Альфа-банк | **Сложность**: ⭐⭐

```java
@PostMapping("/login")
public ResponseEntity<TokenDto> login(@RequestBody LoginRequest request) {
    log.info("Login attempt: {}", request);

    User user = userService.authenticate(request.getUsername(), request.getPassword());
    if (user == null) {
        log.warn("Failed login for user: {}, password: {}",
                 request.getUsername(), request.getPassword());
        return ResponseEntity.status(401).build();
    }

    return ResponseEntity.ok(tokenService.generateToken(user));
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что попадёт в логи?
- Кто имеет доступ к логам?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Пароль в логах

**Хорошо**: Объяснить риски (compliance, аудит)

**Отлично**: Маскирование, @ToString(exclude)

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** `log.info("Login attempt: {}", request)` — если LoginRequest.toString() выводит пароль, он попадёт в логи

**Проблема 2:** Явное логирование пароля в warn

**Решение:**
```java
@Data
public class LoginRequest {
    private String username;
    @ToString.Exclude  // Lombok
    private String password;
}

@PostMapping("/login")
public ResponseEntity<TokenDto> login(@RequestBody LoginRequest request) {
    log.info("Login attempt for user: {}", request.getUsername());

    User user = userService.authenticate(request.getUsername(), request.getPassword());
    if (user == null) {
        log.warn("Failed login for user: {}", request.getUsername());
        // Никогда не логируем пароль!
        return ResponseEntity.status(401).build();
    }

    return ResponseEntity.ok(tokenService.generateToken(user));
}
```

</details>

---

### Задача 4.4: Hardcoded credentials

**Компания**: Росбанк | **Сложность**: ⭐

```java
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://prod-db:5432/app")
            .username("admin")
            .password("SuperSecret123!")
            .build();
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Где хранится этот код?
- Кто имеет доступ к репозиторию?
- Как поменять пароль?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Пароль в коде

**Хорошо**: Environment variables

**Отлично**: Vault, AWS Secrets Manager

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Credentials в коде:
- Видны всем кто имеет доступ к репозиторию
- Остаются в git history навсегда
- Сложно ротировать

**Решение:**
```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

```java
// Или через Vault
@Configuration
public class DatabaseConfig {
    @Value("${spring.datasource.password}")
    private String password;
}
```

</details>

---

# Категория 5: Collections и контракты

---

### Задача 5.1: equals без hashCode

**Компания**: ВТБ | **Сложность**: ⭐⭐

```java
public class User {
    private Long id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    // hashCode не переопределён
}

// Использование:
Set<User> users = new HashSet<>();
users.add(new User(1L, "John"));
users.contains(new User(1L, "John")); // ???
```

<details>
<summary>🔍 Подсказки</summary>

- Какой контракт между equals и hashCode?
- Как HashSet ищет элементы?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: hashCode не переопределён

**Хорошо**: Объяснить контракт equals/hashCode

**Отлично**: Как работает HashSet.contains()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Контракт: если `a.equals(b)`, то `a.hashCode() == b.hashCode()`.

Без переопределения hashCode используется Object.hashCode() — разные объекты, разные хеши.

```
User u1 = new User(1L, "John");
User u2 = new User(1L, "John");
u1.equals(u2) // true
u1.hashCode() == u2.hashCode() // false!
```

HashSet сначала проверяет hashCode, потом equals. Разные hashCode — элемент не найдётся.

**Решение:**
```java
@Override
public int hashCode() {
    return Objects.hash(id);
}
```

</details>

---

### Задача 5.2: Mutable key в HashMap

**Компания**: Яндекс | **Сложность**: ⭐⭐⭐

```java
public class CacheKey {
    private String type;
    private List<String> tags;

    // getters, setters, equals, hashCode (на основе type и tags)
}

// Использование:
Map<CacheKey, Data> cache = new HashMap<>();
CacheKey key = new CacheKey("user", Arrays.asList("active"));
cache.put(key, data);

key.getTags().add("premium"); // Модифицируем ключ
cache.get(key); // ???
```

<details>
<summary>🔍 Подсказки</summary>

- Что происходит с hashCode после модификации?
- В какой bucket попадёт get()?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Mutable ключ — проблема

**Хорошо**: hashCode изменился, bucket не тот

**Отлично**: Immutable ключи или defensive copy

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** После модификации tags изменился hashCode. HashMap ищет в bucket по новому hashCode, но элемент лежит в bucket по старому hashCode. Элемент "потерян".

**Решение:**
```java
public class CacheKey {
    private final String type;
    private final List<String> tags; // Immutable!

    public CacheKey(String type, List<String> tags) {
        this.type = type;
        this.tags = List.copyOf(tags); // Defensive copy
    }

    public List<String> getTags() {
        return tags; // Уже immutable
    }
}
```

</details>

---

### Задача 5.3: Arrays.asList модификация

**Компания**: Тинькофф | **Сложность**: ⭐⭐

```java
public List<String> getDefaultRoles() {
    List<String> roles = Arrays.asList("USER", "VIEWER");
    roles.add("GUEST");
    return roles;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Какой List возвращает Arrays.asList()?
- Можно ли добавлять/удалять элементы?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: UnsupportedOperationException

**Хорошо**: Объяснить что это fixed-size list

**Отлично**: new ArrayList<>(Arrays.asList(...))

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `Arrays.asList()` возвращает fixed-size list, backed by массив. add/remove бросают `UnsupportedOperationException`.

**Решение:**
```java
public List<String> getDefaultRoles() {
    List<String> roles = new ArrayList<>(Arrays.asList("USER", "VIEWER"));
    roles.add("GUEST");
    return roles;
}

// Или Java 9+:
public List<String> getDefaultRoles() {
    return new ArrayList<>(List.of("USER", "VIEWER", "GUEST"));
}
```

</details>

---

### Задача 5.4: Неправильный Comparator

**Компания**: Авито | **Сложность**: ⭐⭐

```java
public List<User> sortByAge(List<User> users) {
    users.sort((u1, u2) -> u1.getAge() - u2.getAge());
    return users;
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если возраст отрицательный? (хм, маловероятно)
- А что если это не возраст, а другое int поле с большими значениями?
- Integer overflow?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Integer overflow при вычитании

**Хорошо**: Integer.compare()

**Отлично**: Comparator.comparingInt()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `u1.getAge() - u2.getAge()` может overflow:
```java
Integer.MAX_VALUE - (-1) = Integer.MIN_VALUE // Неправильный порядок!
```

**Решение:**
```java
users.sort(Comparator.comparingInt(User::getAge));
// Или
users.sort((u1, u2) -> Integer.compare(u1.getAge(), u2.getAge()));
```

</details>

---

### Задача 5.5: Remove в foreach

**Компания**: Сбер | **Сложность**: ⭐

```java
public void removeNulls(List<String> items) {
    for (String item : items) {
        if (item == null) {
            items.remove(item);
        }
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Это уже было в Concurrency, но здесь про коллекции
- ConcurrentModificationException

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: ConcurrentModificationException

**Хорошо**: removeIf()

</details>

<details>
<summary>📝 Полный разбор</summary>

**Решение:**
```java
public void removeNulls(List<String> items) {
    items.removeIf(Objects::isNull);
}
```

</details>

---

# Категория 6: Spring @Transactional

---

### Задача 6.1: Private метод

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
@Service
public class OrderService {

    public void processOrder(Order order) {
        validateOrder(order);
        saveOrder(order);
    }

    @Transactional
    private void saveOrder(Order order) {
        orderRepository.save(order);
        inventoryService.reserve(order.getItems());
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Как работает @Transactional?
- Какие методы перехватывает proxy?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: @Transactional на private не работает

**Хорошо**: Объяснить proxy-механизм

**Отлично**: Альтернативы (public, AspectJ)

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Spring AOP создаёт proxy вокруг бина. Proxy перехватывает только public методы. @Transactional на private методе игнорируется.

**Решение:**
```java
@Transactional
public void saveOrder(Order order) {
    orderRepository.save(order);
    inventoryService.reserve(order.getItems());
}
```

</details>

---

### Задача 6.2: Self-invocation

**Компания**: Альфа-банк | **Сложность**: ⭐⭐⭐

```java
@Service
public class PaymentService {

    public void processPayment(Payment payment) {
        validatePayment(payment);
        this.savePayment(payment); // Self-invocation
    }

    @Transactional
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
        notificationService.notify(payment.getUserId());
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Через что проходит вызов `this.savePayment()`?
- Где находится proxy?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Self-invocation обходит proxy

**Хорошо**: Два решения

**Отлично**: Почему Spring так работает

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `this.savePayment()` вызывает метод напрямую, минуя proxy. @Transactional не срабатывает.

**Решение 1: Self-injection**
```java
@Service
public class PaymentService {
    @Autowired
    private PaymentService self;

    public void processPayment(Payment payment) {
        validatePayment(payment);
        self.savePayment(payment); // Через proxy
    }
}
```

**Решение 2: Выделить в отдельный сервис**
```java
@Service
public class PaymentService {
    @Autowired
    private PaymentTransactionService txService;

    public void processPayment(Payment payment) {
        validatePayment(payment);
        txService.savePayment(payment);
    }
}
```

</details>

---

### Задача 6.3: Внешний вызов в транзакции

**Компания**: Тинькофф | **Сложность**: ⭐⭐

```java
@Transactional
public void createUser(UserDto dto) {
    User user = mapper.toEntity(dto);
    userRepository.save(user);

    // Отправляем email
    emailService.sendWelcomeEmail(user.getEmail());

    // Уведомляем CRM
    crmClient.notifyNewUser(user);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если emailService работает 30 секунд?
- Что если crmClient упадёт после отправки email?
- Держит ли транзакция connection?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Внешние вызовы в транзакции — плохо

**Хорошо**: Блокировка connection, откат после отправки

**Отлично**: @TransactionalEventListener

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема 1:** Транзакция держит connection из пула. Долгие внешние вызовы — connection exhaustion.

**Проблема 2:** Если crmClient упадёт — транзакция откатится, но email уже ушёл.

**Решение:**
```java
@Transactional
public void createUser(UserDto dto) {
    User user = mapper.toEntity(dto);
    userRepository.save(user);

    eventPublisher.publishEvent(new UserCreatedEvent(user));
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onUserCreated(UserCreatedEvent event) {
    emailService.sendWelcomeEmail(event.getUser().getEmail());
    crmClient.notifyNewUser(event.getUser());
}
```

</details>

---

### Задача 6.4: Checked exception не откатывает

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount)
        throws InsufficientFundsException {

    Account from = accountRepository.findById(fromId).orElseThrow();
    Account to = accountRepository.findById(toId).orElseThrow();

    if (from.getBalance().compareTo(amount) < 0) {
        throw new InsufficientFundsException("Not enough money");
    }

    from.setBalance(from.getBalance().subtract(amount));
    to.setBalance(to.getBalance().add(amount));

    accountRepository.save(from);
    accountRepository.save(to);
}

public class InsufficientFundsException extends Exception { }
```

<details>
<summary>🔍 Подсказки</summary>

- Какие исключения по умолчанию откатывают транзакцию?
- InsufficientFundsException extends Exception или RuntimeException?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Checked exception не откатывает

**Хорошо**: rollbackFor

**Отлично**: RuntimeException vs Checked

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** По умолчанию @Transactional откатывает только на RuntimeException и Error. Checked exceptions (extends Exception) — коммитят транзакцию!

**Решение 1:**
```java
@Transactional(rollbackFor = InsufficientFundsException.class)
```

**Решение 2:** Сделать исключение runtime
```java
public class InsufficientFundsException extends RuntimeException { }
```

</details>

---

### Задача 6.5: readOnly для записи

**Компания**: ВТБ | **Сложность**: ⭐⭐

```java
@Transactional(readOnly = true)
public void updateUserStatus(Long userId, String status) {
    User user = userRepository.findById(userId).orElseThrow();
    user.setStatus(status);
    userRepository.save(user);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что делает readOnly = true?
- Будет ли save работать?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: readOnly = true для записи

**Хорошо**: Может сработать, но flush mode

**Отлично**: Оптимизации Hibernate при readOnly

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** `readOnly = true` устанавливает flush mode в MANUAL (или NEVER). Изменения могут не сохраниться. Поведение зависит от драйвера БД и версии Hibernate.

**Решение:**
```java
@Transactional // readOnly = false по умолчанию
public void updateUserStatus(Long userId, String status) {
    User user = userRepository.findById(userId).orElseThrow();
    user.setStatus(status);
    // save не обязателен при dirty checking
}
```

</details>

---

### Задача 6.6: REQUIRES_NEW в том же классе

**Компания**: Яндекс | **Сложность**: ⭐⭐⭐

```java
@Service
public class OrderService {

    @Transactional
    public void processOrder(Order order) {
        orderRepository.save(order);

        try {
            this.logOrderEvent(order); // Хотим независимую транзакцию
        } catch (Exception e) {
            log.warn("Logging failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderEvent(Order order) {
        eventRepository.save(new OrderEvent(order.getId(), "CREATED"));
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Работает ли REQUIRES_NEW при self-invocation?
- Это комбинация двух проблем

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Self-invocation, REQUIRES_NEW не сработает

**Хорошо**: Лог не сохранится отдельно при откате

**Отлично**: Выделить в отдельный сервис

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Self-invocation. `this.logOrderEvent()` обходит proxy, REQUIRES_NEW не создаёт новую транзакцию. При откате основной транзакции откатится и лог.

**Решение:**
```java
@Service
public class OrderService {
    @Autowired
    private OrderEventLogger eventLogger;

    @Transactional
    public void processOrder(Order order) {
        orderRepository.save(order);

        try {
            eventLogger.logOrderEvent(order);
        } catch (Exception e) {
            log.warn("Logging failed", e);
        }
    }
}

@Service
public class OrderEventLogger {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderEvent(Order order) {
        eventRepository.save(new OrderEvent(order.getId(), "CREATED"));
    }
}
```

</details>

---

# Категория 7: Spring Beans

---

### Задача 7.1: Prototype в Singleton

**Компания**: Тинькофф | **Сложность**: ⭐⭐⭐

```java
@Component
@Scope("prototype")
public class RequestContext {
    private String requestId = UUID.randomUUID().toString();
    // getters
}

@Service // Singleton по умолчанию
public class OrderService {
    @Autowired
    private RequestContext requestContext;

    public void processOrder(Order order) {
        log.info("Processing order {} with request {}",
                 order.getId(), requestContext.getRequestId());
        // ...
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Когда создаётся Singleton bean?
- Когда инжектится RequestContext?
- Будет ли requestId разный для разных запросов?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Prototype инжектится один раз

**Хорошо**: ObjectFactory / Provider

**Отлично**: @Lookup, proxy scope

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Singleton создаётся один раз при старте. Prototype бин инжектится при создании Singleton — тоже один раз. Все запросы будут видеть один и тот же requestId.

**Решение 1: ObjectFactory**
```java
@Service
public class OrderService {
    @Autowired
    private ObjectFactory<RequestContext> contextFactory;

    public void processOrder(Order order) {
        RequestContext ctx = contextFactory.getObject(); // Новый каждый раз
        log.info("Processing order {} with request {}",
                 order.getId(), ctx.getRequestId());
    }
}
```

**Решение 2: @Lookup**
```java
@Service
public abstract class OrderService {
    @Lookup
    protected abstract RequestContext getRequestContext();

    public void processOrder(Order order) {
        RequestContext ctx = getRequestContext();
        // ...
    }
}
```

**Решение 3: Proxy scope**
```java
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext { }
```

</details>

---

### Задача 7.2: @Async без @EnableAsync

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
@Service
public class NotificationService {

    @Async
    public void sendEmail(String email, String message) {
        // Долгая отправка email
        emailClient.send(email, message);
    }
}

@RestController
public class UserController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        User user = userService.create(dto);
        notificationService.sendEmail(user.getEmail(), "Welcome!");
        return ResponseEntity.ok(user); // Ждём email?
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что нужно для работы @Async?
- Выполняется ли sendEmail асинхронно?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Нужен @EnableAsync

**Хорошо**: Метод выполняется синхронно

**Отлично**: Self-invocation тоже не сработает

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Без `@EnableAsync` на конфигурации, @Async игнорируется. Метод выполняется синхронно.

**Решение:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        return executor;
    }
}
```

</details>

---

### Задача 7.3: Circular dependency

**Компания**: Альфа-банк | **Сложность**: ⭐⭐

```java
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService;

    public void createOrder(Order order) {
        // ...
        paymentService.processPayment(order);
    }
}

@Service
public class PaymentService {
    @Autowired
    private OrderService orderService;

    public void refund(Long orderId) {
        Order order = orderService.getOrder(orderId);
        // ...
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что происходит при создании бинов?
- Какую ошибку выдаст Spring Boot 2.6+?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Circular dependency

**Хорошо**: @Lazy, рефакторинг

**Отлично**: Почему это design smell

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Spring Boot 2.6+ по умолчанию запрещает circular dependencies:
```
The dependencies of some of the beans in the application context form a cycle
```

**Решение 1: @Lazy**
```java
@Service
public class PaymentService {
    @Lazy
    @Autowired
    private OrderService orderService;
}
```

**Решение 2: Рефакторинг (лучше)**
Выделить общую логику в третий сервис, убрать циклическую зависимость.

</details>

---

### Задача 7.4: @PostConstruct + @Transactional

**Компания**: Иннотех | **Сложность**: ⭐⭐⭐

```java
@Service
public class CacheWarmupService {
    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    @Transactional
    public void warmupCache() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::cacheProduct);
    }

    private void cacheProduct(Product product) {
        // ...
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Когда вызывается @PostConstruct?
- Готов ли proxy к этому моменту?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: @Transactional в @PostConstruct не работает

**Хорошо**: ApplicationRunner, @EventListener

**Отлично**: Порядок инициализации Spring

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** @PostConstruct вызывается до того, как proxy полностью готов. @Transactional не сработает.

**Решение 1: ApplicationRunner**
```java
@Component
public class CacheWarmupService implements ApplicationRunner {
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Product> products = productRepository.findAll();
        products.forEach(this::cacheProduct);
    }
}
```

**Решение 2: @EventListener**
```java
@Service
public class CacheWarmupService {
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void warmupCache() {
        // ...
    }
}
```

</details>

---

# Категория 8: Рефакторинг

---

### Задача 8.1: if-else chain

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
public class CodeProcessor {

    public void process(List<Code> codes) {
        for (Code code : codes) {
            if (CodeType.ITCP == code.getCodeType()) {
                doSmthngITCP();
            }
            else if (CodeType.TLS == code.getCodeType()) {
                doSmthngTLS();
            }
            else if (CodeType.OTHER == code.getCodeType()) {
                doSmthngOther();
            }
            else {
                doDefault();
            }
        }
    }

    // ... методы doSmthng*
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что если добавится новый CodeType?
- Нарушение какого принципа SOLID?
- Strategy pattern?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: if-else chain — code smell

**Хорошо**: Strategy pattern или метод в enum

**Отлично**: Open-Closed Principle, Map<CodeType, Handler>

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:**
- Нарушение Open-Closed Principle — для нового типа нужно менять существующий код
- Дублирование условий
- Сложно тестировать

**Решение 1: Метод в enum**
```java
public enum CodeType {
    ITCP {
        @Override
        public void process() {
            System.out.println("Handling ITCP");
        }
    },
    TLS {
        @Override
        public void process() {
            System.out.println("Handling TLS");
        }
    },
    OTHER {
        @Override
        public void process() {
            System.out.println("Handling Other");
        }
    };

    public abstract void process();
}

// Использование
public void process(List<Code> codes) {
    codes.forEach(code -> code.getCodeType().process());
}
```

**Решение 2: Map handlers**
```java
private final Map<CodeType, Runnable> handlers = Map.of(
    CodeType.ITCP, this::doSmthngITCP,
    CodeType.TLS, this::doSmthngTLS,
    CodeType.OTHER, this::doSmthngOther
);

public void process(List<Code> codes) {
    for (Code code : codes) {
        handlers.getOrDefault(code.getCodeType(), this::doDefault).run();
    }
}
```

</details>

---

### Задача 8.2: Magic numbers

**Компания**: ВТБ | **Сложность**: ⭐

```java
public BigDecimal calculateDiscount(Order order, Customer customer) {
    BigDecimal discount = BigDecimal.ZERO;

    if (order.getTotal().compareTo(new BigDecimal("1000")) > 0) {
        discount = discount.add(new BigDecimal("0.05"));
    }

    if (customer.getOrderCount() > 10) {
        discount = discount.add(new BigDecimal("0.03"));
    }

    if (customer.getRegistrationDate().isBefore(LocalDate.now().minusYears(2))) {
        discount = discount.add(new BigDecimal("0.02"));
    }

    return discount.min(new BigDecimal("0.15"));
}
```

<details>
<summary>🔍 Подсказки</summary>

- Что означает 1000? 0.05? 10? 2?
- Как изменить пороги?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Magic numbers

**Хорошо**: Константы с понятными именами

**Отлично**: Конфигурация через properties

</details>

<details>
<summary>📝 Полный разбор</summary>

**Решение:**
```java
public class DiscountCalculator {
    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_DISCOUNT = new BigDecimal("0.05");

    private static final int LOYAL_CUSTOMER_ORDER_COUNT = 10;
    private static final BigDecimal LOYAL_CUSTOMER_DISCOUNT = new BigDecimal("0.03");

    private static final int OLD_CUSTOMER_YEARS = 2;
    private static final BigDecimal OLD_CUSTOMER_DISCOUNT = new BigDecimal("0.02");

    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("0.15");

    public BigDecimal calculateDiscount(Order order, Customer customer) {
        BigDecimal discount = BigDecimal.ZERO;

        if (order.getTotal().compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            discount = discount.add(LARGE_ORDER_DISCOUNT);
        }
        // ...
    }
}
```

</details>

---

### Задача 8.3: God Method

**Компания**: Альфа-банк | **Сложность**: ⭐⭐

```java
public OrderResult processOrder(OrderRequest request) {
    // Валидация (20 строк)
    if (request.getItems() == null || request.getItems().isEmpty()) {
        throw new ValidationException("Items required");
    }
    if (request.getCustomerId() == null) {
        throw new ValidationException("Customer required");
    }
    // ... ещё 15 проверок

    // Расчёт стоимости (30 строк)
    BigDecimal total = BigDecimal.ZERO;
    for (OrderItem item : request.getItems()) {
        Product product = productRepository.findById(item.getProductId());
        BigDecimal price = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
        // ... скидки, налоги, доставка
        total = total.add(price);
    }

    // Проверка оплаты (20 строк)
    PaymentResult payment = paymentService.authorize(request.getPaymentDetails());
    if (!payment.isSuccessful()) {
        // ... обработка ошибок
    }

    // Создание заказа (15 строк)
    Order order = new Order();
    order.setCustomerId(request.getCustomerId());
    // ... заполнение полей

    // Отправка уведомлений (10 строк)
    notificationService.sendOrderConfirmation(order);

    return new OrderResult(order);
}
```

<details>
<summary>🔍 Подсказки</summary>

- Сколько ответственностей у этого метода?
- Single Responsibility Principle?
- Extract Method?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Слишком много логики в одном методе

**Хорошо**: Extract Method для каждого блока

**Отлично**: Выделить в отдельные сервисы

</details>

<details>
<summary>📝 Полный разбор</summary>

**Решение:**
```java
public OrderResult processOrder(OrderRequest request) {
    validateOrder(request);

    BigDecimal total = calculateTotal(request);

    PaymentResult payment = processPayment(request, total);

    Order order = createOrder(request, total);

    sendNotifications(order);

    return new OrderResult(order);
}

private void validateOrder(OrderRequest request) {
    orderValidator.validate(request);
}

private BigDecimal calculateTotal(OrderRequest request) {
    return priceCalculator.calculate(request.getItems());
}

// ... остальные методы
```

</details>

---

### Задача 8.4: Feature Envy

**Компания**: Яндекс | **Сложность**: ⭐⭐

```java
public class OrderProcessor {

    public BigDecimal calculateShipping(Order order) {
        BigDecimal baseRate = order.getDeliveryAddress().getCity().equals("Москва")
            ? new BigDecimal("200")
            : new BigDecimal("500");

        BigDecimal weightSurcharge = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            weightSurcharge = weightSurcharge.add(
                item.getProduct().getWeight()
                    .multiply(new BigDecimal("10"))
            );
        }

        if (order.getCustomer().isPremium()) {
            return BigDecimal.ZERO;
        }

        return baseRate.add(weightSurcharge);
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Сколько объектов "опрашивает" этот метод?
- В каком классе должна быть эта логика?
- Law of Demeter?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Метод использует данные другого объекта

**Хорошо**: Переместить логику в Order

**Отлично**: Tell, Don't Ask

</details>

<details>
<summary>📝 Полный разбор</summary>

**Проблема:** Feature Envy — метод "завидует" данным Order. Нарушение инкапсуляции.

**Решение:**
```java
public class Order {
    public BigDecimal calculateShipping() {
        if (customer.isPremium()) {
            return BigDecimal.ZERO;
        }

        return deliveryAddress.getBaseShippingRate()
            .add(calculateWeightSurcharge());
    }

    private BigDecimal calculateWeightSurcharge() {
        return items.stream()
            .map(OrderItem::getWeightSurcharge)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

public class DeliveryAddress {
    public BigDecimal getBaseShippingRate() {
        return city.equals("Москва")
            ? new BigDecimal("200")
            : new BigDecimal("500");
    }
}
```

</details>

---

### Задача 8.5: Deep Nesting

**Компания**: Тинькофф | **Сложность**: ⭐⭐

```java
public String processRequest(Request request) {
    if (request != null) {
        if (request.isValid()) {
            User user = userService.findUser(request.getUserId());
            if (user != null) {
                if (user.isActive()) {
                    Permission permission = user.getPermission();
                    if (permission != null) {
                        if (permission.canAccess(request.getResource())) {
                            return "Access granted";
                        } else {
                            return "Access denied";
                        }
                    } else {
                        return "No permissions";
                    }
                } else {
                    return "User inactive";
                }
            } else {
                return "User not found";
            }
        } else {
            return "Invalid request";
        }
    } else {
        return "Request is null";
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Сколько уровней вложенности?
- Early return?
- Guard clauses?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Слишком глубокая вложенность

**Хорошо**: Early return / guard clauses

**Отлично**: Optional chain

</details>

<details>
<summary>📝 Полный разбор</summary>

**Решение: Guard Clauses**
```java
public String processRequest(Request request) {
    if (request == null) {
        return "Request is null";
    }
    if (!request.isValid()) {
        return "Invalid request";
    }

    User user = userService.findUser(request.getUserId());
    if (user == null) {
        return "User not found";
    }
    if (!user.isActive()) {
        return "User inactive";
    }

    Permission permission = user.getPermission();
    if (permission == null) {
        return "No permissions";
    }
    if (!permission.canAccess(request.getResource())) {
        return "Access denied";
    }

    return "Access granted";
}
```

</details>

---

### Задача 8.6: Дублирование логики

**Компания**: Сбер | **Сложность**: ⭐⭐

```java
public class PaymentService {

    public PaymentResult processCardPayment(CardPayment payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        if (payment.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new ValidationException("Amount exceeds limit");
        }

        // Логирование
        log.info("Processing card payment: {} for amount {}",
                 payment.getId(), payment.getAmount());

        // Обработка
        return cardGateway.process(payment);
    }

    public PaymentResult processBankTransfer(BankTransfer payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        if (payment.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new ValidationException("Amount exceeds limit");
        }

        // Логирование
        log.info("Processing bank transfer: {} for amount {}",
                 payment.getId(), payment.getAmount());

        // Обработка
        return bankGateway.process(payment);
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Какой код дублируется?
- DRY principle?
- Template Method?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Дублирование валидации и логирования

**Хорошо**: Extract Method

**Отлично**: Template Method или общий интерфейс Payment

</details>

<details>
<summary>📝 Полный разбор</summary>

**Решение:**
```java
public interface Payment {
    String getId();
    BigDecimal getAmount();
}

public abstract class PaymentProcessor<T extends Payment> {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    public PaymentResult process(T payment) {
        validateAmount(payment);
        logPayment(payment);
        return doProcess(payment);
    }

    private void validateAmount(Payment payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        if (payment.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Amount exceeds limit");
        }
    }

    private void logPayment(Payment payment) {
        log.info("Processing payment: {} for amount {}",
                 payment.getId(), payment.getAmount());
    }

    protected abstract PaymentResult doProcess(T payment);
}
```

</details>

---

# Категория 9: Комплексные задачи

---

### Задача 9.1: Полный сервис

**Компания**: Сбер | **Сложность**: ⭐⭐⭐

```java
@Service
public class UserService2 {
    private Map<Long, User> cache = new HashMap<>();

    @Autowired
    JdbcTemplate jdbc;

    @Transactional
    private User findUser(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        String sql = "SELECT * FROM users WHERE id = " + id;
        User user = jdbc.queryForObject(sql, new UserRowMapper());
        cache.put(id, user);
        return user;
    }

    public void updateUser(Long id, String name) {
        User user = this.findUser(id);
        user.setName(name);

        try {
            String sql = "UPDATE users SET name = '" + name + "' WHERE id = " + id;
            jdbc.execute(sql);
        } catch (Exception e) {
            // Игнорируем
        }

        cache.put(id, user);
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Thread safety кеша?
- SQL injection?
- @Transactional на private?
- Self-invocation?
- Exception handling?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: 3 проблемы

**Хорошо**: 5 проблем

**Отлично**: 7+ проблем с решениями

</details>

<details>
<summary>📝 Полный разбор</summary>

1. **HashMap не thread-safe** → ConcurrentHashMap
2. **@Transactional на private** → не работает
3. **SQL Injection** (2 места) → PreparedStatement
4. **Self-invocation** `this.findUser()` → @Transactional не сработает
5. **Пустой catch** → логирование, rethrow
6. **Нет валидации id** → может быть null
7. **Кеш не инвалидируется при update** → в updateUser кладём старый объект
8. **Field injection** (`@Autowired JdbcTemplate`) → constructor injection

**Решение:**
```java
@Service
public class UserService {
    private final Map<Long, User> cache = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbc;

    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public User findUser(Long id) {
        Objects.requireNonNull(id, "User id must not be null");

        return cache.computeIfAbsent(id, this::loadUser);
    }

    private User loadUser(Long id) {
        return jdbc.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new UserRowMapper(),
            id
        );
    }

    @Transactional
    public void updateUser(Long id, String name) {
        jdbc.update("UPDATE users SET name = ? WHERE id = ?", name, id);
        cache.remove(id); // Инвалидация
    }
}
```

</details>

---

### Задача 9.2: REST контроллер

**Компания**: ОТП банк | **Сложность**: ⭐⭐⭐

```java
@RestController
public class AccountController {

    @Autowired
    AccountRepository repo;

    @GetMapping("/transfer")
    @Transactional
    public String transfer(Long from, Long to, double amount) {
        Account fromAcc = repo.findById(from).get();
        Account toAcc = repo.findById(to).get();

        String sql = "UPDATE accounts SET balance = balance - " + amount +
                     " WHERE id = " + from;
        repo.executeNative(sql);

        sql = "UPDATE accounts SET balance = balance + " + amount +
              " WHERE id = " + to;
        repo.executeNative(sql);

        return "OK";
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- HTTP метод для модификации?
- SQL injection?
- double для денег?
- Валидация?
- Response?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: 3 проблемы

**Хорошо**: 5 проблем

**Отлично**: 7+ проблем

</details>

<details>
<summary>📝 Полный разбор</summary>

1. **GET для модификации** → POST/PUT
2. **SQL Injection** (2 места)
3. **double для денег** → BigDecimal
4. **Optional.get()** без проверки
5. **Нет валидации amount** (отрицательный?)
6. **Field injection**
7. **Return String** → ResponseEntity с DTO
8. **Нет проверки баланса** fromAcc

**Решение:**
```java
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final TransferService transferService;

    public AccountController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResult> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResult result = transferService.transfer(
            request.getFromAccountId(),
            request.getToAccountId(),
            request.getAmount()
        );

        return ResponseEntity.ok(result);
    }
}

public class TransferRequest {
    @NotNull
    private Long fromAccountId;

    @NotNull
    private Long toAccountId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
```

</details>

---

### Задача 9.3: DAO с проблемами

**Компания**: Иннотех | **Сложность**: ⭐⭐⭐

```java
public class TicketDao {
    private DataSource ds;

    public Ticket findById(Long id) {
        Connection conn = ds.getConnection();
        String sql = "SELECT * FROM tickets WHERE id = " + id;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        Ticket ticket = null;
        if (rs.next()) {
            ticket = new Ticket();
            ticket.setId(rs.getLong("id"));
            ticket.setTitle(rs.getString("title"));
            ticket.setStatus(rs.getString("status"));
        }

        return ticket;
    }

    public void updateStatus(Long id, String status) {
        Connection conn = ds.getConnection();
        String sql = "UPDATE tickets SET status = '" + status + "' WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        conn.commit();
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- Ресурсы закрываются?
- SQL injection?
- Exception handling?
- Throws declaration?
- AutoCommit?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: Resource leak + SQL injection

**Хорошо**: + Exception handling

**Отлично**: Полное решение с try-with-resources

</details>

<details>
<summary>📝 Полный разбор</summary>

1. **Resource leak** — Connection, Statement, ResultSet не закрываются
2. **SQL Injection** (оба метода)
3. **Нет throws SQLException**
4. **conn.commit()** — connection может быть в autoCommit=true
5. **return null** — лучше Optional

**Решение:**
```java
public class TicketDao {
    private final DataSource ds;

    public TicketDao(DataSource ds) {
        this.ds = ds;
    }

    public Optional<Ticket> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTicket(rs));
                }
                return Optional.empty();
            }
        }
    }

    public void updateStatus(Long id, String status) throws SQLException {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setStatus(rs.getString("status"));
        return ticket;
    }
}
```

</details>

---

### Задача 9.4: Многопоточный кеш

**Компания**: Авито | **Сложность**: ⭐⭐⭐

```java
public class DataCache {
    private Map<String, Object> cache = new HashMap<>();
    private boolean initialized = false;

    public Object get(String key) {
        if (!initialized) {
            init();
        }
        return cache.get(key);
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    private void init() {
        // Долгая инициализация
        loadDataFromDatabase();
        initialized = true;
    }

    public void clear() {
        cache.clear();
        initialized = false;
    }
}
```

<details>
<summary>🔍 Подсказки</summary>

- HashMap thread-safe?
- initialized без volatile?
- Double-checked locking нужен?
- init() может вызваться дважды?

</details>

<details>
<summary>✅ Критерии ответа</summary>

**Минимум**: HashMap + initialized не thread-safe

**Хорошо**: ConcurrentHashMap + volatile

**Отлично**: Полное решение с правильной синхронизацией

</details>

<details>
<summary>📝 Полный разбор</summary>

1. **HashMap** не thread-safe
2. **initialized** без volatile — visibility проблема
3. **Race condition** в get() — init() может вызваться несколькими потоками
4. **clear()** — race между clear и get

**Решение:**
```java
public class DataCache {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private final Object initLock = new Object();

    public Object get(String key) {
        ensureInitialized();
        return cache.get(key);
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    private void ensureInitialized() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    loadDataFromDatabase();
                    initialized = true;
                }
            }
        }
    }

    public synchronized void clear() {
        cache.clear();
        initialized = false;
    }
}
```

</details>

---

## Статистика по категориям

| Категория | Кол-во | Сложность |
|-----------|--------|-----------|
| Простые баги | 6 | ⭐-⭐⭐ |
| Ресурсы и исключения | 5 | ⭐-⭐⭐ |
| Concurrency | 6 | ⭐⭐-⭐⭐⭐ |
| Безопасность | 4 | ⭐⭐ |
| Collections | 5 | ⭐-⭐⭐⭐ |
| Spring @Transactional | 6 | ⭐⭐-⭐⭐⭐ |
| Spring Beans | 4 | ⭐⭐-⭐⭐⭐ |
| Рефакторинг | 6 | ⭐-⭐⭐ |
| Комплексные | 4 | ⭐⭐⭐ |
| **Всего** | **42** | |

---

> 💡 **Совет**: Начни с простых категорий (1, 2), затем переходи к Concurrency и Spring. Комплексные задачи решай последними — они проверяют все навыки сразу.
