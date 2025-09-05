package exercise.concurency;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadTasks {
    public static void main(String[] args) throws InterruptedException {
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(100);
        AtomicInteger c = new AtomicInteger(0);
// 3 потока синхронизируются в точке barrier.await()
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println("Работаю...");
                try {
                    barrier.await(); // ждём всех
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                c.incrementAndGet();
                System.out.println("Все готовы, продолжаем!");
            }).start();


        }
        System.out.println("incremenbt - " + c);
    }
}

class SimpleCyclicBarrier {
    private final int parties; // сколько потоков должно дождаться
    private int waiting = 0;
    private final Object monitor = new Object();
    //private boolean flag = true;

    public SimpleCyclicBarrier(int parties) {
        this.parties = parties;
    }

    // await() - ждёт пока все parties потоков не вызовут await()
    // последний поток будит всех через notifyAll()

    void await() throws InterruptedException {
        synchronized (monitor) {
            waiting++;
            while (waiting < parties && waiting != 0) {
                monitor.wait();
            }

            waiting = 0;
            monitor.notifyAll();
        }
    }

}

class SimpleReadWriteLock {
    private int readers = 0;
    private boolean writer = false;
    private final Object monitor = new Object();

    void readLock() throws InterruptedException {
        synchronized (monitor) {
            while (writer) {
                monitor.wait();
            }
            readers++;
        }
    }

    void readUnlock() throws InterruptedException {
        synchronized (monitor) {
            readers--;
            if (readers == 0) {
                monitor.notifyAll();
            }
        }
    }

    void writeLock() throws InterruptedException {
        synchronized (monitor) {
            while (writer || readers > 0) {
                monitor.wait();
            }
            writer = true;
        }
    }

    void writeUnlock() {
        synchronized (monitor) {
            writer = false;
            monitor.notifyAll();
        }
    }

//    SimpleReadWriteLock lock = new SimpleReadWriteLock();
//
//    Thread writer = new Thread(() -> {
//        System.out.println("Поток писатель");
//        try {
//            lock.writeLock();
//            System.out.println("Писатель заблокировал");
//            System.out.println("Пишу ");
//            Thread.sleep(1000);
//            lock.writeUnlock();
//            System.out.println("Писатель разблокировал");
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    });
//
//
//    Thread[] readerThreads = new Thread[5];
//        for (int i = 0; i < 5; i++) {
//        int finalI = i;
//        readerThreads[i] = new Thread(() -> {
//            System.out.println("Тред ридер - " + finalI);
//            try {
//                lock.readLock();
//                System.out.println("Тред ридер читает - " + finalI);
//                lock.readUnlock();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//        writer.start();
//
//        for (Thread readerThread : readerThreads) {
//        readerThread.start();
//    }
//
//
//        writer.join();
//        for (Thread readerThread : readerThreads) {
//        readerThread.join();
//    }
    // readLock() - может быть много читателей одновременно
    // readUnlock() - освобождает чтение
    // writeLock() - только один писатель, блокирует всех
    // writeUnlock() - освобождает запись
}

class SimpleSemaphore {
    int permits;
    private final Object monitor = new Object();

    public SimpleSemaphore(int permits) {
        this.permits = permits;
    }

    void acquire() throws InterruptedException {
        synchronized (monitor) {
            while (permits == 0) {
                monitor.wait();
            }
            permits--;
        }
    }

    void release() throws InterruptedException {
        synchronized (monitor) {
            permits++;
            monitor.notifyAll();
        }
    }

    // acquire() - получить разрешение (ждёт если permits == 0)
    // release() - освободить разрешение

    //        SimpleSemaphore semaphore = new SimpleSemaphore(3);
//
//        for (int i = 0; i < 10; i++) {
//            int finalI = i;
//            new Thread(() -> {
//                try {
//                    while (true) {
//                        semaphore.acquire();
//                        System.out.println("Do something" + semaphore.permits + finalI);
//                        Thread.sleep(1000);
//                        semaphore.release();
//
//                    }
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
}

class SimpleBuffer {
    private String data = null;
    private final Object monitor = new Object();

    // TODO: put(String value) - кладет данные, если буфер пуст
    // TODO: take() - берет данные, если буфер не пуст

    void put(String value) throws InterruptedException {
        synchronized (monitor) {
            while (data != null) { // ждём пока освободится
                monitor.wait();
            }
            data = value;
            monitor.notifyAll(); // будим consumer'ов
        }
    }

    String take() throws InterruptedException {
        synchronized (monitor) {
            while (data == null) { // ждём данные
                monitor.wait();
            }
            String result = data;
            data = null; // освобождаем буфер!
            monitor.notifyAll(); // будим producer'ов
            return result;
        }

    }

//    SimpleBuffer simpleBuffer = new SimpleBuffer();
//
//        new Thread(() -> {
//        System.out.println("Waiting for take...");
//        while (true) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                System.out.println(simpleBuffer.take());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }).start();
//
//        while (true) {
//        String sc = new Scanner(System.in).nextLine();
//        String finalI = sc;
//        new Thread(() -> {
//            try {
//                simpleBuffer.put("String - " + finalI);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//    }
}

