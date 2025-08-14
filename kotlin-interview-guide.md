# План подготовки к Middle Kotlin Developer

## Неделя 1: Kotlin Core + Spring Boot

### День 1-2: Kotlin Корутины
**Теория (3-4 часа)**
- Корутины vs Threads
- CoroutineScope, CoroutineContext, Dispatcher
- suspend функции и Continuation
- Structured Concurrency
- Flow API

**Практика**
```kotlin
// Реализовать:
// 1. Параллельную загрузку данных из 3 API
// 2. Retry механизм с exponential backoff
// 3. Timeout + cancellation handling
// 4. Flow с debounce для поиска
```

**Вопросы собеседования**
- Разница между launch/async
- Что такое SupervisorJob и когда использовать
- Cold vs Hot Flow
- Как избежать утечек корутин
- runBlocking vs coroutineScope

### День 3-4: Spring Boot + Kotlin
**Теория (2 часа)**
- @ConfigurationProperties с data class
- @Bean vs @Component в Kotlin
- Null-safety в Spring
- WebFlux с корутинами

**Практика**
```kotlin
// Создать REST сервис:
// - CRUD для User entity
// - Pagination + sorting
// - Global exception handler
// - Custom validators
// - Swagger документация
```

**Код для изучения**
```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping
    suspend fun getUsers(
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<UserDto> = userService.findAll(pageable)
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createUser(
        @Valid @RequestBody dto: CreateUserDto
    ): UserDto = userService.create(dto)
}
```

### День 5: Jackson + Serialization
**Теория (2 часа)**
- @JsonProperty, @JsonIgnore, @JsonInclude
- Custom serializers/deserializers
- Kotlinx.serialization vs Jackson
- Polymorphic serialization

**Практика**
```kotlin
// Реализовать:
// 1. Custom LocalDateTime serializer
// 2. Polymorphic DTO (sealed class)
// 3. JSON patch support
// 4. CSV/XML конвертеры
```

## Неделя 2: БД + Тестирование

### День 6-7: PostgreSQL + JPA
**Теория (3 часа)**
- N+1 проблема: @EntityGraph, JOIN FETCH
- Индексы: B-tree, Hash, GiST, GIN
- EXPLAIN ANALYZE
- Window functions, CTE
- Optimistic/Pessimistic locking

**SQL задачи**
```sql
-- 1. Найти топ-3 продукта по продажам в каждой категории
-- 2. Рекурсивный запрос для дерева категорий
-- 3. Оптимизировать запрос с 5 JOIN
-- 4. Партиционирование таблицы по дате
```

**JPA практика**
```kotlin
@Entity
@Table(name = "users")
@NamedEntityGraph(
    name = "User.withOrders",
    attributeNodes = [NamedAttributeNode("orders")]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    var email: String,
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    @BatchSize(size = 10)
    val orders: MutableList<Order> = mutableListOf()
)
```

### День 8: MongoDB + Redis
**MongoDB (2 часа)**
- Aggregation pipeline
- Индексы: compound, text, geospatial
- Transactions
- Change Streams

**Redis (2 часа)**
```kotlin
@Service
class CacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    @Cacheable(value = ["users"], key = "#id")
    suspend fun getUser(id: Long): User
    
    @CacheEvict(value = ["users"], key = "#user.id")
    suspend fun updateUser(user: User)
    
    // Реализовать:
    // 1. Distributed lock
    // 2. Rate limiter
    // 3. Session storage
    // 4. Pub/Sub messaging
}
```

### День 9-10: Тестирование
**Unit тесты с MockK**
```kotlin
@Test
fun `should return user when exists`() = runTest {
    // given
    val userId = 1L
    val expected = User(id = userId, email = "test@test.com")
    coEvery { repository.findById(userId) } returns expected
    
    // when
    val result = service.getUser(userId)
    
    // then
    result shouldBe expected
    coVerify(exactly = 1) { repository.findById(userId) }
}
```

