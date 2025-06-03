package arrays

//**
// * 345. Reverse Vowels of a String
// * Difficulty: Easy
//Example 1:
//
//Input: Input: s = "IceCreAm"
//Output: "AceCreIm"
//
//Input: s = "leetcode"
//Output: "leotcede"
// */



fun reverseVowels(s: String): String {
    println("\nНачинаем обработку строки: \"$s\"")

    val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')
    val charArray = s.toCharArray()
    var left = 0
    var right = s.length - 1

    println("Инициализация: left=$left, right=$right")

    while (left < right) {
        // Ищем гласную слева
        while (left < right && !vowels.contains(charArray[left])) {
            println("Пропускаем согласную '${charArray[left]}' слева, left=$left")
            left++
        }

        // Ищем гласную справа
        while (left < right && !vowels.contains(charArray[right])) {
            println("Пропускаем согласную '${charArray[right]}' справа, right=$right")
            right--
        }

        // Если нашли пару гласных - меняем их местами
        if (left < right) {
            println("Нашли пару гласных: '${charArray[left]}' и '${charArray[right]}'")
            println("Текущее состояние: ${charArray.joinToString("")}")

            val temp = charArray[left]
            charArray[left] = charArray[right]
            charArray[right] = temp

            println("После замены: ${charArray.joinToString("")}")

            left++
            right--
        }
    }

    val result = String(charArray)
    println("Результат: \"$result\"")
    return result
}

// Тестируем функцию
fun main() {
    println("Тест 1:")
    reverseVowels("IceCreAm")

    println("\nТест 2:")
    reverseVowels("leetcode")
}
