# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a LeetCode 75 study project implemented in Kotlin/Java using Gradle. The project focuses on solving algorithmic problems with clean, efficient code and comprehensive test coverage.

## Build System & Commands

**Build and Test:**
```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew test --tests "arrays.SolutionArraysTest"  # Run specific test class
```

**Development:**
```bash
./gradlew compileKotlin  # Compile Kotlin sources
./gradlew compileJava    # Compile Java sources
```

## Code Architecture

**Package Structure:**
- `arrays/` - Array and string manipulation problems (Solutions 1-5)
- `utils/` - Common testing utilities and data structures (TestUtils.kt)
- `exercise/` - Java-based practice problems and utilities

**Solution Organization:**
- Each solution is in a separate class (Solution1, Solution2, etc.)
- Solutions are organized by problem category in packages
- Each solution class contains the main algorithm implementation
- Test files follow the pattern `*Test.kt` and use JUnit 5

**Key Architectural Patterns:**
- Solutions use Kotlin's functional programming features and concise syntax
- TestUtils provides common data structures (ListNode, TreeNode) and utilities for testing
- Tests use both JUnit assertions and AssertJ for enhanced readability
- The project uses Kotlin 2.0.21 with JVM target 17

**Testing Strategy:**
- Comprehensive test coverage with edge cases
- Uses JUnit 5 platform with AssertJ assertions
- Tests include performance measurement utilities
- TestUtils provides helpers for linked lists, binary trees, and array comparisons

**Progress Tracking:**
The project tracks progress through README.md categories:
- Arrays & Strings (currently 3/15 completed)
- Two Pointers & Sliding Window
- Trees & Binary Search
- Graphs & BFS/DFS
- Dynamic Programming