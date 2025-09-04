Java Concurrency + Mini Message Broker (чистый JDK)

Цель: руками освежить многопоточность в Java и реализовать упрощенный in-memory message broker без сторонних зависимостей.

Как запускать упражнения
- Требуется установленный `javac`/`java` (Java 11+ рекомендуется, 17+ идеально).
- Запуск примеров: `bash scripts/run.sh ex01_threads` (или другой каталог упражнения).

Структура
- `ex01_threads`: базовые потоки, `Thread`, `Runnable`, `join`, `sleep`.
- `ex02_bounded_buffer`: реализовать ограниченную очередь с `wait/notify` (producer/consumer).
- `ex03_thread_pool`: написать простой фиксированный пул потоков.
- `ex04_atomic_counter`: гонки, `volatile`, `Atomic*` и корректировка.
- `ex05_message_broker`: каркас in-memory брокера с темами, подписками и доставкой.

Роадмап по реализации (рекомендация)
1) Пробежать `ex01_threads` (готовый пример). 
2) Реализовать `ex02_bounded_buffer` (блокирующая очередь через `synchronized` + `wait/notifyAll`).
3) Реализовать `ex03_thread_pool` (очередь задач + воркеры + `shutdown`).
4) Поиграться с `ex04_atomic_counter` (увидеть гонку и исправить).
5) Доделать `ex05_message_broker`: темы, подписки, публикация, поток-диспетчер.

Подсказки
- Минимизируйте синхронизацию: защищайте только общий mutable state.
- Для `wait/notify`: всегда проверяйте условие в цикле `while`.
- Атомарные типы: `AtomicInteger`, `AtomicReference`, `LongAdder`.
- Корректно завершайте потоки (`interrupt`, флажки завершения, `shutdown`/`await`).

