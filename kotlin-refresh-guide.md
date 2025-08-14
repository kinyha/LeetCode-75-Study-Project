# Kotlin Refresh Guide (без корутин)

## 1. Базовый синтаксис и отличия от Java

### Переменные и типы
```kotlin
// Иммутабельные (предпочтительно)
val name: String = "John"  // явный тип
val age = 25               // type inference
// val age = null           // Error! Нужен nullable тип

// Мутабельные
var counter = 0
counter++ // OK

// Константы compile-time
const val MAX_COUNT = 100  // только примитивы и String

// Type aliases
typealias UserId = Long
typealias UserMap = Map<String, User>
```

### Функции
```kotlin
// Single expression
fun add(a: Int, b: Int) = a + b

// Block body
fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}

// Default параметры и named arguments
fun createUser(
    name: String,
    age: Int = 18,
    active: Boolean = true
): User {
    return User(name, age, active)
}

// Вызов
createUser("John")
createUser("Jane", active = false)  // named argument

// Extension functions
fun String.isEmail(): Boolean = contains("@")
"test@test.com".isEmail() // true

// Infix functions
infix fun Int.power(n: Int): Int = Math.pow(this.toDouble(), n.toDouble()).toInt()
2 power 3  // 8
```

### Условия и циклы
```kotlin
// if как expression
val result = if (x > 0) "positive" else "negative"

// when (заменяет switch)
val description = when (x) {
    0 -> "zero"
    1, 2, 3 -> "small"
    in 4..10 -> "medium"
    is Int -> "integer"
    else -> "unknown"
}

// when без аргумента
when {
    x < 0 -> println("negative")
    x == 0 -> println("zero")
    else -> println("positive")
}

// Ranges и циклы
for (i in 1..10) { }           // 1 to 10
for (i in 1 until 10) { }      // 1 to 9
for (i in 10 downTo 1) { }     // 10 to 1
for (i in 1..10 step 2) { }    // 1, 3, 5, 7, 9

// Destructuring в циклах
val map = mapOf("a" to 1, "b" to 2)
for ((key, value) in map) {
    println("$key = $value")
}
```

## 2. Null Safety

### Nullable типы
```kotlin
var nullable: String? = "test"
nullable = null  // OK

// Safe call
val length = nullable?.length  // null если nullable == null

// Elvis operator
val length = nullable?.length ?: 0  // default value

// Not-null assertion (избегать!)
val length = nullable!!.length  // кинет NPE если null

// Safe cast
val str: String? = obj as? String  // null если cast невозможен

// let для nullable
nullable?.let { 
    println(it.length)  // выполнится только если не null
}

// Smart casts
if (nullable != null) {
    println(nullable.length)  // автоматически String, не String?
}
```

### lateinit и lazy
```kotlin
// lateinit для non-null var, инициализируемых позже
class Service {
    lateinit var repository: Repository  // без = null
    
    fun init() {
        repository = Repository()
    }
    
    fun isInitialized() = ::repository.isInitialized
}

// lazy для val
class Heavy {
    val data: String by lazy {
        println("Computing...")
        loadData()  // вызовется только при первом обращении
    }
}
```

## 3. Классы и объекты

### Data classes
```kotlin
// Автоматически: equals, hashCode, toString, copy, componentN
data class User(
    val id: Long,
    val name: String,
    var age: Int = 18  // default значение
)

val user = User(1, "John")
val copy = user.copy(name = "Jane")  // копия с изменением

// Destructuring
val (id, name, age) = user
```

### Sealed classes и enum
```kotlin
// Sealed - закрытая иерархия
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Exhaustive when
fun handle(result: Result<String>) = when (result) {
    is Result.Success -> println(result.data)
    is Result.Error -> println(result.message)
    Result.Loading -> println("Loading...")
    // else не нужен!
}

// Enum с параметрами
enum class Status(val code: Int) {
    ACTIVE(1),
    INACTIVE(0);
    
    fun isActive() = this == ACTIVE
}
```

### Классы и наследование
```kotlin
// Primary constructor
open class Animal(val name: String) {
    // init блок
    init {
        println("Animal created: $name")
    }
    
    // Secondary constructor
    constructor(name: String, age: Int) : this(name) {
        println("Age: $age")
    }
    
    open fun sound() = "Some sound"
}

// Наследование
class Dog(name: String, val breed: String) : Animal(name) {
    override fun sound() = "Woof"
}

// Interfaces с default методами
interface Clickable {
    fun click()
    fun show() = println("Showing...")  // default implementation
}

// Abstract классы
abstract class Vehicle {
    abstract fun drive()
    
    fun honk() {  // обычный метод
        println("Beep!")
    }
}
```