**Integration с Testcontainers**
```kotlin
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserIntegrationTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
            .withDatabaseName("testdb")
        
        @Container
        val redis = GenericContainer("redis:7")
            .withExposedPorts(6379)
    }
    
    @Test
    fun `should create user and cache`() {
        // Полный e2e тест
    }
}
```

## Неделя 3: REST API + DevOps

### День 11-12: REST API Design
**Проектирование API**
- RESTful conventions: PUT vs PATCH
- HATEOAS implementation
- Versioning strategies
- Rate limiting
- Pagination: offset vs cursor

**OpenAPI спецификация**
```yaml
openapi: 3.0.0
paths:
  /users:
    get:
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
      responses:
        200:
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserPage'
```

**Валидация**
```kotlin
data class CreateUserDto(
    @field:Email
    @field:NotBlank
    val email: String,
    
    @field:Size(min = 8, max = 100)
    @field:Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$")
    val password: String,
    
    @field:Valid
    val profile: ProfileDto
)

// Custom validator
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueEmailValidator::class])
annotation class UniqueEmail(
    val message: String = "Email already exists"
)
```

### День 13: Docker + CI/CD
**Dockerfile оптимизация**
```dockerfile
# Multi-stage build
FROM gradle:7-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 app && adduser -u 1001 -G app -D app
COPY --from=builder /app/build/libs/*.jar app.jar
USER app
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**GitLab CI Pipeline**
```yaml
stages:
  - test
  - build
  - deploy

test:
  stage: test
  script:
    - ./gradlew test
  artifacts:
    reports:
      junit: build/test-results/test/**/TEST-*.xml

