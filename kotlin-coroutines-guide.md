# Kotlin Coroutines: Complete Interview Guide

## 1. Теоретические основы

### Что такое корутины и зачем они нужны
**Корутины** - это lightweight threads (легковесные потоки). Не блокируют OS thread при приостановке, что позволяет запускать тысячи корутин на небольшом пуле потоков.

**Ключевые преимущества:**
- **Эффективность**: 100K корутин vs 100K threads = 100MB vs 100GB памяти
- **Простота**: последовательный код для асинхронных операций (без callback hell)
- **Отменяемость**: structured concurrency гарантирует управление жизненным циклом
- **Интеграция**: native поддержка в Spring Boot 3.x, Ktor, Android

**Сравнение подходов:**
```kotlin
// 1. Blocking (плохо - блокирует thread)
fun loadUser(): User {
    return blockingApiCall()  // Thread заблокирован
}

// 2. Callbacks (callback hell)
fun loadUser(callback: (User) -> Unit) {
    apiCall { user ->
        enrichCall { enriched ->
            validateCall { validated ->
                callback(validated)
            }
        }
    }
}

// 3. CompletableFuture (verbose)
fun loadUser(): CompletableFuture<User> {
    return apiCall()
        .thenCompose { enrichCall(it) }
        .thenCompose { validateCall(it) }
}

// 4. Coroutines (читаемо и эффективно)
suspend fun loadUser(): User {
    val user = apiCall()        // приостановка без блокировки
    val enriched = enrichCall(user)
    return validateCall(enriched)
}
```

### Как работают корутины под капотом

**Continuation Passing Style (CPS)**
Компилятор превращает suspend функции в state machine:

```kotlin
// Что пишем
suspend fun fetchData(): String {
    delay(1000)
    return "Data"
}

// Во что компилируется (упрощенно)
fun fetchData(continuation: Continuation<String>): Any? {
    class StateMachine : ContinuationImpl {
        var state = 0
        var result: Any? = null
        
        override fun invokeSuspend(result: Result<Any?>): Any? {
            when (state) {
                0 -> {
                    state = 1
                    return delay(1000, this)  // COROUTINE_SUSPENDED
                }
                1 -> {
                    return "Data"
                }
            }
        }
    }
    // ...
}
```

**COROUTINE_SUSPENDED** - маркер, что функция приостановлена и освободила thread.

### Continuation и восстановление контекста
```kotlin
interface Continuation<in T> {
    val context: CoroutineContext
    fun resumeWith(result: Result<T>)
}
```

При приостановке:
1. Сохраняется состояние (локальные переменные, точка возврата)
2. Thread освобождается
3. При готовности данных вызывается `resumeWith`
4. Корутина продолжается (возможно на другом thread)

## 2. CoroutineContext и Dispatchers

### CoroutineContext - окружение корутины
```kotlin
// Context = набор элементов (Job + Dispatcher + Name + ExceptionHandler + ...)
val context = Job() + Dispatchers.IO + CoroutineName("loader")

// Доступ к элементам
val job = context[Job]
val dispatcher = context[ContinuationInterceptor]

// Каждая корутина имеет context
launch(Dispatchers.IO + CoroutineName("worker")) {
    println(coroutineContext[CoroutineName])  // "worker"
}
```

### Dispatchers - где выполняется код

```kotlin
// Main - UI thread (Android), или создается вручную в backend
Dispatchers.Main

// Default - CPU-intensive задачи, пул = кол-во ядер
Dispatchers.Default  // parallelism = Runtime.getRuntime().availableProcessors()

// IO - блокирующий I/O, пул до 64 threads (или больше ядер)
Dispatchers.IO  // shares threads with Default, но может создать больше

// Unconfined - начинает в текущем thread, продолжает где придется (избегать!)
Dispatchers.Unconfined

// Custom thread pool
val customDispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()

// Single thread
@OptIn(ExperimentalCoroutinesApi::class)
val singleThread = newSingleThreadContext("MyThread")
```

**Важно для Spring Boot:**
```kotlin
// В Spring Boot корутины по умолчанию на Dispatchers.Unconfined
// Для блокирующих операций ВСЕГДА оборачивай:
suspend fun findUser(id: Long): User = withContext(Dispatchers.IO) {
    userRepository.findById(id)  // JPA блокирует thread
}

// Или настрой глобально через CoroutineScope bean
@Configuration
class CoroutineConfig {
    @Bean
    fun applicationScope(): CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
}
```

