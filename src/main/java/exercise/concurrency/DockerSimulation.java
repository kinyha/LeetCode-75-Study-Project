package exercise.concurrency;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class DockerSimulation {

    public static void main(String[] args) {
        // 1. СИМУЛЯЦИЯ ДОКЕРА С ЛИМИТОМ
        // Мы говорим JVM: "У тебя есть только 1 реальный поток для виртуальных тредов".
        // Это аналог того, как если бы JVM увидела 1 ядро в контейнере.
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");

        System.out.println("=== 🚀 ЗАПУСК: 10 Виртуальных потоков на 1 Реальном потоке ===");
        printSystemInfo();

        var start = Instant.now();

        // 2. Создаем Executor для виртуальных потоков
        // try-with-resources гарантирует, что мы дождемся завершения всех задач перед выходом
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            IntStream.range(1, 11).forEach(i -> {
                executor.submit(() -> {
                    // ЭТАП 1: Работа на CPU (Mount)
                    String taskName = "Task-" + i;
                    printLog(taskName, "🟢 Начинаю работу (расчет хеша)");

                    // Симуляция работы процессора
                    heavyCalculation();

                    // ЭТАП 2: Блокировка / I/O (Unmount)
                    // В этот момент JVM снимает этот тред с носителя!
                    printLog(taskName, "💤 Иду в базу данных (Sleep 1s)...");
                    try {
                        Thread.sleep(Duration.ofSeconds(1));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // ЭТАП 3: Возврат к работе (Remount)
                    // Тред снова садится на носитель (возможно, уже на другой, но у нас он 1)
                    printLog(taskName, "🔥 Вернулся с данными, завершаю!");
                });
            });
        }

        var end = Instant.now();
        System.out.println("=== 🏁 ФИНИШ ===");
        System.out.println("Всего времени заняло: " + Duration.between(start, end).toMillis() + " ms");
        System.out.println("Заметь: 10 задач по 1 секунде каждая выполнились почти за 1 секунду на 1 потоке!");
    }

    // Хелпер для красивого вывода
    private static void printLog(String task, String message) {
        // Thread.currentThread() для виртуального треда выведет что-то типа:
        // VirtualThread[#21]/runnable@ForkJoinPool-1-worker-1
        String fullThreadName = Thread.currentThread().toString();

        // Вытаскиваем имя реального потока-носителя (Carrier Thread)
        // Обычно это хвост строки после '@'
        String carrierThread = fullThreadName.substring(fullThreadName.lastIndexOf("@") + 1);

        System.out.printf("[%s] on [%-25s] -> %s%n", task, carrierThread, message);
    }

    private static void heavyCalculation() {
        // Просто немного нагружаем CPU, чтобы имитировать бурную деятельность
        double val = 0;
        for (int i = 0; i < 100_000; i++) {
            val += Math.sin(i);
        }
    }

    private static void printSystemInfo() {
        System.out.println("Доступные процессоры (JVM view): " + Runtime.getRuntime().availableProcessors());
        System.out.println("Parallelism setting: " + System.getProperty("jdk.virtualThreadScheduler.parallelism"));
        System.out.println("----------------------------------------------------------------");
    }
}
