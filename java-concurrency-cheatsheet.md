# Шпаргалка по многопоточности в Java

## Содержание
1. [Основы потоков](#основы-потоков)
2. [Синхронизация](#синхронизация)
3. [Атомарные операции](#атомарные-операции)
4. [Locks (Блокировки)](#locks-блокировки)
5. [ExecutorService и пулы потоков](#executorservice-и-пулы-потоков)
6. [CompletableFuture](#completablefuture)
7. [Concurrent коллекции](#concurrent-коллекции)
8. [Синхронизаторы](#синхронизаторы)
9. [BlockingQueue и Producer-Consumer](#blockingqueue-и-producer-consumer)
10. [ForkJoinPool](#forkjoinpool)
11. [Виртуальные потоки (Java 21+)](#виртуальные-потоки-java-21)
12. [Проблемы многопоточности](#проблемы-многопоточности)
13. [Лучшие практики](#лучшие-практики)

---

## Основы потоков

### Создание потоков

#### 1. Наследование от Thread
```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Поток выполняется: " + Thread.currentThread().getName());
    }
}

// Использование
MyThread thread = new MyThread();
thread.start(); // НЕ thread.run()!
```

#### 2. Реализация Runnable
```java
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Поток выполняется: " + Thread.currentThread().getName());
    }
}

// Использование
Thread thread = new Thread(new MyRunnable());
thread.start();

// Или с лямбдой (Java 8+)
Thread thread = new Thread(() -> {
    System.out.println("Лямбда в потоке");
});
thread.start();
```

### Основные методы Thread

```java
// Управление потоком
thread.start();           // Запуск потока
thread.join();            // Ожидание завершения потока
thread.join(1000);        // Ожидание с таймаутом
thread.interrupt();       // Прерывание потока
Thread.sleep(1000);       // Пауза на 1 секунду

// Информация о потоке
thread.getId();           // ID потока
thread.getName();         // Имя потока
thread.getState();        // Состояние потока
thread.isAlive();         // Жив ли поток
thread.isDaemon();        // Демон-поток?

// Daemon потоки
thread.setDaemon(true);   // Установить как демон ПЕРЕД start()
```

### Состояния потоков
- **NEW** - создан, но не запущен
- **RUNNABLE** - выполняется или готов к выполнению
- **BLOCKED** - заблокирован на мониторе
- **WAITING** - ожидает неопределенно долго
- **TIMED_WAITING** - ожидает определенное время
- **TERMINATED** - завершен

---

## Синхронизация

### synchronized

#### Синхронизированные методы
```java
public class Counter {
    private int count = 0;
    
    // Синхронизированный метод
    public synchronized void increment() {
        count++;
    }
    
    // Синхронизированный статический метод (блокировка на классе)
    public static synchronized void staticMethod() {
        // код
    }
}
```

#### Синхронизированные блоки
```java
public class Counter {
    private int count = 0;
    private final Object lock = new Object();
    
    public void increment() {
        synchronized(lock) {
            count++;
        }
    }
    
    // Синхронизация на this
    public void increment2() {
        synchronized(this) {
            count++;
        }
    }
    
    // Синхронизация на классе
    public void staticIncrement() {
        synchronized(Counter.class) {
            // код для статических переменных
        }
    }
}
```

### volatile

```java
public class VolatileExample {
    private volatile boolean flag = false;
    private volatile int counter = 0;
    
    public void setFlag() {
        flag = true; // Изменения видны всем потокам
    }
    
    public boolean getFlag() {
        return flag; // Всегда актуальное значение
    }
    
    // ВНИМАНИЕ: volatile НЕ гарантирует атомарность!
    public void increment() {
        counter++; // НЕ атомарная операция!
    }
}
```

**Когда использовать volatile:**
- Простые флаги состояния
- Одиночные переменные, которые читаются/записываются разными потоками
- НЕ для сложных операций (increment, check-then-act)

---

## Атомарные операции

### Основные Atomic классы

```java
import java.util.concurrent.atomic.*;

// AtomicInteger
AtomicInteger atomicInt = new AtomicInteger(0);
atomicInt.incrementAndGet();    // ++i
atomicInt.getAndIncrement();    // i++
atomicInt.addAndGet(5);         // добавить 5 и вернуть
atomicInt.compareAndSet(0, 10); // CAS: если 0, то установить 10

// AtomicLong
AtomicLong atomicLong = new AtomicLong(0);

// AtomicBoolean
AtomicBoolean atomicBool = new AtomicBoolean(false);
atomicBool.compareAndSet(false, true);

// AtomicReference
AtomicReference<String> atomicRef = new AtomicReference<>("initial");
atomicRef.compareAndSet("initial", "new value");
```

### Пример использования

```java
public class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet(); // Потокобезопасно!
    }
    
    public int getCount() {
        return count.get();
    }
    
    // Сложные операции с помощью CAS
    public void incrementIfLessThan(int max) {
        int current;
        do {
            current = count.get();
            if (current >= max) return;
        } while (!count.compareAndSet(current, current + 1));
    }
}
```

---

## Locks (Блокировки)

### ReentrantLock

```java
import java.util.concurrent.locks.ReentrantLock;

public class LockExample {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // ОБЯЗАТЕЛЬНО в finally!
        }
    }
    
    // Попытка заблокировать
    public void tryLockExample() {
        if (lock.tryLock()) {
            try {
                // критическая секция
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("Не удалось получить блокировку");
        }
    }
    
    // С таймаутом
    public void tryLockWithTimeout() throws InterruptedException {
        if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
            try {
                // критическая секция
            } finally {
                lock.unlock();
            }
        }
    }
}
```

### ReentrantReadWriteLock

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    
    private String data = "";
    
    public String readData() {
        readLock.lock();
        try {
            return data; // Несколько потоков могут читать одновременно
        } finally {
            readLock.unlock();
        }
    }
    
    public void writeData(String newData) {
        writeLock.lock();
        try {
            data = newData; // Только один поток может писать
        } finally {
            writeLock.unlock();
        }
    }
}
```

---

## ExecutorService и пулы потоков

### Типы ExecutorService

```java
import java.util.concurrent.*;

// Фиксированный пул потоков
ExecutorService fixedPool = Executors.newFixedThreadPool(4);

// Кэшированный пул (создает потоки по необходимости)
ExecutorService cachedPool = Executors.newCachedThreadPool();

// Одиночный поток
ExecutorService singleThread = Executors.newSingleThreadExecutor();

// Пул с планировщиком
ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);
```

### Использование ExecutorService

```java
public class ExecutorExample {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Отправка Runnable задач
        executor.execute(() -> System.out.println("Task 1"));
        
        // Отправка Callable задач
        Future<String> future = executor.submit(() -> {
            Thread.sleep(1000);
            return "Результат";
        });
        
        // Получение результата
        String result = future.get(); // блокирующий вызов
        System.out.println(result);
        
        // Проверка статуса
        if (future.isDone()) {
            System.out.println("Задача завершена");
        }
        
        // Завершение работы
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
```

### Кастомный ThreadPoolExecutor

```java
ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
    4,                                    // corePoolSize
    8,                                    // maximumPoolSize
    60L,                                  // keepAliveTime
    TimeUnit.SECONDS,                     // timeUnit
    new LinkedBlockingQueue<>(100),       // workQueue
    new ThreadFactory() {                 // threadFactory
        private int counter = 0;
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Worker-" + counter++);
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy() // rejectionHandler
);
```

### ScheduledExecutorService

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

// Выполнить через 5 секунд
scheduler.schedule(() -> System.out.println("Delayed task"), 5, TimeUnit.SECONDS);

// Повторять каждые 2 секунды с начальной задержкой 1 секунда
scheduler.scheduleAtFixedRate(
    () -> System.out.println("Periodic task"),
    1,    // initial delay
    2,    // period
    TimeUnit.SECONDS
);

// Повторять с задержкой 3 секунды между выполнениями
scheduler.scheduleWithFixedDelay(
    () -> System.out.println("Fixed delay task"),
    0,    // initial delay
    3,    // delay
    TimeUnit.SECONDS
);
```

---

## CompletableFuture

### Основные методы создания

```java
// Создание завершенного CompletableFuture
CompletableFuture<String> completed = CompletableFuture.completedFuture("результат");

// Асинхронное выполнение без результата
CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
    System.out.println("Асинхронная задача");
});

// Асинхронное выполнение с результатом
CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
    // некоторая долгая операция
    return "результат";
});

// С кастомным Executor
CompletableFuture<String> withExecutor = CompletableFuture.supplyAsync(
    () -> "результат",
    Executors.newFixedThreadPool(2)
);
```

### Цепочки операций

```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")           // преобразование результата
    .thenApply(String::toUpperCase)         // еще одно преобразование
    .thenCompose(s -> CompletableFuture     // композиция с другим Future
        .supplyAsync(() -> s + "!"))
    .thenAccept(System.out::println)        // потребление результата
    .thenRun(() -> System.out.println("Готово")); // выполнить после завершения
```

### Обработка ошибок

```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        if (Math.random() > 0.5) {
            throw new RuntimeException("Ошибка!");
        }
        return "Успех";
    })
    .exceptionally(throwable -> {
        System.out.println("Произошла ошибка: " + throwable.getMessage());
        return "Значение по умолчанию";
    })
    .handle((result, throwable) -> {
        if (throwable != null) {
            return "Ошибка: " + throwable.getMessage();
        }
        return result;
    });
```

### Комбинирование CompletableFuture

```java
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

// Комбинирование результатов двух Future
CompletableFuture<String> combined = future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2);

// Ожидание завершения всех Future
CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2);

// Ожидание завершения любого из Future
CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future1, future2);
```

---

## Concurrent коллекции

### ConcurrentHashMap

```java
import java.util.concurrent.ConcurrentHashMap;

ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Основные операции
map.put("key1", 1);
map.putIfAbsent("key2", 2);           // добавить, если нет
map.replace("key1", 1, 10);           // заменить, если значение равно 1
map.compute("key1", (k, v) -> v + 1); // вычислить новое значение

// Атомарные операции
map.merge("counter", 1, Integer::sum); // если ключ есть - суммировать, иначе - установить 1

// Массовые операции
map.forEach((k, v) -> System.out.println(k + ": " + v));
map.search(1, (k, v) -> v > 5 ? k : null);
map.reduce(1, (k, v) -> v, Integer::sum);
```

### CopyOnWriteArrayList

```java
import java.util.concurrent.CopyOnWriteArrayList;

// Подходит для сценариев с частым чтением и редкой записью
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("item1");
list.add("item2");

// Итерация безопасна без синхронизации
for (String item : list) {
    System.out.println(item);
}
```

### Другие concurrent коллекции

```java
// Потокобезопасные очереди
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

// Навигационные мапы
ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
ConcurrentSkipListSet<String> skipListSet = new ConcurrentSkipListSet<>();
```

---

## Синхронизаторы

### CountDownLatch

```java
import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        int numberOfWorkers = 3;
        CountDownLatch latch = new CountDownLatch(numberOfWorkers);
        
        for (int i = 0; i < numberOfWorkers; i++) {
            new Thread(() -> {
                System.out.println("Работник " + Thread.currentThread().getName() + " работает");
                // выполнение работы...
                latch.countDown(); // уменьшить счетчик
            }).start();
        }
        
        latch.await(); // ждать, пока счетчик не станет 0
        System.out.println("Все работники закончили, продолжаем");
    }
}
```

### CyclicBarrier

```java
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierExample {
    public static void main(String[] args) {
        int numberOfWorkers = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfWorkers, () -> {
            System.out.println("Все потоки достигли барьера!");
        });
        
        for (int i = 0; i < numberOfWorkers; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " работает");
                    Thread.sleep(2000); // имитация работы
                    System.out.println(Thread.currentThread().getName() + " ждет на барьере");
                    barrier.await(); // ждать остальных
                    System.out.println(Thread.currentThread().getName() + " продолжает после барьера");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

### Semaphore

```java
import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    private static final Semaphore semaphore = new Semaphore(2); // максимум 2 потока
    
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire(); // получить разрешение
                    System.out.println(Thread.currentThread().getName() + " получил доступ");
                    Thread.sleep(3000); // использование ресурса
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println(Thread.currentThread().getName() + " освободил ресурс");
                    semaphore.release(); // освободить разрешение
                }
            }).start();
        }
    }
}
```

### Phaser (Java 7+)

```java
import java.util.concurrent.Phaser;

public class PhaserExample {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(3); // 3 участника
        
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " - фаза 1");
                phaser.arriveAndAwaitAdvance(); // ждать всех на фазе 1
                
                System.out.println(Thread.currentThread().getName() + " - фаза 2");
                phaser.arriveAndAwaitAdvance(); // ждать всех на фазе 2
                
                System.out.println(Thread.currentThread().getName() + " - фаза 3");
                phaser.arriveAndDeregister(); // покинуть phaser
            }).start();
        }
    }
}
```

---

## BlockingQueue и Producer-Consumer

### Типы BlockingQueue

```java
import java.util.concurrent.*;

// Фиксированного размера
BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(10);

// Неограниченная очередь (на основе связного списка)
BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();

// Ограниченная очередь на основе связного списка
BlockingQueue<String> boundedLinkedQueue = new LinkedBlockingQueue<>(100);

// Синхронная очередь (размер 0)
BlockingQueue<String> synchronousQueue = new SynchronousQueue<>();

// Очередь с приоритетом
BlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>();

// Очередь с задержкой
BlockingQueue<DelayedElement> delayQueue = new DelayQueue<>();
```

### Producer-Consumer пример

```java
import java.util.concurrent.*;

public class ProducerConsumerExample {
    private static final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
    
    // Producer
    static class Producer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    queue.put(i); // блокируется, если очередь полная
                    System.out.println("Произведено: " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // Consumer
    static class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Integer item = queue.take(); // блокируется, если очередь пустая
                    System.out.println("Потреблено: " + item);
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
    }
}
```

---

## ForkJoinPool

### RecursiveTask (с результатом)

```java
import java.util.concurrent.*;

public class ForkJoinSumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private final int[] array;
    private final int start;
    private final int end;
    
    public ForkJoinSumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        int length = end - start;
        
        if (length <= THRESHOLD) {
            // Прямое вычисление для малых задач
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Разделяем задачу
            int middle = start + length / 2;
            ForkJoinSumTask leftTask = new ForkJoinSumTask(array, start, middle);
            ForkJoinSumTask rightTask = new ForkJoinSumTask(array, middle, end);
            
            leftTask.fork(); // запускаем левую подзадачу асинхронно
            long rightResult = rightTask.compute(); // вычисляем правую синхронно
            long leftResult = leftTask.join(); // ждем результат левой
            
            return leftResult + rightResult;
        }
    }
    
    public static void main(String[] args) {
        int[] array = new int[10000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }
        
        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinSumTask task = new ForkJoinSumTask(array, 0, array.length);
        long result = pool.invoke(task);
        
        System.out.println("Сумма: " + result);
        pool.shutdown();
    }
}
```

### RecursiveAction (без результата)

```java
import java.util.concurrent.*;

