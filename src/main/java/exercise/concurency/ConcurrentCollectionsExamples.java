package exercise.concurency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConcurrentCollectionsExamples - Demonstrates Java's concurrent collection classes
 * 
 * CONCURRENT COLLECTIONS OVERVIEW:
 * Java provides thread-safe collection implementations that are optimized for concurrent access.
 * These collections use various synchronization techniques to achieve better performance
 * than simply wrapping regular collections with synchronized wrappers.
 * 
 * KEY BENEFITS:
 * 1. Thread Safety: Multiple threads can safely access without external synchronization
 * 2. Performance: Optimized for concurrent access patterns
 * 3. Scalability: Better performance under high contention compared to synchronized collections
 * 4. Non-blocking: Many operations don't block threads (lock-free algorithms)
 * 
 * MAIN CATEGORIES:
 * - Maps: ConcurrentHashMap, ConcurrentSkipListMap
 * - Queues: ConcurrentLinkedQueue, BlockingQueue implementations
 * - Lists: CopyOnWriteArrayList, CopyOnWriteArraySet
 * - Deques: ConcurrentLinkedDeque
 */
public class ConcurrentCollectionsExamples {
    
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 1000;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Collections Examples ===\n");
        
        // 1. ConcurrentHashMap
//        demonstrateConcurrentHashMap();
        
