# Яндекс Java Coding: Расширенный гайд по типам задач

## 📊 Карта типов задач

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ТИПЫ ЗАДАЧ ЯНДЕКСА                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. БИЗНЕС-ЛОГИКА        │  2. STREAM API                          │
│     • Лимиты платежей    │     • Трансформация Map                  │
│     • Rate Limiter       │     • Группировка + агрегация            │
│     • Баланс/Кошелек     │     • flatMap для вложенных              │
│                          │                                          │
│  3. ПАТТЕРНЫ/ООП         │  4. МНОГОПОТОЧНОСТЬ                      │
│     • Мультиитератор     │     • Синхронизация потоков              │
│     • Command (undo)     │     • Producer-Consumer                  │
│     • Builder            │     • Atomic операции                    │
│                          │                                          │
│  5. РАБОТА С КОДОМ       │  6. СТРУКТУРЫ ДАННЫХ                     │
│     • Дописать по тесту  │     • LRU Cache                          │
│     • Рефакторинг        │     • Stack с операциями                 │
│     • Code review        │     • Custom коллекции                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

# ТИП 1: Бизнес-логика (Лимиты, Балансы)

> См. основной гайд — там подробно разобрано

**Ключевые элементы:**
- record для моделей
- Map-based repository
- Clock для тестируемости
- ValidationResult pattern

---

# ТИП 2: Stream API трансформации

## Задача: Развернуть мультимапу

```
Вход:  Map<Integer, List<Long>> = {0: [1, 2], 1: [1], 2: [2, 3]}
Выход: Map<Long, Integer>       = {1: 0, 2: 0, 1: 1, 2: 2, 3: 2}
                                  (но Map не допускает дубли ключей!)
```

### Решение 1: Если ключи уникальны
```java
public Map<Long, Integer> invertMap(Map<Integer, List<Long>> input) {
    return input.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(value -> Map.entry(value, entry.getKey())))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
}
```

### Решение 2: Если ключи могут повторяться → List
```java
public Map<Long, List<Integer>> invertMapToList(Map<Integer, List<Long>> input) {
    return input.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(value -> Map.entry(value, entry.getKey())))
        .collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));
}
```

### Решение 3: С merge function (последний выигрывает)
```java
public Map<Long, Integer> invertMapLastWins(Map<Integer, List<Long>> input) {
    return input.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(value -> Map.entry(value, entry.getKey())))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> replacement  // merge function
        ));
}
```

---

## Задача: Группировка транзакций

```
Дано: List<Transaction> где Transaction(String currency, BigDecimal amount)
Найти: максимальную транзакцию в каждой валюте
```

### Модель
```java
public record Transaction(
    String id,
    String currency,
    BigDecimal amount,
    Instant timestamp
) {}
```

### Решение: maxBy в группировке
```java
public Map<String, Optional<Transaction>> maxByCurrency(List<Transaction> transactions) {
    return transactions.stream()
        .collect(Collectors.groupingBy(
            Transaction::currency,
            Collectors.maxBy(Comparator.comparing(Transaction::amount))
        ));
}
```

### Решение: без Optional в значении
```java
public Map<String, Transaction> maxByCurrencyUnwrapped(List<Transaction> transactions) {
    return transactions.stream()
        .collect(Collectors.toMap(
            Transaction::currency,
            Function.identity(),
            (t1, t2) -> t1.amount().compareTo(t2.amount()) >= 0 ? t1 : t2
        ));
}
```

### Решение: collectingAndThen для unwrap
```java
public Map<String, Transaction> maxByCurrencyClean(List<Transaction> transactions) {
    return transactions.stream()
        .collect(Collectors.groupingBy(
            Transaction::currency,
            Collectors.collectingAndThen(
                Collectors.maxBy(Comparator.comparing(Transaction::amount)),
                opt -> opt.orElseThrow()
            )
        ));
}
```

---

## Stream API: Шпаргалка по Collectors

```java
// Группировка
Collectors.groupingBy(T::getKey)                    // Map<K, List<T>>
Collectors.groupingBy(T::getKey, Collectors.counting())  // Map<K, Long>
Collectors.groupingBy(T::getKey, Collectors.summingLong(T::getValue))  // Map<K, Long>
Collectors.groupingBy(T::getKey, Collectors.maxBy(comparator))  // Map<K, Optional<T>>

// Преобразование после группировки
Collectors.collectingAndThen(downstream, finisher)

// В Map
Collectors.toMap(keyMapper, valueMapper)
Collectors.toMap(keyMapper, valueMapper, mergeFunction)
Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier)

// Партиционирование (разделение на 2 группы)
Collectors.partitioningBy(predicate)               // Map<Boolean, List<T>>

// Статистика
Collectors.summarizingLong(T::getValue)            // LongSummaryStatistics

// Joining строк
Collectors.joining(", ")                           // String
Collectors.joining(", ", "[", "]")                 // [a, b, c]
```

