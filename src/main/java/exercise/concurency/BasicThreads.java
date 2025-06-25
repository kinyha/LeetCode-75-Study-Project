package exercise.concurency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BasicThreads - Demonstrates fundamental concurrency concepts in Java
 * 
 * This class covers:
 * 1. Race Conditions - When multiple threads access shared data without proper synchronization
 * 2. Synchronization - Using synchronized blocks/methods to prevent race conditions
 * 3. Atomic Operations - Lock-free thread-safe operations using atomic classes
 * 4. Producer-Consumer Pattern - Classic concurrency pattern for communication between threads
 * 5. CountDownLatch - Coordination mechanism for waiting on multiple threads
 * 6. Semaphore - Controlling access to limited resources
 */
public class BasicThreads {
    
    // Shared counter without synchronization (race condition example)
    // Multiple threads can read-modify-write this variable simultaneously,
    // causing lost updates (race condition)
    // MEMORY VISIBILITY: Changes made by one thread may not be visible to others
    // without proper synchronization (volatile, synchronized, or atomic operations)
    private static int unsafeCounter = 0;
    
    // Shared counter with synchronization
    // Protected by synchronized blocks to ensure thread safety
    // HAPPENS-BEFORE RELATIONSHIP: Synchronized blocks create ordering guarantees
    // - All actions before releasing a lock happen-before acquiring the same lock
    // - This ensures both mutual exclusion AND memory visibility
    private static int safeCounter = 0;
    private static final Object lock = new Object(); // Dedicated lock object (not 'this')
    // BEST PRACTICE: Use dedicated lock objects instead of 'this' to avoid accidental
    // synchronization conflicts with other code that might synchronize on the same object
    
    // Atomic counter - provides lock-free thread-safe operations
    // Uses Compare-And-Swap (CAS) operations internally
    // LOCK-FREE PROGRAMMING: No threads are blocked, better performance under contention
    // CAS LOOP: If update fails due to concurrent modification, operation retries automatically
    // MEMORY ORDERING: Atomic operations provide volatile semantics (visibility guarantees)
    private static AtomicInteger atomicCounter = new AtomicInteger(0);
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrency Practice Exercises ===\n");

        // Reset counters for clean demonstrations
        unsafeCounter = 0;
        safeCounter = 0;
        atomicCounter.set(0);

//        // 1. Basic thread creation and race conditions
//        //demonstrateRaceCondition();
//
//        //2
//        demonstrateSynchronization();
//
//        // 3. Atomic operations
//        demonstrateAtomicOperations();
//
//        // 4. Producer-Consumer pattern
//        demonstrateProducerConsumer();
        
        // 5. CountDownLatch example
//        demonstrateCountDownLatch();
        