        // 2. CopyOnWriteArrayList
        demonstrateCopyOnWriteArrayList();
//
//        // 3. BlockingQueue variants
//        demonstrateBlockingQueues();
//
//        // 4. ConcurrentLinkedQueue
//        demonstrateConcurrentLinkedQueue();
//
//        // 5. ConcurrentSkipListMap
//        demonstrateConcurrentSkipListMap();
//
//        // 6. Performance comparison
//        demonstratePerformanceComparison();
//
//        // 7. Producer-Consumer with BlockingQueue
//        demonstrateProducerConsumerWithBlockingQueue();
    }
    
    /**
     * Exercise 1: ConcurrentHashMap
     * 
     * WHAT IS CONCURRENTHASHMAP?
     * A thread-safe hash table implementation that allows concurrent reads and writes
     * without blocking the entire map. It uses segment-based locking (Java 7) or
     * CAS operations with synchronized blocks (Java 8+).
     * 
     * KEY FEATURES:
     * - Lock-free reads: Multiple threads can read simultaneously
     * - Segment locking: Writes only lock specific segments/buckets
     * - Atomic operations: compute(), putIfAbsent(), replace() methods
     * - No null keys/values: Unlike HashMap, nulls are not allowed
     * - Fail-safe iterators: Don't throw ConcurrentModificationException
     * 
     * PERFORMANCE CHARACTERISTICS:
     * - O(1) average time for get/put operations
     * - Better than synchronized HashMap under concurrent access
     * - Memory overhead due to additional synchronization structures
     * 
     * WHEN TO USE:
     * - High read-to-write ratio scenarios
     * - Multiple threads need to access the same map
     * - Need atomic operations like putIfAbsent, compute
     */
    public static void demonstrateConcurrentHashMap() throws InterruptedException {
        System.out.println("1. ConcurrentHashMap Example:");
        System.out.println("   Demonstrating thread-safe map operations with multiple producers");
        
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        // Multiple threads adding to the map
        // CONCURRENT WRITES: Each thread adds unique keys, demonstrating thread safety
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String key = "key" + (threadId * 100 + j);
                    // PUT OPERATION: Thread-safe insertion
                    // ConcurrentHashMap uses CAS operations + synchronized blocks
                    // for specific buckets, allowing high concurrency
                    concurrentMap.put(key, threadId * 100 + j);
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        System.out.println("ConcurrentHashMap size: " + concurrentMap.size());
        
        // ATOMIC OPERATIONS: Demonstrating thread-safe computations
        concurrentMap.put("counter", 0);
        CountDownLatch counterLatch = new CountDownLatch(THREAD_COUNT);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    // COMPUTE METHOD: Atomically reads, computes, and updates value
                    // This is thread-safe equivalent of:
                    // int current = map.get(key); map.put(key, current + 1);
                    // The lambda is executed atomically for the specific key
                    concurrentMap.compute("counter", (k, v) -> v == null ? 1 : v + 1);
                }
                counterLatch.countDown();
            }).start();
        }
        
        counterLatch.await();
        System.out.println("Counter value: " + concurrentMap.get("counter"));
        System.out.println("ConcurrentHashMap completed!\n");
    }
    
    /**
     * Comparison: Collections.synchronizedList() vs CopyOnWriteArrayList
     * Shows the difference in iteration behavior
     */
    public static void demonstrateSynchronizedListVsCopyOnWrite() throws InterruptedException {
        System.out.println("=== Synchronized List vs CopyOnWriteArrayList Comparison ===");

        // Setup synchronized list
        List<Integer> regularList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            regularList.add(i);
        }
        List<Integer> synchronizedList = Collections.synchronizedList(regularList);

        // Setup CopyOnWriteArrayList
        CopyOnWriteArrayList<Integer> cowList = new CopyOnWriteArrayList<>();
        for (int i = 1; i <= 10; i++) {
            cowList.add(i);
        }

        System.out.println("\n1. SYNCHRONIZED LIST:");
        System.out.println("   - Throws ConcurrentModificationException during unsafe iteration");

        // Start modifier thread for synchronized list
        new Thread(() -> {
            try {
                Thread.sleep(50); // Let iteration start first
                for (int i = 0; i < 5; i++) {
                    synchronizedList.add(1000 + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Try to iterate synchronized list (will likely throw exception)
        try {
            System.out.print("   Iterating synchronized list: ");
            int count = 0;
            for (Integer value : synchronizedList) {
                if (count++ < 15) {
                    System.out.print(value + " ");
                    Thread.sleep(80);  // Slow iteration to allow concurrent modification
                } else {
                    break;
                }
            }
            System.out.println("\n   ✓ Iteration completed successfully");
        } catch (Exception e) {
            System.out.println("\n   ✗ Exception: " + e.getClass().getSimpleName());
        }

        Thread.sleep(1000); // Let first test complete

        System.out.println("\n2. COPYONWRITEARRAYLIST:");
        System.out.println("   - Safe iteration with snapshot semantics");

        // Start modifier thread for CopyOnWriteArrayList
        new Thread(() -> {
            try {
                Thread.sleep(50);
                for (int i = 0; i < 5; i++) {
                    cowList.add(2000 + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Iterate CopyOnWriteArrayList (always safe)
        try {
            System.out.print("   Iterating CopyOnWriteArrayList: ");
            int count = 0;
            for (Integer value : cowList) {
                if (count++ < 15) {
                    System.out.print(value + " ");
                    Thread.sleep(80);
                } else {
                    break;
                }
            }
            System.out.println("\n   ✓ Iteration completed safely - no concurrent modifications visible");
        } catch (Exception e) {
            System.out.println("\n   ✗ Unexpected exception: " + e.getClass().getSimpleName());
        }

        Thread.sleep(1000);
        System.out.println("\nFinal sizes:");
        System.out.println("   Synchronized List: " + synchronizedList.size());
        System.out.println("   CopyOnWriteArrayList: " + cowList.size());
        System.out.println("=== Comparison completed ===\n");
    }

    /**
     * Exercise 2: CopyOnWriteArrayList
     *
     * WHAT IS COPYONWRITEARRAYLIST?
     * A thread-safe list implementation where write operations (add, set, remove)
     * create a new copy of the underlying array. Reads access the current array
     * without any synchronization.
     *
     * COPY-ON-WRITE MECHANISM:
     * 1. Reads: Direct access to array, no locking (very fast)
     * 2. Writes: Create new array with modifications, atomically replace reference
     * 3. Iterators: Use snapshot of array at creation time (never see concurrent modifications)
     *
     * PERFORMANCE CHARACTERISTICS:
     * - Fast reads: O(1) with no synchronization overhead
     * - Expensive writes: O(n) due to array copying
     * - Memory overhead: Multiple array versions may exist temporarily
     *
     * WHEN TO USE:
     * - Read-heavy scenarios (many reads, few writes)
     * - Event listeners, observer patterns
     * - Configuration lists that change infrequently
     * - Need consistent iteration without ConcurrentModificationException
     *
     * WHEN NOT TO USE:
     * - Write-heavy scenarios (frequent additions/removals)
     * - Large lists (copying becomes expensive)
     * - Memory-constrained environments
     */
    public static void demonstrateCopyOnWriteArrayList() throws InterruptedException {
        System.out.println("2. CopyOnWriteArrayList Example:");
        System.out.println("   Demonstrating copy-on-write semantics and safe iteration");

        CopyOnWriteArrayList<Integer> copyOnWriteList = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // Multiple threads adding elements
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    copyOnWriteList.add(threadId * 50 + j);
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("CopyOnWriteArrayList size: " + copyOnWriteList.size());

        // SAFE ITERATION DEMONSTRATION: Showing snapshot semantics
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                copyOnWriteList.add(1000 + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // ITERATOR SNAPSHOT: Iterator uses array snapshot from creation time
        // Concurrent modifications during iteration are NOT visible
        // This prevents ConcurrentModificationException but may show stale data
        System.out.println("Iterating (won't see concurrent modifications):");
        int count = 0;
        for (Integer value : copyOnWriteList) {
            if (count++ < 10) {
                System.out.print(value + " ");
            } else {
                break;
            }
        }
        System.out.println("\nCopyOnWriteArrayList completed!\n");

        // COMPARISON: Synchronized List behavior
        demonstrateSynchronizedListVsCopyOnWrite();
    }

    /**
     * Exercise 3: BlockingQueue variants
     * 
     * WHAT IS BLOCKINGQUEUE?
     * An interface extending Queue with blocking operations:
     * - put(): Blocks if queue is full (bounded queues)
     * - take(): Blocks if queue is empty
     * - offer(timeout): Tries to insert with timeout
     * - poll(timeout): Tries to remove with timeout
     * 
     * COMMON IMPLEMENTATIONS:
     * 
     * 1. ARRAYBLOCKINGQUEUE:
     *    - Fixed capacity backed by array
     *    - Single lock for put/take operations
     *    - Fair/unfair locking options
     *    - Good for bounded producer-consumer
     * 
     * 2. LINKEDBLOCKINGQUEUE:
     *    - Optionally bounded, backed by linked nodes
     *    - Separate locks for put/take (better concurrency)
     *    - Default capacity: Integer.MAX_VALUE
     *    - Better for high-throughput scenarios
     * 
     * 3. PRIORITYBLOCKINGQUEUE:
     *    - Unbounded priority queue
     *    - Elements ordered by Comparator or natural ordering
     *    - Useful for task scheduling based on priority
     * 
     * 4. SYNCHRONOUSQUEUE:
     *    - No storage capacity (capacity = 0)
     *    - Each put() waits for corresponding take()
     *    - Direct thread-to-thread handoff
     *    - Used in thread pools (cached thread pool)
     * 
     * USAGE PATTERNS:
     * - Producer-Consumer: Coordinate between data producers and consumers
     * - Thread Pools: Task queues for worker threads
     * - Rate Limiting: Control processing speed with bounded queues
     */
    public static void demonstrateBlockingQueues() throws InterruptedException {
        System.out.println("3. BlockingQueue Variants Example:");
        System.out.println("   Comparing different BlockingQueue implementations");
        
        // ArrayBlockingQueue
        System.out.println("ArrayBlockingQueue (bounded):");
        BlockingQueue<Integer> arrayQueue = new ArrayBlockingQueue<>(5);
        demonstrateBlockingQueue(arrayQueue, "ArrayBlockingQueue");
        
        // LinkedBlockingQueue
        System.out.println("LinkedBlockingQueue (unbounded):");
        BlockingQueue<Integer> linkedQueue = new LinkedBlockingQueue<>();
        demonstrateBlockingQueue(linkedQueue, "LinkedBlockingQueue");
        
        // PriorityBlockingQueue
        System.out.println("PriorityBlockingQueue (priority order):");
        BlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>(10, Collections.reverseOrder());
        demonstratePriorityBlockingQueue(priorityQueue);
        
        // SynchronousQueue
        System.out.println("SynchronousQueue (no storage):");
        BlockingQueue<Integer> synchronousQueue = new SynchronousQueue<>();
        demonstrateSynchronousQueue(synchronousQueue);
        
        System.out.println("BlockingQueue variants completed!\n");
    }
    
    private static void demonstrateBlockingQueue(BlockingQueue<Integer> queue, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        
        // Producer
        new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.put(i);
                    System.out.println(name + " produced: " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Consumer
        new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Integer value = queue.take();
                    System.out.println(name + " consumed: " + value);
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        latch.await();
    }
    
    private static void demonstratePriorityBlockingQueue(BlockingQueue<Integer> queue) throws InterruptedException {
        // Add elements in random order
        int[] values = {3, 1, 4, 1, 5, 9, 2, 6};
        for (int value : values) {
            queue.put(value);
        }
        
        System.out.print("Priority order: ");
        while (!queue.isEmpty()) {
            System.out.print(queue.take() + " ");
        }
        System.out.println();
    }
    
    private static void demonstrateSynchronousQueue(BlockingQueue<Integer> queue) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        
        new Thread(() -> {
            try {
                System.out.println("SynchronousQueue putting 42...");
                // PUT BLOCKS: This will block until another thread calls take()
                // SynchronousQueue has no internal storage - direct handoff
                queue.put(42);
                System.out.println("SynchronousQueue put completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        Thread.sleep(1000); // Delay consumer to show blocking behavior
        
        new Thread(() -> {
            try {
                System.out.println("SynchronousQueue taking...");
                // DIRECT HANDOFF: This take() will unblock the put() operation
                // Data is transferred directly from producer to consumer thread
                Integer value = queue.take();
                System.out.println("SynchronousQueue took: " + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }).start();
        
        latch.await();
    }
    
    /**
     * Exercise 4: ConcurrentLinkedQueue
     * 
     * WHAT IS CONCURRENTLINKEDQUEUE?
     * A thread-safe, unbounded queue based on linked nodes using lock-free algorithms.
     * It uses Compare-And-Swap (CAS) operations to achieve thread safety without locks.
     * 
     * KEY CHARACTERISTICS:
     * - Lock-free: Uses CAS operations instead of locks
     * - Unbounded: Can grow as needed (limited by memory)
     * - FIFO ordering: First In, First Out semantics
     * - Non-blocking: Operations don't block threads
     * - Weakly consistent iterators: May not reflect recent modifications
     * 
     * ALGORITHM:
     * Uses Michael & Scott lock-free queue algorithm:
     * 1. CAS operations on head/tail pointers
     * 2. Retry loops when CAS fails due to concurrent modifications
     * 3. ABA problem handling with careful pointer management
     * 
     * PERFORMANCE:
     * - Better than synchronized LinkedList under contention
     * - No thread blocking, better for real-time systems
     * - Memory overhead for CAS-friendly node structure
     * 
     * WHEN TO USE:
     * - High-throughput producer-consumer scenarios
     * - When you need unbounded queue without blocking
     * - Real-time systems where blocking is unacceptable
     * - Publisher-subscriber patterns
     */
    public static void demonstrateConcurrentLinkedQueue() throws InterruptedException {
        System.out.println("4. ConcurrentLinkedQueue Example:");
        System.out.println("   Demonstrating lock-free queue operations with multiple producers/consumers");
        
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger totalAdded = new AtomicInteger(0);
        
        // Multiple threads adding elements
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    queue.offer(threadId * 100 + j);
                    totalAdded.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        System.out.println("Total elements added: " + totalAdded.get());
        System.out.println("Queue size: " + queue.size());
        
        // CONCURRENT POLLING: Multiple consumers competing for elements
        CountDownLatch pollLatch = new CountDownLatch(5);
        AtomicInteger totalPolled = new AtomicInteger(0);
        
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (!queue.isEmpty()) {
                    // POLL OPERATION: Non-blocking removal
                    // Uses CAS to atomically update head pointer
                    // Returns null if queue becomes empty during operation
                    Integer value = queue.poll();
                    if (value != null) {
                        totalPolled.incrementAndGet();
                    }
                    // RACE CONDITION HANDLING: Multiple threads may see !isEmpty()
                    // but queue becomes empty before poll() - hence null check
                }
                pollLatch.countDown();
            }).start();
        }
        
        pollLatch.await();
        System.out.println("Total elements polled: " + totalPolled.get());
        System.out.println("Remaining in queue: " + queue.size());
        System.out.println("ConcurrentLinkedQueue completed!\n");
    }
    
    /**
     * Exercise 5: ConcurrentSkipListMap
     * 
     * WHAT IS CONCURRENTSKIPLISTMAP?
     * A thread-safe, sorted map implementation using skip list data structure.
     * It provides O(log n) operations while maintaining sorted order and thread safety.
     * 
     * SKIP LIST ALGORITHM:
     * - Probabilistic data structure with multiple levels
     * - Each level is a sorted linked list
     * - Higher levels have fewer elements (probability-based)
     * - Search starts from top level, drops down when needed
     * - Achieves O(log n) performance without tree rebalancing
     * 
     * THREAD SAFETY:
     * - Lock-free implementation using CAS operations
     * - No global locks, fine-grained synchronization per node
     * - Handles concurrent insertions/deletions safely
     * 
     * KEY FEATURES:
     * - Sorted ordering: Natural or custom Comparator
     * - NavigableMap: firstKey(), lastKey(), subMap() operations
     * - Range queries: headMap(), tailMap(), subMap()
     * - Atomic operations: putIfAbsent(), replace(), etc.
     * 
     * PERFORMANCE:
     * - O(log n) for get, put, remove operations
     * - Better than TreeMap for concurrent access
     * - Memory overhead due to skip list structure
     * 
     * WHEN TO USE:
     * - Need sorted map with concurrent access
     * - Range queries on sorted data
     * - Alternative to ConcurrentHashMap when ordering matters
     * - Priority-based processing systems
     */
    public static void demonstrateConcurrentSkipListMap() throws InterruptedException {
        System.out.println("5. ConcurrentSkipListMap Example:");
        System.out.println("   Demonstrating sorted concurrent map with range operations");
        
        ConcurrentSkipListMap<Integer, String> skipListMap = new ConcurrentSkipListMap<>();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        // Multiple threads adding elements
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    int key = threadId * 50 + j;
                    skipListMap.put(key, "Value" + key);
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        System.out.println("SkipListMap size: " + skipListMap.size());
        
        // Show sorted order
        System.out.println("First 10 entries (sorted):");
        skipListMap.entrySet().stream()
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));
        
        // RANGE OPERATIONS: Demonstrating NavigableMap capabilities
        System.out.println("Entries between 100 and 200:");
        // SUBMAP: Returns a view of portion of map with keys in range [100, 200)
        // This is a live view - changes to original map are reflected
        // Thread-safe iteration over the submap
        skipListMap.subMap(100, 200)
                .entrySet()
                .stream()
                .limit(5)
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));
        
        System.out.println("ConcurrentSkipListMap completed!\n");
    }
    
    /**
     * Exercise 6: Performance comparison
     * 
     * PERFORMANCE COMPARISON: Synchronized vs Concurrent Collections
     * 
     * SYNCHRONIZED COLLECTIONS (Collections.synchronizedMap()):
     * - Use single lock for entire collection
     * - All operations are mutually exclusive
     * - Simple but poor scalability under contention
     * - Can cause thread convoy effects
     * 
     * CONCURRENT COLLECTIONS (ConcurrentHashMap):
     * - Use fine-grained locking or lock-free algorithms
     * - Allow concurrent reads and some concurrent writes
     * - Better performance under high contention
     * - More complex implementation but better scalability
     * 
     * SCALABILITY FACTORS:
     * - Number of threads: Concurrent collections scale better
     * - Read/write ratio: Concurrent collections excel with more reads
     * - Contention level: Higher contention favors concurrent collections
     * - Operation types: Bulk operations may favor different approaches
     */
    public static void demonstratePerformanceComparison() throws InterruptedException {
        System.out.println("6. Performance Comparison Example:");
        System.out.println("   Comparing synchronized HashMap vs ConcurrentHashMap under contention");
        
        // Compare HashMap vs ConcurrentHashMap
        Map<Integer, Integer> hashMap = new HashMap<>();
        Map<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        // Test with HashMap (synchronized)
        long startTime = System.nanoTime();
        testMapPerformance(Collections.synchronizedMap(hashMap), "Synchronized HashMap");
        long syncTime = System.nanoTime() - startTime;
        
        // Test with ConcurrentHashMap
        startTime = System.nanoTime();
        testMapPerformance(concurrentMap, "ConcurrentHashMap");
        long concurrentTime = System.nanoTime() - startTime;
        
        System.out.println("Synchronized HashMap time: " + syncTime / 1_000_000 + " ms");
        System.out.println("ConcurrentHashMap time: " + concurrentTime / 1_000_000 + " ms");
        System.out.println("Performance comparison completed!\n");
    }
    
    private static void testMapPerformance(Map<Integer, Integer> map, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    int key = threadId * OPERATIONS_PER_THREAD + j;
                    // PERFORMANCE TEST: Mix of puts and gets
                    // Synchronized map: Each operation acquires full map lock
                    // ConcurrentHashMap: Allows concurrent reads, segment-level write locking
                    map.put(key, key * 2);
                    map.get(key); // Read operation - concurrent maps allow multiple readers
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        System.out.println(name + " final size: " + map.size());
    }
    
    /**
     * Exercise 7: Producer-Consumer with BlockingQueue
     * 
     * PRODUCER-CONSUMER WITH BLOCKINGQUEUE:
     * BlockingQueue provides built-in coordination for producer-consumer pattern,
     * eliminating need for manual wait/notify synchronization.
     * 
     * ADVANTAGES OVER MANUAL SYNCHRONIZATION:
     * 1. Automatic blocking: put() blocks when full, take() blocks when empty
     * 2. Timeout support: offer(timeout), poll(timeout) for time-limited operations
     * 3. Capacity management: Bounded queues provide backpressure automatically
     * 4. Exception safety: Proper handling of interruptions
     * 5. Multiple producers/consumers: Natural support without complex coordination
     * 
     * BACKPRESSURE MECHANISM:
     * - Bounded queue prevents fast producers from overwhelming slow consumers
     * - Producer blocks when queue is full, naturally slowing down production
     * - Consumer blocks when queue is empty, waiting for more work
     * 
     * REAL-WORLD APPLICATIONS:
     * - Web servers: Request queues between listener and worker threads
     * - Message processing: Queue messages between receiver and processor
     * - Batch processing: Queue work items between stages
     * - Thread pools: Task queues in executor services
     */
    public static void demonstrateProducerConsumerWithBlockingQueue() throws InterruptedException {
        System.out.println("7. Producer-Consumer with BlockingQueue Example:");
        System.out.println("   Multiple producers and consumers with automatic coordination");
        
        // BOUNDED QUEUE: Capacity of 100 provides backpressure mechanism
        BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>(100);
        CountDownLatch latch = new CountDownLatch(5); // 2 producers + 3 consumers
        
        // Producers
        for (int i = 1; i <= 2; i++) {
            int producerId = i;
            new Thread(() -> {
                try {
                    for (int j = 1; j <= 10; j++) {
                        Task task = new Task(producerId, j, "Task from producer " + producerId);
                        taskQueue.put(task);
                        System.out.println("Producer " + producerId + " created: " + task);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // Consumers
        for (int i = 1; i <= 3; i++) {
            int consumerId = i;
            new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        // POLL WITH TIMEOUT: Waits up to 2 seconds for task
                        // Returns null if timeout expires - graceful shutdown mechanism
                        Task task = taskQueue.poll(2, TimeUnit.SECONDS);
                        if (task == null) break; // Timeout, assuming no more tasks
                        
                        System.out.println("Consumer " + consumerId + " processing: " + task);
                        Thread.sleep(200); // Simulate processing time
                        System.out.println("Consumer " + consumerId + " completed: " + task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await();
        System.out.println("Producer-Consumer with BlockingQueue completed!\n");
    }
    
    /**
     * Helper class representing a work item in producer-consumer pattern
     * 
     * TASK DESIGN CONSIDERATIONS:
     * - Immutable: All fields are final to ensure thread safety
     * - Identifiable: Contains producer and task IDs for tracking
     * - Descriptive: Includes description for debugging/monitoring
     * - Serializable: Could be extended to support persistence or network transfer
     */
    static class Task {
        private final int producerId;
        private final int taskId;
        private final String description;
        
        public Task(int producerId, int taskId, String description) {
            this.producerId = producerId;
            this.taskId = taskId;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "Task{" +
                    "producerId=" + producerId +
                    ", taskId=" + taskId +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}