### Object и companion object
```kotlin
// Singleton
object Database {
    fun connect() { }
}
Database.connect()

// Companion object (статические методы)
class User {
    companion object {
        const val MAX_AGE = 120
        
        @JvmStatic  // для Java interop
        fun create(name: String): User = User(name)
    }
}

User.MAX_AGE
User.create("John")

// Object expressions (анонимные классы)
val listener = object : ClickListener {
    override fun onClick() { }
}
```

## 4. Функции высшего порядка и лямбды

### Lambda синтаксис
```kotlin
// Полная форма
val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }

// Сокращенная
val sum = { a: Int, b: Int -> a + b }

// С type inference
val list = listOf(1, 2, 3)
list.filter { it > 1 }  // it - implicit имя единственного параметра

// Trailing lambda
list.filter { it > 1 }
    .map { it * 2 }
    .forEach { println(it) }

// Function references
fun isPositive(x: Int) = x > 0
list.filter(::isPositive)
list.map(String::toInt)

// Lambda с receiver
val buildString: StringBuilder.() -> Unit = {
    append("Hello")
    append(" World")
}
StringBuilder().apply(buildString)
```

### Inline functions
```kotlin
// inline раскрывает функцию в месте вызова
inline fun <T> measureTime(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    println("Time: ${System.currentTimeMillis() - start}ms")
    return result
}

// reified - доступ к типу в runtime
inline fun <reified T> isInstance(value: Any): Boolean {
    return value is T
}

// crossinline - запрет non-local return
inline fun transaction(crossinline body: () -> Unit) {
    try {
        begin()
        body()  // не может сделать return из внешней функции
        commit()
    } catch (e: Exception) {
        rollback()
    }
}

// noinline - отключает inline для параметра
inline fun process(
    inlined: () -> Unit,
    noinline notInlined: () -> Unit  // можно передать в другую функцию
) { }
```

## 5. Collections и Sequences

### Коллекции
```kotlin
// Immutable (по умолчанию)
val list = listOf(1, 2, 3)
val set = setOf(1, 2, 3)
val map = mapOf("a" to 1, "b" to 2)

// Mutable
val mutableList = mutableListOf(1, 2, 3)
mutableList.add(4)

// Создание
val list1 = List(5) { it * 2 }  // [0, 2, 4, 6, 8]
val list2 = generateSequence(1) { it + 1 }.take(5).toList()

// Операции (создают новые коллекции)
val doubled = list.map { it * 2 }
val filtered = list.filter { it > 1 }
val sum = list.reduce { acc, i -> acc + i }
val sumOrNull = list.reduceOrNull { acc, i -> acc + i }

// Группировка и ассоциация
val grouped = list.groupBy { it % 2 }  // {1=[1,3], 0=[2]}
val associated = list.associateBy { it.toString() }  // {"1"=1, "2"=2, "3"=3}
val pairs = list.map { it to it * 2 }.toMap()

// Полезные операции
list.any { it > 2 }     // true
list.all { it > 0 }     // true
list.none { it < 0 }    // true
list.firstOrNull { it > 2 }  // 3
list.partition { it > 2 }  // Pair([3], [1,2])
list.chunked(2)         // [[1,2], [3]]
list.windowed(2)        // [[1,2], [2,3]]
list.zip(listOf("a", "b", "c"))  // [(1,"a"), (2,"b"), (3,"c")]
```

### Sequences (ленивые вычисления)
```kotlin
// Для больших данных или цепочек операций
val sequence = list.asSequence()
    .filter { println("filter $it"); it > 0 }
    .map { println("map $it"); it * 2 }
    .take(2)
    .toList()  // terminal операция, запускает вычисления

// Генерация
val fibonacci = sequence {
    var a = 0
    var b = 1
    while (true) {
        yield(a)
        val temp = a
        a = b
        b = temp + b
    }
}

fibonacci.take(10).toList()
```

## 6. Scope Functions

```kotlin
val user = User("John", 25)

// let - it, возвращает результат lambda
val length = user.name.let {
    println("Name: $it")
    it.length  // return value
}

// Часто для nullable
user?.let {
    saveToDb(it)
}

// run - this, возвращает результат lambda
val result = user.run {
    age += 1
    "User $name is now $age"  // return value
}

// with - this, возвращает результат lambda (не extension)
val description = with(user) {
    "Name: $name, Age: $age"
}

// apply - this, возвращает объект
val configured = user.apply {
    age = 30
    name = "Jane"
}  // возвращает user

// also - it, возвращает объект
val saved = user.also {
    println("Saving user: $it")
    saveToDb(it)
}  // возвращает user

// Мнемоника:
// let, also - работают с it
// run, with, apply - работают с this
// let, run, with - возвращают результат lambda
// also, apply - возвращают объект
```