public class ForkJoinQuickSort extends RecursiveAction {
    private static final int THRESHOLD = 100;
    private final int[] array;
    private final int low;
    private final int high;
    
    public ForkJoinQuickSort(int[] array, int low, int high) {
        this.array = array;
        this.low = low;
        this.high = high;
    }
    
    @Override
    protected void compute() {
        if (high - low <= THRESHOLD) {
            // Прямая сортировка для малых массивов
            Arrays.sort(array, low, high + 1);
        } else {
            int pivotIndex = partition(array, low, high);
            ForkJoinQuickSort leftTask = new ForkJoinQuickSort(array, low, pivotIndex - 1);
            ForkJoinQuickSort rightTask = new ForkJoinQuickSort(array, pivotIndex + 1, high);
            
            // Запускаем обе задачи параллельно
            invokeAll(leftTask, rightTask);
        }
    }
    
    private int partition(int[] array, int low, int high) {
        // реализация разбиения для quicksort
        // ...
    }
}
```

---

## Виртуальные потоки (Java 21+)

### Создание виртуальных потоков

```java
// Создание и запуск виртуального потока
Thread virtualThread = Thread.ofVirtual().start(() -> {
    System.out.println("Виртуальный поток: " + Thread.currentThread());
});

// Создание без автоматического запуска
Thread unstarted = Thread.ofVirtual().unstarted(() -> {
    System.out.println("Незапущенный виртуальный поток");
});
unstarted.start();

