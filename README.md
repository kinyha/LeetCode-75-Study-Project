# Java/Kotlin Practice Project

Проект для подготовки к техническим собеседованиям и практики программирования на Java/Kotlin.

## Содержание

- **Алгоритмы** — LeetCode задачи на Kotlin
- **Stream API** — упражнения 4 уровней сложности
- **Многопоточность** — примеры и паттерны concurrency
- **Яндекс задачи** — типовые задачи с собеседований (ATM, лимиты платежей)
- **Документация** — шпаргалки и гайды

## Структура проекта

```
src/main/
├── kotlin/
│   └── arrays/                    # LeetCode 75 решения
│       ├── Solution1-5.kt
│       └── TestUtils.kt           # Утилиты для тестирования
│
└── java/exercise/
    ├── streamExercise/            # Stream API практика
    │   ├── Tasks_v1.java          # Уровни 1-4
    │   ├── Tasks_v2.java          # Расширенная версия
    │   └── data_v1/, data_v2/     # Модели данных
    │
    ├── concurrency/               # Многопоточность
    │   ├── BasicThreads.java      # Основы потоков
    │   ├── ExecutorExamples.java  # ExecutorService
    │   ├── CompletableFutureExamples.java
    │   ├── ConcurrentCollectionsExamples.java
    │   └── theori/                # Теория и паттерны
    │
    ├── codex/                     # Структурированные упражнения
    │   ├── ex01_threads/          # Основы Thread/Runnable
    │   ├── ex02_bounded_buffer/   # Producer-Consumer
    │   ├── ex03_thread_pool/      # Пул потоков
    │   ├── ex04_atomic_counter/   # Atomic операции
    │   └── ex05_message_broker/   # Message broker
    │
    ├── yandex/                    # Задачи для собеседований
    │   ├── dev/tasks/
    │   │   ├── atm/               # Банкомат
    │   │   └── paymentLimit*/     # Проверка лимитов
    │   └── dev/info/              # Гайды и разборы
    │
    └── practice/algo/             # Алгоритмические задачи
        └── AlgorithmicTasks.java  # 13 задач уровней 1-3

docs/                              # Документация
├── java-concurrency-cheatsheet.md
├── java-threading-qa.md
├── kotlin-coroutines-guide.md
├── kotlin-interview-guide.md
├── spring-boot-jpa-guide.md
└── ...
```

## Быстрый старт

```bash
# Сборка
./gradlew build

# Запуск тестов
./gradlew test

# Запуск конкретного теста
./gradlew test --tests "arrays.SolutionArraysTest"
./gradlew test --tests "exercise.TasksTest"
```

## Запуск примеров

```bash
# Stream API
java -cp build/classes/java/main exercise.streamExercise.Tasks_v1

# Многопоточность
java -cp build/classes/java/main exercise.concurrency.BasicThreads
java -cp build/classes/java/main exercise.concurrency.CompletableFutureExamples
```

## Технологии

- **Языки:** Kotlin 2.1, Java 17
- **Сборка:** Gradle (Kotlin DSL)
- **Тестирование:** JUnit 5, AssertJ, Mockito

## Документация

Гайды и шпаргалки находятся в папке `docs/`:

| Файл | Описание |
|------|----------|
| `java-concurrency-cheatsheet.md` | Шпаргалка по многопоточности |
| `java-threading-qa.md` | Q&A по потокам |
| `kotlin-coroutines-guide.md` | Гайд по корутинам |
| `kotlin-interview-guide.md` | Вопросы для собеседований |
| `spring-boot-jpa-guide.md` | Spring Boot + JPA |
