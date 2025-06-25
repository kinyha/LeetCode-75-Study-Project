package exercise.concurency;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * CompletableFutureExamples - Demonstrates asynchronous programming with CompletableFuture
 * 
 * COMPLETABLEFUTURE OVERVIEW:
 * CompletableFuture is Java's implementation of a Promise/Future pattern for asynchronous programming.
 * It represents a computation that may complete in the future and provides methods to
 * compose, combine, and handle asynchronous operations.
 * 
 * KEY CONCEPTS:
 * 1. ASYNCHRONOUS EXECUTION: Operations run in background threads
 * 2. NON-BLOCKING: Main thread doesn't wait for completion
 * 3. COMPOSABLE: Chain operations together with thenApply, thenCompose
 * 4. COMBINABLE: Merge multiple futures with thenCombine, allOf, anyOf
 * 5. EXCEPTION HANDLING: Handle errors with handle, exceptionally
 * 
 * BENEFITS OVER TRADITIONAL THREADING:
 * - Functional composition of async operations
 * - Built-in exception handling mechanisms
 * - Easier coordination of multiple async tasks
 * - Integration with streams and lambda expressions
 * - Timeout and cancellation support
 * 
 * COMMON PATTERNS:
 * - Pipeline processing: Chain transformations
 * - Fan-out/Fan-in: Parallel execution then combine results
 * - Fire-and-forget: Async execution without waiting for result
 * - Timeout handling: Fail-fast for long-running operations
 */
public class CompletableFutureExamples {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== CompletableFuture and Async Programming Examples ===\n");
        
        // 1. Basic CompletableFuture
        demonstrateBasicCompletableFuture();
        
        // 2. Chaining operations
        demonstrateChaining();
        
        // 3. Combining multiple futures
        demonstrateCombining();
        
        // 4. Exception handling
        demonstrateExceptionHandling();
        
        // 5. Async composition
        demonstrateAsyncComposition();
        
        // 6. Parallel processing with CompletableFuture
        demonstrateParallelProcessing();
        
