package exercise.concurency;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int maxSize;
    
    public SimpleQueue(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() >= maxSize) {
            wait(); // Wait until space is available
        }
        queue.offer(item);
        notifyAll(); // Notify waiting consumers
    }
    
    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // Wait until item is available
        }
        T item = queue.poll();
        notifyAll(); // Notify waiting producers
        return item;
    }
    
    public synchronized int size() {
        return queue.size();
    }
}