## 7. Делегирование

### Делегирование классов
```kotlin
interface Printer {
    fun print(text: String)
}

class ConsolePrinter : Printer {
    override fun print(text: String) = println(text)
}

// Делегирование реализации
class FormattedPrinter(
    private val delegate: Printer
) : Printer by delegate {
    override fun print(text: String) {
        delegate.print("*** $text ***")
    }
}
```

### Property делегаты
```kotlin
// Встроенные делегаты
class Example {
    // lazy - вычисляется при первом обращении
    val heavy by lazy {
        println("Computing...")
        "Heavy string"
    }
    
    // observable - уведомление об изменениях
    var name: String by Delegates.observable("initial") { prop, old, new ->
        println("$old -> $new")
    }
    
    // vetoable - валидация изменений
    var age: Int by Delegates.vetoable(0) { prop, old, new ->
        new >= 0  // false отменяет изменение
    }
    
    // map делегат
    val map = mutableMapOf<String, Any>()
    var id: Int by map
    var email: String by map
}

// Custom делегат
class CachedProperty<T>(private val loader: () -> T) {
    private var cached: T? = null
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return cached ?: loader().also { cached = it }
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        cached = value
    }
}

// Использование
class Service {
    val data by CachedProperty { loadData() }
}
```

## 8. Generics и Variance

### Основы generics
```kotlin
// Generic класс
class Box<T>(val value: T)

// Generic функция
fun <T> singletonList(item: T): List<T> = listOf(item)

// Multiple bounds
fun <T> process(item: T) where T : Comparable<T>, T : Cloneable {
    // item и Comparable и Cloneable
}

// Reified в inline функциях
inline fun <reified T> isType(value: Any): Boolean = value is T
```

### Variance (in/out)
```kotlin
// out - covariance (производитель)
interface Producer<out T> {
    fun produce(): T
    // fun consume(t: T)  // Error! T в in-позиции
}

// in - contravariance (потребитель)
interface Consumer<in T> {
    fun consume(t: T)
    // fun produce(): T  // Error! T в out-позиции
}

// Пример с коллекциями
val strings: List<String> = listOf("a", "b")
val objects: List<Any> = strings  // OK, List<out T>

val mutableStrings: MutableList<String> = mutableListOf()
// val mutableObjects: MutableList<Any> = mutableStrings  // Error!

// Star projection
fun printAll(list: List<*>) {  // List<out Any?>
    list.forEach { println(it) }
}
```

## 9. Аннотации и Reflection

### Аннотации
```kotlin
// Targets для Kotlin properties
class User {
    @field:NotNull
    @get:JsonProperty("user_name")
    @param:Size(min = 2)
    var name: String = ""
}

// Use-site targets
@file:JvmName("Utils")
@field:     // поле
@get:       // getter
@set:       // setter
@param:     // параметр конструктора
@property:  // property
@receiver:  // receiver параметр

// Создание аннотаций
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Loggable(val level: String = "INFO")

@Loggable("DEBUG")
class Service
```

### Базовая рефлексия
```kotlin
// KClass
val kClass = String::class
val javaClass = String::class.java

// Properties и functions
class Person(val name: String, var age: Int) {
    fun greet() = "Hello"
}

val person = Person("John", 30)
val nameProperty = Person::name
println(nameProperty.get(person))

// Проверка инициализации lateinit
class Service {
    lateinit var repo: Repository
    
    fun isRepoInitialized() = ::repo.isInitialized
}
```

## 10. DSL и Builders

### Type-safe builders
```kotlin
// HTML DSL пример
class Tag(val name: String) {
    private val children = mutableListOf<Tag>()
    private val attributes = mutableMapOf<String, String>()
    
    fun set(name: String, value: String) {
        attributes[name] = value
    }
    
    operator fun String.unaryPlus() {
        children.add(Tag(this))
    }
    
    operator fun invoke(init: Tag.() -> Unit) {
        this.init()
    }
}

fun html(init: Tag.() -> Unit): Tag {
    return Tag("html").apply(init)
}

// Использование
val doc = html {
    set("lang", "en")
    "head" {
        "title" { +"My Page" }
    }
    "body" {
        "h1" { +"Hello World" }
    }
}

// @DslMarker для ограничения scope
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class HtmlTag : Tag("html")
```

## 11. Interop с Java

### Вызов Java из Kotlin
```kotlin
// Platform types (тип с ! - может быть null)
val list = JavaClass.getList()  // List<String!>!

// SAM conversion
button.setOnClickListener { println("Clicked") }

// Getters/Setters как properties
javaObject.name = "John"  // setName("John")
val name = javaObject.name  // getName()

// Static методы
JavaClass.staticMethod()  // напрямую
```

