package exercise.alls;

public class MaiTh{
    public static void main(String[] args) {
        MyThred thred = new MyThred();

        thred.start();
    }
}

class MyThred extends Thread {

    @Override
    public void run() {
        String name = "11";
        System.out.println("qwe - name}");
    }
}
