package exercise.concurency;

//Реализую собственный пул потоков.
//В качестве аргументов конструктора пула передается его емкость (количество рабочих потоков).
//Как только пул создан, он сразу инициализирует и запускает потоки.

//Внутри пула очередь задач на исполнение организуется через LinkedList<Runnable>.

//При выполнении у пула потоков метода execute(Runnable), указанная задача должна попасть
//в очередь исполнения, и как только появится свободный поток - должна быть выполнена.

//Также необходимо реализовать метод shutdown(), после выполнения которого новые задачи больше не принимаются
//и пулом (при попытке добавить задачу можно бросать IllegalStateException).
//И все потоки для которых больше нет задачи завершают свою работу.

//дополнительно можно добавить метод awaitTermination() без таймаута,
//работающий аналогично стандартным пулам потоков на Java

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Мини-пул потоков на wait/notify с ограниченной очередью.
 * <p>
 * Ключевые идеи корректности:
 * 1) Вся общая структура данных (очередь задач + счет состояния) защищена одним монитором: synchronized(queue).
 * Это дает взаимное исключение и гарантии видимости (happens-before при входе/выходе из монитора).
 * 2) Все ожидания оформлены как while(condition) { wait(); } — защита от ложных пробуждений и гонки сигналов.
 * 3) Пользовательский код (task.run()) выполняется ВНЕ критической секции — замок не удерживается во время работы задачи.
 * 4) Завершение (shutdown) — "graceful": новые задачи не принимаем, но уже стоящие в очереди и взятые в работу — дорабатываются.
 * Когда очередь опустеет и воркер вернется в ожидание, он увидит флаг isShutdown и завершится.
 * <p>
 * Примечание про "утечку this" из конструктора:
 * - Мы создаем и запускаем worker-потоки в конструкторе. Это обычно анти-паттерн (this "escape"),
 * но здесь безопасно, т.к. все поля, к которым обращается worker (queue, capacity, isShutdown),
 * уже инициализированы до старта потоков: очередь и списки созданы, capacity присвоен,
 * isShutdown = false (volatile). В интервью можно отметить, что "правильнее" фабрика/Builder.
 */

public class CustomThreadPool {
    /**
     * Воркеры для join() при завершении.
     */
    private final List<Thread> workers = new ArrayList<>();
    /**
     * Очередь задач — используем также как объект-монитор.
     */
    private final LinkedList<Runnable> queue = new LinkedList<>(); //монитор
    /**
     * Емкость очереди (bounded).
     */
    private int capacity; //0 => inbounded
    /**
     * Флаг мягкой остановки.
     * volatile гарантирует видимость изменения без входа в монитор (например, если поток читает вне synchronized).
     * Мы все равно читаем/пишем его под замком в важных местах, но volatile упрощает аргументацию видимости.
     */
    private volatile boolean isShutdown = false;

    public CustomThreadPool(int nThreads) {
        this(nThreads, 10);
    }