---

# ТИП 3: Паттерны и ООП

## Задача: Мультиитератор

```
Реализовать Iterator, который последовательно проходит по нескольким итераторам.
MultiIterator([iter1, iter2]) → элементы iter1, затем элементы iter2
```

### Решение: Для двух итераторов
```java
public class MultiIterator<T> implements Iterator<T> {
    private final Iterator<T> first;
    private final Iterator<T> second;
    private Iterator<T> current;
    
    public MultiIterator(Iterator<T> first, Iterator<T> second) {
        this.first = first;
        this.second = second;
        this.current = first;
    }
    
    @Override
    public boolean hasNext() {
        if (current.hasNext()) {
            return true;
        }
        if (current == first) {
            current = second;
            return current.hasNext();
        }
        return false;
    }
    
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current.next();
    }
}
```

### Решение: Для произвольного количества
```java
public class MultiIterator<T> implements Iterator<T> {
    private final Iterator<Iterator<T>> iterators;
    private Iterator<T> current;
    
    @SafeVarargs
    public MultiIterator(Iterator<T>... iterators) {
        this.iterators = Arrays.asList(iterators).iterator();
        this.current = this.iterators.hasNext() ? this.iterators.next() : Collections.emptyIterator();
    }
    
    public MultiIterator(List<Iterator<T>> iterators) {
        this.iterators = iterators.iterator();
        this.current = this.iterators.hasNext() ? this.iterators.next() : Collections.emptyIterator();
    }
    
    @Override
    public boolean hasNext() {
        while (!current.hasNext() && iterators.hasNext()) {
            current = iterators.next();
        }
        return current.hasNext();
    }
    
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current.next();
    }
}
```

### Тесты для мультиитератора
```java
class MultiIteratorTest {
    
    @Test
    void shouldIterateOverTwoIterators() {
        var iter1 = List.of(1, 2).iterator();
        var iter2 = List.of(3, 4).iterator();
        var multi = new MultiIterator<>(iter1, iter2);
        
        var result = new ArrayList<Integer>();
        while (multi.hasNext()) {
            result.add(multi.next());
        }
        
        assertEquals(List.of(1, 2, 3, 4), result);
    }
    
    @Test
    void shouldHandleEmptyFirstIterator() {
        var iter1 = Collections.<Integer>emptyIterator();
        var iter2 = List.of(1, 2).iterator();
        var multi = new MultiIterator<>(iter1, iter2);
        
        assertTrue(multi.hasNext());
        assertEquals(1, multi.next());
    }
    
    @Test
    void shouldHandleAllEmpty() {
        var multi = new MultiIterator<Integer>(
            Collections.emptyIterator(),
            Collections.emptyIterator()
        );
        
        assertFalse(multi.hasNext());
    }
    
    @Test
    void shouldThrowWhenNoMoreElements() {
        var multi = new MultiIterator<>(List.of(1).iterator());
        multi.next();
        
        assertThrows(NoSuchElementException.class, multi::next);
    }
}
```

---

## Задача: Буфер редактора с Undo (Command Pattern)

```
Реализовать буфер текстового редактора:
- append(String text)     — добавить в конец
- delete(int n)           — удалить последние n символов
- undo()                  — отменить последнюю операцию
- getContent()            — получить содержимое
```

### Решение с Command Pattern
```java
public class TextBuffer {
    private StringBuilder content = new StringBuilder();
    private final Deque<Command> history = new ArrayDeque<>();
    
    // Интерфейс команды
    private interface Command {
        void undo();
    }
    
    public void append(String text) {
        content.append(text);
        int length = text.length();
        history.push(() -> content.delete(content.length() - length, content.length()));
    }
    
    public void delete(int n) {
        if (n > content.length()) {
            n = content.length();
        }
        String deleted = content.substring(content.length() - n);
        content.delete(content.length() - n, content.length());
        history.push(() -> content.append(deleted));
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }
    
    public String getContent() {
        return content.toString();
    }
}
```

