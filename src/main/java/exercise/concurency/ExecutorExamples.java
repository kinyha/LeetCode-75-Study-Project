package exercise.concurency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * ExecutorExamples - Demonstrates Java's ExecutorService and Thread Pool concepts
 * 
 * This class covers:
 * 1. Fixed Thread Pool - Fixed number of threads, reused for multiple tasks
 * 2. Cached Thread Pool - Creates threads as needed, reuses idle threads
 * 3. Single Thread Executor - Sequential execution with single thread
 * 4. Scheduled Thread Pool - Delayed and periodic task execution
 * 5. Future and Callable - Getting results from asynchronous tasks
 * 6. Timeout handling - Preventing tasks from running too long
 * 7. Parallel Processing - Coordinating multiple concurrent tasks
 * 
 * KEY CONCEPTS:
 * - ExecutorService: High-level API for managing threads
 * - Thread Pools: Reuse threads to avoid creation/destruction overhead
 * - Future: Represents result of asynchronous computation
 * - Callable: Like Runnable but can return values and throw exceptions
 */
public class ExecutorExamples {
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== ExecutorService and ThreadPool Examples ===\n");
        
        // 1. Fixed Thread Pool
        demonstrateFixedThreadPool();
        
        // 2. Cached Thread Pool
        // demonstrateCachedThreadPool();
        
        // 3. Single Thread Executor
        // demonstrateSingleThreadExecutor();
        
        // 4. Scheduled Thread Pool
        // demonstrateScheduledThreadPool();
        
        // 5. Future and Callable
        // demonstrateFutureAndCallable();
        
        // 6. ExecutorService with timeout
        // demonstrateTimeout();
        
