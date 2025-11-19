package exercise.yandex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task1 {
     void main() {

        int[] arr = new int[]{1,2,3,4,5,6};
        int target = 5;

         System.out.println(Arrays.toString(findIndex(arr, target)));
    }

    //arrr = [1,2,3,4,5] target = 8 -> {2,4}

     public int[] findIndex(int[] arr, int target) {
        Map<Integer,Integer> map = new HashMap<>();

        for (int i = 0; i < arr.length; i++) {
            map.put(arr[i], i);
        }
         System.out.println(map);
         for (int i = 0; i < arr.length; i++) {
             int complement = target - arr[i];

             if (map.containsKey(complement)) {
                 return new int[]{i, map.get(complement)};
             }
         }

        return new int[]{-1};
    }
}
