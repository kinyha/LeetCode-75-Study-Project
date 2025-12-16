# Spring Boot + JPA: Complete Interview Guide

## 1. N+1 Problem - детальный разбор

### Что такое N+1 и почему это критично

**N+1 проблема** - это ситуация, когда для загрузки N сущностей с их связями выполняется N+1 запрос:
- 1 запрос для получения основных сущностей
- N запросов для получения связанных данных каждой сущности

**Пример проблемы:**
```kotlin
// Entity
@Entity
class Order(
    @Id
    val id: Long,
    
    @ManyToOne(fetch = FetchType.LAZY)
    val customer: Customer,
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    val items: List<OrderItem> = emptyList()
)

// Проблемный код
val orders = orderRepository.findAll()  // 1 запрос
orders.forEach { order ->
    println(order.customer.name)  // N запросов для customers
    println(order.items.size)     // N запросов для items
}
// Итого: 1 + N + N = 2N + 1 запросов!
```

### Как обнаружить N+1

**1. SQL логирование:**
```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        generate_statistics: true  # Статистика запросов
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE  # Параметры запросов
```

**2. P6Spy для production:**
```kotlin
// build.gradle.kts
implementation("p6spy:p6spy:3.9.1")

// spy.properties
driverlist=org.postgresql.Driver
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(executionTime)|%(sql)
excludecategories=result,resultset
```

**3. Hibernate Statistics:**
```kotlin
@Component
class HibernateStatsInterceptor : HandlerInterceptor {
    @PersistenceContext
    lateinit var em: EntityManager
    
    override fun postHandle(...) {
        val stats = em.entityManagerFactory.unwrap(SessionFactory::class.java).statistics
        if (stats.queryExecutionCount > 10) {
            logger.warn("Too many queries: ${stats.queryExecutionCount}")
        }
        stats.clear()
    }
}
```

### Решение 1: JOIN FETCH

```kotlin
// JPQL с JOIN FETCH
@Query("""
    SELECT DISTINCT o 
    FROM Order o 
    LEFT JOIN FETCH o.customer 
    LEFT JOIN FETCH o.items
    WHERE o.status = :status
""")
fun findOrdersWithDetails(@Param("status") status: OrderStatus): List<Order>

// Criteria API
fun findWithFetch(spec: Specification<Order>): List<Order> {
    val cb = em.criteriaBuilder
    val query = cb.createQuery(Order::class.java)
    val root = query.from(Order::class.java)
    
    root.fetch<Order, Customer>("customer", JoinType.LEFT)
    root.fetch<Order, List<OrderItem>>("items", JoinType.LEFT)
    
    query.select(root).distinct(true)
    spec.toPredicate(root, query, cb)?.let { query.where(it) }
    
    return em.createQuery(query).resultList
}
```

**⚠️ Проблемы JOIN FETCH:**
- **Cartesian product** при multiple collections
- Нельзя использовать с пагинацией (Hibernate выполнит в памяти!)
- `DISTINCT` обязателен для коллекций

### Решение 2: EntityGraph

```kotlin
// Статический Named EntityGraph
@Entity
@NamedEntityGraph(
    name = "Order.full",
    attributeNodes = [
        NamedAttributeNode("customer"),
        NamedAttributeNode(value = "items", subgraph = "items")
    ],
    subgraphs = [
        NamedSubgraph(
            name = "items",
            attributeNodes = [NamedAttributeNode("product")]
        )
    ]
)
class Order { /*...*/ }

// Использование в репозитории
@EntityGraph(value = "Order.full", type = EntityGraph.EntityGraphType.LOAD)
override fun findById(id: Long): Optional<Order>

// Динамический EntityGraph
@Repository
class OrderRepository(private val em: EntityManager) {
    fun findWithDynamicGraph(id: Long, vararg paths: String): Order? {
        val graph = em.createEntityGraph(Order::class.java)
        paths.forEach { path ->
            if (path.contains(".")) {
                // Nested path: "items.product"
                val parts = path.split(".")
                var subgraph = graph.addSubgraph<Any>(parts[0])
                parts.drop(1).forEach { 
                    subgraph = subgraph.addSubgraph<Any>(it)
                }
            } else {
                graph.addAttributeNodes(path)
            }
        }
        
        return em.find(Order::class.java, id, mapOf(
            "jakarta.persistence.loadgraph" to graph
        ))
    }
}

// Использование
val order = repo.findWithDynamicGraph(1L, "customer", "items.product")
```

**EntityGraph типы:**
- `LOAD` (loadgraph) - загружает указанное + FetchType.EAGER
- `FETCH` (fetchgraph) - загружает ТОЛЬКО указанное