    public CustomThreadPool(int nThreads, int capacity) {//создать и запустить воркер
        if (nThreads <= 0) throw new IllegalArgumentException("nThreads > 0");
        if (capacity <= 0) throw new IllegalArgumentException("capacity <= 0");
        this.capacity = capacity;

        // Создаем и запускаем воркеры. Они сразу уходят в workerLoop(),
        // где будут ждать задачи на очереди через queue.wait().
        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread(this::workerLoop, "pool-worker-" + i);
            t.start();
            workers.add(t);
        }
    }


    /**
     * Главный цикл воркера:
     * - Достать задачу (или дождаться, пока она появится),
     * - Разбудить потенциально ожидающих продюсеров (когда мы освободили место в очереди),
     * - Выполнить задачу вне замка,
     * - Повторить, пока не пришел shutdown и очередь не пуста.
     */
    private void workerLoop() {
        try {
            while (true) {
                Runnable task;
                synchronized (queue) {
                    // Условие ожидания:
                    //  - Если очередь пуста и еще НЕ shutdown — нам нечего делать, ждем notifyAll().
                    //  - Важно именно while, а не if: possible spurious wakeups + гонка сигналов.
                    while (queue.isEmpty() && !isShutdown) {
                        queue.wait(); // освобождает монитор и усыпляет поток
                    }

                    // Два сценария выхода из ожидания:
                    // A) В очереди есть задача — забираем и идем выполнять.
                    // B) Очередь пуста, но isShutdown == true — пора завершаться (graceful exit).
                    if (queue.isEmpty() && isShutdown) {
                        return; // корректное завершение воркера
                    }

                    // Забираем ЗАДАЧУ под замком, публикация состояния случится на выходе из synchronized.
                    task = queue.removeFirst();

                    // Так как мы освободили одно место в bounded-очереди, стоит разбудить продюсеров,
                    // которые могли застрять в execute() из-за fullness (queue.size() == capacity).
                    // notifyAll() — потому что на этом же мониторе могут одновременно ждать И продюсеры, И другие воркеры.
                    queue.notifyAll();
                }

                // Важно: ВЫПОЛНЯЕМ задачу ВНЕ монитора.
                // Иначе пользовательский код может зависнуть, удерживая наш замок, и весь пул "замрет".
                try {
                    task.run();
                } catch (RuntimeException e) {
                    // Никогда не даем воркеру умереть от пользовательского исключения.
                    // В проде — логируем; на интервью достаточно проглотить/отметить.
                }

            }
        } catch (InterruptedException e) {
            // Поддерживаем семантику прерываний: помечаем флаг и выходим из цикла.
            // Этот путь возможен, если мы решим в будущем поддержать "shutdownNow()" с interrupt всех воркеров.
            Thread.currentThread().interrupt(); //поддержка прирывания на остновке
        }
    }



    /**
     * Поставить задачу на выполнение.
     *
     * Семантика:
     * - Если пул закрыт (shutdown), задача не принимается => бросаем RejectedExecutionException.
     * - Если очередь заполнена (size == capacity), вызывающий поток БЛОКИРУЕТСЯ до освобождения места или shutdown.
     * - При успешной постановке будим воркеров (notifyAll), чтобы они могли взять задачу.
     *
     * Почему мы не объявляем throws InterruptedException?
     * - Для простоты интерфейса оставим как Executor#execute (без checked исключений).
     *   Если поток прервут во время ожидания свободного места, аккуратно переустановим флаг interrupt
     *   и бросим RejectedExecutionException("interrupted while submitting"), чтобы вызывающий не завис.
     */
    public void execute(Runnable task) { //добавление задачи с блокировкой/ожиданием
        if (task == null) throw new NullPointerException("task");
        synchronized (queue) {
            // Если уже закрыты — отклоняем. Видимость обновления isShutdown обеспечивается
            // либо через volatile, либо через вход в монитор (что тоже создает HB).
            if(isShutdown) {
                throw new RejectedExecutionException("Poll is shut down");
            }

            // Если очередь переполнена, ждём освобождения места.
            // Ожидание в while, т.к. после пробуждения надо перепроверить условия (мог прийти shutdown).
            while (queue.size() == capacity && !isShutdown) {
                try {
                    queue.wait(); // освобождает монитор, ставит поток в wait-set
                } catch (InterruptedException ie) {
                    // Политика: не держим вызывающий поток насильно — восстанавливаем interrupt-статус
                    // и сигналим, что операция отклонена из-за прерывания.
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException("Interrupted while submitting task",ie);
                }
            }

            // Если проснулись из-за shutdown — новые задачи больше не принимаем.
            if (isShutdown) {
                throw new RejectedExecutionException("Poll is shutdown");
            }

            // Здесь гарантированно есть место: добавляем задачу.
            queue.addLast(task);

            // Будим всех возможных потребителей (воркеры ждут "есть ли задача?").
            // notifyAll(), а не notify(): один монитор — два типа ожидателей (продюсеры/консюмеры).
            queue.notifyAll();
        }
    }

    /**
     * Мягкая остановка:
     * - Запрещаем прием НОВЫХ задач (isShutdown = true),
     * - Будим всех, кто ждет: воркеров (чтобы они вышли, когда очередь опустеет) и продюсеров,
     *   которые могли ждать свободного места (чтобы они перепроверили условие и получили RejectedExecutionException).
     *
     * NB: Мы НЕ прерываем воркеры — они закончат, когда доработают оставшиеся задачи.
     * Если нужно "быстро и сейчас" — предусмотрите shutdownNow() с interrupt всех worкеров.
     */
    public void shutdown() {
        synchronized (queue) {
            isShutdown = true;   // публикация запрета на прием задач
            queue.notifyAll();   // разбудить всех ожидающих на этом мониторе
        }
    }

    /**
     * Ожидать завершения всех воркеров.
     * - Предполагается, что сначала вызван shutdown(), иначе join может ждать бесконечно.
     * - Если текущий поток прервут — пробрасываем InterruptedException (классическая семантика).
     */
    public void awaitTermination() throws InterruptedException {
        for (Thread t : workers) {
            // join() не держит монитор очереди — это важно: остановка/завершение прогрессируют параллельно.
            t.join();
        }
    }

    private class Worker extends Thread {

        @Override
        public void run() {

        }
    }

    /**
     * Микро-стресс-тест для SimpleThreadPool на wait/notify.
     *
     * Что важно увидеть по логам:
     * 1) "submit took ~Х ms" — время вызова execute(). Когда очередь переполнена (10),
     *    этот вызов блокируется, пока воркеры не освободят место. Мы увидим "скачки" во времени.
     * 2) Старт/финиш задач: параллельность  ≈ числу воркеров (3), очередь "подпитывает" их.
     * 3) После shutdown(): пул дорабатывает очередь и корректно завершается (awaitTermination() вернётся).
     * 4) Попытка submit после shutdown() -> RejectedExecutionException.
     */
    public static void main(String[] args) throws InterruptedException {
        // 1) Создаём пул: 3 воркера, bounded очередь на 10 элементов.
        CustomThreadPool pool = new CustomThreadPool(3);

        final int totalTasks = 30;
        AtomicInteger running = new AtomicInteger(0);    // Сколько задач сейчас "в работе"
        AtomicInteger completed = new AtomicInteger(0);  // Сколько задач завершилось

        // 2) Сабмитим 30 задач. Каждая задача:
        //    - логирует старт/финиш + имя потока
        //    - имитирует работу 100..180ms
        // Замеряем длительность submit(), чтобы увидеть блокировку при переполненной очереди.
        for (int i = 1; i <= totalTasks; i++) {
            final int id = i;
            long t0 = System.currentTimeMillis();
            try {
                pool.execute(() -> {
                    int nowRunning = running.incrementAndGet();
                    System.out.printf("task #%02d START on %s | running=%d%n",
                            id, Thread.currentThread().getName(), nowRunning);
                    try {
                        // Имитация разной "тяжести": чуть разные sleep, чтобы перемешивать порядок завершений
                        Thread.sleep(100 + (id % 5) * 20L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // корректная поддержка interrupt
                    } finally {
                        int after = running.decrementAndGet();
                        int done = completed.incrementAndGet();
                        System.out.printf("task #%02d DONE  on %s | running=%d | completed=%d%n",
                                id, Thread.currentThread().getName(), after, done);
                    }
                });
            } catch (RejectedExecutionException rex) {
                // В нормальном сценарии до shutdown сюда не попадём.
                System.out.printf("submit #%02d REJECTED: %s%n", id, rex.getMessage());
            } finally {
                long dt = System.currentTimeMillis() - t0;
                // Если очередь была полной, dt будет заметно больше (submit блокировался).
                System.out.printf("submit #%02d took ~%d ms%n", id, dt);
            }
        }

        // 3) Запрашиваем мягкую остановку:
        //    - новые задачи не принимаются,
        //    - воркеры дорабатывают уже стоящие в очереди,
        //    - когда очередь опустеет, воркеры завершатся.
        System.out.println(">>> calling shutdown()");
        pool.shutdown();

        // 4) Демонстрация: submit ПОСЛЕ shutdown должен быть отклонён.
        try {
            pool.execute(() -> System.out.println("should NOT run"));
            System.out.println("UNEXPECTED: submit after shutdown did not throw");
        } catch (RejectedExecutionException expected) {
            System.out.println("OK: submit after shutdown rejected: " + expected.getMessage());
        }

        // 5) Ждём завершения всех воркеров. Если всё корректно — вернётся.
        System.out.println(">>> awaiting termination ...");
        pool.awaitTermination();
        System.out.println(">>> pool terminated.");

        // 6) Простая проверка-инвариант:
        if (completed.get() != totalTasks) {
            throw new AssertionError("Not all tasks completed: " + completed.get() + "/" + totalTasks);
        } else {
            System.out.println("OK: all tasks completed = " + completed.get());
        }

        // Подсказка: чтобы усилить эффект блокировок submit(),
        // увеличь totalTasks, либо уменьшай число воркеров/увеличивай работу внутри задач.
    }
    //Output:

    /* Worker-1 executing task 1
    Worker-0 executing task 0
    Worker-2 executing task 2
    Worker-1 executing task 3
    Worker-2 executing task 4
    ....
     */
}