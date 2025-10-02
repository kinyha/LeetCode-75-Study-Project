package exercise.practice.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Коллекция алгоритмических задач для подготовки к собеседованиям
 * Уровни 1-3 (Junior - Middle-)
 * <p>
 * Формат: готовые методы-заглушки с описанием задач
 */
public class AlgorithmicTasks {


    // ============================================
    // ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ДЛЯ ТЕСТИРОВАНИЯ
    // ============================================

    /**
     * Простые sout-тесты для проверки решений (по 3 кейса на задачу)
     */
    public static void main(String[] args) {
        System.out.println("=== ТЕСТЫ УРОВЕНЬ 1 ===");

//        // Задача 1: findMax
//        System.out.println("findMax [3,7,2,9,1] => " + findMax(new int[]{3, 7, 2, 9, 1})); // 9
//        System.out.println("findMax [-5,-2,-10,-1] => " + findMax(new int[]{-5, -2, -10, -1})); // -1
//        System.out.println("findMax [42] => " + findMax(new int[]{42})); // 42
//
//        // Задача 2: reverseString
//        System.out.println("reverseString 'hello' => '" + reverseString("hello") + "'"); // 'olleh'
//        System.out.println("reverseString '' => '" + reverseString("") + "'"); // ''
//        System.out.println("reverseString 'a' => '" + reverseString("a") + "'"); // 'a'

//        // Задача 3: countVowels
//        System.out.println("countVowels 'Hello World' => " + countVowels("Hello World")); // 3
//        System.out.println("countVowels 'bcdfg' => " + countVowels("bcdfg")); // 0
//        System.out.println("countVowels '' => " + countVowels("")); // 0
////
        // Задача 4: isEven
        System.out.println("isEven 4 => " + isEven(4)); // true
        System.out.println("isEven 7 => " + isEven(7)); // false
        System.out.println("isEven -2 => " + isEven(-2)); // true
//
        // Задача 5: sumArray
        System.out.println("sumArray [1,2,3,4,5] => " + sumArray(new int[]{1, 2, 3, 4, 5})); // 15
        System.out.println("sumArray [-1,-2,3] => " + sumArray(new int[]{-1, -2, 3})); // 0
        System.out.println("sumArray [] => " + sumArray(new int[]{})); // 0
//
//        System.out.println("\n=== ТЕСТЫ УРОВЕНЬ 2 ===");
//
//        // Задача 6: findFirstDuplicate
//        System.out.println("findFirstDuplicate [1,2,3,2,4] => " + findFirstDuplicate(new int[]{1, 2, 3, 2, 4})); // 2
//        System.out.println("findFirstDuplicate [1,1,2] => " + findFirstDuplicate(new int[]{1, 1, 2})); // 1
//        System.out.println("findFirstDuplicate [1,2,3] => " + findFirstDuplicate(new int[]{1, 2, 3})); // -1
//
//        // Задача 7: areAnagrams
//        System.out.println("areAnagrams 'listen' & 'silent' => " + areAnagrams("listen", "silent")); // true
//        System.out.println("areAnagrams 'The Eyes' & 'They See' => " + areAnagrams("The Eyes", "They See")); // true
//        System.out.println("areAnagrams 'hello' & 'world' => " + areAnagrams("hello", "world")); // false
//
//        // Задача 8: fibonacci
//        System.out.println("fibonacci 0 => " + fibonacci(0)); // 0
//        System.out.println("fibonacci 1 => " + fibonacci(1)); // 1
//        System.out.println("fibonacci 10 => " + fibonacci(10)); // 55
//
//        // Задача 9: removeDuplicateChars
//        System.out.println("removeDuplicateChars 'programming' => '" + removeDuplicateChars("programming") + "'"); // 'progamin'
//        System.out.println("removeDuplicateChars 'aabbcc' => '" + removeDuplicateChars("aabbcc") + "'"); // 'abc'
//        System.out.println("removeDuplicateChars 'abc' => '" + removeDuplicateChars("abc") + "'"); // 'abc'
//
//        // Задача 10: findSecondMax
//        System.out.println("findSecondMax [5,2,9,1,7] => " + findSecondMax(new int[]{5, 2, 9, 1, 7})); // 7
//        System.out.println("findSecondMax [10,10,5] => " + findSecondMax(new int[]{10, 10, 5})); // 5
//        System.out.println("findSecondMax [2,1] => " + findSecondMax(new int[]{2, 1})); // 1
//
//        System.out.println("\n=== ТЕСТЫ УРОВЕНЬ 3 ===");
//
//        // Задача 11: twoSum
//        int[] r1 = twoSum(new int[]{2, 7, 11, 15}, 9);
//        int[] r2 = twoSum(new int[]{3, 2, 4}, 6);
//        int[] r3 = twoSum(new int[]{-1, -2, -3, -4}, -5);
//        System.out.println("twoSum [2,7,11,15],9 => " + java.util.Arrays.toString(r1)); // [0,1]
//        System.out.println("twoSum [3,2,4],6 => " + java.util.Arrays.toString(r2)); // [1,2]
//        System.out.println("twoSum [-1,-2,-3,-4],-5 => " + java.util.Arrays.toString(r3)); // e.g. [1,2]
//
//        // Задача 12: groupAnagrams
//        System.out.println("groupAnagrams [eat,tea,tan,ate,nat,bat] => " + groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}));
//        System.out.println("groupAnagrams [''] => " + groupAnagrams(new String[]{""}));
//        System.out.println("groupAnagrams ['a'] => " + groupAnagrams(new String[]{"a"}));
//
//        // Задача 13: isValidParentheses
//        System.out.println("isValidParentheses '()' => " + isValidParentheses("()")); // true
//        System.out.println("isValidParentheses '()[]{}' => " + isValidParentheses("()[]{}")); // true
//        System.out.println("isValidParentheses '([)]' => " + isValidParentheses("([)]")); // false
    }