### Решение 3: Batch Fetching

```kotlin
// Глобальная настройка
spring.jpa.properties.hibernate.default_batch_fetch_size=100

// На уровне entity
@Entity
@BatchSize(size = 25)  // Загружать по 25 штук
class Customer { /*...*/ }

// На уровне коллекции
@Entity
class Order {
    @OneToMany
    @BatchSize(size = 50)
    val items: List<OrderItem> = emptyList()
}

// Как работает:
// Вместо: SELECT * FROM customer WHERE id = 1
//         SELECT * FROM customer WHERE id = 2
//         SELECT * FROM customer WHERE id = 3
// Будет:  SELECT * FROM customer WHERE id IN (1, 2, 3, ..., 25)
```

**Оптимальные размеры batch:**
- PostgreSQL: 100-500 (зависит от размера IN)
- MySQL: 100-200
- Oracle: 1000 (максимум для IN clause)

### Решение 4: DTO Projection

```kotlin
// Interface-based projection
interface OrderSummary {
    val id: Long
    val customerName: String  // Nested property
    val itemCount: Int
}

@Query("""
    SELECT o.id as id,
           c.name as customerName,
           SIZE(o.items) as itemCount
    FROM Order o
    JOIN o.customer c
""")
fun findOrderSummaries(): List<OrderSummary>

// Class-based projection
data class OrderDto(
    val id: Long,
    val customerName: String,
    val totalAmount: BigDecimal
)

@Query("""
    SELECT new com.example.OrderDto(
        o.id, 
        o.customer.name,
        SUM(i.price * i.quantity)
    )
    FROM Order o
    JOIN o.items i
    GROUP BY o.id, o.customer.name
""")
fun findOrderDtos(): List<OrderDto>

// Native query с projection
@Query(
    value = """
        SELECT o.id,
               c.name as customer_name,
               COUNT(i.id) as item_count,
               SUM(i.price * i.quantity) as total
        FROM orders o
        JOIN customers c ON o.customer_id = c.id
        LEFT JOIN order_items i ON o.id = i.order_id
        GROUP BY o.id, c.name
    """,
    nativeQuery = true
)
fun findOrderSummariesNative(): List<Map<String, Any>>
```

### Решение 5: Двухэтапная загрузка

```kotlin
@Service
class OrderService(
    private val orderRepo: OrderRepository,
    private val em: EntityManager
) {
    
    fun findOrdersWithPagination(
        pageable: Pageable,
        spec: Specification<Order>
    ): Page<Order> {
        // Этап 1: Получаем ID с пагинацией
        val idPage = orderRepo.findAll(spec, pageable)
            .map { it.id }
        
        if (idPage.isEmpty) {
            return Page.empty()
        }
        
        // Этап 2: Загружаем полные данные по ID
        val orders = em.createQuery("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN FETCH o.customer
            LEFT JOIN FETCH o.items
            WHERE o.id IN :ids
            ORDER BY o.id
        """, Order::class.java)
            .setParameter("ids", idPage.content)
            .resultList
        
        // Восстанавливаем порядок
        val orderMap = orders.associateBy { it.id }
        val sortedOrders = idPage.content.mapNotNull { orderMap[it] }
        
        return PageImpl(sortedOrders, pageable, idPage.totalElements)
    }
}
```

### Решение 6: Second Level Cache

```kotlin
// Настройка кеша
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.jakarta.persistence.sharedCache.mode=ENABLE_SELECTIVE

// Entity с кешем
@Entity
@Cacheable
@org.hibernate.annotations.Cache(
    usage = CacheConcurrencyStrategy.READ_WRITE,
    region = "orders"
)
class Order { /*...*/ }

// Кеш для коллекций
@Entity
class Customer {
    @OneToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val orders: List<Order> = emptyList()
}

// Query cache
@QueryHints(
    QueryHint(name = "org.hibernate.cacheable", value = "true"),
    QueryHint(name = "org.hibernate.cacheRegion", value = "query.orders")
)
@Query("SELECT o FROM Order o WHERE o.status = :status")
fun findByStatusCached(@Param("status") status: OrderStatus): List<Order>
```

## 2. JPA/Hibernate оптимизации

### Правильная работа с сессией

