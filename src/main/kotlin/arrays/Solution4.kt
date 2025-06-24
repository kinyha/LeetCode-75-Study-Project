package arrays

//**
// * 605. Can Place Flowers
// * Difficulty: Easy
//Example 1:
//
//Input: flowerbed = [1,0,0,0,1], n = 1
//Output: true
//Example 2:
//
//Input: flowerbed = [1,0,0,0,1], n = 2
//Output: false
// */



//Оптимищировать дичь
fun canPlaceFlowers(flowerbed: IntArray, n: Int): Boolean {
    if (n == 0) return true
    print(flowerbed.size)
    if (flowerbed.size == 1) {
        if (flowerbed[0] == 1) return false else return true
    }
    var res = 0
    for (i in 0..<flowerbed.size) {
        when(i) {
            0 -> {
                if (flowerbed[i] == 0 && flowerbed[i + 1] == 0) {
                    res++
                    flowerbed[i] = 1
                }
            }
            flowerbed.size - 1 -> {
                if (flowerbed[i] == 0 && flowerbed[i - 1] == 0) {
                    res++
                    flowerbed[i] = 1
                }
            }
            else -> {
                if (flowerbed[i - 1] == 0 && flowerbed[i] == 0 && flowerbed[i + 1] == 0) {
                    res++
                    flowerbed[i] = 1
                }
            }
        }

    }
    return n <= res
}