    // ============================================
    // УРОВЕНЬ 1 - БАЗОВЫЙ (5 задач)
    // ============================================

    /**
     * Задача 1: Поиск максимального элемента в массиве
     * <p>
     * Уровень: 1
     * Время: 5 минут
     * <p>
     * Дан массив целых чисел, найти максимальный элемент.
     * <p>
     * Примеры:
     * Input: [3, 7, 2, 9, 1]
     * Output: 9
     * <p>
     * Input: [-5, -2, -10, -1]
     * Output: -1
     * <p>
     * Edge cases:
     * - Пустой массив
     * - Массив из одного элемента
     * - Все элементы одинаковые
     * <p>
     * Критерии оценки:
     * - Junior: базовый цикл, работает
     * - Middle: обработка edge cases
     */
    public static int findMax(int[] nums) {
        int max = Integer.MIN_VALUE;

        if (nums.length == 0) {
            throw new RuntimeException("Wrong data");
        }

        for (int i = 0; i < nums.length; i++) {
            max = max < nums[i] ? nums[i] : max;
        }
        return max;
    }

    /**
     * Задача 2: Реверс строки
     * <p>
     * Уровень: 1
     * Время: 5 минут
     * <p>
     * Развернуть строку в обратном порядке.
     * <p>
     * Примеры:
     * Input: "hello"
     * Output: "olleh"
     * <p>
     * Input: "Java"
     * Output: "avaJ"
     * <p>
     * Edge cases:
     * - Пустая строка
     * - Строка из одного символа
     * - null на входе
     * <p>
     * Критерии оценки:
     * - Junior: использование StringBuilder
     * - Middle: проверка на null/empty
     */
    public static String reverseString(String str) {
        //use Builder
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            result.append(str.charAt(str.length() - 1 - i));
        }
        return result.toString();
    }

    /**
     * Задача 3: Подсчет гласных в строке
     * <p>
     * Уровень: 1
     * Время: 7 минут
     * <p>
     * Подсчитать количество гласных букв (a, e, i, o, u) в строке.
     * Регистр не важен.
     * <p>
     * Примеры:
     * Input: "Hello World"
     * Output: 3 (e, o, o)
     * <p>
     * Input: "Programming"
     * Output: 3 (o, a, i)
     * <p>
     * Edge cases:
     * - Пустая строка
     * - Строка без гласных
     * - Смешанный регистр
     * <p>
     * Критерии оценки:
     * - Junior: работает для латиницы
     * - Middle: игнорирует регистр, обработка edge cases
     */
    public static int countVowels(String str) {
        return (int) str.chars()
                .mapToObj(c -> (char) c)
                .filter(AlgorithmicTasks::isVowels)
                .count();
    }

    public static boolean isVowels(char letter) {
        List<Character> vowels = List.of('a', 'e', 'i', 'o', 'u');

        for (Character q : vowels) {
            if (q == letter) {
                return true;
            }
        }
        return false;
    }

    /**
     * Задача 4: Проверка на четность числа
     * <p>
     * Уровень: 1
     * Время: 3 минута
     * <p>
     * Проверить, является ли число четным.
     * <p>
     * Примеры:
     * Input: 4
     * Output: true
     * <p>
     * Input: 7
     * Output: false
     * <p>
     * Input: 0
     * Output: true
     * <p>
     * Edge cases:
     * - Отрицательные числа
     * - Ноль
     * <p>
     * Критерии оценки:
     * - Junior: использование % или битовых операций
     */
    public static boolean isEven(int num) {
        return num % 2 == 0;
    }

    /**
     * Задача 5: Сумма элементов массива
     * <p>
     * Уровень: 1
     * Время: 5 минут
     * <p>
     * Вычислить сумму всех элементов массива.
     * <p>
     * Примеры:
     * Input: [1, 2, 3, 4, 5]
     * Output: 15
     * <p>
     * Input: [-1, -2, 3]
     * Output: 0
     * <p>
     * Edge cases:
     * - Пустой массив
     * - Переполнение int (использовать long)
     * <p>
     * Критерии оценки:
     * - Junior: базовый цикл
     * - Middle: Stream API или обработка переполнения
     */
    public static int sumArray(int[] nums) {
        var a = Arrays.stream(nums)
                .mapToLong(Integer::toUnsignedLong)
                .sum();
        return (int) a;
    }

    // ============================================
    // УРОВЕНЬ 2 - JUNIOR (5 задач)
    // ============================================

    /**
     * Задача 6: Поиск дубликатов в массиве
     * <p>
     * Уровень: 2
     * Время: 10 минут
     * Компания: Магнит
     * <p>
     * Найти первый повторяющийся элемент в массиве.
     * Вернуть -1 если дубликатов нет.
     * <p>
     * Примеры:
     * Input: [1, 2, 3, 2, 4]
     * Output: 2
     * <p>
     * Input: [1, 2, 3, 4]
     * Output: -1
     * <p>
     * Edge cases:
     * - Пустой массив
     * - Все элементы уникальны
     * - Все элементы одинаковые
     * <p>
     * Критерии оценки:
     * - Junior: O(n²) с вложенными циклами
     * - Middle: O(n) с использованием HashSet
     * <p>
     * Подводные камни:
     * - Забыть проверку на пустой массив
     * - Неоптимальная сложность
     */
    public static int findFirstDuplicate(int[] nums) {
        // TODO: implement
        return -1;
    }

    /**
     * Задача 7: Проверка на анаграмму
     * <p>
     * Уровень: 2
     * Время: 10 минут
     * Компания: InStock Technologies
     * <p>
     * Проверить, являются ли две строки анаграммами.
     * Анаграммы - строки с одинаковым набором символов.
     * <p>
     * Примеры:
     * Input: "listen", "silent"
     * Output: true
     * <p>
     * Input: "hello", "world"
     * Output: false
     * <p>
     * Input: "The Eyes", "They See"
     * Output: true (игнорируя регистр и пробелы)
     * <p>
     * Edge cases:
     * - Разная длина строк
     * - Пустые строки
     * - Разный регистр
     * - Пробелы и спецсимволы
     * <p>
     * Критерии оценки:
     * - Junior: сортировка символов
     * - Middle: HashMap для подсчета частоты
     * <p>
     * Подводные камни:
     * - Не учесть регистр
     * - Не обработать пробелы
     */
    public static boolean areAnagrams(String s1, String s2) {
        // TODO: implement
        return false;
    }

    /**
     * Задача 8: Числа Фибоначчи
     * <p>
     * Уровень: 2
     * Время: 10 минут
     * Компания: Сбер
     * <p>
     * Вернуть N-е число Фибоначчи.
     * Последовательность: 0, 1, 1, 2, 3, 5, 8, 13, 21...
     * <p>
     * Примеры:
     * Input: 0
     * Output: 0
     * <p>
     * Input: 5
     * Output: 5
     * <p>
     * Input: 10
     * Output: 55
     * <p>
     * Edge cases:
     * - n = 0
     * - Отрицательный n
     * - Большие значения (переполнение)
     * <p>
     * Критерии оценки:
     * - Junior: рекурсия (неэффективно)
     * - Middle: итеративный подход O(n)
     * - Senior: мемоизация или формула Бине
     * <p>
     * Подводные камни:
     * - StackOverflowError при рекурсии
     * - Переполнение long для больших n
     */
    public static long fibonacci(int n) {
        // TODO: implement
        return 0;
    }

    /**
     * Задача 9: Удаление дубликатов из строки
     * <p>
     * Уровень: 2
     * Время: 10 минут
     * Компания: Ростелеком
     * <p>
     * Удалить все повторяющиеся символы, оставив только первое вхождение.
     * Сохранить исходный порядок.
     * <p>
     * Примеры:
     * Input: "programming"
     * Output: "progamin"
     * <p>
     * Input: "aabbcc"
     * Output: "abc"
     * <p>
     * Edge cases:
     * - Пустая строка
     * - Все символы уникальны
     * - Все символы одинаковые
     * <p>
     * Критерии оценки:
     * - Junior: вложенные циклы O(n²)
     * - Middle: LinkedHashSet для сохранения порядка O(n)
     * <p>
     * Подводные камни:
     * - Потерять порядок символов
     * - Не обработать пустую строку
     */
    public static String removeDuplicateChars(String str) {
        // TODO: implement
        return null;
    }

    /**
     * Задача 10: Второй максимальный элемент
     * <p>
     * Уровень: 2
     * Время: 12 минут
     * Компания: Астон
     * <p>
     * Найти второй по величине элемент в массиве.
     * <p>
     * Примеры:
     * Input: [5, 2, 9, 1, 7]
     * Output: 7
     * <p>
     * Input: [10, 10, 5]
     * Output: 5
     * <p>
     * Edge cases:
     * - Массив из одного элемента
     * - Все элементы одинаковые
     * - Два элемента
     * <p>
     * Критерии оценки:
     * - Junior: сортировка и взять предпоследний
     * - Middle: один проход O(n)
     * <p>
     * Подводные камни:
     * - Не учесть одинаковые максимальные элементы
     * - Неправильная обработка малых массивов
     */
    public static Integer findSecondMax(int[] nums) {
        // TODO: implement
        return null;
    }

    // ============================================
    // УРОВЕНЬ 3 - MIDDLE- (3 задачи)
    // ============================================

    /**
     * Задача 11: Two Sum
     * <p>
     * Уровень: 3
     * Время: 15 минут
     * Компания: ОТП банк, Лига цифровой экономики
     * <p>
     * Найти индексы двух чисел в массиве, сумма которых равна target.
     * Гарантируется, что решение существует и единственно.
     * <p>
     * Примеры:
     * Input: nums = [2, 7, 11, 15], target = 9
     * Output: [0, 1]
     * <p>
     * Input: nums = [3, 2, 4], target = 6
     * Output: [1, 2]
     * <p>
     * Edge cases:
     * - Использование одного элемента дважды
     * - Отрицательные числа
     * - Дубликаты в массиве
     * <p>
     * Критерии оценки:
     * - Junior: O(n²) с вложенными циклами
     * - Middle: O(n) с HashMap
     * - Senior: учет edge cases, clean code
     * <p>
     * Подводные камни:
     * - Использовать один индекс дважды
     * - Вернуть значения вместо индексов
     * <p>
     * Follow-up вопросы:
     * 1. Что если массив отсортирован?
     * 2. Что если нужны все пары?
     * 3. Как изменится для Three Sum?
     */
    public static int[] twoSum(int[] nums, int target) {
        // TODO: implement
        return null;
    }

    /**
     * Задача 12: Группировка анаграмм
     * <p>
     * Уровень: 3
     * Время: 20 минут
     * Компания: Авангард
     * <p>
     * Сгруппировать строки-анаграммы вместе.
     * <p>
     * Примеры:
     * Input: ["eat", "tea", "tan", "ate", "nat", "bat"]
     * Output: [["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]
     * <p>
     * Input: [""]
     * Output: [[""]]
     * <p>
     * Input: ["a"]
     * Output: [["a"]]
     * <p>
     * Edge cases:
     * - Пустые строки
     * - Одна строка
     * - Разный регистр
     * <p>
     * Критерии оценки:
     * - Junior: сортировка каждой строки как ключ
     * - Middle: частотный массив как ключ (оптимизация)
     * - Senior: обработка unicode, производительность
     * <p>
     * Подводные камни:
     * - Неэффективная генерация ключа
     * - Не учесть пустые строки
     * <p>
     * Follow-up вопросы:
     * 1. Как оптимизировать для очень длинных строк?
     * 2. Что если строки на разных языках?
     */
    public static java.util.List<java.util.List<String>> groupAnagrams(String[] strs) {
        // TODO: implement
        return null;
    }

    /**
     * Задача 13: Проверка валидности скобок
     * <p>
     * Уровень: 3
     * Время: 15 минут
     * Паттерн: Stack
     * <p>
     * Проверить правильность расстановки скобок в строке.
     * Виды скобок: (), {}, []
     * <p>
     * Примеры:
     * Input: "()"
     * Output: true
     * <p>
     * Input: "()[]{}"
     * Output: true
     * <p>
     * Input: "(]"
     * Output: false
     * <p>
     * Input: "([)]"
     * Output: false
     * <p>
     * Input: "{[]}"
     * Output: true
     * <p>
     * Edge cases:
     * - Пустая строка (true)
     * - Только открывающие скобки
     * - Только закрывающие скобки
     * - Неправильный порядок
     * <p>
     * Критерии оценки:
     * - Junior: использование Stack
     * - Middle: обработка всех edge cases
     * - Senior: оптимизация памяти, clean code
     * <p>
     * Подводные камни:
     * - Не проверить на пустой стек при pop
     * - Забыть проверить остаток в стеке в конце
     * <p>
     * Follow-up вопросы:
     * 1. Как адаптировать для любых парных символов?
     * 2. Можно ли обойтись без Stack?
     * 3. Как найти позицию первой ошибки?
     */
    public static boolean isValidParentheses(String s) {
        //char[] vowels


        return false;
    }


}