```kotlin
@Service
@Transactional(readOnly = true)  // По умолчанию read-only
class ProductService(
    private val repo: ProductRepository,
    private val em: EntityManager
) {
    
    // FlushMode.COMMIT для read-only операций
    fun searchProducts(criteria: SearchCriteria): List<Product> {
        em.flush()  // Сброс изменений перед поиском
        em.clear()  // Очистка контекста для экономии памяти
        
        return repo.findByCriteria(criteria)
    }
    
    @Transactional(readOnly = false)
    fun updateProduct(id: Long, data: UpdateData): Product {
        val product = repo.findById(id).orElseThrow()
        
        // Dirty checking автоматически
        product.apply {
            name = data.name
            price = data.price
            updatedAt = Instant.now()
        }
        
        // em.flush() // Необязательно - будет при commit
        return product
    }
    
    // Batch операции
    @Transactional
    fun bulkUpdate(updates: List<ProductUpdate>) {
        updates.chunked(50).forEach { batch ->
            batch.forEach { update ->
                em.createQuery("""
                    UPDATE Product p 
                    SET p.price = :price, p.updatedAt = :now
                    WHERE p.id = :id
                """)
                .setParameter("id", update.id)
                .setParameter("price", update.price)
                .setParameter("now", Instant.now())
                .executeUpdate()
            }
            
            em.flush()
            em.clear()  // Очистка контекста каждые 50 записей
        }
    }
}
```

### ID генерация и производительность

```kotlin
// ❌ ПЛОХО: TABLE - дополнительные запросы
@Id
@GeneratedValue(strategy = GenerationType.TABLE)
val id: Long = 0

// ❌ ПЛОХО: IDENTITY - нет батчинга
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
val id: Long = 0

// ✅ ХОРОШО: SEQUENCE с правильным allocation
@Id
@GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "product_seq"
)
@SequenceGenerator(
    name = "product_seq",
    sequenceName = "product_sequence",
    allocationSize = 50  // Hibernate кеширует 50 значений
)
val id: Long = 0

// ✅ АЛЬТЕРНАТИВА: UUID для распределенных систем
@Id
@GeneratedValue(strategy = GenerationType.UUID)
val id: UUID = UUID.randomUUID()
```

### Правильные маппинги

```kotlin
@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_product_sku", columnList = "sku", unique = true),
        Index(name = "idx_product_status_created", columnList = "status,created_at")
    ]
)
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,
    
    // Immutable поля
    @Column(nullable = false, updatable = false, length = 50)
    val sku: String,
    
    // Оптимизация строк
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    // Enum как string для читаемости
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: ProductStatus,
    
    // Версионирование для optimistic locking
    @Version
    var version: Long = 0,
    
    // Audit поля
    @CreatedDate
    @Column(updatable = false)
    val createdAt: Instant = Instant.now(),
    
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
) {
    // Lazy loading для тяжелых полей
    @Basic(fetch = FetchType.LAZY)
    @Lob
    val image: ByteArray? = null
    
    // Правильная связь Many-to-One
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    lateinit var category: Category
    
    // Оптимальная One-to-Many
    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.PERSIST],
        orphanRemoval = true
    )
    @BatchSize(size = 20)
    private val _reviews: MutableList<Review> = mutableListOf()
    val reviews: List<Review> get() = _reviews
    
    fun addReview(review: Review) {
        _reviews.add(review)
        review.product = this
    }
    
    fun removeReview(review: Review) {
        _reviews.remove(review)
        review.product = null
    }
}
```

### Hibernate-specific оптимизации

```kotlin
// 1. Hibernate-specific аннотации
@Entity
@org.hibernate.annotations.Where(clause = "deleted = false")  // Soft delete
@org.hibernate.annotations.FilterDef(
    name = "tenantFilter",
    parameters = [ParamDef(name = "tenantId", type = Long::class)]
)
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
class TenantEntity {
    // Multi-tenancy support
}

// 2. Formula для вычисляемых полей
@Entity
class OrderEntity {
    @Formula("""
        (SELECT SUM(oi.quantity * oi.price) 
         FROM order_items oi 
         WHERE oi.order_id = id)
    """)
    val totalAmount: BigDecimal? = null
}

// 3. Immutable entities для справочников
@Entity
@Immutable
@Cacheable
class Country(
    @Id val code: String,
    val name: String
)

// 4. Natural ID для бизнес-ключей
@Entity
class User {
    @Id
    @GeneratedValue
    val id: Long = 0
    
    @NaturalId
    @Column(unique = true, nullable = false)
    val email: String
    
    @NaturalId
    @Column(unique = true, nullable = false)
    val username: String
}

// Поиск по Natural ID (кешируется)
val user = session.byNaturalId(User::class.java)
    .using("email", "test@test.com")
    .using("username", "testuser")
    .load()
```

## 3. Транзакции в Spring Boot

### Основы @Transactional

