package exercise.diff;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

public class Check {
    public static void main(String[] args) throws Exception {
        var employee = new Employee("Петров", 100000);

        // 1. Получение информации о record
        Class<?> clazz = employee.getClass();
        System.out.println("Is record: " + clazz.isRecord()); // true

        // 2. Получение компонентов record
        RecordComponent[] components = clazz.getRecordComponents();
        for (RecordComponent component : components) {
            System.out.println("Component: " + component.getName() +
                    ", Type: " + component.getType());
        }

        // 3. Доступ к полям через рефлексию
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true); // Нужно для доступа к private полю
        String name = (String) nameField.get(employee);
        System.out.println("Name via reflection: " + name);

        // 4. Попытка изменить поле - ВАЖНО!
        try {
            nameField.set(employee, "Иванов"); // Выбросит исключение!
        } catch (IllegalAccessException e) {
            System.out.println("Ошибка: " + e.getMessage());
            // Поля final, изменить нельзя!
        }

        // 5. Вызов методов через рефлексию
        Method nameMethod = clazz.getMethod("name");
        String nameViaMethod = (String) nameMethod.invoke(employee);
        System.out.println("Name via method: " + nameViaMethod);

    }
}
record Employee(String name, int salary) implements Person {}
interface  Person {}