### Вызов Kotlin из Java
```kotlin
// @JvmStatic для companion object
class Utils {
    companion object {
        @JvmStatic
        fun helper() { }  // Utils.helper() в Java
    }
}

// @JvmOverloads для default параметров
@JvmOverloads
fun create(name: String, age: Int = 18) { }
// В Java: create("John"), create("John", 25)

// @JvmField для полей без getter/setter
@JvmField
val CONSTANT = 42

// @JvmName для изменения имени
@JvmName("getUsername")
fun getUserName(): String = ""

// @Throws для checked exceptions
@Throws(IOException::class)
fun readFile(path: String): String { }
```

## 12. Топ вопросы на собеседовании

### 1. val vs var vs const val?
- `val` - read-only (неизменяемая ссылка), вычисляется в runtime
- `var` - mutable
- `const val` - compile-time константа, только примитивы и String

### 2. Когда использовать какую scope function?
```kotlin
// Конфигурация объекта -> apply
val user = User().apply {
    name = "John"
    age = 30
}

// Nullable chaining -> let
user?.let { saveToDb(it) }

// Вычисления с объектом -> run/with
val result = user.run { "$name is $age years old" }

// Side effects -> also
users.filter { it.age > 18 }
    .also { println("Found ${it.size} adults") }
    .map { it.name }
```

### 3. data class ограничения?
- Минимум один параметр в primary constructor
- Все параметры должны быть val/var
- Не может быть abstract, open, sealed, inner
- equals/hashCode только по primary constructor параметрам

### 4. Разница между List и MutableList?
- `List<T>` - read-only интерфейс (нет add/remove)
- `MutableList<T>` - расширяет List, добавляет мутирующие методы
- Актуальная коллекция может быть mutable, даже если тип List

### 5. Когда Sequence вместо Collection?
- Большие данные (>1000 элементов)
- Много промежуточных операций
- Может не понадобиться весь результат (take, first)
- Бесконечные последовательности

### 6. inline функции - когда и зачем?
- Функции высшего порядка (избегаем создания объектов для lambda)
- reified generics
- Минус: увеличение размера кода
- Не использовать для больших функций

### 7. sealed class vs enum?
- enum - фиксированный набор экземпляров
- sealed - фиксированный набор типов (могут быть разные классы с разными полями)

### 8. Как работает null safety?
- Типы nullable (String?) и non-null (String)
- Safe call (?.), Elvis (?:), not-null assertion (!!)
- Smart casts после проверки на null
- Platform types из Java (!)

### 9. by lazy vs lateinit?
- `by lazy` - для val, thread-safe по умолчанию, вычисляется при первом обращении
- `lateinit` - для var, не thread-safe, нельзя примитивы, можно проверить isInitialized

### 10. Variance (out/in)?
- `out T` - можем только возвращать T (Producer)
- `in T` - можем только принимать T (Consumer)
- Invariant - без модификаторов, точное соответствие типа

## Quick Reference - что точно спросят

**Must know:**
- Null safety (?.  ?:  !!)
- data class, sealed class
- Scope functions (let, run, apply, also, with)
- Extension functions
- Higher-order functions и lambda
- Collections vs Sequences

**Should know:**
- Делегаты (by lazy, observable)
- inline/reified/crossinline
- Variance (in/out)
- Companion object vs object
- Destructuring

**Nice to have:**
- DSL builders
- Type aliases
- Contracts
- Inline classes (value classes)

## Примеры кода для практики

```kotlin
// Задача 1: Реализовать extension для проверки email
fun String.isValidEmail(): Boolean = 
    matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))

// Задача 2: Safe builder pattern
class UserBuilder {
    var name: String? = null
    var age: Int? = null
    
    fun build(): User {
        return User(
            name ?: throw IllegalStateException("Name required"),
            age ?: 18
        )
    }
}

fun buildUser(init: UserBuilder.() -> Unit): User {
    return UserBuilder().apply(init).build()
}

// Использование
val user = buildUser {
    name = "John"
    age = 30
}

// Задача 3: Generic repository с constraint
interface Entity {
    val id: Long
}

class Repository<T : Entity> {
    private val storage = mutableMapOf<Long, T>()
    
    fun save(entity: T) {
        storage[entity.id] = entity
    }
    
    fun findById(id: Long): T? = storage[id]
}

// Задача 4: Результат операции через sealed class
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

// Использование
fun fetchUser(): ApiResult<User> = 
    ApiResult.Success(User(1, "John"))

val result = fetchUser()
    .map { it.name }
    .map { it.uppercase() }
```