```kotlin
@Service
class OrderService {
    
    // 1. Базовая транзакция
    @Transactional
    fun createOrder(dto: CreateOrderDto): Order {
        // Всё в одной транзакции
        val order = orderRepo.save(dto.toEntity())
        inventoryService.reserve(dto.items)
        paymentService.charge(dto.payment)
        return order
    }
    
    // 2. Read-only оптимизация
    @Transactional(readOnly = true)
    fun findOrders(): List<Order> {
        // FlushMode.MANUAL, без dirty checking
        return orderRepo.findAll()
    }
    
    // 3. Таймаут транзакции
    @Transactional(timeout = 5)  // 5 секунд
    fun processLongOperation() {
        // ...
    }
    
    // 4. Rollback правила
    @Transactional(
        rollbackFor = [BusinessException::class],
        noRollbackFor = [ValidationException::class]
    )
    fun updateOrder(id: Long, data: UpdateData) {
        // Rollback при BusinessException
        // Commit при ValidationException
    }
}
```

### Propagation - распространение транзакций

```kotlin
@Service
class PaymentService {
    
    // REQUIRED (default) - использует существующую или создает новую
    @Transactional(propagation = Propagation.REQUIRED)
    fun processPayment(amount: BigDecimal) {
        // Joins existing transaction or creates new
    }
    
    // REQUIRES_NEW - всегда новая транзакция
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun auditLog(message: String) {
        // Независимая транзакция - запишется даже если основная откатится
        auditRepo.save(AuditEntry(message))
    }
    
    // MANDATORY - требует существующую
    @Transactional(propagation = Propagation.MANDATORY)
    fun updateBalance(delta: BigDecimal) {
        // Кинет исключение если нет активной транзакции
    }
    
    // SUPPORTS - выполняется в транзакции если есть
    @Transactional(propagation = Propagation.SUPPORTS)
    fun checkStatus(): Status {
        // С транзакцией или без
    }
    
    // NOT_SUPPORTED - приостанавливает текущую
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun sendEmail(to: String) {
        // Выполнится вне транзакции
    }
    
    // NEVER - кинет исключение если есть транзакция
    @Transactional(propagation = Propagation.NEVER)
    fun validateExternal() {
        // Только вне транзакций
    }
    
    // NESTED - вложенная с savepoint
    @Transactional(propagation = Propagation.NESTED)
    fun tryRiskyOperation() {
        // Может откатиться независимо
    }
}
```

### Isolation - уровни изоляции

```kotlin
@Service
class AccountService {
    
    // READ_UNCOMMITTED - видит незакоммиченные изменения
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    fun dirtyRead(): List<Account> {
        // Минимальная изоляция, максимальная производительность
        // Риск: dirty reads
    }
    
    // READ_COMMITTED (PostgreSQL default)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun normalRead(): Account {
        // Видит только закоммиченные данные
        // Риск: non-repeatable reads, phantom reads
    }
    
    // REPEATABLE_READ
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun consistentRead(id: Long): Account {
        val first = accountRepo.findById(id)
        // ... другие операции
        val second = accountRepo.findById(id)
        // first == second гарантировано
        // Риск: phantom reads
    }
    
    // SERIALIZABLE - максимальная изоляция
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun criticalOperation() {
        // Как если бы транзакции выполнялись последовательно
        // Риск: deadlocks, низкая производительность
    }
}
```

### Проблемы и решения с транзакциями

```kotlin
@Service
class TransactionPitfalls {
    
    // ❌ ПРОБЛЕМА: Self-invocation
    fun publicMethod() {
        internalTransactional()  // @Transactional НЕ работает!
    }
    
    @Transactional
    fun internalTransactional() { }
    
    // ✅ РЕШЕНИЕ 1: Выделить в отдельный сервис
    @Service
    class SeparateService {
        @Transactional
        fun transactionalMethod() { }
    }
    
    // ✅ РЕШЕНИЕ 2: Self-injection
    @Service
    class SelfInjection {
        @Autowired
        private lateinit var self: SelfInjection
        
        fun publicMethod() {
            self.transactionalMethod()  // Работает!
        }
        
        @Transactional
        fun transactionalMethod() { }
    }
    
    // ✅ РЕШЕНИЕ 3: TransactionTemplate
    @Service
    class ProgrammaticTransaction(
        private val transactionTemplate: TransactionTemplate
    ) {
        fun executeInTransaction() {
            transactionTemplate.execute { status ->
                // Код в транзакции
                if (shouldRollback) {
                    status.setRollbackOnly()
                }
            }
        }
    }
    
    // ❌ ПРОБЛЕМА: Lazy loading после транзакции
    @Transactional
    fun getOrder(id: Long): Order {
        return orderRepo.findById(id).orElseThrow()
    }
    
    fun problem() {
        val order = getOrder(1)
        // Транзакция закрыта!
        order.items.size  // LazyInitializationException
    }
    
    // ✅ РЕШЕНИЕ: Open Session in View или явная загрузка
    @Transactional
    fun getOrderWithItems(id: Long): Order {
        return orderRepo.findByIdWithItems(id)  // JOIN FETCH
    }
}
```

