package exercise.codex.ex02_bounded_buffer.src;

import java.util.ArrayDeque;
import java.util.Deque;

// Реализуйте класс как блокирующую ограниченную очередь с использованием synchronized + wait/notifyAll
// Требования:
// - Конструктор BoundedBuffer(int capacity)
// - Метод put(T item): блокируется при полной очереди
// - Метод take(): блокируется при пустой очереди
// - Защищайте доступ к очереди одним монитором (this)
// - Для ожидания используйте цикл while (а не if)
// - Будьте аккуратны с notifyAll после изменения состояния
public class BoundedBuffer<T> {
    private final int capacity;
    private final Deque<T> deque = new ArrayDeque<>();

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity>0");
        this.capacity = capacity;
    }

    public void put(T item) {
        // TODO: synchronized(this) { while(deque.size()==capacity) wait(); deque.addLast(item); notifyAll(); }
        throw new UnsupportedOperationException("Implement put");
    }

    public T take() {
        // TODO: synchronized(this) { while(deque.isEmpty()) wait(); T v = deque.removeFirst(); notifyAll(); return v; }
        throw new UnsupportedOperationException("Implement take");
    }
}

