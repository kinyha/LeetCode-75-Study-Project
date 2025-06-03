package exercise;

import exercise.env.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static exercise.env.Employee.printEmployees;

public class Tasks {
    public static void main(String[] args) {
        List<Employee> employees = StreamTasksData.employees;
        List<Customer> customers = StreamTasksData.customers;
        List<Transaction> transactions = StreamTasksData.transactions;
        List<Order> orders = StreamTasksData.orders;

        //orders.forEach(System.out::println);

//        findEmplFrom(employees,"Москва").forEach(System.out::println);
//        getActiveCustomerNames(customers).forEach(System.out::println);
//        System.out.println(countCompletedTransactions(transactions));
//        System.out.println(findMaxSalary(employees));
//        System.out.println(hasVipCustomerInCity(customers,"Boston"));
//        StreamTasksData.printSampleData();

        //2
        //transactions.forEach(System.out::println);
        //getHighPaidItEmployees(employees).forEach(System.out::println);
        //getRecentTransactionCategories(transactions,30).forEach(System.out::println);
        //getTopExpensiveOrders(orders,3).forEach(System.out::println);
        createEmailToCustomerMap(customers).forEach((key, value) -> System.out.println(key + " -> " + value));
        System.out.println(calculateCustomerPayments(transactions,1L));
    }


    //1 find List<Employee> from Moscow
    public static List<Employee> findEmplFrom(List<Employee> employees, String city) {
        return employees.
                stream()
                .filter(employee -> employee.getCity().equals(city))
                .collect(Collectors.toList());
    }

    // 1.2 Get names of all active customers
    public static List<String> getActiveCustomerNames(List<Customer> customers) {
        // TODO: filter active customers and map to names

        return customers.stream()
                .filter(Customer::isActive)
                .map(Customer::getName)
                .toList();
    }

    // 1.3 Count completed transactions
    public static long countCompletedTransactions(List<Transaction> transactions) {
        // TODO: count transactions with status COMPLETED
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.COMPLETED)
                .count();
    }

    // 1.4 Find maximum salary
    public static Optional<Double> findMaxSalary(List<Employee> employees) {
        // TODO: find max salary or empty
        return employees.stream()
                .map(Employee::getSalary)
                .max(Double::compareTo)
                .or(Optional::empty);
    }

    // 1.5 Check if any VIP customer in specific city
    public static boolean hasVipCustomerInCity(List<Customer> customers, String city) {
        // TODO: check if exists VIP customer in city
        return customers.stream()
                .anyMatch(customer -> customer.getType() == CustomerType.VIP && customer.getCity().equals(city));
//        return customers.stream()
//                .filter(customer -> customer.getType() == CustomerType.VIP)
//                .filter(customer -> customer.getCity().equals(city))
//                .anyMatch(customer -> true);
//
    }

    // ============ LEVEL 2 (Junior+) ============

    // 2.1 Get IT employees with salary > 100000, sorted by salary DESC
    public static List<Employee> getHighPaidItEmployees(List<Employee> employees) {
        // TODO: filter IT dept, salary > 100000, sort DESC
        return employees.stream()
                .filter(employee -> employee.getDepartment().equals("Engineering") && employee.getSalary() > 10000)
                .sorted(Comparator.comparingDouble(Employee::getSalary)
                        .reversed())
                .collect(Collectors.toList());
    }

    // 2.2 Get unique categories from last 30 days transactions
    public static Set<String> getRecentTransactionCategories(List<Transaction> transactions, int days) {
        // TODO: filter by date, get unique categories
        return transactions.stream()
                .filter(transaction -> transaction.getDateTime().isAfter(LocalDateTime.now().minusDays(days)))
                .map(Transaction::getCategory)
                .collect(Collectors.toSet());
    }

    // 2.3 Find top N most expensive orders
    public static List<Order> getTopExpensiveOrders(List<Order> orders, int topN) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getTotalAmount)
                        .reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    // 2.4 Create map: email -> customer (only active)
    public static Map<String, Customer> createEmailToCustomerMap(List<Customer> customers) {
        return customers.stream()
                .filter(Customer::isActive)
                //.collect(Collectors.groupingBy(Customer::getEmail));
                .collect(Collectors.toMap(Customer::getEmail,Function.identity()));
    }

    // 2.5 Calculate total payment amount for specific customer
    public static double calculateCustomerPayments(List<Transaction> transactions, Long customerId) {
        return transactions
                .stream()
                .filter(transaction -> transaction.getCustomerId() == customerId)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // ============ LEVEL 3 (Middle) ============

    // 3.1 Count employees by department
    public static Map<String, Long> countEmployeesByDepartment(List<Employee> employees) {
        // TODO: group by department, count
        return null;
    }

    // 3.2 Average salary by city, sorted by salary DESC
    public static LinkedHashMap<String, Double> getAvgSalaryByCitySorted(List<Employee> employees) {
        // TODO: group by city, calculate avg, sort by value DESC
        return null;
    }

    // 3.3 Get all unique product names from all orders, sorted
    public static List<String> getAllUniqueProductsSorted(List<Order> orders) {
        // TODO: flatMap order items, distinct products, sort
        return null;
    }

    // 3.4 Group transactions by customer type and status, sum amounts
    public static Map<CustomerType, Map<TransactionStatus, Double>> groupTransactionsByCustomerTypeAndStatus(
            List<Transaction> transactions, List<Customer> customers) {
        // TODO: join with customers, group by type then status, sum
        return null;
    }

    // 3.5 Find first IT employee: 2+ years experience and salary > dept average
    public static Optional<Employee> findExperiencedItEmployeeAboveAvg(List<Employee> employees) {
        // TODO: calculate IT avg salary, filter by criteria, findFirst
        return Optional.empty();
    }

}
