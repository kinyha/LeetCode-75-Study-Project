package exercise.concurency.theori;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * РЕШЕНИЯ задачек по Java Concurrency
 */
public class ConcurrencyTasksSolutions {

    // ==================== УРОВЕНЬ 1: Thread Basics ====================

    /**
     * Решение 1: Создать поток тремя разными способами
     */
    static class Solution1_CreateThreads {
        // Способ 1: Extends Thread
        static class MyThread extends Thread {
            @Override
            public void run() {
                System.out.println("Hello from " + Thread.currentThread().getName() + " (extends Thread)");
            }
        }

        // Способ 2: Implements Runnable
        static class MyRunnable implements Runnable {
            @Override
            public void run() {
                System.out.println("Hello from " + Thread.currentThread().getName() + " (implements Runnable)");
            }
        }

        public static void main(String[] args) throws InterruptedException {
            // Способ 1
            MyThread thread1 = new MyThread();
            thread1.start();

            // Способ 2
            Thread thread2 = new Thread(new MyRunnable());
            thread2.start();

            // Способ 3: Lambda
            Thread thread3 = new Thread(() -> {
                System.out.println("Hello from " + Thread.currentThread().getName() + " (lambda)");
            });
            thread3.start();

            // Дождемся завершения
            thread1.join();
            thread2.join();
            thread3.join();
        }
    }