### withContext vs launch для смены dispatcher
```kotlin
// withContext - для возврата результата, sequential
suspend fun loadData(): Data = withContext(Dispatchers.IO) {
    // выполняется в IO dispatcher
    database.load()
}

// launch - fire and forget, concurrent
fun process() {
    coroutineScope.launch(Dispatchers.Default) {
        // параллельная обработка
    }
}
```

## 3. Coroutine Builders

### launch - fire and forget
```kotlin
val job: Job = scope.launch {
    // Возвращает Job для управления
    delay(1000)
    println("Done")
}

// Параметры
scope.launch(
    context = Dispatchers.IO,
    start = CoroutineStart.LAZY  // не стартует сразу
) {
    // code
}

job.start()    // запуск lazy корутины
job.cancel()   // отмена
job.join()     // ожидание завершения
```

### async - для результата
```kotlin
val deferred: Deferred<String> = scope.async {
    delay(1000)
    "Result"
}

val result = deferred.await()  // suspend до получения результата

// Параллельное выполнение
coroutineScope {
    val a = async { api1() }
    val b = async { api2() }
    
    // Запускаются параллельно, ждем оба
    println("${a.await()} ${b.await()}")
}
```

### runBlocking - мост между sync и async
```kotlin
// Блокирует текущий thread (использовать только в main/tests)
fun main() = runBlocking {
    launch { 
        delay(1000)
        println("World")
    }
    println("Hello")
}

// В тестах
@Test
fun test() = runBlocking {
    val result = service.suspendFunction()
    assertEquals("expected", result)
}

// НИКОГДА не используй в production коде!
suspend fun bad() {
    runBlocking {  // НЕПРАВИЛЬНО! Блокирует корутину
        someWork()
    }
}
```

### coroutineScope и supervisorScope
```kotlin
// coroutineScope - создает scope и ждет всех детей
suspend fun loadUserData(): UserData = coroutineScope {
    val profile = async { loadProfile() }
    val posts = async { loadPosts() }
    
    // Если любая упадет - отменятся все
    UserData(profile.await(), posts.await())
}

// supervisorScope - дети независимы
suspend fun loadOptionalData(): Data = supervisorScope {
    val required = async { loadRequired() }
    val optional = async { 
        try {
            loadOptional()
        } catch (e: Exception) {
            null  // Не повлияет на required
        }
    }
    
    Data(required.await(), optional.await())
}
```

## 4. Structured Concurrency

### Иерархия и жизненный цикл
```kotlin
// Parent-child relationship
val parentJob = Job()
val scope = CoroutineScope(parentJob)

scope.launch {  // child 1
    launch {    // grandchild 1.1
        delay(1000)
    }
    launch {    // grandchild 1.2
        delay(2000)
    }
}

// Правила:
// 1. Parent ждет всех детей
// 2. Отмена parent отменяет детей
// 3. Ошибка ребенка отменяет parent (кроме SupervisorJob)
```

### Job vs SupervisorJob
```kotlin
// Job - ошибка распространяется вверх и вниз
val scope1 = CoroutineScope(Job())
scope1.launch { 
    throw Exception()  // Отменит весь scope
}

// SupervisorJob - ошибки не влияют на siblings
val scope2 = CoroutineScope(SupervisorJob())
scope2.launch { 
    throw Exception()  // Не повлияет на другие корутины в scope
}
scope2.launch { 
    // Продолжает работать
}

// SupervisorScope для конкретного блока
supervisorScope {
    launch { error() }  // упадет
    launch { work() }   // продолжит работать
}
```

### CoroutineScope и управление жизненным циклом
```kotlin
class UserService : CoroutineScope {
    // Собственный scope для сервиса
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
    
    fun loadUsers() {
        launch {  // Привязан к жизни сервиса
            // ...
        }
    }
    
    fun cleanup() {
        coroutineContext.cancel()  // Отменяет все корутины сервиса
    }
}

// Spring Boot интеграция
@Component
class DataProcessor(
    private val scope: CoroutineScope  // inject from config
) {
    fun process() {
        scope.launch {
            // Управляется Spring lifecycle
        }
    }
}
```

## 5. Отмена и таймауты