// С именем
Thread named = Thread.ofVirtual()
    .name("my-virtual-thread")
    .start(() -> {
        System.out.println("Именованный виртуальный поток");
    });

// Фабрика для виртуальных потоков
ThreadFactory factory = Thread.ofVirtual().factory();
Thread vt = factory.newThread(() -> System.out.println("Из фабрики"));
vt.start();
```

### ExecutorService с виртуальными потоками

```java
// ExecutorService с виртуальными потоками
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Каждая задача выполняется в новом виртуальном потоке
    for (int i = 0; i < 1000; i++) {
        executor.submit(() -> {
            // долгая операция ввода-вывода
            try {
                Thread.sleep(1000);
                System.out.println("Задача в виртуальном потоке");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
} // автоматически закрывается
```

### Важные особенности виртуальных потоков

```java
// ⚠️ ОСТОРОЖНО: Виртуальные потоки "прикрепляются" к carrier thread при:
// 1. Использовании synchronized
synchronized(this) {
    // поток прикрепляется к carrier thread
    Thread.sleep(1000); // блокирует carrier thread!
}

// ✅ ЛУЧШЕ: Используйте ReentrantLock
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    Thread.sleep(1000); // НЕ блокирует carrier thread
} finally {
    lock.unlock();
}

// 2. Вызове нативных методов или foreign функций
// В этих случаях виртуальный поток не может быть "размонтирован"
```

### Мониторинг виртуальных потоков

```java
// Проверка, является ли поток виртуальным
if (Thread.currentThread().isVirtual()) {
    System.out.println("Это виртуальный поток");
}

// JFR события для мониторинга
// jdk.VirtualThreadStart
// jdk.VirtualThreadEnd  
// jdk.VirtualThreadPinned - когда поток прикреплен
```

---

## Проблемы многопоточности

### Race Condition (Состояние гонки)

```java
// ❌ НЕПРАВИЛЬНО: Race condition
public class RaceConditionExample {
    private int counter = 0;
    
    public void increment() {
        counter++; // НЕ атомарная операция: read -> modify -> write
    }
}

// ✅ ПРАВИЛЬНО: Различные способы решения
public class FixedCounter {
    // Вариант 1: synchronized
    private int counter1 = 0;
    public synchronized void increment1() {
        counter1++;
    }
    
    // Вариант 2: AtomicInteger
    private AtomicInteger counter2 = new AtomicInteger(0);
    public void increment2() {
        counter2.incrementAndGet();
    }
    
    // Вариант 3: ReentrantLock
    private int counter3 = 0;
    private final ReentrantLock lock = new ReentrantLock();
    public void increment3() {
        lock.lock();
        try {
            counter3++;
        } finally {
            lock.unlock();
        }
    }
}
```

### Deadlock (Взаимная блокировка)

```java
// ❌ НЕПРАВИЛЬНО: Возможен deadlock
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            System.out.println("Thread 1: захватил lock1");
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            synchronized (lock2) {
                System.out.println("Thread 1: захватил lock2");
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) { // Другой порядок захвата!
            System.out.println("Thread 2: захватил lock2");
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            synchronized (lock1) {
                System.out.println("Thread 2: захватил lock1");
            }
        }
    }
}