    /**
     * Решение 2: Thread.sleep() и join()
     */
    static class Solution2_SleepAndJoin {
        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(() -> {
                try {
                    System.out.println("Thread 1: sleeping 1 second");
                    Thread.sleep(1000);
                    System.out.println("Thread 1: done");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    System.out.println("Thread 2: sleeping 2 seconds");
                    Thread.sleep(2000);
                    System.out.println("Thread 2: done");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread t3 = new Thread(() -> {
                try {
                    System.out.println("Thread 3: sleeping 3 seconds");
                    Thread.sleep(3000);
                    System.out.println("Thread 3: done");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            t1.start();
            t2.start();
            t3.start();

            // Ждем завершения всех
            t1.join();
            t2.join();
            t3.join();

            System.out.println("All done!");
        }
    }

    /**
     * Решение 3: Daemon поток
     */
    static class Solution3_DaemonThread {
        public static void main(String[] args) throws InterruptedException {
            Thread daemon = new Thread(() -> {
                while (true) {
                    System.out.println("Working... (daemon)");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });

            daemon.setDaemon(true); // ВАЖНО: до start()
            daemon.start();

            System.out.println("Main thread sleeping for 2 seconds...");
            Thread.sleep(2000);
            System.out.println("Main thread exiting (daemon will be killed automatically)");
        }
    }

    /**
     * Решение 4: Race Condition
     */
    static class Solution4_RaceCondition {
        private static int counter = 0;

        public static void main(String[] args) throws InterruptedException {
            Thread[] threads = new Thread[10];

            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        counter++; // НЕ thread-safe!
                    }
                });
                threads[i].start();
            }

            // Ждем завершения всех
            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Expected: 10000");
            System.out.println("Actual: " + counter); // Будет < 10000 из-за race condition
        }
    }

    // ==================== УРОВЕНЬ 2: Synchronization ====================

    /**
     * Решение 5: Synchronized счетчик
     */
    static class Solution5_SynchronizedCounter {
        private int counter = 0;

        public synchronized void increment() {
            counter++;
        }

        public synchronized int getCounter() {
            return counter;
        }

        public static void main(String[] args) throws InterruptedException {
            Solution5_SynchronizedCounter sc = new Solution5_SynchronizedCounter();
            Thread[] threads = new Thread[10];

            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        sc.increment();
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Result: " + sc.getCounter()); // Всегда 10000
        }
    }

    /**
     * Решение 6: Корректная остановка потока
     */
    static class Solution6_StopThread {
        static class Worker implements Runnable {
            private volatile boolean running = true;

            @Override
            public void run() {
                int count = 0;
                while (running) {
                    System.out.println("Working: " + count++);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println("Worker stopped");
            }

            public void stop() {
                running = false;
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Worker worker = new Worker();
            Thread thread = new Thread(worker);
            thread.start();

            Thread.sleep(3000);
            System.out.println("Stopping worker...");
            worker.stop();

            thread.join();
            System.out.println("Main thread finished");
        }
    }

    /**
     * Решение 7: InterruptedException
     */
    static class Solution7_InterruptHandling {
        public static void main(String[] args) throws InterruptedException {
            Thread worker = new Thread(() -> {
                try {
                    int count = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        System.out.println("Working: " + count++);
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted via InterruptedException");
                    Thread.currentThread().interrupt(); // Восстанавливаем флаг
                }
                System.out.println("Worker finished");
            });

            worker.start();
            Thread.sleep(2000);

            System.out.println("Interrupting worker...");
            worker.interrupt();

            worker.join();
            System.out.println("Main finished");
        }
    }

    /**
     * Решение 8: synchronized блок vs метод
     */
    static class Solution8_SynchronizedBlock {
        private int count1 = 0;
        private int count2 = 0;
        private final Object lock = new Object();

        // Синхронизация на this
        public synchronized void incrementCount1() {
            count1++;
            System.out.println(Thread.currentThread().getName() + " - count1: " + count1);
        }

        // Синхронизация на отдельном lock
        public void incrementCount2() {
            synchronized (lock) {
                count2++;
                System.out.println(Thread.currentThread().getName() + " - count2: " + count2);
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Solution8_SynchronizedBlock demo = new Solution8_SynchronizedBlock();

            // Потоки для count1 (блокируются на this)
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    demo.incrementCount1();
                }
            }, "Thread-1");

            // Потоки для count2 (блокируются на lock)
            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    demo.incrementCount2();
                }
            }, "Thread-2");

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            System.out.println("Final count1: " + demo.count1);
            System.out.println("Final count2: " + demo.count2);
        }
    }

    // ==================== УРОВЕНЬ 3: wait/notify ====================

    /**
     * Решение 9: Producer-Consumer с wait/notify
     */
    static class Solution9_ProducerConsumer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int MAX_SIZE = 5;
        private final Object lock = new Object();

        public void produce(int value) throws InterruptedException {
            synchronized (lock) {
                while (queue.size() == MAX_SIZE) {
                    System.out.println("Queue full, producer waiting...");
                    lock.wait();
                }
                queue.add(value);
                System.out.println("Produced: " + value + " | Queue size: " + queue.size());
                lock.notifyAll();
            }
        }

        public int consume() throws InterruptedException {
            synchronized (lock) {
                while (queue.isEmpty()) {
                    System.out.println("Queue empty, consumer waiting...");
                    lock.wait();
                }
                int value = queue.poll();
                System.out.println("Consumed: " + value + " | Queue size: " + queue.size());
                lock.notifyAll();
                return value;
            }
        }

        public static void main(String[] args) {
            Solution9_ProducerConsumer pc = new Solution9_ProducerConsumer();

            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        pc.produce(i);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer");

            Thread consumer = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        pc.consume();
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer");

            producer.start();
            consumer.start();
        }
    }

    /**
     * Решение 10: Spurious Wakeup
     */
    static class Solution10_SpuriousWakeup {
        private final Object lock = new Object();
        private boolean condition = false;

        public void waitForCondition() throws InterruptedException {
            synchronized (lock) {
                // ВАЖНО: while, а не if!
                while (!condition) {
                    System.out.println(Thread.currentThread().getName() + ": waiting...");
                    lock.wait();
                }
                System.out.println(Thread.currentThread().getName() + ": Condition met!");
            }
        }

        public void setCondition() {
            synchronized (lock) {
                condition = true;
                System.out.println("Condition set to true");
                lock.notifyAll();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Solution10_SpuriousWakeup demo = new Solution10_SpuriousWakeup();

            Thread waiter = new Thread(() -> {
                try {
                    demo.waitForCondition();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Waiter");

            waiter.start();
            Thread.sleep(1000);
            demo.setCondition();
            waiter.join();
        }
    }

    // ==================== УРОВЕНЬ 4: ReentrantLock ====================

    /**
     * Решение 11: ReentrantLock basic
     */
    static class Solution11_ReentrantLockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int counter = 0;

        public void increment() {
            lock.lock();
            try {
                counter++;
            } finally {
                lock.unlock();
            }
        }

        public int getCounter() {
            lock.lock();
            try {
                return counter;
            } finally {
                lock.unlock();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Solution11_ReentrantLockCounter counter = new Solution11_ReentrantLockCounter();
            Thread[] threads = new Thread[10];

            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        counter.increment();
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Result: " + counter.getCounter()); // 10000
        }
    }

    /**
     * Решение 12: tryLock() для избежания deadlock
     */
    static class Solution12_TryLock {
        static class BankAccount {
            private final ReentrantLock lock = new ReentrantLock();
            private int balance;
            private final String name;

            public BankAccount(String name, int balance) {
                this.name = name;
                this.balance = balance;
            }

            public boolean transfer(BankAccount target, int amount) {
                if (this.lock.tryLock()) {
                    try {
                        if (target.lock.tryLock()) {
                            try {
                                if (this.balance >= amount) {
                                    this.balance -= amount;
                                    target.balance += amount;
                                    System.out.println("Transfer successful: " + amount +
                                            " from " + this.name + " to " + target.name);
                                    return true;
                                } else {
                                    System.out.println("Insufficient funds in " + this.name);
                                    return false;
                                }
                            } finally {
                                target.lock.unlock();
                            }
                        } else {
                            System.out.println("Could not acquire lock on target account");
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }
                System.out.println("Could not acquire lock on source account");
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

        public static void main(String[] args) throws InterruptedException {
            BankAccount account1 = new BankAccount("Account1", 1000);
            BankAccount account2 = new BankAccount("Account2", 1000);

            account1.transfer(account2, 100);
            System.out.println("Account1 balance: " + account1.getBalance());
            System.out.println("Account2 balance: " + account2.getBalance());
        }
    }

    /**
     * Решение 13: Condition - улучшенный Producer-Consumer
     */
    static class Solution13_ConditionProducerConsumer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int MAX_SIZE = 5;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        public void produce(int value) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == MAX_SIZE) {
                    System.out.println("Queue full, producer waiting...");
                    notFull.await();
                }
                queue.add(value);
                System.out.println("Produced: " + value + " | Queue size: " + queue.size());
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public int consume() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    System.out.println("Queue empty, consumer waiting...");
                    notEmpty.await();
                }
                int value = queue.poll();
                System.out.println("Consumed: " + value + " | Queue size: " + queue.size());
                notFull.signal();
                return value;
            } finally {
                lock.unlock();
            }
        }

        public static void main(String[] args) {
            Solution13_ConditionProducerConsumer pc = new Solution13_ConditionProducerConsumer();

            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        pc.produce(i);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer");

            Thread consumer = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        pc.consume();
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer");

            producer.start();
            consumer.start();
        }
    }

    /**
     * Решение 14: ReadWriteLock - простой кэш
     */
    static class Solution14_ReadWriteLock {
        private final Map<String, String> cache = new HashMap<>();
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();

        public String get(String key) {
            readLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " reading: " + key);
                Thread.sleep(100); // Имитируем чтение
                return cache.get(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                readLock.unlock();
            }
        }

        public void put(String key, String value) {
            writeLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " writing: " + key + "=" + value);
                Thread.sleep(200); // Имитируем запись
                cache.put(key, value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
            }
        }

        public void clear() {
            writeLock.lock();
            try {
                cache.clear();
            } finally {
                writeLock.unlock();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            Solution14_ReadWriteLock cache = new Solution14_ReadWriteLock();

            // Писатель
            Thread writer = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    cache.put("key" + i, "value" + i);
                }
            }, "Writer");

            // Несколько читателей (работают параллельно)
            Thread reader1 = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    cache.get("key" + i);
                }
            }, "Reader-1");

            Thread reader2 = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    cache.get("key" + i);
                }
            }, "Reader-2");

            writer.start();
            Thread.sleep(100); // Дать писателю заполнить кэш

            reader1.start();
            reader2.start();

            writer.join();
            reader1.join();
            reader2.join();
        }
    }

    // ==================== УРОВЕНЬ 5: Продвинутое ====================

    /**
     * Решение 15: Deadlock демонстрация и исправление
     */
    static class Solution15_Deadlock {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        // ❌ DEADLOCK версия
        public void method1_deadlock() {
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

        public void method2_deadlock() {
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

        // ✅ ИСПРАВЛЕННАЯ версия - lock ordering
        public void method1_fixed() {
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

        public void method2_fixed() {
            // Захватываем в том же порядке: lock1 -> lock2
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

        public static void main(String[] args) throws InterruptedException {
            Solution15_Deadlock demo = new Solution15_Deadlock();

            // Раскомментируйте для демонстрации deadlock:
            /*
            Thread t1 = new Thread(demo::method1_deadlock, "Thread-1");
            Thread t2 = new Thread(demo::method2_deadlock, "Thread-2");
            */

            // Исправленная версия:
            Thread t1 = new Thread(demo::method1_fixed, "Thread-1");
            Thread t2 = new Thread(demo::method2_fixed, "Thread-2");

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            System.out.println("Finished without deadlock!");
        }
    }

    /**
     * Решение 16: Fair vs Non-Fair Lock
     */
    static class Solution16_FairLock {
        public static void testLock(ReentrantLock lock, String name) throws InterruptedException {
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
            ReentrantLock fairLock = new ReentrantLock(true);
            System.out.println("=== Fair Lock (FIFO order) ===");
            testLock(fairLock, "Fair");

            Thread.sleep(1000);

            ReentrantLock nonFairLock = new ReentrantLock(false);
            System.out.println("\n=== Non-Fair Lock (random order) ===");
            testLock(nonFairLock, "Non-Fair");

            Thread.sleep(1000);
        }
    }

    /**
     * Решение 17: Reentrant property
     */
    static class Solution17_Reentrant {
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
            lock.lock(); // Повторный захват того же lock!
            try {
                System.out.println("Inner method - hold count: " + lock.getHoldCount());
            } finally {
                lock.unlock();
            }
        }

        public static void main(String[] args) {
            Solution17_Reentrant demo = new Solution17_Reentrant();
            demo.outerMethod();
            // Вывод:
            // Outer method - hold count: 1
            // Inner method - hold count: 2  <-- поток захватил lock дважды
            // Outer method after inner - hold count: 1
        }
    }

    /**
     * Решение 18: lockInterruptibly()
     */
    static class Solution18_LockInterruptibly {
        private final ReentrantLock lock = new ReentrantLock();

        public void doWork() {
            try {
                System.out.println(Thread.currentThread().getName() + ": trying to acquire lock");
                lock.lockInterruptibly(); // Можно прервать!
                try {
                    System.out.println(Thread.currentThread().getName() + ": acquired lock, working for 5 seconds");
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
            Solution18_LockInterruptibly demo = new Solution18_LockInterruptibly();

            Thread t1 = new Thread(demo::doWork, "Thread-1");
            Thread t2 = new Thread(demo::doWork, "Thread-2");

            t1.start();
            Thread.sleep(100); // Даем t1 захватить lock

            t2.start(); // t2 будет ждать lock
            Thread.sleep(1000);

            System.out.println("Main: interrupting Thread-2");
            t2.interrupt(); // Прерываем t2 во время ожидания

            t1.join();
            t2.join();

            System.out.println("Main finished");
        }
    }
}