build:
  stage: build
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
```

### День 14: Финальный проект
**Реализовать микросервис**
- REST API для интернет-магазина
- PostgreSQL + Redis cache
- Полное покрытие тестами (>80%)
- Docker + docker-compose
- CI/CD pipeline
- Swagger документация

**Требования:**
1. User service: регистрация, JWT auth
2. Product catalog: CRUD, поиск, фильтры
3. Order service: создание, статусы, история
4. Async notifications (корутины)
5. Rate limiting, circuit breaker

## Типовые вопросы на собеседовании

### Kotlin
1. **inline, reified, crossinline, noinline - когда использовать?**
   - inline: для HOF, избегаем создания объектов
   - reified: доступ к типу в runtime
   - crossinline: запрет non-local return
   - noinline: отключение inline для параметра

2. **Делегаты: lazy, observable, vetoable**
   ```kotlin
   val heavy by lazy { computeHeavyObject() }
   var name by Delegates.observable("") { _, old, new ->
       println("$old -> $new")
   }
   ```

3. **Sealed class vs enum**
   - Sealed: иерархия с разными типами данных
   - Enum: фиксированный набор констант

### Spring Boot
1. **@Transactional propagation levels**
   - REQUIRED (default), REQUIRES_NEW, NESTED, SUPPORTS, NOT_SUPPORTED

2. **Circular dependency решения**
   - @Lazy injection
   - Setter injection
   - @PostConstruct
   - Рефакторинг архитектуры

3. **Spring Boot Actuator endpoints**
   - /health, /metrics, /info
   - Custom health indicators
   - Micrometer metrics

### PostgreSQL
1. **ACID и уровни изоляции**
   - Read Uncommitted, Read Committed, Repeatable Read, Serializable
   - Phantom reads, non-repeatable reads, dirty reads

2. **Оптимизация запросов**
   - EXPLAIN ANALYZE
   - Index types и когда использовать
   - Vacuum и autovacuum
   - Партиционирование

### Архитектура
1. **CAP теорема**
   - Consistency, Availability, Partition tolerance
   - PostgreSQL: CP, MongoDB: CP/AP configurable

2. **Паттерны resilience**
   - Circuit Breaker (Resilience4j)
   - Retry with backoff
   - Bulkhead isolation
   - Rate limiting

## Ресурсы для подготовки

### Книги (приоритетные главы)
1. "Kotlin in Action" - главы 5-8 (lambdas, type system, coroutines)
2. "Spring Boot in Action" - главы 3-4, 7 (REST, data, testing)
3. "PostgreSQL Up & Running" - главы 4-6 (queries, functions, performance)

### Online курсы
1. [Kotlin Coroutines by JetBrains](https://play.kotlinlang.org/hands-on/Introduction%20to%20Coroutines%20and%20Channels)
2. [Spring Boot Testing](https://www.baeldung.com/spring-boot-testing)
3. [PostgreSQL Performance](https://use-the-index-luke.com)

### GitHub репозитории для изучения
```bash
# Клонировать и изучить:
git clone https://github.com/Kotlin/kotlinx.coroutines
git clone https://github.com/spring-projects/spring-boot
git clone https://github.com/testcontainers/testcontainers-java
```

## Чек-лист готовности

### Must have (80% вопросов)
- [ ] Корутины: написал 10+ примеров с разными scope
- [ ] Spring Boot: REST API с полным CRUD
- [ ] PostgreSQL: оптимизировал 5+ сложных запросов
- [ ] Тесты: покрытие >80% для своего проекта
- [ ] Docker: собрал multi-stage образ <100MB

### Nice to have (20% вопросов)
- [ ] Ktor: базовый REST сервис
- [ ] MongoDB: aggregation pipeline
- [ ] Kubernetes: базовый deployment
- [ ] GraphQL: простой resolver
- [ ] gRPC: client-server пример

## Домашние задания

### Задание 1: Async Order Processing
```kotlin
// Реализовать сервис обработки заказов:
// 1. Проверка наличия товара (БД)
// 2. Расчет стоимости доставки (внешний API)
// 3. Проверка платежа (внешний API)
// 4. Создание заказа (БД)
// 5. Отправка уведомления (async)
// Все шаги параллельно где возможно
// Timeout 5 секунд на весь процесс
```

### Задание 2: Cache-Aside Pattern
```kotlin
// Реализовать:
// 1. Загрузка из кеша
// 2. При miss - загрузка из БД
// 3. Сохранение в кеш с TTL
// 4. Инвалидация при update
// 5. Защита от cache stampede
```

### Задание 3: Rate Limiter
```kotlin
// Реализовать на Redis:
// 1. Sliding window algorithm
// 2. 100 requests per minute per user
// 3. Возврат headers: X-RateLimit-Limit, X-RateLimit-Remaining
// 4. 429 Too Many Requests при превышении
```

## Примерное расписание дня подготовки

**Утро (2 часа)**
- Теория: читать документацию/книгу
- Делать конспект ключевых моментов

**День (3 часа)**
- Практика: писать код по теме дня
- Отладка и эксперименты

**Вечер (2 часа)**
- LeetCode: 1-2 задачи на Kotlin
- Повторение пройденного
- Подготовка вопросов

## Советы для собеседования

1. **Код на доске**: практикуйся писать без IDE
2. **Проговаривай решение**: думай вслух
3. **Задавай вопросы**: уточняй требования
4. **Признавай незнание**: "Не работал с X, но знаю похожий Y"
5. **Готовь вопросы**: про команду, процессы, технологии

## Red Flags в коде (избегать)

```kotlin
// Плохо
class Service {
    fun process() {
        Thread.sleep(1000) // блокировка в корутине
        runBlocking { ... } // в suspend функции
    }
}

// Плохо
@Transactional
suspend fun save() { } // не работает

// Плохо
list.forEach { 
    launch { processItem(it) } // утечка корутин
}
```

## Минимальный набор для прохождения

Если времени мало (3-5 дней), фокус на:
1. **Корутины**: async/await, Flow, exception handling
2. **Spring Boot REST**: controller, service, validation
3. **PostgreSQL**: JOIN, индексы, N+1 проблема
4. **Тесты**: MockK basics, 1 integration test
5. **Docker**: простой Dockerfile

Успех = 70% практики + 20% теории + 10% soft skills