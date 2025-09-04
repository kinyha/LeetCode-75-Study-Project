import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {
    private final AtomicInteger value = new AtomicInteger();
    public void inc() { value.incrementAndGet(); }
    public int get() { return value.get(); }
}