## 4. REST API Best Practices

### RESTful дизайн

```kotlin
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management API")
class ProductController(
    private val productService: ProductService
) {
    
    // GET - получение ресурсов
    @GetMapping
    @Operation(summary = "Get all products")
    fun getAllProducts(
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(defaultValue = "id,asc") sort: Array<String>
    ): Page<ProductDto> {
        val pageable = PageRequest.of(page, size, Sort.by(*sort))
        return productService.findAll(category, pageable)
    }
    
    // GET by ID - единичный ресурс
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProduct(
        @PathVariable @Positive id: Long
    ): ProductDto {
        return productService.findById(id)
    }
    
    // POST - создание
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new product")
    fun createProduct(
        @Valid @RequestBody dto: CreateProductDto,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: String?
    ): ResponseEntity<ProductDto> {
        val created = productService.create(dto, idempotencyKey)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        
        return ResponseEntity
            .created(location)
            .header("X-Resource-Id", created.id.toString())
            .body(created)
    }
    
    // PUT - полное обновление
    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody dto: UpdateProductDto,
        @RequestHeader(value = "If-Match", required = false) etag: String?
    ): ProductDto {
        return productService.update(id, dto, etag)
    }
    
    // PATCH - частичное обновление
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update product")
    fun patchProduct(
        @PathVariable id: Long,
        @RequestBody updates: Map<String, Any?>
    ): ProductDto {
        return productService.patch(id, updates)
    }
    
    // DELETE - удаление
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product")
    fun deleteProduct(
        @PathVariable id: Long
    ) {
        productService.delete(id)
    }
    
    // Bulk операции
    @PostMapping("/bulk")
    @Operation(summary = "Bulk create products")
    fun bulkCreate(
        @Valid @RequestBody @Size(min = 1, max = 100) dtos: List<CreateProductDto>
    ): BulkOperationResult {
        return productService.bulkCreate(dtos)
    }
    
    // Действия над ресурсом
    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish product")
    fun publishProduct(
        @PathVariable id: Long
    ): ProductDto {
        return productService.publish(id)
    }
    
    // Подресурсы
    @GetMapping("/{id}/reviews")
    fun getProductReviews(
        @PathVariable id: Long,
        pageable: Pageable
    ): Page<ReviewDto> {
        return reviewService.findByProductId(id, pageable)
    }
}
```

### Валидация и DTO

```kotlin
// Группы валидации
interface OnCreate
interface OnUpdate

// Base DTO с общими полями
sealed class ProductDtoBase {
    abstract val name: String
    abstract val description: String?
}

// Create DTO
data class CreateProductDto(
    @field:NotBlank(groups = [OnCreate::class])
    @field:Size(min = 3, max = 100)
    override val name: String,
    
    @field:Size(max = 1000)
    override val description: String? = null,
    
    @field:NotNull(groups = [OnCreate::class])
    @field:Positive
    val price: BigDecimal,
    
    @field:NotNull
    @field:Min(0)
    val stock: Int,
    
    @field:Valid
    val attributes: List<AttributeDto> = emptyList()
) : ProductDtoBase()

// Update DTO
data class UpdateProductDto(
    @field:NotBlank(groups = [OnUpdate::class])
    override val name: String,
    
    override val description: String?,
    
    @field:PositiveOrZero
    val price: BigDecimal?,
    
    @field:Min(0)
    val stock: Int?
) : ProductDtoBase()

// Response DTO
data class ProductDto(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int,
    val status: ProductStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val _links: Map<String, Link> = emptyMap()  // HATEOAS
)

// Custom validator
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueSkuValidator::class])
annotation class UniqueSku(
    val message: String = "SKU must be unique",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Component
class UniqueSkuValidator(
    private val productRepo: ProductRepository
) : ConstraintValidator<UniqueSku, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return !productRepo.existsBySku(value)
    }
}
```

### Обработка ошибок (RFC 7807)