### Кооперативная отмена
```kotlin
// Корутина должна проверять isActive
launch {
    while (isActive) {  // Проверка отмены
        processChunk()
    }
}

// Или использовать suspend функции (они проверяют автоматически)
launch {
    repeat(1000) {
        delay(100)  // Точка отмены
        process()
    }
}

// ensureActive() для явной проверки
launch {
    for (item in hugeList) {
        ensureActive()  // Кинет CancellationException если отменено
        processItem(item)
    }
}

// yield() - проверка отмены + дает другим корутинам поработать
launch {
    while (true) {
        yield()
        cpuIntensiveWork()
    }
}
```

### NonCancellable для финализации
```kotlin
val job = launch {
    try {
        repeat(1000) { i ->
            println("Working $i")
            delay(500)
        }
    } finally {
        withContext(NonCancellable) {
            // Выполнится даже при отмене
            delay(100)
            println("Cleanup completed")
            saveState()
        }
    }
}

delay(1300)
job.cancelAndJoin()
```

### Таймауты
```kotlin
// withTimeout - кидает TimeoutCancellationException
try {
    withTimeout(1000) {
        repeat(100) {
            delay(100)
        }
    }
} catch (e: TimeoutCancellationException) {
    println("Timeout!")
}

// withTimeoutOrNull - возвращает null
val result = withTimeoutOrNull(1000) {
    slowOperation()
} ?: defaultValue

// Комбинирование таймаутов
withTimeout(5000) {  // Общий таймаут
    val a = async { 
        withTimeout(2000) { api1() }  // Индивидуальный
    }
    val b = async { 
        withTimeout(3000) { api2() }
    }
    a.await() + b.await()
}
```

## 6. Exception Handling

### Try-catch в корутинах
```kotlin
// launch - исключения выбрасываются сразу
scope.launch {
    try {
        riskyOperation()
    } catch (e: Exception) {
        // Обработка внутри корутины
    }
}

// async - исключение при await()
val deferred = scope.async {
    throw Exception("Failed")
}

try {
    deferred.await()  // Тут получим исключение
} catch (e: Exception) {
    // handle
}

// async с SupervisorJob требует await для получения ошибки
supervisorScope {
    val deferred = async { throw Exception() }
    // Исключение НЕ распространится пока не вызовем await
}
```

### CoroutineExceptionHandler
```kotlin
val handler = CoroutineExceptionHandler { context, exception ->
    println("Caught $exception in ${context[CoroutineName]}")
}

// Работает только для launch (не для async!)
val scope = CoroutineScope(Job() + handler)

scope.launch {
    throw Exception("Test")  // Поймается handler
}

scope.async {
    throw Exception("Test")  // НЕ поймается, нужен try-catch при await
}

// Глобальный handler для Spring Boot
@Component
class GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("Unhandled coroutine exception", exception)
    }
}
```

### Exception propagation
```kotlin
// 1. launch в обычном scope - пробрасывает наверх
coroutineScope {
    launch {
        throw Exception()  // Отменит весь scope
    }
}

// 2. launch в supervisorScope - изолирует
supervisorScope {
    launch {
        throw Exception()  // Не влияет на других
    }
    launch {
        // Продолжает работать
    }
}

// 3. async требует явной обработки
coroutineScope {
    val d1 = async { throw Exception() }
    val d2 = async { "OK" }
    
    try {
        d1.await()  // Обрабатываем тут
    } catch (e: Exception) { }
    
    d2.await()  // Получим результат
}
```

## 7. Flow - холодные асинхронные потоки

### Основы Flow
```kotlin
// Создание Flow
fun numbers(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        emit(i)  // Отправка значения
    }
}

// Cold stream - выполняется при collect
val flow = numbers()  // Ничего не происходит
flow.collect { println(it) }  // Запуск

// Builders
flowOf(1, 2, 3)
listOf(1, 2, 3).asFlow()
(1..3).asFlow()

// Генерация
flow {
    emit(1)
    emitAll(flowOf(2, 3))
}
```

