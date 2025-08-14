import kotlin.math.pow


data class User(val name: String, val age: Int, var active: Boolean)


typealias UserId = Long
//fun main() {
//    val a = 3
//    val b = 8
//
//    println(add(a,b))
//    println(sub(a,b))
//
//    val name = "Ulad"
//    val age = 30
//    var active = true
//    val userUlad = createUser(name, age, active)
//    val jane = createUser("Jane", active = false, age =  20)
//    println(userUlad)
//
//
//    println("asdasd@kassd".isEmail())
//    val aaa = 2 power 3
//    println(aaa)
//}

fun add(a: Int, b: Int): Int {
    return a + b
}

fun sub(a: Int, b: Int) = a - b

fun createUser(
    name: String,
    age: Int = 18,
    active: Boolean
): User {
    return User(name, age, active)
}

fun String.isEmail(): String {
    return "Is email  - $this " + contains("@")
}

infix fun Int.power(n: Int): Int = this.toDouble().pow(n.toDouble()).toInt()

//fun main() {
//    var x = 5
//    val result = if (x > 0) "positive x" else "negative x"
//
//    val description = when (x) {
//        0 -> "zero"
//        1,2,3 -> "small"
//        in 4..10 -> "large number"
//        is Int -> "integer"
//        else -> "uknown"
//    }
//
//
//    for (i in 1..10) { }
//    for (i in 1 until 10) {}
//    for (i in 10 downTo 1) {}
//    for (i in 1..10 step 2) {}
//
//    val map = mapOf(1 to "one", 2 to "two", 3 to "three")
//    for ((key, value) in map) {
//        println("$key -> $value")
//    }
//}


//null safety
//fun main() {
//    var nullS: String? = "Test"
//    //nullS = null
//    val length = nullS?.length
//    val length2 = nullS?.length ?: 0
//   // val length3 = nullS!!.length
//    val str: String = nullS as? String ?: "null"
//    //val length4 = nullS!!.length
//
//    nullS?.let {
//        println(it.length)
//    }
//
//}

//lateinit//lazy
class Service {
    lateinit var repo: Repository

    fun init() {
        repo = Repository()
    }

    fun isInitialized() = ::repo.isInitialized
}

class Repository {
    private val users = mutableListOf<User>()
    fun isInitialized(): Boolean = false
}

// Enum с параметрами
enum class Status(val code: Int) {
    ACTIVE(1),
    INACTIVE(0);

    fun isActive() = this == ACTIVE
}

//fun main() {
//    val sum = { a: Int, b: Int -> a + b }
//    val substring = { a: Int, b: Int -> a * b }
//    val multiply = { a: Int, b: Int -> a * b }
//    val divide = { a: Int, b: Int -> a / b }
//
//    val c = sumS(5, 6, multiply)
//    println(c)
//
//
//    val list = listOf(1, 2, 3, 4, 5, 6)
//    list.map { it + 2 }
//        .filter { it % 2 == 0 }
//        .filter { isSmallerThan5(it) }
//        .forEach(::println)
//}

fun isSmallerThan5(n: Int): Boolean = n >= 5

fun sumS(a: Int, b: Int, func: (Int, Int) -> Int): Int {
    return func(a, b)
}

open class Animal(val name: String) {
    init {
        println("Animal created: $name")
    }

    constructor(name: String, age: Int) : this(name) {
        println("AGE: $age")
    }

    open fun sound() = "some sound"
}

class Dog(name: String, val breed: String) : Animal(name) {
    override fun sound() = "Woof"
}

interface Clickable {
    fun click()
    //fun show() = println("Showing ... ") //def method

}

abstract class Vehicle {
    abstract fun drive()

    fun funk() {
        println("Beep")
    }
}

class Click : Clickable {
    override fun click() {
        println("click")
    }
}
//
//fun main() {
//    val cat = Animal("Pux")
//    val dog = Dog("Charlie","gow")
//
////    println(cat.sound())
////    println(dog.sound())
//
//    val b = Click()
//    //println(b.show())
//    b.click()
//
//}


//fun main() {
//    val list = listOf(1, 2, 3, 4, 5)
//    val set = setOf(1, 2, 3, 4)
//    val map = mapOf("a" to 1, "b" to 2)
//
//    val mutableList = mutableListOf(1, 2, 3)
//    mutableList.add(4)
//
//    val list1 = List(5) { it * 2 }
//    val list2 = generateSequence(2) { it + 1 }.take(5).toList();
//    println(list2) //2,3,4,5,6
//
//    val doubled = list.map { it * 2 }
//    val filtered = list.filter { it < 1 }
//    val sum = list.reduce { acc, i ->  acc + i}
//
//    val group = list.groupBy { it % 2 } //{1 = [1,3], 0 = [2]}
//    val associadte = list.associateBy { it.toString() } // {"1"=1,...
//    val pairs = list.map {it to it * 2}.toMap()
//
//
//    val fibonacci = sequence {
//        var a = 0
//        var b = 1
//        while (true) {
//            yield(a)
//            val temp = a
//            a = b
//            b = temp + a
//        }
//    }
//    println(fibonacci.take(10).toList())
//}


// scope FUnc

//fun main() {
//    val user = User(name = "John", age = 25, active = true)
//
//    val legth = user.name.let {
//        println("Name : $it")
//        it.length
//    }
//    println(legth)
//
//    user?.let {
////        saveToDb(it)
//    }
//    var age = 10;
//    val result = user.run {
//        age += 1
//        "User $name is now $age"
//    }
//
//    println(result)
//}
// Мнемоника:
// let, also - работают с it
// run, with, apply - работают с this
// let, run, with - возвращают результат lambda
// also, apply - возвращают объект


//7

fun main() {
   // listOf(List)
}


































