package exercise.concurency.theori;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Задачки по Java Concurrency: Thread Basics & Synchronization
 * От простых к сложным
 * <p>
 * Теория:
 * - Thread Basics & Lifecycle
 * - Synchronization & Locks (synchronized, ReentrantLock, ReadWriteLock)
 */
public class ConcurrencyTasks {

    // ==================== УРОВЕНЬ 1: Thread Basics ====================

    /**
     * Задача 1: Создать поток тремя разными способами
     * - Extends Thread
     * - Implements Runnable
     * - Lambda
     * <p>
     * Каждый должен вывести: "Hello from Thread-X"
     */
    static class Task1_CreateThreads {
        public static void main(String[] args) {
            // TODO: Способ 1 - extends Thread
            ThreadExtends threadExtends = new ThreadExtends();
            threadExtends.start();
            // TODO: Способ 2 - implements Runnable
            Thread threadImplemets = new Thread(new ThreadImplemets());
            threadImplemets.run();
            // TODO: Способ 3 - lambda
            new Thread(() -> {
                System.out.println("Lambda thread");
            }).start();
        }
    }

    static class ThreadExtends extends Thread {
        @Override
        public void run() {
            System.out.println("Hello world " + ThreadExtends.class.getSimpleName());
        }
    }

    static class ThreadImplemets implements Runnable {
        @Override
        public void run() {
            System.out.println("Hello world " + ThreadImplemets.class.getSimpleName());
        }
    }

    /**
     * Задача 2: Thread.sleep() и join()
     * Создать 3 потока, каждый спит разное время (1s, 2s, 3s)
     * Main поток должен дождаться завершения всех и вывести "All done"
     */
    static class Task2_SleepAndJoin {
        public static void main(String[] args) throws InterruptedException {
            // TODO: Создать и запустить 3 потока с разным временем сна
            // TODO: Дождаться всех через join()

            Thread[] threads = new Thread[3];
            for (int i = 0; i < threads.length; i++) {
                final int sleepTime = (i + 1) * 1000;
                threads[i] = new NewThread(sleepTime);
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join(); // ✅ Ждем завершения каждого
            }


            System.out.println("All done");

        }
    }

    static class NewThread extends Thread {
        private static int count = 1;
        int sleepTime;