### Операторы Flow
```kotlin
numbers()
    .map { it * 2 }
    .filter { it > 2 }
    .take(2)
    .onEach { println("Processing $it") }
    .catch { e -> emit(-1) }  // Error handling
    .onCompletion { println("Done") }
    .flowOn(Dispatchers.Default)  // Upstream context
    .collect { result ->  // Terminal operator
        println(result)
    }

// Transform - универсальный оператор
flow.transform { value ->
    emit(value)
    emit(value * 2)
}

// Combine flows
val flow1 = flowOf(1, 2, 3)
val flow2 = flowOf("A", "B", "C")

flow1.zip(flow2) { a, b -> "$a$b" }  // 1A, 2B, 3C
flow1.combine(flow2) { a, b -> "$a$b" }  // Все комбинации при изменении

// FlatMap варианты
flow.flatMapConcat { }  // Sequential
flow.flatMapMerge { }   // Parallel
flow.flatMapLatest { }  // Отменяет предыдущий при новом значении
```

### StateFlow и SharedFlow
```kotlin
// StateFlow - hot, всегда имеет значение
class ViewModel {
    private val _state = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()
    
    fun updateState(new: State) {
        _state.value = new  // Thread-safe
    }
}

// SharedFlow - hot, настраиваемый replay
class EventBus {
    private val _events = MutableSharedFlow<Event>(
        replay = 1,  // Кол-во значений для новых подписчиков
        extraBufferCapacity = 10,  // Доп буфер
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()
    
    suspend fun emit(event: Event) {
        _events.emit(event)
    }
}

// Разница:
// StateFlow - состояние (текущее значение)
// SharedFlow - события (поток значений)
```

### Flow в Spring Boot
```kotlin
@RestController
class StreamController {
    // Server-Sent Events
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): Flow<Data> = flow {
        repeat(10) {
            emit(Data(it))
            delay(1000)
        }
    }
    
    // Reactive repository
    fun findUsersReactive(): Flow<User> = flow {
        userRepository.findAll().forEach {
            emit(it)
            delay(100)  // Backpressure
        }
    }.flowOn(Dispatchers.IO)
}
```

## 8. Channels - горячая коммуникация

### Основы Channel
```kotlin
// Channel = hot pipe между корутинами
val channel = Channel<Int>()

launch {
    for (x in 1..5) {
        channel.send(x)  // Suspend если буфер полон
    }
    channel.close()
}

launch {
    for (y in channel) {  // Получение до close()
        println(y)
    }
}

// Типы буферов
Channel<Int>()  // Rendezvous - нет буфера
Channel<Int>(Channel.CONFLATED)  // Последнее значение
Channel<Int>(Channel.UNLIMITED)  // Неограниченный
Channel<Int>(10)  // Фиксированный размер
Channel<Int>(Channel.BUFFERED)  // По умолчанию 64
```

### Паттерны с Channel
```kotlin
// Fan-out - несколько consumers
val channel = Channel<Int>()
repeat(3) { id ->
    launch {
        for (msg in channel) {
            println("Worker $id: $msg")
        }
    }
}

// Fan-in - несколько producers
val channel = Channel<Int>()
repeat(3) { id ->
    launch {
        channel.send(id)
    }
}

// Pipeline
fun CoroutineScope.pipeline(): ReceiveChannel<String> {
    val numbers = produce {
        for (x in 1..5) send(x)
    }
    
    val squares = produce {
        for (x in numbers) send(x * x)
    }
    
    return produce {
        for (x in squares) send("Result: $x")
    }
}
```

### Channel vs Flow
```kotlin
// Channel - hot, один consumer получает значение
val channel = Channel<Int>()
launch { channel.send(1) }
launch { println(channel.receive()) }  // 1
launch { println(channel.receive()) }  // Не получит

// Flow - cold, каждый collector получает все
val flow = flowOf(1, 2, 3)
launch { flow.collect { println("A: $it") } }  // A: 1, 2, 3
launch { flow.collect { println("B: $it") } }  // B: 1, 2, 3
```

## 9. Практические паттерны

### Retry с backoff
```kotlin
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 100,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            if (e is ClientException) throw e  // Не ретраим 4xx
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong()
    }
    return block()  // Последняя попытка
}

// Использование
val result = retryWithBackoff {
    apiCall()
}
```

### Circuit Breaker
```kotlin
class CircuitBreaker(
    private val threshold: Int = 5,
    private val timeout: Long = 60_000
) {
    private val failures = AtomicInteger(0)
    private var openedAt: Long = 0
    
    enum class State { CLOSED, OPEN, HALF_OPEN }
    
    suspend fun <T> call(block: suspend () -> T): T {
        when (state()) {
            State.OPEN -> throw CircuitOpenException()
            State.HALF_OPEN -> {
                return try {
                    block().also { reset() }
                } catch (e: Exception) {
                    open()
                    throw e
                }
            }
            State.CLOSED -> {
                return try {
                    block()
                } catch (e: Exception) {
                    if (failures.incrementAndGet() >= threshold) {
                        open()
                    }
                    throw e
                }
            }
        }
    }
}
```

