package exercise.justCode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Code {
    public static void main(String[] args) {

        System.out.println("Run tests: .\\gradlew.bat test --tests \"exercise.justCode.CodeTest\"");
        System.out.println(reverseWords("text мой"));
    }

    static String reverseWords(String text) {
        var s = text.toCharArray();

        for (int i = 0; i < s.length / 2; i++) {
            var temp = s[s.length - 1 - i];
            s[s.length - 1 - i] = s[i];
            s[i] = temp;
        }
        return new String(s);
    }

    static Optional<Character> firstNonRepeatingChar(String text) {
        throw new UnsupportedOperationException("TODO: implement firstNonRepeatingChar");
    }

    static int[] twoSum(int[] nums, int target) {
        throw new UnsupportedOperationException("TODO: implement twoSum");
    }

    static Map<Character, List<String>> groupWordsByFirstLetter(List<String> words) {
        throw new UnsupportedOperationException("TODO: implement groupWordsByFirstLetter");
    }

    static List<String> topKFrequent(List<String> words, int k) {
        throw new UnsupportedOperationException("TODO: implement topKFrequent");
    }
}