### Альтернатива: Хранение состояний (Memento)
```java
public class TextBufferMemento {
    private StringBuilder content = new StringBuilder();
    private final Deque<String> snapshots = new ArrayDeque<>();
    
    public void append(String text) {
        snapshots.push(content.toString());  // сохранить состояние ДО
        content.append(text);
    }
    
    public void delete(int n) {
        snapshots.push(content.toString());
        int start = Math.max(0, content.length() - n);
        content.delete(start, content.length());
    }
    
    public void undo() {
        if (!snapshots.isEmpty()) {
            content = new StringBuilder(snapshots.pop());
        }
    }
    
    public String getContent() {
        return content.toString();
    }
}
```

### Тесты для буфера
```java
class TextBufferTest {
    
    private TextBuffer buffer;
    
    @BeforeEach
    void setUp() {
        buffer = new TextBuffer();
    }
    
    @Test
    void shouldAppendText() {
        buffer.append("Hello");
        buffer.append(" World");
        
        assertEquals("Hello World", buffer.getContent());
    }
    
    @Test
    void shouldDeleteLastNCharacters() {
        buffer.append("Hello World");
        buffer.delete(6);
        
        assertEquals("Hello", buffer.getContent());
    }
    
    @Test
    void shouldUndoAppend() {
        buffer.append("Hello");
        buffer.append(" World");
        buffer.undo();
        
        assertEquals("Hello", buffer.getContent());
    }
    
    @Test
    void shouldUndoDelete() {
        buffer.append("Hello World");
        buffer.delete(6);
        buffer.undo();
        
        assertEquals("Hello World", buffer.getContent());
    }
    
    @Test
    void shouldUndoMultipleOperations() {
        buffer.append("A");
        buffer.append("B");
        buffer.delete(1);
        buffer.undo();  // отменить delete
        buffer.undo();  // отменить append B
        
        assertEquals("A", buffer.getContent());
    }
    
    @Test
    void shouldHandleUndoOnEmptyHistory() {
        buffer.undo();  // не должно падать
        assertEquals("", buffer.getContent());
    }
    
    @Test
    void shouldHandleDeleteMoreThanLength() {
        buffer.append("Hi");
        buffer.delete(100);
        
        assertEquals("", buffer.getContent());
    }
}
```

---

# ТИП 4: Многопоточность

## Задача: Робот с двумя ногами

```
Два потока печатают "Left" и "Right".
Нужно синхронизировать: Left, Right, Left, Right, ...
```

### Решение 1: wait/notify
```java
public class Robot {
    private final Object lock = new Object();
    private boolean leftTurn = true;
    
    public void left() {
        synchronized (lock) {
            while (!leftTurn) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("Left");
            leftTurn = false;
            lock.notify();
        }
    }
    
    public void right() {
        synchronized (lock) {
            while (leftTurn) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("Right");
            leftTurn = true;
            lock.notify();
        }
    }
    
    public static void main(String[] args) {
        Robot robot = new Robot();
        
        Thread leftThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                robot.left();
            }
        });
        
        Thread rightThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                robot.right();
            }
        });
        
        leftThread.start();
        rightThread.start();
    }
}
```

### Решение 2: Semaphore
```java
public class RobotSemaphore {
    private final Semaphore leftSemaphore = new Semaphore(1);
    private final Semaphore rightSemaphore = new Semaphore(0);
    
    public void left() throws InterruptedException {
        leftSemaphore.acquire();
        System.out.println("Left");
        rightSemaphore.release();
    }
    
    public void right() throws InterruptedException {
        rightSemaphore.acquire();
        System.out.println("Right");
        leftSemaphore.release();
    }
}
```

### Решение 3: Lock + Condition
```java
public class RobotLock {
    private final Lock lock = new ReentrantLock();
    private final Condition leftCondition = lock.newCondition();
    private final Condition rightCondition = lock.newCondition();
    private boolean leftTurn = true;
    
    public void left() throws InterruptedException {
        lock.lock();
        try {
            while (!leftTurn) {
                leftCondition.await();
            }
            System.out.println("Left");
            leftTurn = false;
            rightCondition.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public void right() throws InterruptedException {
        lock.lock();
        try {
            while (leftTurn) {
                rightCondition.await();
            }
            System.out.println("Right");
            leftTurn = true;
            leftCondition.signal();
        } finally {
            lock.unlock();
        }
    }
}
```

