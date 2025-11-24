package exercise.yandex.algo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Task1 {
    void main() {

        int[] arr = new int[]{1, 2, 3, 4, 5, 6};
        int target = 5;

//         System.out.println(Arrays.toString(findIndex(arr, target)));
        System.out.println(Arrays.toString(reverseArr(arr)));
        System.out.println(reverseStr("Hello world"));

        System.out.println(isPallindrome("Maam"));
    }

    //arrr = [1,2,3,4,5] target = 8 -> {2,4}

    public int[] findIndex(int[] arr, int target) {
        Map<Integer, Integer> map = new HashMap<>();

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

    //reverse arr {1,2,3,4,5,6,}

    public int[] reverseArr(int[] arr) {
        int left = 0;
        int right = arr.length - 1;

        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;

            left++;
            right--;
        }
        return arr;
    }

    //string reverse str = "Hello world"
    public String reverseStr(String str) {
        char[] st = str.toCharArray();
        int left = 0;
        int right = str.length() - 1;

        while (left < right) {
            char temp = st[left];
            st[left] = st[right];
            st[right] = temp;

            left++;
            right--;
        }
        return new String(st);
    }

    // is strPalindro
    public Boolean isPallindrome(String str) {
        int left = 0, right = str.length() - 1;
        char[] chars = str.toCharArray();

        while (left < right) {
            if (Character.toLowerCase(chars[left]) != Character.toLowerCase(chars[right])) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
