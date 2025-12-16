# Q&A по многопоточности в Java - Собеседование

## Содержание
1. [Базовые вопросы](#базовые-вопросы)
2. [Синхронизация](#синхронизация)
3. [Locks и Atomic](#locks-и-atomic)
4. [ExecutorService и пулы потоков](#executorservice-и-пулы-потоков)
5. [CompletableFuture](#completablefuture)
6. [Concurrent коллекции](#concurrent-коллекции)
7. [Синхронизаторы](#синхронизаторы)
8. [BlockingQueue](#blockingqueue)
9. [Виртуальные потоки (Java 21+)](#виртуальные-потоки-java-21)
10. [Проблемы многопоточности](#проблемы-многопоточности)
11. [Практические задачи](#практические-задачи)
12. [Продвинутые вопросы](#продвинутые-вопросы)

---

## Базовые вопросы

### Q1: Что такое поток (Thread) в Java?

**Ответ:** Поток - это независимый путь выполнения кода внутри процесса. Каждый поток имеет собственный стек вызовов, счетчик команд и регистры, но разделяет память с другими потоками того же процесса.

**Ключевые моменты:**
- Легковесная единица выполнения
- Разделяют адресное пространство процесса
- Имеют собственный стек, но разделяют heap
- Управляются JVM и планировщиком ОС

### Q2: В чем разница между процессом и потоком?

**Ответ:**
- **Процесс** - отдельная программа в памяти со своим адресным пространством
- **Поток** - единица выполнения внутри процесса

| Аспект | Процесс | Поток |
|--------|---------|-------|
| Память | Собственное адресное пространство | Разделяют память процесса |
| Создание | Дорогое (копирование памяти) | Дешевое |
| Коммуникация | IPC (pipes, sockets) | Разделяемая память |
| Изоляция | Высокая | Низкая |

### Q3: Как создать поток в Java?

**Ответ:** Есть несколько способов:

```java
// 1. Наследование от Thread
class MyThread extends Thread {
    public void run() {
        System.out.println("Running in thread");
    }
}

// 2. Реализация Runnable
class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Running via Runnable");
    }
}

// 3. Реализация Callable (с результатом)
class MyCallable implements Callable<String> {
    public String call() {
        return "Task result";
    }
}

// 4. Lambda (Java 8+)
Thread t = new Thread(() -> System.out.println("Lambda thread"));
```

### Q4: В чем разница между start() и run()?

**Ответ:**
- **start()** - создает новый поток и вызывает run() в контексте этого потока
- **run()** - выполняется в текущем потоке (как обычный метод)

```java
Thread t = new Thread(() -> System.out.println("Thread: " + Thread.currentThread().getName()));
t.run();   // Выполнится в main потоке
t.start(); // Выполнится в новом потоке
```

### Q5: Можно ли запустить поток дважды?

**Ответ:** Нет! После вызова start() поток переходит в состояние TERMINATED после завершения. Повторный вызов start() бросит `IllegalThreadStateException`.

```java
Thread t = new Thread(() -> System.out.println("Hello"));
t.start(); // OK
t.start(); // IllegalThreadStateException!
```

### Q6: Какие состояния может иметь поток?

**Ответ:** В Java поток может находиться в одном из следующих состояний:

- **NEW** - создан, но не запущен
- **RUNNABLE** - выполняется или готов к выполнению
- **BLOCKED** - заблокирован на получении монитора
- **WAITING** - ждет неопределенно долго
- **TIMED_WAITING** - ждет определенное время
- **TERMINATED** - завершен

### Q7: Что такое демон-потоки?

**Ответ:** Демон-потоки - это служебные потоки, которые не препятствуют завершению JVM. Когда остаются только демон-потоки, JVM завершает работу.

```java
Thread daemon = new Thread(() -> {
    while (true) {
        // бесконечная работа
    }
});
daemon.setDaemon(true); // Должно быть ДО start()!
daemon.start();
```

**Примеры:** сборщик мусора, финализаторы потоков.

---

## Синхронизация

### Q8: Что такое synchronized в Java?

**Ответ:** Ключевое слово synchronized обеспечивает взаимное исключение - только один поток может выполнять синхронизированный блок/метод в данный момент.

**Два вида:**
```java
// Синхронизированный метод
public synchronized void method() { }

// Синхронизированный блок
synchronized(object) { 
    // критическая секция
}
```

### Q9: Какая разница между синхронизированным методом и блоком?

**Ответ:**

| Аспект | Синхронизированный метод | Синхронизированный блок |
|--------|-------------------------|------------------------|
| Объект блокировки | this (для instance) или Class (для static) | Любой объект |
| Гранулярность | Весь метод | Выбранный участок кода |
| Производительность | Может блокировать дольше | Более точечная блокировка |

```java
public synchronized void method1() {
    // Блокируется на this
}

public void method2() {
    Object lock = new Object();
    synchronized(lock) {
        // Блокируется на lock
        // Только критическая секция
    }
}
```

### Q10: Что такое volatile?

**Ответ:** Volatile гарантирует:
1. **Видимость** - изменения одним потоком видны другим
2. **Запрет кэширования** - чтение/запись всегда из основной памяти
3. **Запрет переупорядочивания** операций

```java
public class VolatileExample {
    private volatile boolean flag = false;
    
    public void setFlag() {
        flag = true; // Изменение сразу видно всем потокам
    }
    
    public boolean getFlag() {
        return flag; // Всегда читается из памяти
    }
}
```

**⚠️ Важно:** volatile НЕ обеспечивает атомарность сложных операций!

### Q11: В чем разница между volatile и synchronized?

**Ответ:**

| Аспект | volatile | synchronized |
|--------|----------|--------------|
| Атомарность | Только для примитивов/ссылок | Гарантирует |
| Видимость | Гарантирует | Гарантирует |
| Блокировка | Нет | Да |
| Производительность | Быстрее | Медленнее |
| Применение | Простые флаги | Сложная синхронизация |

### Q12: Что такое wait(), notify(), notifyAll()?

**Ответ:** Методы для координации потоков:

- **wait()** - поток отпускает монитор и ждет
- **notify()** - будит один ожидающий поток
- **notifyAll()** - будит все ожидающие потоки

```java
public class ProducerConsumer {
    private final Object lock = new Object();
    private boolean ready = false;
    
    public void consume() throws InterruptedException {
        synchronized(lock) {
            while (!ready) {
                lock.wait(); // Ждем, пока не будет готово
            }
            // Потребляем данные
        }
    }
    
    public void produce() {
        synchronized(lock) {
            // Производим данные
            ready = true;
            lock.notify(); // Уведомляем ожидающих
        }
    }
}
```

**⚠️ Важно:** Вызывать только внутри synchronized блока на том же объекте!

---

## Locks и Atomic

### Q13: В чем преимущества ReentrantLock над synchronized?

**Ответ:**

| Возможность | synchronized | ReentrantLock |
|------------|-------------|---------------|
| Попытка блокировки | Нет | tryLock() |
| Таймаут | Нет | tryLock(timeout) |
| Прерывание | Нет | lockInterruptibly() |
| Справедливость | Нет | fair lock |
| Условные переменные | Одна (wait/notify) | Множественные Condition |

```java
ReentrantLock lock = new ReentrantLock(true); // справедливый замок

public void method() {
    if (lock.tryLock(1, TimeUnit.SECONDS)) {
        try {
            // критическая секция
        } finally {
            lock.unlock(); // ОБЯЗАТЕЛЬНО в finally!
        }
    }
}
```

### Q14: Что такое атомарные классы?

**Ответ:** Классы из пакета java.util.concurrent.atomic, которые обеспечивают потокобезопасные операции без синхронизации:

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();    // ++counter (атомарно)
counter.getAndIncrement();    // counter++ (атомарно)
counter.compareAndSet(0, 10); // CAS операция

AtomicReference<String> ref = new AtomicReference<>("initial");
ref.compareAndSet("initial", "new value");
```

**Принцип работы:** Compare-and-Swap (CAS) на уровне процессора.

### Q15: Что такое CAS (Compare-and-Swap)?

**Ответ:** Атомарная операция процессора, которая:
1. Сравнивает текущее значение с ожидаемым
2. Если совпадает - заменяет на новое
3. Возвращает результат операции

```java
public final boolean compareAndSet(int expect, int update) {
    // Атомарно на уровне процессора:
    // if (current == expect) {
    //     current = update;
    //     return true;
    // }
    // return false;
}
```

**Преимущества:** lock-free алгоритмы, высокая производительность.

---

## ExecutorService и пулы потоков

### Q16: Что такое ExecutorService?

**Ответ:** Высокоуровневый API для управления потоками, который предоставляет:
- Пулы потоков
- Планирование задач
- Управление жизненным циклом
- Получение результатов выполнения

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

// Выполнение без результата
executor.execute(() -> System.out.println("Task"));

// Выполнение с результатом
Future<String> future = executor.submit(() -> "Result");
String result = future.get();

// Завершение
executor.shutdown();
```

### Q17: Какие типы пулов потоков существуют?

**Ответ:**

```java
// Фиксированное количество потоков
ExecutorService fixed = Executors.newFixedThreadPool(4);

// Кэшированный пул (создает по необходимости)
ExecutorService cached = Executors.newCachedThreadPool();

// Один поток
ExecutorService single = Executors.newSingleThreadExecutor();

// Планировщик задач
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
```

### Q18: В чем разница между execute() и submit()?

**Ответ:**

| Метод | Возвращает | Принимает | Обработка исключений |
|-------|-----------|----------|---------------------|
| execute() | void | Runnable | Проглатывает |
| submit() | Future | Runnable/Callable | Через Future.get() |

```java
executor.execute(() -> System.out.println("Fire and forget"));

Future<?> future = executor.submit(() -> {
    if (Math.random() > 0.5) throw new RuntimeException();
});

try {
    future.get(); // Исключение будет брошено здесь
} catch (ExecutionException e) {
    Throwable cause = e.getCause(); // Оригинальное исключение
}
```

### Q19: Как правильно завершить ExecutorService?

**Ответ:**

```java
public void shutdownExecutor(ExecutorService executor) {
    executor.shutdown(); // Новые задачи не принимаются
    
    try {
        // Ждем завершения текущих задач
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Принудительное завершение
            
            // Ждем ответа на прерывание
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Pool did not terminate");
            }
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

### Q20: Что такое ThreadPoolExecutor?

**Ответ:** Гибкая реализация ExecutorService с настраиваемыми параметрами:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                                    // corePoolSize
    4,                                    // maximumPoolSize
    60L, TimeUnit.SECONDS,               // keepAliveTime
    new LinkedBlockingQueue<>(100),      // workQueue
    Executors.defaultThreadFactory(),    // threadFactory
    new ThreadPoolExecutor.AbortPolicy() // rejectedExecutionHandler
);
```

**Параметры:**
- **corePoolSize** - минимальное количество потоков
- **maximumPoolSize** - максимальное количество
- **keepAliveTime** - время жизни лишних потоков
- **workQueue** - очередь задач
- **rejectionHandler** - что делать при переполнении

---

## CompletableFuture

### Q21: Что такое CompletableFuture?

**Ответ:** CompletableFuture - это Future, который можно завершить вручную и поддерживает цепочки асинхронных операций.

```java
// Создание
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "Hello World";
});

// Цепочка операций
CompletableFuture<String> result = future
    .thenApply(s -> s.toUpperCase())
    .thenApply(s -> s + "!")
    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s.length()))
    .thenApply(len -> "Length: " + len);
```

### Q22: В чем разница между thenApply(), thenCompose() и thenCombine()?

**Ответ:**

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello");

// thenApply - преобразование результата (синхронное)
future.thenApply(s -> s.toUpperCase()); // CompletableFuture<String>

// thenCompose - цепочка асинхронных операций (flatMap)
future.thenCompose(s -> CompletableFuture.supplyAsync(() -> s.length())); // CompletableFuture<Integer>

// thenCombine - объединение двух Future
CompletableFuture<String> other = CompletableFuture.supplyAsync(() -> "world");
future.thenCombine(other, (s1, s2) -> s1 + " " + s2); // CompletableFuture<String>
```

### Q23: Как обрабатывать ошибки в CompletableFuture?

**Ответ:**

```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        if (Math.random() > 0.5) throw new RuntimeException("Error!");
        return "Success";
    })
    .exceptionally(throwable -> {
        System.out.println("Error: " + throwable.getMessage());
        return "Default value";
    })
    .handle((result, throwable) -> {
        if (throwable != null) {
            return "Handled error: " + throwable.getMessage();
        }
        return result;
    });
```

---

## Concurrent коллекции

### Q24: В чем разница между HashMap и ConcurrentHashMap?

**Ответ:**

| Аспект | HashMap | ConcurrentHashMap |
|--------|---------|------------------|
| Потокобезопасность | Нет | Да |
| Производительность | Высокая (одиночный поток) | Высокая (многопоточность) |
| Null значения | Разрешает | Не разрешает |
| Итераторы | fail-fast | weakly consistent |

```java
// HashMap - НЕ потокобезопасен
Map<String, String> map = new HashMap<>();

// ConcurrentHashMap - потокобезопасен
Map<String, String> concurrentMap = new ConcurrentHashMap<>();

// Атомарные операции
concurrentMap.putIfAbsent("key", "value");
concurrentMap.compute("key", (k, v) -> v + "!");
concurrentMap.merge("counter", 1, Integer::sum);
```

### Q25: Что такое CopyOnWriteArrayList?

**Ответ:** Потокобезопасная реализация List, которая создает копию массива при каждой модификации.

```java
List<String> list = new CopyOnWriteArrayList<>();
list.add("item1");
list.add("item2");

// Итерация всегда безопасна
for (String item : list) {
    // Даже если другой поток модифицирует список
    System.out.println(item);
}
```

**Применение:** частое чтение, редкая запись (listeners, observers).

### Q26: Какие еще concurrent коллекции есть в Java?

**Ответ:**

```java
// Очереди
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<>();

// Навигационные структуры
ConcurrentSkipListMap<String, Integer> skipMap = new ConcurrentSkipListMap<>();
ConcurrentSkipListSet<String> skipSet = new ConcurrentSkipListSet<>();

// Блокирующие очереди
BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(10);
```

---

## Синхронизаторы

### Q27: Что такое CountDownLatch?

**Ответ:** Синхронизатор, который позволяет потокам ждать, пока счетчик не достигнет нуля.

```java
CountDownLatch latch = new CountDownLatch(3); // ждем 3 события

// Рабочие потоки
for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        // работа...
        latch.countDown(); // уменьшаем счетчик
    }).start();
}

// Главный поток
latch.await(); // ждем, пока счетчик не станет 0
System.out.println("Все задачи выполнены!");
```

**Особенность:** одноразовый (нельзя сбросить).

### Q28: В чем разница между CountDownLatch и CyclicBarrier?

**Ответ:**

| Аспект | CountDownLatch | CyclicBarrier |
|--------|---------------|---------------|
| Переиспользование | Одноразовый | Многоразовый |
| Кто ждет | Другие потоки | Сами участники |
| Действие при срабатывании | Нет | Может быть |

```java
// CountDownLatch - ждут другие
CountDownLatch latch = new CountDownLatch(3);
// работники делают countDown(), главный поток ждет

// CyclicBarrier - ждут сами участники
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("Все достигли барьера!");
});

// каждый поток вызывает barrier.await()
```

### Q29: Что такое Semaphore?

**Ответ:** Семафор контролирует доступ к ресурсу с ограниченным количеством разрешений.

```java
Semaphore semaphore = new Semaphore(2); // максимум 2 потока

public void useResource() {
    try {
        semaphore.acquire(); // получить разрешение
        // использование ресурса
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        semaphore.release(); // освободить разрешение
    }
}
```

**Применение:** пул соединений, лимиты на ресурсы.

---

## BlockingQueue

### Q30: Что такое BlockingQueue?

**Ответ:** Очередь, которая блокирует операции при определенных условиях:
- **put()** блокируется, если очередь полная
- **take()** блокируется, если очередь пустая

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

// Producer
new Thread(() -> {
    try {
        queue.put("item"); // блокируется при переполнении
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();

// Consumer
new Thread(() -> {
    try {
        String item = queue.take(); // блокируется при пустой очереди
        System.out.println("Consumed: " + item);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();
```

### Q31: Какие реализации BlockingQueue существуют?

**Ответ:**

```java
// Фиксированный размер (массив)
BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(100);

// Неограниченный размер (связный список)
BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();

// Ограниченный связный список
BlockingQueue<String> boundedLinked = new LinkedBlockingQueue<>(1000);

// Синхронная передача (размер 0)
BlockingQueue<String> synchronous = new SynchronousQueue<>();

// С приоритетом
BlockingQueue<Integer> priority = new PriorityBlockingQueue<>();

// С задержкой
BlockingQueue<DelayedTask> delay = new DelayQueue<>();
```

---

## Виртуальные потоки (Java 21+)

### Q32: Что такое виртуальные потоки?

**Ответ:** Легковесные потоки, управляемые JVM, а не ОС. Позволяют создавать миллионы потоков без значительных накладных расходов.

```java
// Создание виртуального потока
Thread virtual = Thread.ofVirtual().start(() -> {
    System.out.println("Virtual thread: " + Thread.currentThread());
});

// С именем
Thread named = Thread.ofVirtual()
    .name("my-virtual-thread")
    .start(() -> { /* task */ });

// ExecutorService с виртуальными потоками
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> { /* task */ });
}
```

### Q33: В чем преимущества виртуальных потоков?

**Ответ:**

**Преимущества:**
- Миллионы потоков без проблем с памятью
- Простая модель программирования (как обычные потоки)
- Автоматическое управление планированием
- Отлично для I/O-интенсивных задач

**Ограничения:**
- Прикрепляются к carrier thread при использовании synchronized
- Не подходят для CPU-интенсивных задач

### Q34: Когда виртуальные потоки "прикрепляются"?

**Ответ:** Виртуальные потоки прикрепляются к carrier thread в двух случаях:

```java
// 1. Использование synchronized
synchronized(this) {
    Thread.sleep(1000); // блокирует carrier thread!
}

// ✅ ЛУЧШЕ: используйте ReentrantLock
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    Thread.sleep(1000); // НЕ блокирует carrier thread
} finally {
    lock.unlock();
}

// 2. Нативные вызовы и foreign функции
// В этих случаях тоже происходит pinning
```

### Q35: Как мониторить виртуальные потоки?

**Ответ:**

```java
// Проверка типа потока
if (Thread.currentThread().isVirtual()) {
    System.out.println("Это виртуальный поток");
}

// JFR события для мониторинга:
// - jdk.VirtualThreadStart
// - jdk.VirtualThreadEnd  
// - jdk.VirtualThreadPinned

// Системные свойства
System.setProperty("jdk.tracePinnedThreads", "full");
```

---

## Проблемы многопоточности

### Q36: Что такое Race Condition?

**Ответ:** Ситуация, когда результат выполнения зависит от порядка выполнения потоков.

```java
// ❌ ПРОБЛЕМА: Race condition
public class Counter {
    private int count = 0;
    
    public void increment() {
        count++; // НЕ атомарная операция: read -> modify -> write
    }
}

// ✅ РЕШЕНИЕ 1: synchronized
public synchronized void increment() {
    count++;
}

// ✅ РЕШЕНИЕ 2: AtomicInteger
private AtomicInteger count = new AtomicInteger(0);
public void increment() {
    count.incrementAndGet();
}
```

### Q37: Что такое Deadlock?

**Ответ:** Взаимная блокировка - ситуация, когда потоки ждут друг друга и никто не может продолжить выполнение.

```java
// ❌ DEADLOCK
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {          // Thread A захватывает lock1
            synchronized (lock2) {      // Thread A ждет lock2
                // работа
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) {          // Thread B захватывает lock2
            synchronized (lock1) {      // Thread B ждет lock1 -> DEADLOCK!
                // работа
            }
        }
    }
}

// ✅ РЕШЕНИЕ: фиксированный порядок захвата
public void method1() {
    synchronized (lock1) {  // Всегда сначала lock1
        synchronized (lock2) {  // Потом lock2
            // работа
        }
    }
}
```

### Q38: Что такое Livelock?

**Ответ:** Ситуация, когда потоки не заблокированы, но постоянно реагируют друг на друга и не могут завершить работу.

```java
// Пример: два "вежливых" потока постоянно уступают ресурс друг другу
class PolitePerson {
    public void passResource(Resource resource, PolitePerson other) {
        while (other.needsResource()) {
            // Уступаем ресурс
            Thread.yield();
            // Но другой тоже уступает - livelock!
        }
    }
}
```

### Q39: Что такое Thread Starvation?

**Ответ:** Ситуация, когда поток не может получить доступ к ресурсам из-за других потоков с более высоким приоритетом.

```java
// Поток с низким приоритетом может никогда не получить процессор
Thread lowPriority = new Thread(() -> {
    // важная работа, но низкий приоритет
});
lowPriority.setPriority(Thread.MIN_PRIORITY);

// ✅ РЕШЕНИЕ: справедливые блокировки
ReentrantLock fairLock = new ReentrantLock(true); // fair = true
```

---

## Практические задачи

### Q40: Реализуйте потокобезопасный Singleton

**Ответ:**

```java
// ✅ Лучшее решение: Initialization-on-demand holder
public class Singleton {
    private Singleton() {}
    
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}

// ✅ Альтернатива: Double-checked locking
public class Singleton2 {
    private static volatile Singleton2 instance;
    
    public static Singleton2 getInstance() {
        if (instance == null) {
            synchronized (Singleton2.class) {
                if (instance == null) {
                    instance = new Singleton2();
                }
            }
        }
        return instance;
    }
}
```

### Q41: Реализуйте Producer-Consumer

**Ответ:**

```java
public class ProducerConsumer {
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    
    class Producer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    String item = "item-" + i;
                    queue.put(item); // блокируется при переполнении
                    System.out.println("Produced: " + item);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String item = queue.take(); // блокируется при пустой очереди
                    System.out.println("Consumed: " + item);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void start() {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
    }
}
```

### Q42: Реализуйте пул соединений

**Ответ:**

```java
public class ConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final int maxSize;
    
    public ConnectionPool(int maxSize) {
        this.maxSize = maxSize;
        this.pool = new ArrayBlockingQueue<>(maxSize);
        
        // Инициализация пула
        for (int i = 0; i < maxSize; i++) {
            pool.offer(createConnection());
        }
    }
    
    public Connection acquire() throws InterruptedException {
        return pool.take(); // ждем, если пул пуст
    }
    
    public void release(Connection connection) {
        if (connection != null) {
            pool.offer(connection); // возвращаем в пул
        }
    }
    
    public Connection acquireWithTimeout(long timeout, TimeUnit unit) 
            throws InterruptedException {
        return pool.poll(timeout, unit);
    }
    
    private Connection createConnection() {
        // создание реального соединения
        return new DatabaseConnection();
    }
}

// Использование
ConnectionPool pool = new ConnectionPool(10);

Connection conn = pool.acquire();
try {
    // работа с соединением
} finally {
    pool.release(conn); // ОБЯЗАТЕЛЬНО в finally!
}
```

---

## Продвинутые вопросы

### Q43: Что такое Lock-free алгоритмы?

**Ответ:** Алгоритмы, которые не используют блокировки, а полагаются на атомарные операции (CAS).

```java
public class LockFreeStack<T> {
    private volatile Node<T> head;
    
    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        Node<T> currentHead;
        do {
            currentHead = head;
            newNode.next = currentHead;
        } while (!compareAndSetHead(currentHead, newNode));
    }
    
    public T pop() {
        Node<T> currentHead;
        Node<T> newHead;
        do {
            currentHead = head;
            if (currentHead == null) return null;
            newHead = currentHead.next;
        } while (!compareAndSetHead(currentHead, newHead));
        
        return currentHead.item;
    }
    
    private boolean compareAndSetHead(Node<T> expect, Node<T> update) {
        // Используем AtomicReference для CAS
        // return headReference.compareAndSet(expect, update);
    }
}
```

### Q44: Что такое Memory Model в Java?

**Ответ:** Набор правил, определяющих как потоки взаимодействуют с памятью.

**Ключевые концепции:**
- **Happens-before** отношение
- **Видимость** изменений между потоками
- **Переупорядочивание** операций

```java
// Happens-before отношения:
// 1. Программный порядок в одном потоке
// 2. Synchronized блоки
// 3. volatile поля
// 4. Thread.start() и Thread.join()
// 5. Конструктор объекта и finalize()

class MemoryModelExample {
    private boolean flag = false;
    private int data = 0;
    
    // Thread A
    public void writer() {
        data = 42;      // 1
        flag = true;    // 2 - может быть переупорядочено с 1!
    }
    
    // Thread B  
    public void reader() {
        if (flag) {     // 3
            int value = data; // 4 - может прочитать 0!
        }
    }
}

// ✅ РЕШЕНИЕ: volatile обеспечивает happens-before
private volatile boolean flag = false;
```

### Q45: Что такое StampedLock?

**Ответ:** Продвинутая блокировка с оптимистичным чтением (Java 8+).

```java
public class StampedLockExample {
    private final StampedLock lock = new StampedLock();
    private double x, y;
    
    // Оптимистичное чтение
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead(); // получаем штамп
        double curX = x, curY = y;             // читаем данные
        
        if (!lock.validate(stamp)) {           // проверяем штамп
            // Данные изменились, нужна настоящая блокировка
            stamp = lock.readLock();
            try {
                curX = x;
                curY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Math.sqrt(curX * curX + curY * curY);
    }
    
    // Эксклюзивная запись
    public void write(double newX, double newY) {
        long stamp = lock.writeLock();
        try {
            x = newX;
            y = newY;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
```

### Q46: Как работает ForkJoinPool?

**Ответ:** Специализированный пул потоков для рекурсивных задач с work-stealing алгоритмом.

```java
public class ForkJoinSumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private final int[] array;
    private final int start, end;
    
    @Override
    protected Long compute() {
        int length = end - start;
        
        if (length <= THRESHOLD) {
            // Прямое вычисление
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Разделяй и властвуй
            int middle = start + length / 2;
            ForkJoinSumTask leftTask = new ForkJoinSumTask(array, start, middle);
            ForkJoinSumTask rightTask = new ForkJoinSumTask(array, middle, end);
            
            leftTask.fork();              // асинхронный запуск
            long rightResult = rightTask.compute(); // синхронное выполнение
            long leftResult = leftTask.join();      // ожидание результата
            
            return leftResult + rightResult;
        }
    }
}

// Использование
ForkJoinPool pool = ForkJoinPool.commonPool();
Long result = pool.invoke(new ForkJoinSumTask(array, 0, array.length));
```

### Q47: Что такое Phaser?

**Ответ:** Гибкая альтернатива CyclicBarrier для многофазных операций.

```java
public class PhaserExample {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(3); // 3 участника
        
        for (int i = 0; i < 3; i++) {
            int workerId = i;
            new Thread(() -> {
                // Фаза 1
                System.out.println("Worker " + workerId + " - Phase 1");
                phaser.arriveAndAwaitAdvance(); // ждем всех
                
                // Фаза 2
                System.out.println("Worker " + workerId + " - Phase 2");
                phaser.arriveAndAwaitAdvance(); // ждем всех
                
                // Фаза 3
                System.out.println("Worker " + workerId + " - Phase 3");
                phaser.arriveAndDeregister(); // покидаем фазер
            }).start();
        }
        
        // Главный поток тоже может участвовать
        phaser.register();
        System.out.println("Main thread participating");
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndDeregister();
    }
}
```

---

## Полезные советы для собеседования

### Как отвечать на вопросы по многопоточности:

1. **Начинайте с определения** - четко объясните базовую концепцию
2. **Приводите примеры кода** - показывайте понимание на практике  
3. **Обсуждайте плюсы и минусы** - демонстрируйте глубокое понимание
4. **Упоминайте альтернативы** - знайте разные подходы к решению
5. **Говорите о проблемах** - обсуждайте типичные ошибки и как их избежать

### Типичные ошибки, которых стоит избегать:

- Путать start() и run()
- Забывать про InterruptedException
- Использовать HashMap вместо ConcurrentHashMap
- Не понимать разницу между synchronized и volatile
- Неправильно завершать ExecutorService
- Не учитывать порядок захвата блокировок (deadlock)

### Ключевые темы для изучения:

- **Базовые концепции**: Thread, Runnable, синхронизация
- **Concurrent пакет**: ExecutorService, CompletableFuture, атомарные классы
- **Продвинутые темы**: Lock-free алгоритмы, Memory Model
- **Виртуальные потоки**: новая функциональность Java 21+
- **Практические задачи**: Producer-Consumer, пулы соединений

**Удачи на собеседовании! 🚀**