### Rate Limiter
```kotlin
class RateLimiter(
    private val permits: Int,
    private val period: Duration
) {
    private val semaphore = Semaphore(permits)
    
    init {
        // Восстановление permits
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(period.toMillis() / permits)
                semaphore.tryAcquire()
                semaphore.release()
            }
        }
    }
    
    suspend fun <T> acquire(block: suspend () -> T): T {
        semaphore.acquire()
        return try {
            block()
        } finally {
            // Permit вернется через период
        }
    }
}
```

### Parallel map с ограничением
```kotlin
suspend fun <T, R> List<T>.mapConcurrently(
    concurrency: Int = 10,
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    val semaphore = Semaphore(concurrency)
    map { item ->
        async {
            semaphore.withPermit {
                transform(item)
            }
        }
    }.awaitAll()
}

// Использование
val results = urls.mapConcurrently(concurrency = 5) { url ->
    downloadUrl(url)
}
```

## 10. Spring Boot + Coroutines

### Controller с suspend
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    // Suspend function в endpoint
    @GetMapping("/{id}")
    suspend fun getUser(@PathVariable id: Long): UserDto {
        return userService.findById(id)
    }
    
    // Параллельная загрузка
    @GetMapping("/{id}/full")
    suspend fun getFullUser(@PathVariable id: Long): FullUserDto = coroutineScope {
        val user = async { userService.findById(id) }
        val posts = async { postService.findByUserId(id) }
        val comments = async { commentService.findByUserId(id) }
        
        FullUserDto(
            user.await(),
            posts.await(),
            comments.await()
        )
    }
    
    // Flow для streaming
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamUsers(): Flow<UserDto> = userService.streamAll()
}
```

### Service layer
```kotlin
@Service
class UserService(
    private val repository: UserRepository,
    private val cache: CacheService
) {
    // Обязательно withContext для блокирующих операций!
    suspend fun findById(id: Long): User = withContext(Dispatchers.IO) {
        cache.get(id) ?: repository.findById(id)?.also {
            cache.put(id, it)
        } ?: throw NotFoundException()
    }
    
    // Batch операции
    suspend fun saveAll(users: List<User>) = withContext(Dispatchers.IO) {
        users.chunked(100).map { batch ->
            async {
                repository.saveAll(batch)
            }
        }.awaitAll().flatten()
    }
    
    // Транзакции работают с suspend (Spring 6+)
    @Transactional
    suspend fun updateUser(id: Long, data: UpdateData): User {
        val user = findById(id)
        user.apply { 
            name = data.name
            updatedAt = Instant.now()
        }
        return repository.save(user)
    }
}
```

### WebClient с корутинами
```kotlin
@Service
class ApiClient(
    private val webClient: WebClient
) {
    suspend fun fetchData(id: String): ApiResponse {
        return webClient
            .get()
            .uri("/data/{id}", id)
            .retrieve()
            .awaitBody<ApiResponse>()  // Suspend extension
    }
    
    // С таймаутом и retry
    suspend fun fetchWithRetry(id: String): ApiResponse {
        return withTimeout(5000) {
            retry(times = 3, delay = 100) {
                webClient
                    .get()
                    .uri("/data/{id}", id)
                    .retrieve()
                    .onStatus({ it.is5xxServerError }) { 
                        Mono.error(RetryableException())
                    }
                    .awaitBody<ApiResponse>()
            }
        }
    }
}
```

## 11. Тестирование корутин

### runTest и TestScope
```kotlin
class UserServiceTest {
    @Test
    fun `should load user concurrently`() = runTest {
        // TestScope с virtual time
        val service = UserService()
        
        val result = service.loadUserData(1)
        
        assertEquals("John", result.name)
        // advanceUntilIdle() - выполнить все корутины
        // advanceTimeBy(1000) - продвинуть время
        // runCurrent() - выполнить текущие задачи
    }
    
