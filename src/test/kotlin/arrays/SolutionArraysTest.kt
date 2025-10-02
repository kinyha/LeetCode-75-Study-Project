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
    fun testCanPlaceFlowers() {
        assertEquals(true, canPlaceFlowers(intArrayOf(1, 0, 0, 0, 1), 1))
        assertEquals(false, canPlaceFlowers(intArrayOf(1, 0, 0, 0, 1), 2))
        assertEquals(true, canPlaceFlowers(intArrayOf(0), 1))
    }

    @Test
    fun testReverseVowels() {
        assertEquals("AceCreIm", reverseVowels("IceCreAm"))
    }


}
