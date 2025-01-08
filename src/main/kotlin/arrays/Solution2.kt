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

fun gcdOfStrings(str1: String, str2: String): String {
    if (str1 + str2 != str2 + str1) return ""
    if (str1 == "" || str2 == "") return ""

    val gcdLength = gcd(str1.length, str2.length)
    return str1.substring(0, gcdLength)
}

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)