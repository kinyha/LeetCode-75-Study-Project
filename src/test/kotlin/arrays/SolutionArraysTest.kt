package arrays

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolutionArraysTest {

    @Test
    fun testMergeAlternately() {
        val solution = Solution1()

        assertEquals("apbqcr", solution.mergeAlternately("abc", "pqr"))
        assertEquals("apbqrs", solution.mergeAlternately("ab", "pqrs"))
        assertEquals("a", solution.mergeAlternately("a", ""))
        assertEquals("", solution.mergeAlternately("", ""))
    }

    @Test
    fun testGcdOfStrings() {
        assertEquals("ABC", gcdOfStrings("ABCABC", "ABC"))
        assertEquals("AB", gcdOfStrings("ABABAB", "ABAB"))
        assertEquals("", gcdOfStrings("LEET", "CODE"))
        assertEquals("", gcdOfStrings("", "ABC"))
        assertEquals("A", gcdOfStrings("AAA", "A"))
    }

    @Test
    fun testKidsWithCandies() {

        assertEquals(listOf(true, true, true, false, true), kidsWithCandies(intArrayOf(2,3,5,1,3), 3))
        assertEquals(listOf(true,false,false,false,false), kidsWithCandies(intArrayOf(4,2,1,1,2), 1))
        assertEquals(listOf(true,false,true), kidsWithCandies(intArrayOf(12,1,12), 10))

    }
}
