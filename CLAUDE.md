# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a LeetCode 75 study project implemented in Kotlin/Java using Gradle. The project focuses on solving algorithmic problems with clean, efficient code and comprehensive test coverage. Additionally, it includes Java concurrency examples and Stream API practice exercises.

## Build System & Commands

**Build and Test:**
```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew test --tests "arrays.SolutionArraysTest"  # Run specific test class
./gradlew test --tests "exercise.TasksTest"  # Run Java Stream API tests
```

**Development:**
```bash
./gradlew compileKotlin  # Compile Kotlin sources
./gradlew compileJava    # Compile Java sources
./gradlew run            # Run main applications (if configured)
```

**Running Examples:**
- Java concurrency examples: Run `BasicThreads.main()` directly
- Stream API exercises: Run `Tasks.main()` with specific method calls enabled

**Direct Class Execution (WSL/Linux):**
```bash
# Run specific Java class with main method
java -cp build/classes/java/main exercise.concurency.ConcurrentCollectionsExamples
java -cp build/classes/java/main exercise.Tasks
java -cp build/classes/java/main exercise.concurency.BasicThreads

# Pattern: java -cp build/classes/java/main <full.package.ClassName>
```
*Note: Use this when ./gradlew has line ending issues in WSL*

## Code Architecture

**Multi-Language Structure:**
The project combines Kotlin (primary) and Java (supplementary) code:
- **Kotlin**: LeetCode algorithm solutions with functional programming patterns
- **Java**: Concurrency examples and Stream API practice exercises

**Package Organization:**
- `src/main/kotlin/arrays/` - LeetCode array/string solutions (Solution1-5)
- `src/main/kotlin/utils/` - TestUtils.kt with data structures and testing utilities
- `src/main/java/exercise/` - Java practice exercises
  - `Tasks.java` - Stream API exercises with 4 difficulty levels
  - `concurrency/` - Comprehensive concurrency examples and patterns
  - `env/` - Data models for Stream API exercises

**Solution Architecture:**
- **LeetCode Solutions**: Each in separate classes (Solution1, Solution2, etc.)
- **Stream API Tasks**: Organized by difficulty levels (1-4) with progressive complexity
- **Concurrency Examples**: Educational implementations covering fundamental concepts

**Key Design Patterns:**
- **Kotlin Solutions**: Leverage functional programming, extension functions, and concise syntax
- **Java Examples**: Demonstrate classic patterns like Producer-Consumer, thread synchronization
- **Test Structure**: TestUtils provides ListNode, TreeNode, and utility methods for algorithm testing
- **Language Integration**: Mixed Kotlin/Java codebase with shared testing infrastructure

**Testing Strategy:**
- **JUnit 5** platform with AssertJ assertions for enhanced readability
- **TestUtils.kt** provides comprehensive testing utilities:
  - LinkedList and BinaryTree creation/conversion
  - Array comparison utilities (ordered/unordered)
  - Performance measurement tools
  - Random data generation for testing
- Tests include edge cases and performance considerations

**Concurrency Coverage:**
The `exercise/concurrency/` package demonstrates:
- Race conditions and synchronization mechanisms
- Atomic operations and lock-free programming
- Producer-Consumer patterns with wait/notify
- CountDownLatch and Semaphore coordination
- Thread pools and CompletableFuture examples
- Concurrent collections usage patterns

**Stream API Practice:**
Tasks.java contains 4 levels of Stream API exercises:
- **Level 1**: Basic filtering, mapping, and terminal operations
- **Level 2**: Intermediate operations with sorting and grouping
- **Level 3**: Complex grouping, custom collectors, and multi-step operations
- **Level 4**: Advanced parallel processing and custom collection patterns