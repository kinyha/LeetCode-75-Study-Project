package exercise.concurency.theori;

public class Main2 {
    static void main() throws InterruptedException {
        SafeCounter counter = new SafeCounter();

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

        System.out.println("Count: " + counter.getCount()); // 10000
    }
}
class SafeCounter {
    private int count = 0;
    private final Object lock = new Object();

    public  void increment() {

        synchronized (lock) {
            count++;
        }

    }

    public  int getCount() {
        return count;
    }
    public void incerementNoSafe(){
        count++;
    }
}