---

## Задача: Потокобезопасный счетчик

```
Показать разницу между обычным int, synchronized и AtomicInteger
```

### Демонстрация проблемы
```java
public class CounterDemo {
    
    // ПРОБЛЕМА: Race condition
    static class UnsafeCounter {
        private int count = 0;
        
        public void increment() {
            count++;  // НЕ атомарно! read-modify-write
        }
        
        public int getCount() {
            return count;
        }
    }
    
    // РЕШЕНИЕ 1: synchronized
    static class SynchronizedCounter {
        private int count = 0;
        
        public synchronized void increment() {
            count++;
        }
        
        public synchronized int getCount() {
            return count;
        }
    }
    
    // РЕШЕНИЕ 2: AtomicInteger (лучше для простых операций)
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        
        public void increment() {
            count.incrementAndGet();
        }
        
        public int getCount() {
            return count.get();
        }
    }
    
    // РЕШЕНИЕ 3: ReentrantLock (для сложной логики)
    static class LockCounter {
        private final Lock lock = new ReentrantLock();
        private int count = 0;
        
        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }
        
        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 100;
        int incrementsPerThread = 1000;
        
        // Тест UnsafeCounter
        var unsafe = new UnsafeCounter();
        runTest(unsafe::increment, numThreads, incrementsPerThread);
        System.out.println("Unsafe: " + unsafe.getCount() + 
            " (expected: " + (numThreads * incrementsPerThread) + ")");
        
        // Тест AtomicCounter
        var atomic = new AtomicCounter();
        runTest(atomic::increment, numThreads, incrementsPerThread);
        System.out.println("Atomic: " + atomic.getCount());
    }
    
    private static void runTest(Runnable task, int numThreads, int iterations) 
            throws InterruptedException {
        var executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    task.run();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}
```

---

## Многопоточность: Шпаргалка

```java
// СИНХРОНИЗАЦИЯ
synchronized (lock) { ... }           // блок
public synchronized void method()     // метод

// WAIT/NOTIFY (только внутри synchronized!)
lock.wait();                          // ждать
lock.notify();                        // разбудить один
lock.notifyAll();                     // разбудить все

// ATOMIC
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();            // ++counter
counter.getAndIncrement();            // counter++
counter.compareAndSet(expected, new); // CAS
counter.updateAndGet(x -> x + 1);     // с функцией

// LOCK
Lock lock = new ReentrantLock();
lock.lock();
try { ... } finally { lock.unlock(); }

// CONDITION
Condition cond = lock.newCondition();
cond.await();                         // как wait()
cond.signal();                        // как notify()

// SEMAPHORE
Semaphore sem = new Semaphore(permits);
sem.acquire();                        // взять permit
sem.release();                        // вернуть permit

// EXECUTOR
ExecutorService exec = Executors.newFixedThreadPool(n);
exec.submit(runnable);
exec.shutdown();
exec.awaitTermination(timeout, unit);

// COMPLETABLEFUTURE
CompletableFuture.supplyAsync(() -> compute())
    .thenApply(result -> transform(result))
    .thenAccept(System.out::println);
```

---

# ТИП 5: Структуры данных

## Задача: Stack с операцией getMax() за O(1)

```
Реализовать стек с операциями:
- push(x)   — O(1)
- pop()     — O(1)  
- peek()    — O(1)
- getMax()  — O(1)  ← это ключевое!
```

### Решение: Два стека
```java
public class MaxStack {
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> maxStack = new ArrayDeque<>();
    
    public void push(int value) {
        stack.push(value);
        if (maxStack.isEmpty() || value >= maxStack.peek()) {
            maxStack.push(value);
        } else {
            maxStack.push(maxStack.peek());
        }
    }
    
    public int pop() {
        if (stack.isEmpty()) {
            throw new NoSuchElementException();
        }
        maxStack.pop();
        return stack.pop();
    }
    
    public int peek() {
        if (stack.isEmpty()) {
            throw new NoSuchElementException();
        }
        return stack.peek();
    }
    
    public int getMax() {
        if (maxStack.isEmpty()) {
            throw new NoSuchElementException();
        }
        return maxStack.peek();
    }
    
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
```

