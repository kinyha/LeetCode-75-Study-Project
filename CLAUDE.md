# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Проект для подготовки к техническим собеседованиям на Java/Kotlin. Включает:
- LeetCode алгоритмы на Kotlin
- Stream API упражнения (4 уровня сложности)
- Многопоточность (примеры и паттерны)
- Задачи для собеседований (Яндекс: ATM, лимиты платежей)
- Документация и шпаргалки

## Build System & Commands

**Build and Test:**
```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew test --tests "arrays.SolutionArraysTest"  # Kotlin tests
./gradlew test --tests "exercise.TasksTest"         # Stream API tests
```

**Direct Class Execution:**
```bash
# Stream API
java -cp build/classes/java/main exercise.streamExercise.Tasks_v1

# Concurrency
java -cp build/classes/java/main exercise.concurrency.BasicThreads
java -cp build/classes/java/main exercise.concurrency.CompletableFutureExamples
java -cp build/classes/java/main exercise.concurrency.ConcurrentCollectionsExamples
```

## Code Architecture

**Package Organization:**

```
src/main/
├── kotlin/arrays/                 # LeetCode решения + TestUtils
│
└── java/exercise/
    ├── streamExercise/            # Stream API (Tasks_v1, Tasks_v2)
    │   ├── data_v1/               # Модели: Customer, Order, Transaction
    │   └── data_v2/               # Модели: University, Student, Course
    │
    ├── concurrency/               # Многопоточность
    │   ├── BasicThreads.java
    │   ├── ExecutorExamples.java
    │   ├── CompletableFutureExamples.java
    │   ├── ConcurrentCollectionsExamples.java
    │   └── theori/                # Теория, Producer-Consumer
    │
    ├── codex/                     # Структурированные упражнения
    │   ├── ex01_threads/          # Thread, Runnable, join
    │   ├── ex02_bounded_buffer/   # wait/notify, Producer-Consumer
    │   ├── ex03_thread_pool/      # Простой пул потоков
    │   ├── ex04_atomic_counter/   # AtomicInteger, volatile
    │   └── ex05_message_broker/   # In-memory broker
    │
    ├── yandex/                    # Задачи для собеседований
    │   ├── dev/tasks/atm/         # Банкомат (выдача купюр)
    │   ├── dev/tasks/paymentLimit*/ # Проверка лимитов платежей
    │   └── dev/info/              # Гайды и разборы задач
    │
    └── practice/algo/             # Алгоритмические задачи
        └── AlgorithmicTasks.java  # 13 задач уровней 1-3

docs/                              # Документация
├── java-concurrency-cheatsheet.md
├── java-threading-qa.md
├── kotlin-*.md
└── spring-boot-jpa-guide.md
```

**Key Files:**

| Файл | Описание |
|------|----------|
| `streamExercise/Tasks_v1.java` | Stream API уровни 1-4 |
| `concurrency/BasicThreads.java` | Основы потоков |
| `yandex/dev/YandexDevTasks.md` | Задачи для практики |
| `practice/algo/AlgorithmicTasks.java` | 13 алгоритмических задач |

**Testing:**
- JUnit 5 + AssertJ + Mockito
- `TestUtils.kt` — утилиты для тестирования (ListNode, TreeNode)

**Concurrency Topics:**
- Race conditions, synchronized, volatile
- Atomic operations (AtomicInteger, CAS)
- Producer-Consumer (wait/notify)
- ExecutorService, CompletableFuture
- Concurrent collections (ConcurrentHashMap, BlockingQueue)

**Stream API Levels:**
- Level 1: filter, map, collect
- Level 2: sorted, groupingBy
- Level 3: custom collectors, flatMap
- Level 4: parallel streams, reduce
