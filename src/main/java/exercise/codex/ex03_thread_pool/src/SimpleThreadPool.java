package exercise.codex.ex03_thread_pool.src;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Задание: реализуйте простой фиксированный пул потоков
// Требования:
// - new SimpleThreadPool(int nThreads): создать n воркеров
// - submit(Runnable): положить задачу в очередь и разбудить воркера
// - shutdown(): прекратить приём задач, дождаться завершения очереди и остановить воркеров
// Подсказки:
// - общий монитор this, очередь задач Deque<Runnable>
// - флажок isShutdown; воркеры завершаются, когда isShutdown && очередь пуста
public class SimpleThreadPool implements AutoCloseable {
    private final List<Thread> workers = new ArrayList<>();
    private final Deque<Runnable> tasks = new ArrayDeque<>();
    private volatile boolean isShutdown = false;

    public SimpleThreadPool(int nThreads) {
        if (nThreads <= 0) throw new IllegalArgumentException("nThreads>0");
        // TODO: создать и запустить nThreads воркеров (threads), каждый: брать задачи из очереди, выполнять
        // Подсказка: synchronized(this) { while(queue empty && !isShutdown) wait(); ... }
        throw new UnsupportedOperationException("Implement constructor");
    }

    public void submit(Runnable task) {
        // TODO: synchronized(this) { if (isShutdown) throw ...; tasks.addLast(task); notifyAll(); }
        throw new UnsupportedOperationException("Implement submit");
    }

    public void shutdown() {
        // TODO: synchronized(this) { isShutdown = true; notifyAll(); } затем join всех воркеров
        throw new UnsupportedOperationException("Implement shutdown");
    }

    @Override public void close() { shutdown(); }
}