### Тесты
```java
class MaxStackTest {
    
    private MaxStack stack;
    
    @BeforeEach
    void setUp() {
        stack = new MaxStack();
    }
    
    @Test
    void shouldTrackMaxOnPush() {
        stack.push(3);
        assertEquals(3, stack.getMax());
        
        stack.push(5);
        assertEquals(5, stack.getMax());
        
        stack.push(2);
        assertEquals(5, stack.getMax());
    }
    
    @Test
    void shouldUpdateMaxOnPop() {
        stack.push(3);
        stack.push(5);
        stack.push(2);
        
        stack.pop();
        assertEquals(5, stack.getMax());
        
        stack.pop();
        assertEquals(3, stack.getMax());
    }
    
    @Test
    void shouldHandleDuplicateMax() {
        stack.push(5);
        stack.push(5);
        stack.push(3);
        
        stack.pop();
        assertEquals(5, stack.getMax());
        
        stack.pop();
        assertEquals(5, stack.getMax());
    }
}
```

---

## Задача: LRU Cache

```
Реализовать кэш с ограниченным размером.
При превышении удаляется наименее недавно использованный элемент.
```

### Решение 1: LinkedHashMap (простое)
```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);  // accessOrder = true!
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

### Решение 2: HashMap + DoublyLinkedList (для понимания)
```java
public class LRUCacheManual<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> cache = new HashMap<>();
    private final Node<K, V> head = new Node<>(null, null);  // dummy
    private final Node<K, V> tail = new Node<>(null, null);  // dummy
    
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public LRUCacheManual(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }
    
    public V get(K key) {
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }
    
    public void put(K key, V value) {
        Node<K, V> node = cache.get(key);
        if (node != null) {
            node.value = value;
            moveToHead(node);
        } else {
            Node<K, V> newNode = new Node<>(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
            
            if (cache.size() > capacity) {
                Node<K, V> removed = removeTail();
                cache.remove(removed.key);
            }
        }
    }
    
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void addToHead(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
    
    private Node<K, V> removeTail() {
        Node<K, V> node = tail.prev;
        removeNode(node);
        return node;
    }
}
```

---

## Задача: uniqueSorted — убрать дубликаты и отсортировать

```java
public <T extends Comparable<T>> List<T> uniqueSorted(Collection<T> input) {
    // Вариант 1: TreeSet (автоматически sorted + unique)
    return new ArrayList<>(new TreeSet<>(input));
    
    // Вариант 2: Stream
    return input.stream()
        .distinct()
        .sorted()
        .toList();
}
```

---

# 📋 Сводный чеклист по всем типам

## Stream API
```
□ flatMap для вложенных структур
□ Collectors.groupingBy + downstream
□ Collectors.toMap с merge function
□ Map.entry(key, value) для пар
□ collectingAndThen для unwrap Optional
```

## Паттерны
```
□ Iterator: hasNext(), next(), NoSuchElementException
□ Command: интерфейс с undo(), Deque для истории
□ Memento: сохранение состояний
□ Builder: цепочка методов
```

## Многопоточность
```
□ synchronized + wait/notify (while loop!)
□ AtomicInteger для счетчиков
□ Semaphore для permits
□ Lock + Condition для сложной логики
□ ExecutorService для пулов
```

## Структуры данных
```
□ Deque для стека: push/pop/peek
□ LinkedHashMap для LRU (accessOrder=true)
□ TreeSet для sorted + unique
□ HashMap + LinkedList для сложных кэшей
```

---

# 🎯 Что точно НЕ будет

```
✗ Графы, деревья (DFS, BFS глубокие)
✗ Dynamic Programming
✗ LeetCode Hard
✗ Фреймворки (Spring, Hibernate)
✗ Внешние API, БД, сеть
```

---

# 📝 Quick Reference на одну страницу

```
STREAM TRANSFORM:
  input.entrySet().stream()
    .flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))

GROUPING + MAX:
  stream.collect(groupingBy(T::getKey, maxBy(comparing(T::getValue))))

MULTI-ITERATOR:
  while (!current.hasNext() && iterators.hasNext()) current = iterators.next();
  return current.hasNext();

COMMAND UNDO:
  history.push(() -> undo_action);
  history.pop().undo();

WAIT/NOTIFY:
  synchronized(lock) { while(!condition) lock.wait(); doWork(); lock.notify(); }

ATOMIC:
  AtomicInteger count = new AtomicInteger(0);
  count.incrementAndGet();

MAX STACK:
  mainStack + maxStack (оба push/pop синхронно)

LRU:
  new LinkedHashMap<>(cap, 0.75f, true) { removeEldestEntry -> size() > cap }
```