    @Test
    fun `should timeout after 5 seconds`() = runTest {
        val job = launch {
            withTimeout(5000) {
                delay(10000)
            }
        }
        
        advanceTimeBy(4999)
        assertTrue(job.isActive)
        
        advanceTimeBy(1)
        assertTrue(job.isCancelled)
    }
}
```

### MockK с suspend
```kotlin
@Test
fun `should cache user`() = runTest {
    val repository = mockk<UserRepository>()
    val cache = mockk<CacheService>()
    val service = UserService(repository, cache)
    
    // Мок suspend функций
    coEvery { cache.get(1) } returns null
    coEvery { repository.findById(1) } returns User(1, "John")
    coEvery { cache.put(any(), any()) } just Runs
    
    val result = service.findById(1)
    
    assertEquals("John", result.name)
    coVerify(exactly = 1) { 
        cache.get(1)
        repository.findById(1)
        cache.put(1, any())
    }
}
```

### Тестирование Flow
```kotlin
@Test
fun `should emit values`() = runTest {
    val flow = flow {
        emit(1)
        delay(100)
        emit(2)
        delay(100)
        emit(3)
    }
    
    val results = mutableListOf<Int>()
    flow.collect { results.add(it) }
    
    assertEquals(listOf(1, 2, 3), results)
}

@Test
fun `should handle errors in flow`() = runTest {
    val flow = flow {
        emit(1)
        throw Exception("Error")
    }.catch { emit(-1) }
    
    val results = flow.toList()
    assertEquals(listOf(1, -1), results)
}

// Turbine library для удобного тестирования
@Test
fun `test with turbine`() = runTest {
    flow.test {
        assertEquals(1, awaitItem())
        assertEquals(2, awaitItem())
        awaitComplete()
    }
}
```

## 12. Частые ошибки и антипаттерны

### ❌ Неправильно
```kotlin
// 1. GlobalScope - unstructured concurrency
GlobalScope.launch {  // НЕТ!
    // Неуправляемая корутина
}

// 2. runBlocking в suspend функции
suspend fun bad() {
    runBlocking {  // Блокирует корутину!
        someWork()
    }
}

// 3. async без await
coroutineScope {
    async { work() }  // Результат потерян
}

// 4. Блокирующий код без withContext
suspend fun bad() {
    Thread.sleep(1000)  // Блокирует thread!
    jdbc.query()  // Блокирует без withContext(IO)
}

// 5. Утечка корутин
class Activity {
    fun onCreate() {
        lifecycleScope.launch {
            while (true) {  // Не проверяет отмену
                work()
                delay(1000)
            }
        }
    }
}

// 6. Неправильная обработка исключений
val result = async { 
    throw Exception()  // Исключение потеряно
}
// ... забыли await()
```

### ✅ Правильно
```kotlin
// 1. Structured concurrency
class Service(
    private val scope: CoroutineScope
) {
    fun process() = scope.launch {
        // Управляемая корутина
    }
}

// 2. coroutineScope вместо runBlocking
suspend fun good() = coroutineScope {
    someWork()
}

// 3. Всегда await или явно launch
coroutineScope {
    val result = async { work() }.await()
    // или
    launch { work() }  // fire and forget
}

// 4. withContext для блокирующего кода
suspend fun good() = withContext(Dispatchers.IO) {
    Thread.sleep(1000)  // OK в IO dispatcher
    jdbc.query()
}

// 5. Проверка отмены
lifecycleScope.launch {
    while (isActive) {  // Проверяем отмену
        work()
        delay(1000)
    }
}

// 6. Правильная обработка исключений
try {
    val result = async { work() }.await()
} catch (e: Exception) {
    handle(e)
}
```

## 13. Performance и оптимизация

### Dispatcher tuning
```kotlin
// Custom dispatcher для CPU-intensive
val cpuDispatcher = Dispatchers.Default.limitedParallelism(
    Runtime.getRuntime().availableProcessors() - 1
)

// Отдельный pool для блокирующих операций
val blockingDispatcher = Executors.newFixedThreadPool(50).asCoroutineDispatcher()

// Использование
withContext(cpuDispatcher) {
    heavyComputation()
}

withContext(blockingDispatcher) {
    legacyBlockingApi()
}
```

### Избегание лишних allocations
```kotlin
// Плохо - создает промежуточные списки
list.map { transform(it) }
    .filter { it.isValid() }
    .take(10)

// Хорошо - lazy evaluation через sequence
list.asSequence()
    .map { transform(it) }
    .filter { it.isValid() }
    .take(10)
    .toList()