```kotlin
// Error response DTO
data class ProblemDetail(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String,
    val timestamp: Instant = Instant.now(),
    val errors: List<FieldError> = emptyList()
)

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any?
)

// Global exception handler
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(
                field = it.field,
                message = it.defaultMessage ?: "Invalid value",
                rejectedValue = it.rejectedValue
            )
        }
        
        val problem = ProblemDetail(
            type = "https://api.example.com/errors/validation",
            title = "Validation Failed",
            status = 400,
            detail = "The request contains invalid fields",
            instance = request.requestURI,
            errors = errors
        )
        
        return ResponseEntity
            .badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }
    
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail(
            type = "https://api.example.com/errors/not-found",
            title = "Resource Not Found",
            status = 404,
            detail = ex.message ?: "The requested resource was not found",
            instance = request.requestURI
        )
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }
    
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ProblemDetail> {
        val message = when {
            ex.message?.contains("duplicate key") == true -> "Resource already exists"
            ex.message?.contains("foreign key") == true -> "Referenced resource not found"
            else -> "Data integrity violation"
        }
        
        val problem = ProblemDetail(
            type = "https://api.example.com/errors/conflict",
            title = "Conflict",
            status = 409,
            detail = message,
            instance = request.requestURI
        )
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }
    
    @ExceptionHandler(OptimisticLockException::class)
    fun handleOptimisticLock(ex: OptimisticLockException): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail(
            type = "https://api.example.com/errors/concurrent-modification",
            title = "Concurrent Modification",
            status = 409,
            detail = "The resource was modified by another process",
            instance = request.requestURI
        )
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .header("Retry-After", "1")
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }
}
```

### Идемпотентность POST

```kotlin
// Idempotency entity
@Entity
@Table(
    name = "idempotency_keys",
    indexes = [Index(name = "idx_idempotency_created", columnList = "created_at")]
)
class IdempotencyKey(
    @Id
    val key: String,
    
    @Column(nullable = false)
    val requestHash: String,
    
    @Column(name = "resource_id")
    val resourceId: String? = null,
    
    @Enumerated(EnumType.STRING)
    var status: IdempotencyStatus = IdempotencyStatus.PROCESSING,
    
    @Column(columnDefinition = "TEXT")
    var response: String? = null,
    
    val createdAt: Instant = Instant.now()
)

enum class IdempotencyStatus {
    PROCESSING, COMPLETED, FAILED
}

// Service implementation
@Service
class IdempotentService(
    private val idempotencyRepo: IdempotencyKeyRepository,
    private val objectMapper: ObjectMapper
) {
    
    fun <T> executeIdempotent(
        key: String?,
        request: Any,
        operation: () -> T
    ): T {
        if (key == null) {
            return operation()
        }
        
        val requestHash = hash(request)
        
        // Try to insert key
        val idempotencyKey = IdempotencyKey(
            key = key,
            requestHash = requestHash
        )
        
        return try {
            idempotencyRepo.save(idempotencyKey)
            
            // Execute operation
            val result = operation()
            
            // Save result
            idempotencyKey.apply {
                status = IdempotencyStatus.COMPLETED
                response = objectMapper.writeValueAsString(result)
                resourceId = extractResourceId(result)
            }
            idempotencyRepo.save(idempotencyKey)
            
            result
        } catch (e: DataIntegrityViolationException) {
            // Key already exists
            val existing = idempotencyRepo.findById(key).orElseThrow()
            
            // Check if same request
            if (existing.requestHash != requestHash) {
                throw ConflictException("Different request with same idempotency key")
            }
            
            // Wait if still processing
            if (existing.status == IdempotencyStatus.PROCESSING) {
                Thread.sleep(100)
                return executeIdempotent(key, request, operation)
            }
            
            // Return cached result
            objectMapper.readValue(existing.response, object : TypeReference<T>() {})
        }
    }
    
    private fun hash(obj: Any): String {
        val json = objectMapper.writeValueAsString(obj)
        return DigestUtils.sha256Hex(json)
    }
    
    // Scheduled cleanup
    @Scheduled(cron = "0 0 2 * * *")  // Daily at 2 AM
    fun cleanupOldKeys() {
        val cutoff = Instant.now().minus(7, ChronoUnit.DAYS)
        idempotencyRepo.deleteByCreatedAtBefore(cutoff)
    }
}
```

### Версионирование API

