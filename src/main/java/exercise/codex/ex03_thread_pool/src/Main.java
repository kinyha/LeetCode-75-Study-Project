package exercise.codex.ex03_thread_pool.src;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Задание: реализуйте SimpleThreadPool (submit/shutdown)");

        // После реализации можно протестировать так:
        // try (var pool = new SimpleThreadPool(3)) {
        //     for (int i = 0; i < 10; i++) {
        //         final int id = i;
        //         pool.submit(() -> {
        //             System.out.printf("task %d on %s%n", id, Thread.currentThread().getName());
        //             try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        //         });
        //     }
        // }
        // System.out.println("done");
    }
}

