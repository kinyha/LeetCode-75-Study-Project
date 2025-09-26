package exercise.alls;

public class Main {
    Node root;

    public Node find(int value) {
        return null;
    }
    public static void main(String[] args) {
//        String input = "EExxampleee Teexxxtttttt!!!!!";
//        String expected = "Example Text!";
//        String deduplicated = deduplicate(input);
//        System.out.println(deduplicated);
//        System.out.println(expected.equals(deduplicated));

        System.out.println("qwe");


    }
}

//    private static String deduplicate(String s) {
//        char[] arrS = s.toCharArray();
//
//        for (int i = 0; i < arrS.length - 1; i++) {
//            if (arrS[i] == arrS[i+1]) {
//                arrS[i] = '~';
//            }
//        }
//        return new String(arrS).replaceAll("~","");
//    }
//}

class Node {
    int value;
    Node left;
    Node right;

    public Node(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}
