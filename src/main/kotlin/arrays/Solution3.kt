package arrays

import java.util.LinkedList

/**
 * 1431. Kids With the greates number of candis
 * Difficulty: Easy
 */

fun kidsWithCandies(candies: IntArray, extraCandies: Int): List<Boolean> {
    val maxCandies = candies.max()
    val result: MutableList<Boolean> = mutableListOf()
    for (candi in candies) {
        if (candi + extraCandies >= maxCandies) result.add(true) else result.add(false)
    }
    return result
}