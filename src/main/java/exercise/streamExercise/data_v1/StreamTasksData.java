package exercise.streamExercise.data_v1;

import java.util.List;

public class StreamTasksData {
    public static final List<Employee> employees = DataGenerator.generateEmployees(750);
    public static final List<Customer> customers = DataGenerator.generateCustomers(500);
    public static final List<Transaction> transactions = DataGenerator.generateTransactions(2000, 500);
    public static final List<Order> orders = DataGenerator.generateOrders(1000, 500);

    // Для удобства отладки
    public static void printSampleData() {
        System.out.println("=== Примеры данных ===");
        System.out.println("Сотрудники: " + employees.stream().limit(3).toList());
        System.out.println("Клиенты: " + customers.stream().limit(3).toList());
        System.out.println("Транзакции: " + transactions.stream().limit(3).toList());
        System.out.println("Заказы: " + orders.stream().limit(3).toList());
    }
}