// ✅ ПРАВИЛЬНО: Фиксированный порядок захвата блокировок
public class DeadlockSolution {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) { // Всегда сначала lock1
            synchronized (lock2) { // Потом lock2
                // работа
            }
        }
    }
    
    public void method2() {
        synchronized (lock1) { // Тот же порядок!
            synchronized (lock2) {
                // работа
            }
        }
    }
}
```

### Livelock

```java
// Пример livelock - потоки постоянно меняют состояние, но не прогрессируют
public class LivelockExample {
    static class Spoon {
        private Diner owner;
        
        public synchronized void use() {
            System.out.printf("%s has eaten!", owner.name);
        }
    }
    
    static class Diner {
        private String name;
        private boolean isHungry;
        
        public void eatWith(Spoon spoon, Diner spouse) {
            while (isHungry) {
                if (spoon.owner != this) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    continue;
                }
                
                if (spouse.isHungry) {
                    System.out.printf("%s: You eat first %s!\n", name, spouse.name);
                    spoon.owner = spouse; // Уступает ложку
                    continue; // Livelock - постоянно уступают друг другу
                }
                
                spoon.use();
                isHungry = false;
                spoon.owner = spouse;
            }
        }
    }
}
```

### Starvation

```java
// Проблема: некоторые потоки могут не получать доступ к ресурсам
public class StarvationExample {
    private final Object lock = new Object();
    
