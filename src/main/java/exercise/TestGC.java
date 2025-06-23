package exercise;

import java.util.ArrayList;
import java.util.List;

public class TestGC {
    public static void main(String[] args) {
        // Включаем логирование GC
        // -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

        List<String> objects = new ArrayList<>();

        for (int i = 0; i < 10000000; i++) {
            // Создаем объекты -> заполняем Eden
            objects.add("Object " + i);

            // Каждые 100k объектов очищаем список
            if (i % 1000000 == 0) {
                objects.clear(); // Делаем объекты доступными для GC

                // Принудительно запускаем GC для демонстрации
                System.gc(); // НЕ используй в prod!

                System.out.printf("Iteration %d, Free memory: %d MB%n",
                        i, Runtime.getRuntime().freeMemory() / 1024 / 1024);
            }
        }
    }
}
