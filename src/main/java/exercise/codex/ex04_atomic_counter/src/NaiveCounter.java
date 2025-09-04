package exercise.codex.ex04_atomic_counter.src;

public class NaiveCounter {
    private int value = 0;
    public void inc() { value++; }
    public int get() { return value; }
}