    // Потоки с высоким приоритетом могут "заморить" потоки с низким приоритетом
    public void highPriorityTask() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        synchronized (lock) {
            // интенсивная работа
        }
    }
    
    public void lowPriorityTask() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        synchronized (lock) {
            // может никогда не получить доступ
        }
    }
}

// ✅ РЕШЕНИЕ: Использовать справедливые блокировки
ReentrantLock fairLock = new ReentrantLock(true); // fair = true
```

---

## Лучшие практики

### 1. Предпочитайте неизменяемые объекты

```java
// ✅ Неизменяемый класс - потокобезопасен по умолчанию
public final class ImmutablePerson {
    private final String name;
    private final int age;
    
    public ImmutablePerson(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
    
    // Создаем новый объект вместо изменения существующего
    public ImmutablePerson withAge(int newAge) {
        return new ImmutablePerson(this.name, newAge);
    }
}
```

### 2. Минимизируйте область синхронизации

```java
// ❌ НЕПРАВИЛЬНО: Слишком большая область синхронизации
public synchronized void badMethod() {
    doSomeWork();
    accessSharedResource();
    doMoreWork();
}

// ✅ ПРАВИЛЬНО: Синхронизация только критической секции
public void goodMethod() {
    doSomeWork();
    synchronized (this) {
        accessSharedResource();
    }
    doMoreWork();
}
```

### 3. Используйте concurrent коллекции

```java
// ❌ НЕПРАВИЛЬНО
Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());

