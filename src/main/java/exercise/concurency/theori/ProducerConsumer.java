package exercise.concurency.theori;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumer {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int MAX_SIZE = 10;
    private final Object lock = new Object();

    public void produce(int value) throws InterruptedException {
        synchronized(lock) {
            while (queue.size() == MAX_SIZE) {
                System.out.println("Queue full, producer waiting");
                lock.wait();
            }
            queue.add(value);
            System.out.println("Produced: " + value);
            lock.notifyAll(); // будим consumers
        }
    }

    public int consume() throws InterruptedException {
        synchronized(lock) {
            while (queue.isEmpty()) {
                System.out.println("Queue empty, consumer waiting");
                lock.wait();
            }
            int value = queue.poll();
            System.out.println("Consumed: " + value);
            lock.notifyAll(); // будим producers
            return value;
        }
    }

    static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    pc.produce(i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    pc.consume();
                    //Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}
