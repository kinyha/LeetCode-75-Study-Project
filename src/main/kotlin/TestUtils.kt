package utils

import java.util.*

/**
 * Утилитный класс для тестирования решений LeetCode
 */

class TestUtils {
    companion object {
        /**
         * Создает связный список из массива
         * Полезно для задач с LinkedList
         */
        fun createLinkedList(arr: IntArray): ListNode? {
            if (arr.isEmpty()) return null
            val dummy = ListNode(0)
            var current = dummy
            for (num in arr) {
                current.next = ListNode(num)
                current = current.next!!
            }
            return dummy.next
        }

        /**
         * Конвертирует связный список в массив для проверки
         */
        fun linkedListToArray(head: ListNode?): IntArray {
            val result = mutableListOf<Int>()
            var current = head
            while (current != null) {
                result.add(current.value)
                current = current.next
            }
            return result.toIntArray()
        }

        /**
         * Создает бинарное дерево из массива (уровневый обход)
         * Полезно для задач с деревьями
         */
        fun createBinaryTree(arr: Array<Int?>): TreeNode? {
            if (arr.isEmpty() || arr[0] == null) return null

            val root = TreeNode(arr[0]!!)
            val queue: Queue<TreeNode> = LinkedList()
            queue.offer(root)

            var i = 1
            while (queue.isNotEmpty() && i < arr.size) {
                val node = queue.poll()

                // Левый потомок
                if (i < arr.size && arr[i] != null) {
                    node.left = TreeNode(arr[i]!!)
                    queue.offer(node.left)
                }
                i++

                // Правый потомок
                if (i < arr.size && arr[i] != null) {
                    node.right = TreeNode(arr[i]!!)
                    queue.offer(node.right)
                }
                i++
            }
            return root
        }

        /**
         * Конвертирует бинарное дерево в массив (уровневый обход)
         */
        fun binaryTreeToArray(root: TreeNode?): Array<Int?> {
            if (root == null) return arrayOf()

            val result = mutableListOf<Int?>()
            val queue: Queue<TreeNode?> = LinkedList()
            queue.offer(root)

            while (queue.isNotEmpty()) {
                val node = queue.poll()
                if (node == null) {
                    result.add(null)
                    continue
                }
                result.add(node.value)
                queue.offer(node.left)
                queue.offer(node.right)
            }

            // Убираем null в конце массива
            while (result.isNotEmpty() && result.last() == null) {
                result.removeAt(result.lastIndex)
            }

            return result.toTypedArray()
        }

        /**
         * Проверяет, равны ли два массива с учетом порядка элементов
         */
        fun assertArrayEquals(expected: IntArray, actual: IntArray): Boolean {
            return expected.contentEquals(actual)
        }

        /**
         * Проверяет, содержат ли два массива одинаковые элементы (без учета порядка)
         */
        fun assertArrayEqualsUnordered(expected: IntArray, actual: IntArray): Boolean {
            return expected.sorted() == actual.sorted()
        }

        /**
         * Проверяет, равны ли две матрицы
         */
        fun assertMatrixEquals(expected: Array<IntArray>, actual: Array<IntArray>): Boolean {
            if (expected.size != actual.size) return false
            for (i in expected.indices) {
                if (!expected[i].contentEquals(actual[i])) return false
            }
            return true
        }

        /**
         * Генерирует случайный массив заданного размера
         */
        fun generateRandomArray(size: Int, minValue: Int = 0, maxValue: Int = 100): IntArray {
            return IntArray(size) { Random().nextInt(maxValue - minValue + 1) + minValue }
        }

        /**
         * Генерирует отсортированный массив заданного размера
         */
        fun generateSortedArray(size: Int, minValue: Int = 0, maxValue: Int = 100): IntArray {
            return generateRandomArray(size, minValue, maxValue).sorted().toIntArray()
        }

        /**
         * Измеряет время выполнения функции
         */
        fun measureExecutionTime(action: () -> Unit): Long {
            val startTime = System.nanoTime()
            action()
            val endTime = System.nanoTime()
            return (endTime - startTime) / 1_000_000 // Конвертируем в миллисекунды
        }
    }
}

/**
 * Вспомогательный класс для представления узла связного списка
 */
data class ListNode(
    var value: Int,
    var next: ListNode? = null
)

/**
 * Вспомогательный класс для представления узла бинарного дерева
 */
data class TreeNode(
    var value: Int,
    var left: TreeNode? = null,
    var right: TreeNode? = null
)