        public NewThread(int sleepTime) {
            count++;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " started, sleeping " + sleepTime + "ms");
                Thread.sleep(sleepTime);
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Задача 3: Daemon поток
     * Создать daemon поток, который выводит "Working..." каждые 500ms
     * Main поток спит 2 секунды и завершается
     * Daemon должен автоматически завершиться с JVM
     */
    static class Task3_DaemonThread {
        public static void main(String[] args) throws InterruptedException {
            // TODO: Создать daemon поток
            // TODO: Убедиться что он завершится автоматически
            Thread deamon = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Working... ");
                }
            });
            deamon.setDaemon(true);
            deamon.start();
            Thread.sleep(5000);


        }
    }

    /**
     * Задача 4: Race Condition
     * Создать счетчик БЕЗ синхронизации
     * 10 потоков по 1000 инкрементов каждый
     * Показать, что результат != 10000 (race condition)
     */
    static class Task4_RaceCondition {
        private static int counter = 0;
        static int inc = 0;

        public static void main(String[] args) throws InterruptedException {
            // TODO: Запустить 10 потоков, каждый делает 1000 инкрементов
            // TODO: Вывести результат (будет != 10000)

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        inc++;
                    }
                });
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.out.println(inc);
        }
    }


    // ==================== УРОВЕНЬ 2: Synchronization ====================

    /**
     * Задача 5: Synchronized счетчик
     * Исправить Task4 с помощью synchronized
     * Должно получиться ровно 10000
     */
    static class Task5_SynchronizedCounter {
        private int counter = 0;

        public synchronized void increment() {
            counter++;
        }

        public synchronized int getCounter() {
            // TODO: Реализовать thread-safe getter
            return counter;
        }

        public static void main(String[] args) throws InterruptedException {
            Task5_SynchronizedCounter counter = new Task5_SynchronizedCounter();
            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 10000; j++) {
                        counter.increment();
                    }
                });
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.out.println(counter.getCounter());
        }
    }

    /**
     * Задача 6: Корректная остановка потока
     * Реализовать Worker с volatile флагом для остановки
     * Worker выводит "Working: 0, 1, 2..." каждые 500ms
     * Main останавливает его через 3 секунды
     */
    static class Task6_StopThread {
        static class Worker implements Runnable {
            private volatile boolean running = true;

            @Override
            public void run() {
                // TODO: Реализовать worker с проверкой флага running
                while (running) {
                    try {
                        System.out.println("Working: 0, 1, 2 ....");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            public void stop() {
                // TODO: Остановить worker через флаг
                running = false;
                System.out.println("STOP");
            }
        }

        public static void main(String[] args) throws InterruptedException {
            // TODO: Запустить worker, подождать 3 секунды, остановить
            Worker worker = new Worker();
            Thread thread = new Thread(worker);

            thread.start();
            Thread.sleep(3000);
            worker.stop();

            thread.join();
            System.out.println("Main thread finished");
        }
    }

    /**
     * Задача 7: InterruptedException
     * Реализовать поток с обработкой interrupt
     * Main прерывает его через 2 секунды
     */
    static class Solution7_InterruptHandling {
        public static void main(String[] args) throws InterruptedException {
            Thread worker = new Thread(() -> {
                    int count = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            System.out.println("Working: " + count++);
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                System.out.println("Worker finished");
            });

            worker.start();
            Thread.sleep(2000);

            System.out.println("Interrupting worker...");
            worker.interrupt();
//
//            worker.join();
//            System.out.println("Main finished");
        }
    }

    /**
     * Задача 8: synchronized блок vs метод
     * Создать класс с разными lock объектами
     * Показать разницу между блокировкой на this и на отдельном объекте
     */
    static class Task8_SynchronizedBlock {
        private int count1 = 0;
        private int count2 = 0;
        private final Object lock = new Object();

        // Синхронизация на this
        public synchronized void incrementCount1() {
            // TODO: Инкремент count1
        }

        // Синхронизация на отдельном lock
        public void incrementCount2() {
            synchronized (lock) {
                // TODO: Инкремент count2
            }
        }

        public static void main(String[] args) {
            // TODO: Продемонстрировать разницу
        }
    }

    // ==================== УРОВЕНЬ 3: wait/notify ====================

    /**
     * Задача 9: Producer-Consumer с wait/notify
     * Реализовать очередь фиксированного размера (5 элементов)
     * Producer добавляет элементы (ждет если полная)
     * Consumer забирает элементы (ждет если пустая)
     */
    static class Task9_ProducerConsumer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int MAX_SIZE = 5;
        private final Object lock = new Object();

        public void produce(int value) throws InterruptedException {
            synchronized (lock) {
                // TODO: Ждать пока очередь не полная (while + wait)
                // TODO: Добавить элемент
                // TODO: Разбудить consumers (notifyAll)
            }
        }

        public int consume() throws InterruptedException {
            synchronized (lock) {
                // TODO: Ждать пока очередь не пустая (while + wait)
                // TODO: Забрать элемент
                // TODO: Разбудить producers (notifyAll)
                return 0;
            }
        }

        public static void main(String[] args) {
            Task9_ProducerConsumer pc = new Task9_ProducerConsumer();

            // TODO: Запустить producer поток (производит 10 элементов)
            // TODO: Запустить consumer поток (потребляет 10 элементов)
        }
    }

    /**
     * Задача 10: Spurious Wakeup
     * Показать почему нужен while вместо if при wait()
     */
    static class Task10_SpuriousWakeup {
        private final Object lock = new Object();
        private boolean condition = false;

        public void waitForCondition() throws InterruptedException {
            synchronized (lock) {
                // TODO: Правильная проверка с while
                while (!condition) {
                    lock.wait();
                }
                System.out.println("Condition met!");
            }
        }

        public void setCondition() {
            synchronized (lock) {
                // TODO: Установить condition и разбудить
            }
        }

        public static void main(String[] args) {
            // TODO: Продемонстрировать работу
        }
    }

    // ==================== УРОВЕНЬ 4: ReentrantLock ====================

    /**
     * Задача 11: ReentrantLock basic
     * Переписать Task5 (счетчик) с использованием ReentrantLock
     */
    static class Task11_ReentrantLockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int counter = 0;

        public void increment() {
            // TODO: Использовать lock.lock() в try-finally
        }

        public int getCounter() {
            // TODO: Использовать lock.lock() в try-finally
            return 0;
        }

        public static void main(String[] args) throws InterruptedException {
            // TODO: Запустить 10 потоков, результат должен быть 10000
        }
    }

    /**
     * Задача 12: tryLock() для избежания deadlock
     * Реализовать transfer между двумя счетами с tryLock()
     */
    static class Task12_TryLock {
        static class BankAccount {
            private final ReentrantLock lock = new ReentrantLock();
            private int balance;

            public BankAccount(int balance) {
                this.balance = balance;
            }

            public boolean transfer(BankAccount target, int amount) {
                // TODO: Использовать tryLock() для обоих аккаунтов
                // TODO: Если успешно - сделать перевод
                // TODO: Не забыть unlock() в finally
                return false;
            }

            public int getBalance() {
                lock.lock();
                try {
                    return balance;
                } finally {
                    lock.unlock();
                }
            }
        }

        public static void main(String[] args) {
            // TODO: Создать 2 счета, попробовать transfer
        }
    }

    /**
     * Задача 13: Condition - улучшенный Producer-Consumer
     * Переписать Task9 с использованием ReentrantLock и Condition
     */
    static class Task13_ConditionProducerConsumer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int MAX_SIZE = 5;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        public void produce(int value) throws InterruptedException {
            lock.lock();
            try {
                // TODO: Ждать пока не полная (notFull.await)
                // TODO: Добавить элемент
                // TODO: Сигнал notEmpty.signal()
            } finally {
                lock.unlock();
            }
        }

        public int consume() throws InterruptedException {
            lock.lock();
            try {
                // TODO: Ждать пока не пустая (notEmpty.await)
                // TODO: Забрать элемент
                // TODO: Сигнал notFull.signal()
                return 0;
            } finally {
                lock.unlock();
            }
        }

        public static void main(String[] args) {
            // TODO: Запустить producer и consumer
        }
    }

    /**
     * Задача 14: ReadWriteLock - простой кэш
     * Реализовать потокобезопасный кэш с ReadWriteLock
     * Много читателей, мало писателей
     */
    static class Task14_ReadWriteLock {
        private final Map<String, String> cache = new HashMap<>();
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();

        public String get(String key) {
            // TODO: Использовать readLock
            return null;
        }

        public void put(String key, String value) {
            // TODO: Использовать writeLock
        }

        public void clear() {
            // TODO: Использовать writeLock
        }

        public static void main(String[] args) throws InterruptedException {
            Task14_ReadWriteLock cache = new Task14_ReadWriteLock();

            // TODO: Запустить несколько читателей и писателей
            // TODO: Показать, что читатели работают параллельно
        }
    }

    // ==================== УРОВЕНЬ 5: Продвинутое ====================

    /**
     * Задача 15: Deadlock демонстрация и исправление
     * Создать deadlock с двумя потоками и двумя locks
     * Затем исправить через lock ordering
     */
    static class Task15_Deadlock {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void method1() {
            synchronized (lock1) {
                System.out.println(Thread.currentThread().getName() + ": holding lock1");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }

                synchronized (lock2) {
                    System.out.println(Thread.currentThread().getName() + ": holding lock1 & lock2");
                }
            }
        }

        public void method2() {
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + ": holding lock2");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }

                synchronized (lock1) {
                    System.out.println(Thread.currentThread().getName() + ": holding lock2 & lock1");
                }
            }
        }

        public static void main(String[] args) {
            Task15_Deadlock demo = new Task15_Deadlock();

            // TODO: Создать deadlock - запустить 2 потока (method1 и method2)
            // TODO: Исправить через lock ordering (всегда lock1 -> lock2)
        }
    }

    /**
     * Задача 16: Fair vs Non-Fair Lock
     * Продемонстрировать разницу между fair и non-fair locks
     */
    static class Task16_FairLock {
        public static void testLock(ReentrantLock lock, String name) throws InterruptedException {
            // Создаем 5 потоков, каждый пытается захватить lock
            for (int i = 0; i < 5; i++) {
                final int threadNum = i;
                new Thread(() -> {
                    lock.lock();
                    try {
                        System.out.println(name + " - Thread " + threadNum + " got lock");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    } finally {
                        lock.unlock();
                    }
                }).start();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            // TODO: Создать fair lock и протестировать
            ReentrantLock fairLock = new ReentrantLock(true);
            System.out.println("=== Fair Lock ===");
            testLock(fairLock, "Fair");

            Thread.sleep(1000);

            // TODO: Создать non-fair lock и протестировать
            ReentrantLock nonFairLock = new ReentrantLock(false);
            System.out.println("\n=== Non-Fair Lock ===");
            testLock(nonFairLock, "Non-Fair");
        }
    }

    /**
     * Задача 17: Reentrant property
     * Продемонстрировать что поток может повторно захватить свой lock
     */
    static class Task17_Reentrant {
        private final ReentrantLock lock = new ReentrantLock();

        public void outerMethod() {
            lock.lock();
            try {
                System.out.println("Outer method - hold count: " + lock.getHoldCount());
                innerMethod();
                System.out.println("Outer method after inner - hold count: " + lock.getHoldCount());
            } finally {
                lock.unlock();
            }
        }

        public void innerMethod() {
            lock.lock();
            try {
                System.out.println("Inner method - hold count: " + lock.getHoldCount());
            } finally {
                lock.unlock();
            }
        }

        public static void main(String[] args) {
            // TODO: Вызвать outerMethod и показать hold count
            Task17_Reentrant demo = new Task17_Reentrant();
            demo.outerMethod();
        }
    }

    /**
     * Задача 18: lockInterruptibly()
     * Продемонстрировать прерывание потока во время ожидания lock
     */
    static class Task18_LockInterruptibly {
        private final ReentrantLock lock = new ReentrantLock();

        public void doWork() {
            try {
                System.out.println(Thread.currentThread().getName() + ": trying to acquire lock");
                lock.lockInterruptibly();
                try {
                    System.out.println(Thread.currentThread().getName() + ": acquired lock");
                    Thread.sleep(5000);
                } finally {
                    lock.unlock();
                    System.out.println(Thread.currentThread().getName() + ": released lock");
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + ": interrupted while waiting for lock");
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Task18_LockInterruptibly demo = new Task18_LockInterruptibly();

            // TODO: Запустить первый поток (захватит lock на 5 секунд)
            // TODO: Запустить второй поток (будет ждать lock)
            // TODO: Прервать второй поток через 1 секунду
        }
    }
}
