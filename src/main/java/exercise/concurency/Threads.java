package exercise.concurency;

public class Threads {
    static int increment = 0;

    public static void main(String[] args) throws InterruptedException {
//        for (int i = 0; i < 10; i++) {
//            MyThread th1 = new MyThread(); // новые объекты каждый раз
//            MyThread th2 = new MyThread();
//
//            th1.start();
//
//            th2.start();
//
//            th1.join();
//            th2.join();
//        }
//
//        Thread[] threads = new Thread[20]; // создаём новые потоки
//
//        for (int i = 0; i < 20; i++) {
//            threads[i] = new Thread(() -> {
//                for (int j = 0; j < 100000; j++) {
//                    concurrentInc();
//                }
//            });
//            threads[i].start();
//        }
//
//        for (Thread t : threads) {
//            t.join();
//        }
//
//        System.out.println("Final: " + increment);

        //counter monitor
        WaitingCounter counter = new WaitingCounter();
        Thread[] threads = new Thread[3];
        // Поток который ждёт
        new Thread(() -> {
            try {
                System.out.println("Waiting for 1500...");
                counter.waitForValue(1500);
                System.out.println("Got 1500!");
            } catch (InterruptedException ignored) {}
        }).start();

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            }).start();
        }
        Thread.sleep(1000);
        System.out.println(counter.count);

    }

    private static synchronized void concurrentInc() {
        increment++; // атомарная операция с sync
    }
}


class MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            Threads.increment++; // прямой доступ к static полю
        }
    }
}

class WaitingCounter {
    public int count = 0;
    private final Object monitor = new Object();

    // TODO: increment() - увеличивает счетчик
    // TODO: waitForValue(int target) - ждет пока счетчик не достигнет target

    void increment() {
        synchronized (monitor) {
            this.count++;
            monitor.notifyAll();
        }
    }

    void waitForValue(int target) throws InterruptedException {
        synchronized (monitor) { // обязательно!
            while (count < target) {
                monitor.wait();
            }
        }
    }
 }