        // 7. Timeout and cancellation
        demonstrateTimeoutAndCancellation();
    }
    
    /**
     * Exercise 1: Basic CompletableFuture
     * 
     * BASIC COMPLETABLEFUTURE OPERATIONS:
     * 
     * 1. COMPLETED FUTURE: Create future with immediate value
     *    - completedFuture(): Returns already completed future
     *    - Useful for testing or when value is already available
     * 
     * 2. ASYNC SUPPLY: Create future from supplier function
     *    - supplyAsync(): Executes supplier in ForkJoinPool.commonPool()
     *    - Returns CompletableFuture<T> where T is supplier's return type
     *    - Non-blocking: Main thread continues while computation runs
     * 
     * THREAD EXECUTION:
     * - Default executor: ForkJoinPool.commonPool()
     * - Custom executor can be provided as second parameter
     * - Daemon threads: JVM can exit without waiting for completion
     * 
     * BLOCKING OPERATIONS:
     * - get(): Blocks until completion, throws checked exceptions
     * - join(): Blocks until completion, throws unchecked exceptions
     * - getNow(defaultValue): Returns immediately with value or default
     */
    public static void demonstrateBasicCompletableFuture() throws ExecutionException, InterruptedException {
        System.out.println("1. Basic CompletableFuture Example:");
        System.out.println("   Showing immediate completion vs async execution");
        
        // COMPLETED FUTURE: Already has a value, no async execution
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("Hello World");
        System.out.println("Completed future result: " + future1.get());
        
        // ASYNC COMPUTATION: Executes in background thread
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing in thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000); // Simulate long-running computation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 42;
        });
        
        // GET BLOCKS: Main thread waits for async computation to complete
        System.out.println("Async computation result: " + future2.get());
        System.out.println("Basic CompletableFuture completed!\n");
    }
    
    /**
     * Exercise 2: Chaining operations
     * 
     * OPERATION CHAINING:
     * CompletableFuture supports functional composition through chaining methods.
     * Each step in the chain processes the result of the previous step.
     * 
     * KEY CHAINING METHODS:
     * 
     * 1. THENAPPLY: Transform result (Function<T, R>)
     *    - Synchronous transformation of the result
     *    - Executed in same thread as previous stage (usually)
     *    - Returns CompletableFuture<R>
     * 
     * 2. THENAPPLYASYNC: Transform result asynchronously
     *    - Executes transformation in different thread
     *    - Useful for CPU-intensive transformations
     * 
     * 3. THENACCEPT: Consume result (Consumer<T>)
     *    - Terminal operation that doesn't return value
     *    - Returns CompletableFuture<Void>
     * 
     * 4. THENRUN: Execute action (Runnable)
     *    - Executes action without access to previous result
     *    - Returns CompletableFuture<Void>
     * 
     * PIPELINE PROCESSING:
     * - Each stage processes output of previous stage
     * - Stages can run in same thread or different threads
     * - Exception in any stage propagates to final result
     * - Lazy evaluation: Chain doesn't execute until terminal operation
     */
    public static void demonstrateChaining() throws ExecutionException, InterruptedException {
        System.out.println("2. Chaining Operations Example:");
        System.out.println("   Building a processing pipeline with transformations");
        
        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Step 1: Fetching data...");
                    sleepQuietly(500);
                    return "raw data";
                })
                // THENAPPLY: Transform the raw data to uppercase
                .thenApply(data -> {
                    System.out.println("Step 2: Processing data...");
                    sleepQuietly(500);
                    return data.toUpperCase();
                })
                // THENAPPLY: Format the processed data
                .thenApply(processed -> {
                    System.out.println("Step 3: Formatting result...");
                    sleepQuietly(500);
                    return "Processed: " + processed;
                });
        
        System.out.println("Final result: " + result.get());
        System.out.println("Chaining completed!\n");
    }
    
    /**
     * Exercise 3: Combining multiple futures
     * 
     * COMBINING MULTIPLE FUTURES:
     * CompletableFuture provides several methods to coordinate multiple async operations.
     * This enables fan-out/fan-in patterns where work is distributed and results aggregated.
     * 
     * KEY COMBINING METHODS:
     * 
     * 1. THENCOMBINE: Combine two futures with binary function
     *    - Waits for both futures to complete
     *    - Applies BiFunction to both results
     *    - Returns CompletableFuture with combined result
     * 
     * 2. ALLOF: Wait for all futures to complete
     *    - Returns CompletableFuture<Void> when all complete
     *    - Doesn't combine results, just waits for completion
     *    - Useful for ensuring all parallel work is done
     * 
     * 3. ANYOF: Wait for any future to complete
     *    - Returns CompletableFuture<Object> with first completed result
     *    - Useful for timeout patterns or redundant requests
     *    - Result type is Object, requires casting
     * 
     * PARALLEL EXECUTION:
     * - All futures start executing immediately when created
     * - Execution happens in parallel in thread pool
     * - Combining waits for required futures to complete
     * - Total time is determined by slowest operation
     * 
     * PERFORMANCE BENEFITS:
     * - Parallel execution reduces total time
     * - CPU utilization improved with multiple threads
     * - I/O operations can overlap with CPU work
     */
    public static void demonstrateCombining() throws ExecutionException, InterruptedException {
        System.out.println("3. Combining Multiple Futures Example:");
        System.out.println("   Parallel execution and result combination");
        
        // PARALLEL EXECUTION: All three futures start immediately
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing first value...");
            sleepQuietly(1000);
            return 10;
        });
        
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing second value...");
            sleepQuietly(1500); // Slowest operation
            return 20;
        });
        
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing third value...");
            sleepQuietly(800);
            return 30;
        });
        
        // CHAINED COMBINATION: Combine futures pairwise
        // Total time = max(1000, 1500, 800) = 1500ms (not 1000+1500+800)
        CompletableFuture<Integer> combined = future1
                .thenCombine(future2, Integer::sum) // Waits for both future1 and future2
                .thenCombine(future3, Integer::sum); // Waits for combined result and future3
        
        System.out.println("Combined result: " + combined.get());
        
        // ALLOF: Wait for all individual futures to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);
        allOf.get();
        System.out.println("All futures completed!");
        
        // ANYOF: Get result from first completed future
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(
                createDelayedFuture(100, 1),
                createDelayedFuture(200, 2),
                createDelayedFuture(150, 3)
        );
        System.out.println("First completed result: " + anyOf.get());
        System.out.println("Combining completed!\n");
    }
    
    /**
     * Exercise 4: Exception handling
     * 
     * EXCEPTION HANDLING IN COMPLETABLEFUTURE:
     * Async operations can fail, and CompletableFuture provides several mechanisms
     * to handle exceptions gracefully without crashing the entire pipeline.
     * 
     * KEY EXCEPTION HANDLING METHODS:
     * 
     * 1. HANDLE: Process both success and failure cases
     *    - BiFunction<T, Throwable, R>
     *    - Always called, regardless of success or failure
     *    - Can transform result or recover from exception
     *    - Returns CompletableFuture<R>
     * 
     * 2. EXCEPTIONALLY: Handle only failure cases
     *    - Function<Throwable, T>
     *    - Called only when exception occurs
     *    - Provides recovery value or alternative computation
     *    - Returns CompletableFuture<T>
     * 
     * 3. WHENCOMPLETE: Observe result without transformation
     *    - BiConsumer<T, Throwable>
     *    - Side-effect only, doesn't change result
     *    - Useful for logging or cleanup
     * 
     * EXCEPTION PROPAGATION:
     * - Uncaught exceptions in CompletableFuture are wrapped in CompletionException
     * - Exceptions propagate through the chain until handled
     * - get() throws ExecutionException, join() throws CompletionException
     * - Exception in one stage stops execution of subsequent stages
     * 
     * BEST PRACTICES:
     * - Handle exceptions at appropriate stage in pipeline
     * - Provide meaningful fallback values
     * - Log exceptions for debugging
     * - Use handle() for transformation, exceptionally() for recovery
     */
    public static void demonstrateExceptionHandling() throws ExecutionException, InterruptedException {
        System.out.println("4. Exception Handling Example:");
        System.out.println("   Graceful error handling and recovery strategies");
        
        // FAULTY FUTURE: May succeed or fail randomly
        CompletableFuture<String> faultyFuture = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("Random failure!");
            }
            return "Success!";
        });
        
        // HANDLE METHOD: Processes both success and failure paths
        CompletableFuture<String> handledFuture = faultyFuture
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Caught exception: " + throwable.getMessage());
                        return "Default value"; // Recovery value
                    }
                    return result; // Pass through successful result
                });
        
        System.out.println("Handled result: " + handledFuture.get());
        
        // EXCEPTIONALLY METHOD: Handles only failure cases
        CompletableFuture<Object> recoveredFuture = CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("Always fails!");
                })
                .exceptionally(throwable -> {
                    System.out.println("Recovering from: " + throwable.getMessage());
                    return "Recovered value"; // Fallback value
                });
        
        System.out.println("Recovered result: " + recoveredFuture.get());
        System.out.println("Exception handling completed!\n");
    }
    
    /**
     * Exercise 5: Async composition
     * 
     * ASYNC COMPOSITION:
     * When chaining operations where each step returns a CompletableFuture,
     * use composition methods to avoid nested futures (CompletableFuture<CompletableFuture<T>>).
     * 
     * KEY COMPOSITION METHODS:
     * 
     * 1. THENCOMPOSE: Chain futures sequentially
     *    - Function<T, CompletableFuture<R>>
     *    - Flattens nested CompletableFuture
     *    - Sequential execution: next step waits for previous
     *    - Equivalent to flatMap in streams
     * 
     * 2. THENCOMPOSEASYNC: Chain futures with async execution
     *    - Same as thenCompose but runs in different thread
     *    - Useful when composition function is CPU-intensive
     * 
     * COMPOSITION VS CHAINING:
     * - thenApply: T -> R (synchronous transformation)
     * - thenCompose: T -> CompletableFuture<R> (async composition)
     * - thenApply with CompletableFuture would create nested futures
     * - thenCompose flattens the nesting automatically
     * 
     * REAL-WORLD PATTERN:
     * This pattern is common in microservices where:
     * 1. Get user ID from request
     * 2. Fetch user profile from user service
     * 3. Fetch user preferences from preferences service
     * 4. Combine data for response
     * 
     * Each step is async and depends on previous step's result.
     */
    public static void demonstrateAsyncComposition() throws ExecutionException, InterruptedException {
        System.out.println("5. Async Composition Example:");
        System.out.println("   Sequential async operations with dependent data");
        
        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Getting user ID...");
                    sleepQuietly(500);
                    return "user123";
                })
                // THENCOMPOSEASYNC: Next step depends on user ID from previous step
                .thenComposeAsync(userId -> {
                    System.out.println("Fetching user profile for: " + userId);
                    return CompletableFuture.supplyAsync(() -> {
                        sleepQuietly(1000); // Simulate database/API call
                        return "Profile[" + userId + "]";
                    });
                })
                // THENCOMPOSEASYNC: Next step depends on profile from previous step
                .thenComposeAsync(profile -> {
                    System.out.println("Fetching preferences for: " + profile);
                    return CompletableFuture.supplyAsync(() -> {
                        sleepQuietly(800); // Simulate another service call
                        return profile + " with preferences";
                    });
                });
        
        System.out.println("Composition result: " + result.get());
        System.out.println("Async composition completed!\n");
    }
    
    /**
     * Exercise 6: Parallel processing
     * 
     * PARALLEL PROCESSING WITH COMPLETABLEFUTURE:
     * CompletableFuture integrates well with streams to process collections in parallel.
     * This pattern is useful for independent operations that can benefit from parallelization.
     * 
     * PARALLEL PROCESSING PATTERN:
     * 1. Transform collection to CompletableFuture stream
     * 2. Each element processed independently in parallel
     * 3. Use allOf() to wait for all completions
     * 4. Collect results into final collection
     * 
     * KEY METHODS:
     * 
     * 1. STREAM + MAP: Create futures for each element
     *    - Each supplyAsync runs in parallel
     *    - ForkJoinPool automatically manages threads
     * 
     * 2. ALLOF + THENAPPLY: Wait and collect results
     *    - allOf waits for all futures to complete
     *    - thenApply transforms void result to collected values
     *    - join() extracts values (doesn't throw checked exceptions)
     * 
     * PERFORMANCE CONSIDERATIONS:
     * - Effective for CPU-bound operations
     * - Thread pool size limits parallelism
     * - Overhead of thread creation and context switching
     * - Memory usage for intermediate CompletableFuture objects
     * 
     * WHEN TO USE:
     * - Independent operations on collection elements
     * - CPU-intensive transformations
     * - I/O operations that can be parallelized
     * - Large datasets where parallelization benefits outweigh overhead
     */
    public static void demonstrateParallelProcessing() throws ExecutionException, InterruptedException {
        System.out.println("6. Parallel Processing Example:");
        System.out.println("   Processing collection elements in parallel");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // PARALLEL TRANSFORMATION: Each number processed independently
        List<CompletableFuture<Integer>> futures = numbers.stream()
                .map(number -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("Processing " + number + " in " + Thread.currentThread().getName());
                    sleepQuietly(100); // Simulate processing time
                    return number * number; // CPU-bound operation
                }))
                .collect(Collectors.toList());
        
        // COLLECT RESULTS: Wait for all and combine into single result
        CompletableFuture<List<Integer>> allResults = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0])) // Wait for all
                .thenApply(v -> futures.stream() // Transform to result collection
                        .map(CompletableFuture::join) // Extract values (non-blocking at this point)
                        .collect(Collectors.toList()));
        
        List<Integer> results = allResults.get();
        System.out.println("Parallel results: " + results);
        
        // AGGREGATE RESULTS: Final computation on collected results
        int sum = results.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Sum of squares: " + sum);
        System.out.println("Parallel processing completed!\n");
    }
    
    /**
     * Exercise 7: Timeout and cancellation
     * 
     * TIMEOUT AND CANCELLATION:
     * Long-running async operations need timeout and cancellation mechanisms
     * to prevent resource leaks and provide responsive user experience.
     * 
     * TIMEOUT MECHANISMS:
     * 
     * 1. ORTIMEOUT: Built-in timeout support (Java 9+)
     *    - Automatically completes with TimeoutException if not completed in time
     *    - Doesn't interrupt the underlying computation
     *    - Clean way to implement timeout patterns
     * 
     * 2. COMPLETEONTIMEOUT: Timeout with default value (Java 9+)
     *    - Completes with provided value if timeout occurs
     *    - Useful for providing fallback values
     * 
     * CANCELLATION MECHANISMS:
     * 
     * 1. CANCEL(MAYINTERRUPTIFRUNNING): Cancel future execution
     *    - mayInterruptIfRunning=true: Interrupts executing thread
     *    - mayInterruptIfRunning=false: Only cancels if not started
     *    - Returns false if already completed
     * 
     * 2. THREAD INTERRUPTION HANDLING:
     *    - Check Thread.currentThread().isInterrupted() in loops
     *    - Respond to interruption by throwing exception
     *    - Allows cooperative cancellation
     * 
     * BEST PRACTICES:
     * - Always set reasonable timeouts for external operations
     * - Check for interruption in long-running loops
     * - Provide fallback values for timeout scenarios
     * - Clean up resources in finally blocks
     * - Use structured concurrency patterns where possible
     * 
     * REAL-WORLD APPLICATIONS:
     * - HTTP client calls with timeouts
     * - Database queries with time limits
     * - User interface responsiveness
     * - Resource cleanup and lifecycle management
     */
    public static void demonstrateTimeoutAndCancellation() {
        System.out.println("7. Timeout and Cancellation Example:");
        System.out.println("   Handling long-running tasks with time limits");
        
        // TIMEOUT PATTERN: Automatic failure after time limit
        CompletableFuture<String> timeoutFuture = CompletableFuture
                .supplyAsync(() -> {
                    sleepQuietly(3000); // Long running task (3 seconds)
                    return "Completed";
                })
                .orTimeout(2, TimeUnit.SECONDS); // Timeout after 2 seconds
        
        try {
            String result = timeoutFuture.get();
            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.out.println("Task timed out: " + e.getMessage());
        }
        
        // CANCELLATION PATTERN: Cooperative cancellation with interruption
        CompletableFuture<String> cancellableFuture = CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 10; i++) {
                // COOPERATIVE CANCELLATION: Check for interruption
                if (Thread.currentThread().isInterrupted()) {
                    throw new CompletionException(new InterruptedException("Task was cancelled"));
                }
                sleepQuietly(500);
                System.out.println("Working... " + (i + 1));
            }
            return "Finished";
        });
        
        // DELAYED CANCELLATION: Cancel after 2 seconds
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
            System.out.println("Cancelling task...");
            cancellableFuture.cancel(true); // mayInterruptIfRunning = true
        });
        
        try {
            String result = cancellableFuture.get();
            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.out.println("Task was cancelled or failed: " + e.getMessage());
        }
        
        System.out.println("Timeout and cancellation completed!\n");
    }
    
    /**
     * Helper methods for demonstrations
     */
    
    /**
     * SLEEPQUIETLY: Utility method for simulating work
     * 
     * Properly handles InterruptedException by:
     * 1. Catching the exception (required for Thread.sleep)
     * 2. Restoring interrupted status for higher-level code
     * 3. Allowing cooperative cancellation to work correctly
     */
    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
    
    /**
     * CREATEDELAYEDFUTURE: Factory method for creating futures with delays
     * 
     * Useful for testing timeout and racing scenarios.
     * Each future completes after specified delay with given value.
     */
    private static CompletableFuture<Integer> createDelayedFuture(long delay, int value) {
        return CompletableFuture.supplyAsync(() -> {
            sleepQuietly(delay);
            return value;
        });
    }
}