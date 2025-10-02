package exercise.concurency.theori;

public class Zed {
    static void main() throws InterruptedException {
        String st1 = "qwe";
        String st2 = "qweewq";
        String st3 = "qwewewqqwewewqqwewewqqwewewq";
        String st4 = "q";


        System.out.println(isPallindrome(st1));
        System.out.println(isPallindrome(st2));
        System.out.println(isPallindrome(st3));
        System.out.println(isPallindrome(st4));
    }



    static boolean isPallindrome(String line) {
        if (line.isBlank() || line.length() == 1)

        for (int i = 0; i < line.length() / 2; i++) {
            if (line.charAt(i) != line.charAt(line.length() -1  - i)) {
                return false;
            }
        }

        return true;
    }
}






class Worker extends Thread {
    @Override
    public void run() {
        System.out.println("Do works ...");
        System.out.println(Thread.currentThread().getName());
    }
}


//is palindrome
