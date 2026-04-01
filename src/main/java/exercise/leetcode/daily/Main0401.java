package exercise.leetcode.daily;

import java.util.List;

/*
2751. Robot Collisions
Hard
Topics: Array, Stack, Sorting, Simulation

There are n 1-indexed robots, each having a position on a line, health, and movement direction.

You are given 0-indexed integer arrays positions, healths, and a string directions
(directions[i] is either 'L' for left or 'R' for right). All integers in positions are unique.

All robots start moving on the line simultaneously at the same speed in their given directions.
If two robots ever share the same position while moving, they will collide.

If two robots collide, the robot with lower health is removed from the line, and the health of
the other robot decreases by one. The surviving robot continues in the same direction it was going.
If both robots have the same health, they are both removed from the line.

Your task is to determine the health of the robots that survive the collisions, in the same order
that the robots were given.

Example 1:
Input: positions = [5,4,3,2,1], healths = [2,17,9,15,10], directions = "RRRRR"
Output: [2,17,9,15,10]

Example 2:
Input: positions = [3,5,2,6], healths = [10,10,15,12], directions = "RLRL"
Output: [14]

Example 3:
Input: positions = [1,2,5,6], healths = [10,10,11,11], directions = "RLRL"
Output: []

Constraints:
1 <= positions.length == healths.length == directions.length == n <= 10^5
1 <= positions[i], healths[i] <= 10^9
directions[i] == 'L' or directions[i] == 'R'
All values in positions are distinct
*/

public class Main0401 {
    public static void main(String[] args) {
        Main0401 solution = new Main0401();

        // Test 1
        System.out.println(solution.survivedRobotsHealths(
                new int[]{5, 4, 3, 2, 1},
                new int[]{2, 17, 9, 15, 10},
                "RRRRR"
        )); // Expected: [2, 17, 9, 15, 10]

        // Test 2
        System.out.println(solution.survivedRobotsHealths(
                new int[]{3, 5, 2, 6},
                new int[]{10, 10, 15, 12},
                "RLRL"
        )); // Expected: [14]

        // Test 3
        System.out.println(solution.survivedRobotsHealths(
                new int[]{1, 2, 5, 6},
                new int[]{10, 10, 11, 11},
                "RLRL"
        )); // Expected: []
    }

    public List<Integer> survivedRobotsHealths(int[] positions, int[] healths, String directions) {
        // TODO: implement
        return null;
    }
}
