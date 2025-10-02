//package exercise.codex.ex04_atomic_counter.src;
//
//public class Main {
//    private static final int THREADS = 8;
//    private static final int INCREMENTS_PER_THREAD = 100_000;
//
//    public static void main(String[] args) throws InterruptedException {
//        System.out.println("NaiveCounter (ожидаем недосчет из-за гонок):");
//        runNaive();
//        System.out.println();
//        System.out.println("AtomicCounter (корректный результат):");
//        runAtomic();
//    }
//
//    static void runNaive() throws InterruptedException {
//        var c = new NaiveCounter();
//        Thread[] ts = new Thread[THREADS];
//        for (int i = 0; i < ts.length; i++) {
//            ts[i] = new Thread(() -> {
//                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) c.inc();
//            });
//            ts[i].start();
//        }
//        for (Thread t : ts) t.join();
//        System.out.printf("expected=%d, actual=%d%n", THREADS * INCREMENTS_PER_THREAD, c.get());
//    }
//
//    static void runAtomic() throws InterruptedException {
//        var c = new AtomicCounter();
//        Thread[] ts = new Thread[THREADS];
//        for (int i = 0; i < ts.length; i++) {
//            ts[i] = new Thread(() -> {
//                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) c.inc();
//            });
//            ts[i].start();
//        }
//        for (Thread t : ts) t.join();
//        System.out.printf("expected=%d, actual=%d%n", THREADS * INCREMENTS_PER_THREAD, c.get());
//    }
//}
//