// ✅ ПРАВИЛЬНО
Map<String, String> concurrentMap = new ConcurrentHashMap<>();
```

### 4. Правильно обрабатывайте InterruptedException

```java
// ✅ ПРАВИЛЬНО: Восстановление статуса прерывания
public void handleInterruption() {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Восстанавливаем статус
        throw new RuntimeException(e); // или другая обработка
    }
}
```

### 5. Используйте ThreadLocal для потокобезопасности

```java
public class ThreadLocalExample {
    private static final ThreadLocal<SimpleDateFormat> dateFormatter = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    
    public String formatDate(Date date) {
        return dateFormatter.get().format(date); // каждый поток имеет свой formatter
    }
}
```

### 6. Избегайте создания потоков вручную

```java
// ❌ НЕПРАВИЛЬНО
new Thread(() -> doWork()).start();

// ✅ ПРАВИЛЬНО: Используйте ExecutorService
ExecutorService executor = Executors.newCachedThreadPool();
executor.submit(() -> doWork());
```

### 7. Правильно завершайте ExecutorService

```java
public void shutdownExecutorService(ExecutorService executor) {
    executor.shutdown();
    try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor не завершился");
            }
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

### 8. Используйте правильные паттерны для ленивой инициализации

```java
// ✅ Lazy initialization with volatile
public class LazyInitialization {
    private volatile SomeClass instance;
    
    public SomeClass getInstance() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = new SomeClass();
                }
            }
        }
        return instance;
    }
}

// ✅ Еще лучше: Initialization-on-demand holder pattern
public class BetterLazyInit {
    private static class Holder {
        static final SomeClass INSTANCE = new SomeClass();
    }
    
    public static SomeClass getInstance() {
        return Holder.INSTANCE;
    }
}
```

---

## Полезные команды для отладки

### JVM флаги для многопоточности

```bash
# Включить подробный вывод GC
-XX:+PrintGC -XX:+PrintGCDetails

# Обнаружение deadlock
-XX:+PrintConcurrentLocks

# Для виртуальных потоков (Java 21+)
-XX:+UnlockExperimentalVMOptions
--enable-preview
```

### JConsole и VisualVM

```bash
# Запуск с поддержкой JMX
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

### Thread dump

```bash
# Получение thread dump
jstack <PID>

# Для виртуальных потоков
jcmd <PID> Thread.dump_to_file -format=text dump.txt
```

---

**Помните:**
- Многопоточность сложна - начинайте с простых решений
- Тестируйте под нагрузкой
- Используйте профайлеры для поиска проблем производительности
- Предпочитайте высокоуровневые абстракции (ExecutorService, CompletableFuture) низкоуровневым (Thread, synchronized)
- Всегда документируйте потокобезопасность ваших классов