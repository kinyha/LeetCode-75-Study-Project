package exercise.codex.ex02_bounded_buffer.src;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Задание: реализуйте BoundedBuffer.put/take с wait/notifyAll");

        // Пример после реализации:
        // var q = new BoundedBuffer<Integer>(2);
        // Thread producer = new Thread(() -> {
        //     for (int i = 0; i < 5; i++) {
        //         try { q.put(i); System.out.println("put " + i); } catch (Exception e) { e.printStackTrace(); }
        //     }
        // });
        // Thread consumer = new Thread(() -> {
        //     for (int i = 0; i < 5; i++) {
        //         try { int v = q.take(); System.out.println("take " + v); } catch (Exception e) { e.printStackTrace(); }
        //     }
        // });
        // producer.start();
        // consumer.start();
        // producer.join();
        // consumer.join();
        // System.out.println("done");
    }
}

