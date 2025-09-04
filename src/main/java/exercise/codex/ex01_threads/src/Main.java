package exercise.codex.ex01_threads.src;

public class Main {
    static class HelloTask implements Runnable {
        private final String name;
        HelloTask(String name) { this.name = name; }
        @Override public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.printf("[%s] hello %d%n", name, i);
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new HelloTask("A"));
        Thread t2 = new Thread(new HelloTask("B"));

        t1.start();
        t2.start();

        // Демонстрация join: ждём завершения потоков
        t1.join();
        t2.join();

        System.out.println("Done main");
    }
}

