package exercise.justCode;

import java.util.List;

/**
 * Разминка после перерыва. Возвращаем мышечную память.
 *
 * Правила игры:
 *   1. task1 уже решён — это твой образец синтаксиса. Прочитай, вспомни форму.
 *   2. task2..task5 — заглушки. Напиши тело САМ, руками. Не подсматривай в интернет.
 *   3. Запусти main и сверь вывод с тем, что написано в комментариях "ожидаем".
 *
 * Запуск:
 *   ./gradlew build  (соберёт проект)
 *   java -cp build/classes/java/main exercise.justCode.Warmup
 */
public class Warmup {

    public static void main(String[] args) {
        // Каждое задание печатает результат. Сверяй с "ожидаем".
        System.out.println("task1 -> " + sum(new int[]{1, 2, 3, 4, 5})); // ожидаем: 15

        System.out.println("task2 -> " + max(new int[]{3, 9, 1, 7, 4}));  // ожидаем: 9
        System.out.println("task3 -> " + reverse("java"));                 // ожидаем: avaj
        System.out.println("task4 -> " + countEven(List.of(1, 2, 3, 4, 6))); // ожидаем: 3
        System.out.println("task5 -> " + isPalindrome("level"));           // ожидаем: true
    }

    // ── task1: ОБРАЗЕЦ (решён) ───────────────────────────────────────────
    // Сумма всех элементов массива.
    static int sum(int[] nums) {
        int total = 0;                 // аккумулятор — переменная, где копим результат
        for (int n : nums) {           // for-each: идём по каждому элементу массива
            total += n;                // total = total + n
        }
        return total;                  // отдаём результат наружу
    }

    // ── task2: напиши сам ────────────────────────────────────────────────
    // Найти максимальный элемент массива. Подсказка: заведи переменную max,
    // положи в неё nums[0], пройди циклом и обновляй, если нашёл больше.
    static int max(int[] nums) {
        return 0; // TODO: замени на свою реализацию
    }

    // ── task3: напиши сам ────────────────────────────────────────────────
    // Развернуть строку: "java" -> "avaj".
    // Подсказка: StringBuilder и метод .reverse(), либо цикл с конца строки.
    static String reverse(String s) {
        return ""; // TODO
    }

    // ── task4: напиши сам ────────────────────────────────────────────────
    // Посчитать, сколько в списке чётных чисел. Чётное: n % 2 == 0.
    static long countEven(List<Integer> nums) {
        return 0; // TODO
    }

    // ── task5: напиши сам ────────────────────────────────────────────────
    // Проверить, палиндром ли строка ("level" -> true, "java" -> false).
    // Подсказка: можно сравнить строку с её разворотом (используй task3!).
    static boolean isPalindrome(String s) {
        return false; // TODO
    }
}
