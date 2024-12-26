package arrays

/**
 * 1071. Greatest Common Divisor of Strings
 * Difficulty: Easy

 * Input: str1 = "ABCABC", str2 = "ABC"
 * Output: "ABC"
 * Example 2:
 *
 * Input: str1 = "ABABAB", str2 = "ABAB"
 * Output: "AB"
 * Example 3:
 *
 * Input: str1 = "LEET", str2 = "CODE"
 * Output: ""
 */
fun main() {
    println(gcdOfStrings("ABCABC","ABC"))
    println(gcdOfStrings("ABABAB","ABAB"))
    println(gcdOfStrings("LEET","CODE"))
}

fun gcdOfStrings(str1: String, str2: String): String {
    // Базовая проверка на существование общего делителя
    if (str1 + str2 != str2 + str1) return ""

    val gcdLength = findGcd(str1.length, str2.length)
    return str1.substring(0, gcdLength)

}

fun findGcd(str1: Int,str2: Int): Int {
    if (str1 == 0) return str2
    var a = str1
    var b = str2

    while (a != 0) {
        val temp = a
        a = b % a
        b = temp
    }
    return b
}