        // 6. Semaphore example
        demonstrateSemaphore();
    }
    
    /**
     * Exercise 1: Race Condition Demonstration
     * 
     * WHAT IS A RACE CONDITION?
     * A race condition occurs when multiple threads access shared data concurrently
     * without proper synchronization, and the final result depends on the timing
     * of thread execution.
     * 
     * THE PROBLEM:
     * unsafeCounter++ is NOT atomic! It consists of 3 operations:
     * 1. Read current value from memory
     * 2. Increment the value
     * 3. Write the new value back to memory
     * 
     * When multiple threads execute these steps simultaneously, they can interfere
     * with each other, causing lost updates.
     * 
     * EXAMPLE SCENARIO:
     * Thread A reads unsafeCounter = 5
     * Thread B reads unsafeCounter = 5 (before A writes back)
     * Thread A increments to 6 and writes back
     * Thread B increments to 6 and writes back
     * Result: Counter is 6 instead of 7 (one increment lost!)
     */
    public static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("1. Race Condition Example:");
        System.out.println("   Creating 10 threads, each incrementing counter 1000 times");
        System.out.println("   Without synchronization, we'll likely lose some increments\n");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter++; // RACE CONDITION! Not thread-safe
                    // This operation is not atomic:
                    // 1. Read unsafeCounter from memory
                    // 2. Add 1 to the value
                    // 3. Write result back to memory
                    // Another thread can interfere between these steps!
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        // JOIN SEMANTICS: join() creates a happens-before relationship
        // - All actions in the joined thread happen-before join() returns
        // - This ensures we see all updates made by worker threads
        for (Thread thread : threads) {
            thread.join(); // Blocks until thread completes
        }
        
        System.out.println("Expected: 10000, Actual: " + unsafeCounter);
        if (unsafeCounter < 10000) {
            System.out.println("Race condition detected! Lost " + (10000 - unsafeCounter) + " increments\n");
        } else {
            System.out.println("No race condition this time (rare but possible)\n");
        }
    }
    
    /**
     * Exercise 2: Synchronization with synchronized keyword
     * 
     * WHAT IS SYNCHRONIZATION?
     * Synchronization ensures that only one thread can access a critical section
     * (shared resource) at a time, preventing race conditions.
     * 
     * HOW SYNCHRONIZED WORKS:
     * - Every object in Java has an intrinsic lock (monitor)
     * - synchronized(object) acquires the lock on that object
     * - Only one thread can hold the lock at a time
     * - Other threads must wait until the lock is released
     * 
     * SYNCHRONIZED GUARANTEES:
     * 1. Mutual Exclusion: Only one thread executes the synchronized block
     * 2. Memory Visibility: Changes made by one thread are visible to others
     *    when they acquire the same lock
     */
    public static void demonstrateSynchronization() throws InterruptedException {
        System.out.println("2. Synchronization Example:");
        System.out.println("   Using synchronized blocks to prevent race conditions");
        System.out.println("   Each thread must acquire the lock before incrementing\n");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    incrementSafeCounter(); // Thread-safe method
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Expected: 10000, Actual: " + safeCounter);
        System.out.println("Synchronization works! No race condition.\n");
    }
    
    /**
     * Thread-safe increment method using synchronized block
     * 
     * The synchronized(lock) block ensures:
     * 1. MUTUAL EXCLUSION: Only one thread can execute this block at a time
     * 2. MEMORY VISIBILITY: Changes are visible to other threads when they acquire the lock
     * 3. ATOMICITY: No race condition can occur during the increment operation
     * 
     * MONITOR CONCEPT: Each object has an intrinsic lock (monitor)
     * - Acquiring monitor: Thread gains exclusive access
     * - Releasing monitor: Changes become visible to other threads
     * - Built-in condition variable: wait/notify operations
     * 
     * PERFORMANCE CONSIDERATIONS:
     * - Synchronized blocks can cause contention (threads waiting for lock)
     * - Consider alternatives: AtomicInteger, ReadWriteLock, or lock-free algorithms
     */
    private static void incrementSafeCounter() {
        synchronized (lock) { // Acquire lock on 'lock' object
            safeCounter++; // Critical section - only one thread at a time
            // AUTOMATIC LOCK RELEASE: Lock is released when exiting block
            // even if exception occurs (try-finally semantics)
        }
    }
    
    /**
     * Exercise 3: Atomic Operations
     * 
     * WHAT ARE ATOMIC OPERATIONS?
     * Atomic operations are indivisible - they complete entirely or not at all.
     * No other thread can observe an intermediate state.
     * 
     * HOW ATOMIC CLASSES WORK:
     * - Use Compare-And-Swap (CAS) operations
     * - CAS is a CPU-level instruction that atomically:
     *   1. Compares a memory location with an expected value
     *   2. If they match, updates the location with a new value
     *   3. Returns success/failure
     * - If CAS fails (another thread changed the value), it retries
     * 
     * ADVANTAGES OF ATOMIC OPERATIONS:
     * 1. Lock-free: No blocking, better performance
     * 2. No deadlock risk
     * 3. Better scalability under high contention
     * 
     * COMMON ATOMIC CLASSES:
     * - AtomicInteger, AtomicLong, AtomicBoolean
     * - AtomicReference for object references
     */
    public static void demonstrateAtomicOperations() throws InterruptedException {
        System.out.println("3. Atomic Operations Example:");
        System.out.println("   Using AtomicInteger for lock-free thread-safe operations");
        System.out.println("   Based on Compare-And-Swap (CAS) CPU instructions\n");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    // incrementAndGet() is atomic - thread-safe without locks
                    // COMPARE-AND-SWAP (CAS) ALGORITHM:
                    // 1. Read current value from memory
                    // 2. Calculate new value (current + 1)
                    // 3. CAS instruction: atomically compare current with expected,
                    //    if equal, update to new value, return success/failure
                    // 4. If CAS fails (another thread modified value), retry from step 1
                    // 
                    // LOCK-FREE BENEFITS:
                    // - No thread blocking (better performance)
                    // - No deadlock possibility
                    // - Better scalability under high contention
                    // - Immune to priority inversion problems
                    atomicCounter.incrementAndGet();
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Expected: 10000, Actual: " + atomicCounter.get());
        System.out.println("Atomic operations work! Lock-free and thread-safe.\n");
    }
    
    /**
     * Exercise 4: Producer-Consumer Pattern
     * 
     * WHAT IS THE PRODUCER-CONSUMER PATTERN?
     * A classic concurrency pattern where:
     * - Producer threads generate data and put it in a shared buffer
     * - Consumer threads take data from the buffer and process it
     * - Buffer has limited capacity (bounded buffer)
     * 
     * KEY CHALLENGES:
     * 1. Synchronization: Multiple producers/consumers accessing shared buffer
     * 2. Coordination: Producer waits when buffer is full, consumer waits when empty
     * 3. Avoiding busy-waiting: Use wait/notify instead of polling
     * 
     * SOLUTION USING WAIT/NOTIFY:
     * - wait(): Releases lock and suspends thread until notified
     * - notify()/notifyAll(): Wakes up waiting threads
     * - Always use in synchronized blocks
     * - Always check condition in a loop (spurious wakeups)
     * 
     * REAL-WORLD EXAMPLES:
     * - Web server: Request queue between listener and worker threads
     * - Logging: Log messages queued between application and file writer
     * - Stream processing: Data pipeline stages
     */
    public static void demonstrateProducerConsumer() throws InterruptedException {
        System.out.println("4. Producer-Consumer Pattern:");
        System.out.println("   Producer generates items, Consumer processes them");
        System.out.println("   Using bounded queue with wait/notify coordination\n");
        
        // Create a bounded queue with capacity 5
        SimpleQueue<Integer> queue = new SimpleQueue<>(5);
        
        // Producer thread: generates data
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put(i); // Blocks if queue is full
                    System.out.println("Produced: " + i + " (queue size: " + queue.size() + ")");
                    Thread.sleep(100); // Simulate production time
                }
            } catch (InterruptedException e) {
                // INTERRUPTION HANDLING: Proper way to handle thread interruption
                // 1. Catch InterruptedException from blocking operations
                // 2. Restore interrupted status for higher-level code
                // 3. Consider cleanup actions before thread terminates
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.out.println("Producer thread interrupted");
            }
        });
        
        // Consumer thread: processes data
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Integer value = queue.take(); // Blocks if queue is empty
                    System.out.println("Consumed: " + value + " (queue size: " + queue.size() + ")");
                    Thread.sleep(150); // Simulate processing time (slower than producer)
                    // BACKPRESSURE DEMONSTRATION: Consumer is slower than producer
                    // This will cause the bounded queue to fill up, demonstrating
                    // how producer will block when queue reaches capacity
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.out.println("Consumer thread interrupted");
            }
        });
        
        // Start both threads
        producer.start();
        consumer.start();
        
        // THREAD LIFECYCLE MANAGEMENT:
        // 1. NEW: Thread created but not started
        // 2. RUNNABLE: Thread started, may be running or waiting for CPU
        // 3. BLOCKED/WAITING: Thread waiting for synchronization or conditions
        // 4. TERMINATED: Thread completed execution
        
        // Wait for both threads to complete
        producer.join(); // Ensures producer completes before main continues
        consumer.join(); // Ensures consumer completes before main continues
        
        System.out.println("Producer-Consumer completed!\n");
    }
    
    /**
     * Exercise 5: CountDownLatch
     * 
     * WHAT IS A COUNTDOWNLATCH?
     * A synchronization aid that allows one or more threads to wait until
     * a set of operations being performed in other threads completes.
     * 
     * HOW IT WORKS:
     * 1. Initialize with a count (number of events to wait for)
     * 2. Worker threads call countDown() when they complete their task
     * 3. Waiting thread calls await() to block until count reaches zero
     * 4. Once count reaches zero, all waiting threads are released
     * 
     * KEY CHARACTERISTICS:
     * - One-time use: Count cannot be reset
     * - Thread-safe: Multiple threads can call countDown() simultaneously
     * - Non-blocking countDown(): Doesn't wait, just decrements count
     * - Blocking await(): Waits until count reaches zero
     * 
     * COMMON USE CASES:
     * 1. Waiting for multiple services to start before proceeding
     * 2. Coordinating start of multiple threads (start all at once)
     * 3. Waiting for multiple tasks to complete before aggregating results
     * 4. Testing: Ensuring all test threads complete before assertion
     */
    public static void demonstrateCountDownLatch() throws InterruptedException {
        System.out.println("5. CountDownLatch Example:");
        System.out.println("   Waiting for multiple tasks to complete");
        System.out.println("   Main thread blocks until all worker threads finish\n");
        
        // Create latch with count = 3 (wait for 3 tasks)
        CountDownLatch latch = new CountDownLatch(3);
        
        // Start 3 worker threads
        for (int i = 1; i <= 3; i++) {
            int taskId = i;
            new Thread(() -> {
                try {
                    System.out.println("Task " + taskId + " starting...");
                    // Simulate different task durations
                    Thread.sleep(1000 + taskId * 500);
                    System.out.println("Task " + taskId + " completed!");
                    
                    // Signal that this task is done (decrements count)
                    latch.countDown();
                    System.out.println("Task " + taskId + " called countDown() - remaining: " + latch.getCount());
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        System.out.println("Main thread waiting for all tasks to complete...");
        
        // Block until all 3 tasks call countDown() (count reaches 0)
        // COORDINATION MECHANISM: await() provides a synchronization point
        // - Main thread waits for all worker threads to signal completion
        // - Once count reaches 0, ALL waiting threads are released simultaneously
        // - This creates a "barrier" - all work must complete before proceeding
        latch.await();
        
        System.out.println("All tasks completed! Main thread continues.\n");
    }
    
    /**
     * Exercise 6: Semaphore
     * 
     * WHAT IS A SEMAPHORE?
     * A counting semaphore maintains a set of permits. Threads can acquire
     * permits (blocking if none available) and release permits.
     * 
     * HOW IT WORKS:
     * 1. Initialize with number of available permits
     * 2. acquire(): Takes a permit, blocks if none available
     * 3. release(): Returns a permit, potentially waking up waiting threads
     * 4. Permits can be acquired/released by different threads
     * 
     * TYPES OF SEMAPHORES:
     * - Binary Semaphore: 1 permit (similar to mutex/lock)
     * - Counting Semaphore: N permits (resource pool)
     * 
     * COMMON USE CASES:
     * 1. Limiting concurrent access to resources (database connections)
     * 2. Rate limiting (API calls per second)
     * 3. Resource pools (thread pools, connection pools)
     * 4. Coordinating access to shared resources
     * 
     * FAIRNESS:
     * - Can be fair (FIFO) or unfair (default)
     * - Fair semaphores prevent starvation but have lower throughput
     */
    public static void demonstrateSemaphore() throws InterruptedException {
        System.out.println("6. Semaphore Example (Resource Pool):");
        System.out.println("   Simulating limited resources (e.g., database connections)");
        System.out.println("   Only 2 resources available, 5 threads competing\n");
        
        // Create semaphore with 2 permits (2 resources available)
        Semaphore semaphore = new Semaphore(2);
        
        Thread[] threads = new Thread[5];
        for (int i = 1; i <= 5; i++) {
            int threadId = i;
            threads[i-1] = new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " waiting for resource... (available: " + semaphore.availablePermits() + ")");
                    
                    // Try to acquire a permit (resource)
                    semaphore.acquire(); // Blocks if no permits available
                    // RESOURCE ACQUISITION: Thread now has exclusive access to one resource
                    // from the pool. Other threads must wait if all permits are taken.
                    
                    System.out.println("Thread " + threadId + " acquired resource! Working... (available: " + semaphore.availablePermits() + ")");
                    
                    // Simulate using the resource
                    Thread.sleep(2000);
                    
                    System.out.println("Thread " + threadId + " releasing resource (available: " + semaphore.availablePermits() + ")");
                    
                    // CRITICAL: Always release permits in finally block to prevent resource leaks
                    // Even if exception occurs, resource must be returned to pool
                    semaphore.release(); // Return resource to pool, wake up waiting threads
                    
                    System.out.println("Thread " + threadId + " released resource (available: " + semaphore.availablePermits() + ")");
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i-1].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Semaphore example completed!\n");
        System.out.println("OBSERVE: Only 2 threads could work simultaneously (2 permits)");
        System.out.println("Other threads had to wait for resources to be released.");
    }
}