```kotlin
// 1. Path versioning (рекомендуется)
@RestController
class ProductControllerV1 {
    @GetMapping("/api/v1/products")
    fun getProductsV1(): List<ProductDtoV1> { }
}

@RestController
class ProductControllerV2 {
    @GetMapping("/api/v2/products")
    fun getProductsV2(): List<ProductDtoV2> { }
}

// 2. Header versioning
@RestController
@RequestMapping("/api/products")
class ProductController {
    
    @GetMapping(headers = ["X-API-Version=1"])
    fun getProductsV1(): List<ProductDtoV1> { }
    
    @GetMapping(headers = ["X-API-Version=2"])
    fun getProductsV2(): List<ProductDtoV2> { }
}

// 3. Content negotiation
@GetMapping(produces = ["application/vnd.company.v1+json"])
fun getProductsV1(): List<ProductDtoV1> { }

@GetMapping(produces = ["application/vnd.company.v2+json"])
fun getProductsV2(): List<ProductDtoV2> { }

// 4. Backward compatibility strategy
data class ProductDtoV2(
    val id: Long,
    val name: String,
    val category: CategoryDto,  // New in v2
    
    @Deprecated("Use category.name instead")
    val categoryName: String? = null  // Kept for v1 compatibility
)
```

## 5. Performance оптимизации

### Query оптимизация

```kotlin
@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    
    // 1. Index hints (PostgreSQL)
    @Query(
        value = """
            SELECT /*+ INDEX(p idx_product_status_created) */ *
            FROM products p
            WHERE p.status = :status
            ORDER BY p.created_at DESC
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findRecentByStatus(status: String, limit: Int): List<Product>
    
    // 2. Covering index usage
    @Query("""
        SELECT new com.example.ProductSummary(p.id, p.name, p.price)
        FROM Product p
        WHERE p.categoryId = :categoryId
        ORDER BY p.price
    """)
    fun findSummariesByCategory(categoryId: Long): List<ProductSummary>
    
    // 3. Window functions
    @Query(
        value = """
            WITH ranked_products AS (
                SELECT *,
                       ROW_NUMBER() OVER (
                           PARTITION BY category_id 
                           ORDER BY sales DESC
                       ) as rank
                FROM products
            )
            SELECT * FROM ranked_products
            WHERE rank <= 3
        """,
        nativeQuery = true
    )
    fun findTop3PerCategory(): List<Product>
    
    // 4. Bulk operations
    @Modifying
    @Query("""
        UPDATE Product p
        SET p.status = :newStatus, p.updatedAt = CURRENT_TIMESTAMP
        WHERE p.status = :oldStatus
        AND p.createdAt < :before
    """)
    fun bulkUpdateStatus(
        oldStatus: ProductStatus,
        newStatus: ProductStatus,
        before: Instant
    ): Int
}
```

### Connection pool настройка

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      leak-detection-threshold: 60000
      
  jpa:
    properties:
      hibernate:
        # Batch processing
        jdbc:
          batch_size: 25
          batch_versioned_data: true
        order_inserts: true
        order_updates: true
        
        # Statement caching
        statement_cache:
          size: 100
          
        # Query plan cache
        query:
          plan_cache_max_size: 2048
          plan_parameter_metadata_max_size: 128