// Или через Flow для suspend операций
list.asFlow()
    .map { suspendTransform(it) }
    .filter { it.isValid() }
    .take(10)
    .toList()
```

### Правильный parallelism
```kotlin
// Ограничение параллелизма
val semaphore = Semaphore(10)

urls.map { url ->
    async {
        semaphore.withPermit {
            download(url)
        }
    }
}.awaitAll()

// Или через chunked
urls.chunked(10).forEach { batch ->
    batch.map { async { download(it) } }.awaitAll()
}
```

## 14. Вопросы с собеседований

### Q: Разница между launch и async?
**A:** 
- `launch` возвращает `Job`, используется для fire-and-forget операций, исключения выбрасываются сразу
- `async` возвращает `Deferred<T>`, для получения результата, исключения при `await()`

В проекте АЦК-Финансы использовал `async` для параллельной загрузки данных из нескольких источников.

### Q: Что такое structured concurrency?
**A:** Принцип организации корутин в иерархию parent-child:
- Parent ждет завершения всех детей
- Отмена parent отменяет детей
- Ошибка ребенка отменяет parent (кроме SupervisorJob)

Гарантирует отсутствие утечек и предсказуемое поведение.

### Q: Когда использовать Flow vs Channel?
**A:**
- **Flow** - cold, declarative, для трансформации данных, каждый collector получает все значения
- **Channel** - hot, imperative, для коммуникации между корутинами, один consumer на значение

Пример: Flow для стриминга данных из БД, Channel для воркер пула.

### Q: Как работает отмена корутин?
**A:** Кооперативная отмена через проверку `isActive` или вызов suspend функций. При отмене выбрасывается `CancellationException`, который не крашит приложение. Для финализации используем `finally` блок с `NonCancellable` контекстом.

### Q: withContext vs launch для смены dispatcher?
**A:**
- `withContext` - синхронно меняет контекст, возвращает результат, для sequential кода
- `launch` - запускает новую корутину в указанном контексте, для concurrent выполнения

### Q: Как обрабатывать исключения в корутинах?
**A:**
1. `try-catch` внутри корутины
2. `CoroutineExceptionHandler` для необработанных (только launch)
3. `SupervisorJob` для изоляции
4. Для `async` - при `await()`

### Q: Что происходит при вызове suspend функции?
**A:** Компилятор преобразует в state machine с Continuation. При приостановке сохраняется состояние, thread освобождается. При возобновлении через `resumeWith` выполнение продолжается (возможно на другом thread).

### Q: Как интегрировать корутины в Spring Boot?
**A:**
- Controller методы могут быть `suspend`
- Для JPA/JDBC оборачивать в `withContext(Dispatchers.IO)`
- `@Transactional` работает с suspend (Spring 6+)
- WebClient имеет suspend extensions (`awaitBody`)

В проекте использовал для асинхронной обработки финансовых транзакций.

### Q: Разница между hot и cold streams?
**A:**
- **Cold** (Flow) - начинает работу при подписке, каждый subscriber получает свой поток
- **Hot** (SharedFlow/StateFlow) - работает независимо от подписчиков, делят один поток

### Q: Как тестировать корутины?
**A:** 
- `runTest` с virtual time для unit тестов
- `coEvery`/`coVerify` в MockK для моков
- `TestScope` с `advanceTimeBy` для проверки таймингов
- Testcontainers для интеграционных тестов

## 15. Production checklist

### Перед деплоем проверь:
- [ ] Все корутины в управляемых scope (не GlobalScope)
- [ ] Блокирующий код обернут в withContext(IO)
- [ ] Есть обработка исключений и отмены
- [ ] Настроены таймауты для внешних вызовов
- [ ] Используется SupervisorJob где нужна изоляция
- [ ] Нет runBlocking в production коде
- [ ] Dispatcher pool размеры адекватны нагрузке
- [ ] Логирование с correlation ID для трейсинга
- [ ] Метрики для мониторинга (активные корутины, время выполнения)
- [ ] Graceful shutdown через scope.cancel()

### Метрики для мониторинга:
```kotlin
@Component
class CoroutineMetrics(
    private val meterRegistry: MeterRegistry
) {
    fun recordCoroutine(name: String, block: suspend () -> Unit) {
        val timer = meterRegistry.timer("coroutine.duration", "name", name)
        timer.recordSuspend(block)
        
        meterRegistry.gauge("coroutine.active", getCurrentCoroutines())
    }
}
```