        // 7. Parallel task processing
        // demonstrateParallelProcessing();
    }
    
    /**
     * Exercise 1: Fixed Thread Pool - Bank Transaction Processing Simulation
     * 
     * WHAT IS A FIXED THREAD POOL?
     * A thread pool with a fixed number of threads that are reused for executing tasks.
     * If all threads are busy, new tasks wait in a queue.
     * 
     * CHARACTERISTICS:
     * - Fixed number of threads (set at creation)
     * - Threads are reused for multiple tasks
     * - Unbounded task queue (LinkedBlockingQueue)
     * - Threads remain alive until pool is shut down
     * 
     * WHEN TO USE:
     * - Predictable workload with steady number of tasks
     * - Want to limit resource usage (memory, CPU)
     * - Need to prevent thread creation overhead
     * 
     * THREAD LIFECYCLE:
     * 1. Pool creates N threads at startup
     * 2. Tasks are submitted to queue
     * 3. Idle threads pick up tasks from queue
     * 4. Threads execute task and return to pool
     * 5. Process repeats until shutdown
     */
    public static void demonstrateFixedThreadPool() throws InterruptedException {
        System.out.println("1. Fixed Thread Pool Example - Bank Transaction Processing:");
        System.out.println("   Simulating bank with 3 tellers processing 12 transactions");
        System.out.println("   Each transaction takes different time based on complexity\n");
        
        // Create thread pool with exactly 3 threads (3 bank tellers)
        ExecutorService bankExecutor = Executors.newFixedThreadPool(3);
        
        // Array of transaction types with different processing times
        String[] transactionTypes = {
            "Quick Deposit", "Account Transfer", "Loan Application", "Cash Withdrawal",
            "Balance Inquiry", "Credit Check", "Mortgage Review", "Investment Consultation",
            "Wire Transfer", "Currency Exchange", "Safe Deposit", "Account Opening"
        };
        
        // Submit 12 different bank transactions
        for (int i = 0; i < transactionTypes.length; i++) {
            final int transactionId = i + 1;
            final String transactionType = transactionTypes[i];
            
            bankExecutor.submit(() -> {
                String tellerName = Thread.currentThread().getName();
                System.out.println("🏦 Teller " + tellerName + " started processing: " + 
                                 transactionType + " (ID: " + transactionId + ")");
                
                try {
                    // Different transaction types take different processing times
                    int processingTime = calculateProcessingTime(transactionType);
                    Thread.sleep(processingTime);
                    
                    // Simulate random processing outcome
                    boolean success = Math.random() > 0.1; // 90% success rate
                    
                    if (success) {
                        System.out.println("✅ Teller " + tellerName + " completed: " + 
                                         transactionType + " (ID: " + transactionId + 
                                         ") in " + processingTime + "ms");
                    } else {
                        System.out.println("❌ Teller " + tellerName + " failed: " + 
                                         transactionType + " (ID: " + transactionId + 
                                         ") - requires manager approval");
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("⚠️  Transaction " + transactionId + " was interrupted");
                }
            });
            
            // Simulate customers arriving at different intervals
            Thread.sleep(200); // New customer every 200ms
        }
        
        // Bank closing procedure:
        System.out.println("\n🏦 Bank is closing - no new customers accepted");
        bankExecutor.shutdown(); // No new transactions accepted
        
        // Wait for all current transactions to complete (max 15 seconds)
        boolean allTransactionsCompleted = bankExecutor.awaitTermination(15, TimeUnit.SECONDS);
        
        if (!allTransactionsCompleted) {
            System.out.println("⏰ Some transactions taking too long - forcing closure");
            bankExecutor.shutdownNow(); // Force shutdown if timeout
        } else {
            System.out.println("✅ All transactions completed successfully!");
        }
        
        System.out.println("\n📊 OBSERVE: Only 3 tellers (threads) processed all 12 transactions");
        System.out.println("   - Threads were reused efficiently");
        System.out.println("   - Queue managed customer wait times");
        System.out.println("   - Resource usage was controlled and predictable\n");
    }
    
    /**
     * Helper method to calculate processing time based on transaction complexity
     * More complex transactions take longer to process
     */
    private static int calculateProcessingTime(String transactionType) {
        return switch (transactionType) {
            case "Balance Inquiry", "Quick Deposit", "Cash Withdrawal" -> 500 + (int)(Math.random() * 300); // 500-800ms
            case "Account Transfer", "Wire Transfer", "Currency Exchange" -> 1000 + (int)(Math.random() * 500); // 1000-1500ms
            case "Credit Check", "Account Opening", "Safe Deposit" -> 1500 + (int)(Math.random() * 700); // 1500-2200ms
            case "Loan Application", "Mortgage Review", "Investment Consultation" -> 2000 + (int)(Math.random() * 1000); // 2000-3000ms
            default -> 1000; // Default processing time
        };
    }
    
    /**
     * Exercise 2: Cached Thread Pool
     * 
     * WHAT IS A CACHED THREAD POOL?
     * A thread pool that creates threads as needed and reuses idle threads.
     * Idle threads are terminated after 60 seconds of inactivity.
     * 
     * CHARACTERISTICS:
     * - Creates threads on demand (no initial threads)
     * - Reuses idle threads if available
     * - Terminates idle threads after 60 seconds
     * - Uses SynchronousQueue (no task buffering)
     * - Can grow to unlimited size
     * 
     * WHEN TO USE:
     * - Unpredictable workload with varying number of tasks
     * - Short-lived tasks
     * - Need quick response time
     * - Tasks don't consume much memory
     * 
     * CAUTION:
     * - Can create too many threads under heavy load
     * - Risk of OutOfMemoryError if tasks pile up
     * - Not suitable for long-running tasks
     */
    public static void demonstrateCachedThreadPool() throws InterruptedException {
        System.out.println("2. Cached Thread Pool Example:");
        System.out.println("   Creates threads as needed, reuses idle threads");
        System.out.println("   Good for short-lived, unpredictable workloads\n");
        
        // Create cached thread pool (starts with 0 threads)
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // Submit 5 tasks quickly
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Cached task " + taskId + " by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500); // Short task duration
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Cached thread pool completed!\n");
        System.out.println("OBSERVE: New threads created as needed, may reuse some threads.");
    }
    
    /**
     * Exercise 3: Single Thread Executor
     * 
     * WHAT IS A SINGLE THREAD EXECUTOR?
     * An executor that uses exactly one thread to execute tasks sequentially.
     * Tasks are executed in the order they were submitted.
     * 
     * CHARACTERISTICS:
     * - Only one thread executes all tasks
     * - Tasks execute sequentially (one after another)
     * - Guarantees order of execution
     * - If thread dies, a new one is created
     * - Uses unbounded queue for pending tasks
     * 
     * WHEN TO USE:
     * - Need sequential execution (order matters)
     * - Want to avoid synchronization issues
     * - Background processing (logging, cleanup)
     * - Event processing where order is important
     * 
     * ADVANTAGES:
     * - No concurrency issues
     * - Guaranteed execution order
     * - Simple to reason about
     * 
     * DISADVANTAGES:
     * - No parallelism
     * - Can become bottleneck
     * - Single point of failure
     */
    public static void demonstrateSingleThreadExecutor() throws InterruptedException {
        System.out.println("3. Single Thread Executor Example:");
        System.out.println("   All tasks execute sequentially on single thread");
        System.out.println("   Guarantees execution order, no concurrency issues\n");
        
        // Create single-threaded executor
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Sequential task " + taskId + " by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Single thread executor completed!\n");
        System.out.println("OBSERVE: Same thread executed all tasks in submission order.");
    }
    
    /**
     * Exercise 4: Scheduled Thread Pool
     * 
     * WHAT IS A SCHEDULED THREAD POOL?
     * An executor that can schedule commands to run after a delay or periodically.
     * Extends ExecutorService with scheduling capabilities.
     * 
     * SCHEDULING METHODS:
     * - schedule(): Run once after delay
     * - scheduleAtFixedRate(): Run periodically at fixed intervals
     * - scheduleWithFixedDelay(): Run with fixed delay between executions
     * 
     * KEY DIFFERENCES:
     * - scheduleAtFixedRate: Next execution starts at fixed intervals
     *   (regardless of how long previous execution took)
     * - scheduleWithFixedDelay: Next execution starts after fixed delay
     *   from completion of previous execution
     * 
     * COMMON USE CASES:
     * - Periodic cleanup tasks
     * - Health checks and monitoring
     * - Batch processing jobs
     * - Cache refresh operations
     * - Timeout handling
     */
    public static void demonstrateScheduledThreadPool() throws InterruptedException {
        System.out.println("4. Scheduled Thread Pool Example:");
        System.out.println("   Demonstrating delayed and periodic task execution");
        System.out.println("   Useful for background jobs and monitoring\n");
        
        // Create scheduled thread pool with 2 threads
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        
        // Schedule a one-time task to run after 1 second delay
        scheduler.schedule(() -> {
            System.out.println("[One-time] Delayed task executed after 1 second by " + Thread.currentThread().getName());
        }, 1, TimeUnit.SECONDS);
        
        // Schedule a periodic task: runs every 2 seconds starting immediately
        // scheduleAtFixedRate = fixed interval between start times
        ScheduledFuture<?> periodicTask = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[Periodic] Task executed at " + System.currentTimeMillis() + " by " + Thread.currentThread().getName());
        }, 0, 2, TimeUnit.SECONDS); // initialDelay=0, period=2 seconds
        
        // Let the periodic task run for 8 seconds, then cancel it
        Thread.sleep(8000);
        
        System.out.println("\nCancelling periodic task...");
        boolean cancelled = periodicTask.cancel(false); // false = don't interrupt running task
        System.out.println("Task cancelled: " + cancelled);
        
        // Shutdown scheduler
        scheduler.shutdown();
        scheduler.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("Scheduled thread pool completed!\n");
    }
    
    /**
     * Exercise 5: Future and Callable
     * 
     * RUNNABLE vs CALLABLE:
     * - Runnable: void run() - Cannot return values or throw checked exceptions
     * - Callable: V call() throws Exception - Can return values and throw exceptions
     * 
     * WHAT IS A FUTURE?
     * Future represents the result of asynchronous computation.
     * Provides methods to check if computation is complete, wait for completion,
     * and retrieve the result.
     * 
     * FUTURE METHODS:
     * - get(): Blocks until result is available (or exception occurs)
     * - get(timeout, unit): Blocks with timeout
     * - isDone(): Check if computation completed
     * - isCancelled(): Check if computation was cancelled
     * - cancel(mayInterruptIfRunning): Attempt to cancel
     * 
     * BENEFITS:
     * - Non-blocking task submission
     * - Can collect results from multiple async tasks
     * - Exception handling for async operations
     * - Timeout support
     */
    public static void demonstrateFutureAndCallable() throws InterruptedException, ExecutionException {
        System.out.println("5. Future and Callable Example:");
        System.out.println("   Using Callable to return values from async tasks");
        System.out.println("   Future allows collecting results when ready\n");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit Callable tasks that return values
        List<Future<Integer>> futures = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            // Submit Callable (returns Future<Integer>)
            Future<Integer> future = executor.submit(() -> { // Callable<Integer>
                Thread.sleep(1000); // Simulate computation
                int result = taskId * taskId;
                System.out.println("Task " + taskId + " computed: " + result + " by " + Thread.currentThread().getName());
                return result; // Return value (not possible with Runnable)
            });
            futures.add(future);
        }
        
        // Collect results from all futures
        System.out.println("\nCollecting results from futures:");
        for (int i = 0; i < futures.size(); i++) {
            // future.get() blocks until result is available
            Integer result = futures.get(i).get(); // May throw ExecutionException
            System.out.println("Result " + (i + 1) + ": " + result);
        }
        
        executor.shutdown();
        System.out.println("Future and Callable completed!\n");
        System.out.println("OBSERVE: Tasks ran in parallel, results collected in order.");
    }
    
    /**
     * Exercise 6: Timeout Handling
     * 
     * WHY TIMEOUTS ARE IMPORTANT:
     * - Prevent tasks from running indefinitely
     * - Provide responsive user experience
     * - Avoid resource exhaustion
     * - Enable graceful degradation
     * 
     * TIMEOUT STRATEGIES:
     * 1. future.get(timeout, unit) - Timeout on result retrieval
     * 2. future.cancel(true) - Interrupt running task
     * 3. CompletableFuture.orTimeout() - Built-in timeout
     * 4. ScheduledExecutorService - Custom timeout logic
     * 
     * EXCEPTION HANDLING:
     * - TimeoutException: Task didn't complete within timeout
     * - ExecutionException: Task threw an exception
     * - InterruptedException: Current thread was interrupted
     * 
     * CANCELLATION:
     * - cancel(false): Don't interrupt if already running
     * - cancel(true): Interrupt running task (if interruptible)
     */
    public static void demonstrateTimeout() throws InterruptedException {
        System.out.println("6. Timeout Example:");
        System.out.println("   Demonstrating timeout handling with Future.get()");
        System.out.println("   Task takes 3 seconds, timeout set to 2 seconds\n");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // Submit a long-running task (3 seconds)
        Future<String> future = executor.submit(() -> {
            System.out.println("Long task starting... (will take 3 seconds)");
            Thread.sleep(3000); // Simulate long-running operation
            return "Task completed successfully";
        });
        
        try {
            // Try to get result with 2-second timeout
            System.out.println("Waiting for result (timeout: 2 seconds)...");
            String result = future.get(2, TimeUnit.SECONDS); // Will timeout!
            System.out.println("Result: " + result);
            
        } catch (TimeoutException e) {
            System.out.println("Task timed out after 2 seconds!");
            System.out.println("Cancelling the task...");
            
            boolean cancelled = future.cancel(true); // true = interrupt if running
            System.out.println("Task cancelled: " + cancelled);
            
        } catch (ExecutionException e) {
            System.out.println("Task failed with exception: " + e.getCause());
        }
        
        executor.shutdown();
        System.out.println("Timeout example completed!\n");
        System.out.println("OBSERVE: Timeout prevented indefinite waiting.");
    }
    
    /**
     * Exercise 7: Parallel Processing Pattern
     * 
     * PARALLEL PROCESSING BENEFITS:
     * - Utilize multiple CPU cores
     * - Reduce total processing time
     * - Handle large datasets efficiently
     * - Scale with available hardware
     * 
     * TYPICAL PATTERN:
     * 1. Split work into independent tasks
     * 2. Submit tasks to thread pool
     * 3. Collect Futures for all tasks
     * 4. Wait for all results and aggregate
     * 
     * CONSIDERATIONS:
     * - Task size: Not too small (overhead) or large (load balancing)
     * - Thread pool size: Usually CPU cores + 1 for CPU-bound tasks
     * - Memory usage: Many futures can consume memory
     * - Exception handling: One failure shouldn't stop others
     * 
     * ALTERNATIVES:
     * - Parallel Streams: stream().parallel()
     * - Fork-Join Pool: For recursive divide-and-conquer
     * - CompletableFuture: More flexible async composition
     */
    public static void demonstrateParallelProcessing() throws InterruptedException, ExecutionException {
        System.out.println("7. Parallel Processing Example:");
        System.out.println("   Processing 20 numbers in parallel with 4 threads");
        System.out.println("   Computing sum of squares (1² + 2² + ... + 20²)\n");
        
        // Create thread pool with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // Create list of numbers to process
        List<Integer> numbers = IntStream.range(1, 21).boxed().toList();
        System.out.println("Numbers to process: " + numbers);
        
        // Submit all tasks and collect futures
        List<Future<Integer>> futures = numbers.stream()
                .map(number -> executor.submit(() -> { // Create Callable for each number
                    // Simulate computation time
                    try {
                        Thread.sleep(100); // 100ms per task
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return 0;
                    }
                    int result = number * number;
                    System.out.println("Computed " + number + "² = " + result + " by " + Thread.currentThread().getName());
                    return result;
                }))
                .toList();
        
        System.out.println("\nAll tasks submitted. Collecting results...");
        
        // Collect results from all futures
        int sum = futures.stream()
                .mapToInt(future -> {
                    try {
                        return future.get(); // Block until result available
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Task failed: " + e.getMessage());
                        return 0; // Use 0 for failed tasks
                    }
                })
                .sum();
        
        System.out.println("\nSum of squares (1-20): " + sum);
        System.out.println("Expected: " + (20 * 21 * 41 / 6)); // Formula: n(n+1)(2n+1)/6
        
        executor.shutdown();
        System.out.println("Parallel processing completed!\n");
        System.out.println("OBSERVE: 4 threads processed 20 tasks in parallel.");
    }
}