```

### Monitoring и метрики

```kotlin
@Component
class JpaMetrics(
    private val meterRegistry: MeterRegistry,
    @PersistenceContext private val em: EntityManager
) {
    
    @EventListener
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        val sessionFactory = em.entityManagerFactory.unwrap(SessionFactory::class.java)
        val stats = sessionFactory.statistics
        
        stats.isStatisticsEnabled = true
        
        // Query metrics
        Gauge.builder("hibernate.query.execution.count") { stats.queryExecutionCount }
            .register(meterRegistry)
            
        Gauge.builder("hibernate.query.execution.max.time") { stats.queryExecutionMaxTime }
            .register(meterRegistry)
        
        // Cache metrics
        Gauge.builder("hibernate.cache.hit.count") { stats.secondLevelCacheHitCount }
            .register(meterRegistry)
            
        Gauge.builder("hibernate.cache.miss.count") { stats.secondLevelCacheMissCount }
            .register(meterRegistry)
        
        // Connection metrics
        val hikariDataSource = em.entityManagerFactory
            .unwrap(SessionFactoryImpl::class.java)
            .connectionProvider
            .unwrap(HikariDataSource::class.java)
            
        hikariDataSource.metricRegistry = meterRegistry
    }
    
    @Component
    class SlowQueryLogger : EmptyInterceptor() {
        override fun onPrepareStatement(sql: String): String {
            val start = System.currentTimeMillis()
            return super.onPrepareStatement(sql).also {
                val duration = System.currentTimeMillis() - start
                if (duration > 1000) {
                    logger.warn("Slow query (${duration}ms): $sql")
                }
            }
        }
    }
}
```

## 6. Топ вопросы на собеседовании

### Q: Как решали N+1 проблему в реальном проекте?
**A:** В проекте SCS была проблема при загрузке заказов с рецептами. При отображении списка из 20 заказов выполнялось 41 запрос (1 для заказов + 20 для клиентов + 20 для рецептов). 

Решение:
1. Добавил `@BatchSize(size = 100)` на коллекции
2. Для single entity использовал `@EntityGraph`
3. Для списков с пагинацией - двухэтапная загрузка

Результат: 3 запроса вместо 41, время ответа уменьшилось с 800ms до 150ms.

### Q: Разница между EAGER и LAZY loading?
**A:**
- **LAZY** - загрузка при первом обращении, требует открытой сессии
- **EAGER** - загрузка сразу с основной сущностью

Всегда использую LAZY по умолчанию, EAGER только для критичных данных размером < 1KB. В АЦК-Финансы это позволило уменьшить потребление памяти на 40%.

### Q: Уровни изоляции транзакций и какой используете?
**A:** 
- **READ_COMMITTED** (default в PostgreSQL) - для большинства операций
- **REPEATABLE_READ** - для финансовых операций в АЦК-Финансы
- **SERIALIZABLE** - только для критичных операций (инвентарь, баланс)

Пример: при обработке платежей использовал REPEATABLE_READ + optimistic locking через @Version.

### Q: Как обеспечить идемпотентность POST запросов?
**A:** Реализовал через Idempotency-Key header:
1. Клиент отправляет уникальный ключ
2. Сохраняем в БД с hash запроса
3. При повторе проверяем hash и возвращаем кешированный результат
4. TTL 24 часа с автоочисткой

Использовал в SCS для предотвращения дублирования заказов при network retry.

### Q: Optimistic vs Pessimistic locking?
**A:**
- **Optimistic** (@Version) - для высокой конкурентности, редкие конфликты
- **Pessimistic** (SELECT FOR UPDATE) - для критичных секций

В проекте использовал optimistic для корзины покупок, pessimistic для списания со склада.

### Q: Как работает @Transactional под капотом?
**A:** Spring создает proxy (CGLIB или JDK Dynamic Proxy):
1. Перехватывает вызов метода
2. Начинает транзакцию
3. Выполняет метод
4. Commit при успехе, rollback при исключении

Важно: не работает при self-invocation (вызов внутри того же класса).

### Q: Entity lifecycle в Hibernate?
**A:**
1. **Transient** - новый объект, не связан с сессией
2. **Persistent** - управляется Hibernate, есть ID
3. **Detached** - был persistent, сессия закрыта
4. **Removed** - помечен для удаления

### Q: Как профилировать медленные запросы?
**A:**
1. `spring.jpa.show-sql=true` + format_sql для dev
2. P6Spy для production логирования
3. `EXPLAIN ANALYZE` в PostgreSQL
4. Hibernate Statistics для метрик
5. Slow query log в БД

В ITSupportMe нашел запрос 5 секунд через профилирование, добавил составной индекс - стало 50ms.

### Q: REST статус коды - когда какой?
**A:**
- **200 OK** - успешный GET/PUT/PATCH
- **201 Created** - успешный POST с Location header
- **204 No Content** - успешный DELETE
- **400 Bad Request** - валидация
- **401 Unauthorized** - не аутентифицирован
- **403 Forbidden** - нет прав
- **404 Not Found** - ресурс не найден
- **409 Conflict** - конфликт состояния
- **422 Unprocessable Entity** - бизнес-валидация

### Q: Как версионировать API?
**A:** Использую path versioning `/api/v1/`:
- Просто для клиентов
- Явно видно в URL
- Легко роутить на разные контроллеры
- Поддержка одновременно 2-3 версий
- Deprecation через headers

## Чек-лист готовности

### JPA/Hibernate
- [ ] Могу объяснить и решить N+1 разными способами
- [ ] Знаю когда использовать EAGER/LAZY
- [ ] Понимаю Entity lifecycle
- [ ] Умею настраивать batch processing
- [ ] Знаю про first/second level cache

### Транзакции
- [ ] Знаю все Propagation уровни
- [ ] Понимаю Isolation levels и их trade-offs
- [ ] Могу объяснить proxy mechanism
- [ ] Знаю про self-invocation problem

### REST API
- [ ] Правильно использую HTTP методы и статусы
- [ ] Умею обеспечить идемпотентность
- [ ] Знаю RFC 7807 для ошибок
- [ ] Могу реализовать версионирование
- [ ] Понимаю HATEOAS (хотя бы базово)

### Оптимизация
- [ ] Умею профилировать запросы
- [ ] Знаю как настроить connection pool
- [ ] Понимаю индексы и EXPLAIN
- [ ] Могу настроить кеширование