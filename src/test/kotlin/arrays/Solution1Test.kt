package arrays

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Solution1Test {

    @Test
    fun testMergeAlternately() {
        val solution = Solution1()

        assertEquals("apbqcr", solution.mergeAlternately("abc", "pqr"))
        assertEquals("apbqrs", solution.mergeAlternately("ab", "pqrs"))
        assertEquals("a", solution.mergeAlternately("a", ""))
        assertEquals("", solution.mergeAlternately("", ""))
    }
}
