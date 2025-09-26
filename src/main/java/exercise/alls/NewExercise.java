package exercise.alls;

import java.util.HashMap;
import java.util.Map;

//Input: aaffbaaaaafcz
// Output: [a =4 ...

public class NewExercise {
    public static void main(String[] args) {
        String testString = "aaffbaaaaafcz";
        Map<Character, Integer> output = maxConsecutiveRepeats(testString);
        System.out.println(output);
    }


    static Map<Character, Integer> maxConsecutiveRepeats(String input) {
        char[] line = input.toCharArray();
        int counter = 0;
        Map<Character, Integer> result = new HashMap<>();

        for (int i = 0; i < line.length - 1; i++) {
            if (line[i] == line[i + 1]) {
                counter++;
            } else {
                counter++;
                if (result.get(line[i]) == null) {
                    result.putIfAbsent(line[i],counter);
                    counter = 0;
                } else {
                    if (result.get(line[i]) < counter) {
                        result.put(line[i],counter);
                        counter = 1;
                    }
                }
            }
        }
        result.put(line[line.length -1],1);

        // if  a,2 a,4   a valu
        return result;
    }

}

