package exercise;

import exercise.env.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tasks {
    public static void main(String[] args) {
        List<Employee> employees = StreamTasksData.employees;
        List<Customer> customers = StreamTasksData.customers;
        List<Transaction> transactions = StreamTasksData.transactions;
        List<Order> orders = StreamTasksData.orders;
        StreamTasksData.printSampleData();

        //1
//        findEmplFrom(employees,"Москва").forEach(System.out::println);
//        getActiveCustomerNames(customers).forEach(System.out::println);
//        System.out.println(countCompletedTransactions(transactions));
//        System.out.println(findMaxSalary(employees));
//        System.out.println(hasVipCustomerInCity(customers,"Boston"));
        //2
//        orders.forEach(System.out::println);
//        getHighPaidItEmployees(employees).forEach(System.out::println);
//        getRecentTransactionCategories(transactions,30).forEach(System.out::println);
//        getTopExpensiveOrders(orders,3).forEach(System.out::println);
//        createEmailToCustomerMap(customers).forEach((key, value) -> System.out.println(key + " -> " + value));
//        System.out.println(calculateCustomerPayments(transactions,1L));
        //3
//        countEmployeesByDepartment(employees).forEach((key,value) -> System.out.println(key + " -> " + value));
//        getAvgSalaryByCitySorted(employees).forEach((key, value) -> System.out.println(key + " -> " + Math.round(value) + "$"));
//        getAllUniqueProductsSorted(orders).forEach(System.out::println);
//        groupTransactionsByCustomerTypeAndStatus(transactions,customers).forEach((key, value) -> System.out.println(key + " -> " + value));
//        System.out.println(findExperiencedItEmployeeAboveAvg(employees).get());
        //4
        //getMonthlyTop3Customers(transactions, customers);
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
                .collect(Collectors.toMap(Customer::getEmail, Function.identity()));
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
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
    }

    // 3.2 Average salary by city, sorted by salary DESC
    public static LinkedHashMap<String, Double> getAvgSalaryByCitySorted(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCity,
                        Collectors.averagingDouble(Employee::getSalary)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // 3.3 Get all unique product names from all orders, sorted
    public static List<String> getAllUniqueProductsSorted(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getProductName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // 3.4 Group transactions by customer type and status, sum amounts
    public static Map<CustomerType, Map<TransactionStatus, Double>> groupTransactionsByCustomerTypeAndStatus(
            List<Transaction> transactions, List<Customer> customers) {
//        var a = customers.stream()
//                        .collect(Collectors.toMap(
//                                //Customer::getType,
//                                Customer::getId,
//                                customer -> transactions.stream()
//                                        .filter(transaction -> transaction.getCustomerId().equals(customer.getId()))
//                                        .collect(Collectors.groupingBy(
//                                                Transaction::getStatus,
//                                                Collectors.summingDouble(Transaction::getAmount)
//                                        ))
//                                        //.toList()
//                                , (old,wen) -> old
//
//                        ));
//
//        a.forEach((key, value) -> System.out.println(key + " -> " + value));

        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));

        return transactions.stream()
                .filter(t -> customerMap.containsKey(t.getCustomerId()))
                .collect(Collectors.groupingBy(
                        t -> customerMap.get(t.getCustomerId()).getType(),
                        Collectors.groupingBy(
                                Transaction::getStatus,
                                Collectors.summingDouble(Transaction::getAmount)
                        )
                ));
    }

    // 3.5 Find first IT employee: 2+ years experience and salary > dept average
    public static Optional<Employee> findExperiencedItEmployeeAboveAvg(List<Employee> employees) {

        return employees.stream()
                .filter(e -> e.getDepartment().equals("Engineering"))
                .filter(e -> LocalDateTime.now().minusYears(e.getHireDate().getYear()).getYear() > 2)
                .filter(e -> e.getSalary() > employees.stream()
                        .filter(employee -> employee.getDepartment().equals("Engineering"))
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0))
                .findFirst();
    }

    // ============ LEVEL 4 (Middle+/Senior) ============

    // 4.1 Top 3 customers by transaction sum for each month
    public static Map<YearMonth, List<CustomerSummary>> getMonthlyTop3Customers(
            List<Transaction> transactions, List<Customer> customers) {

        //Map<YearMonth, Map<Long,Double>
        var monthlyAmounts = transactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> YearMonth.from(transaction.getDateTime()),

                        Collectors.groupingBy(
                                Transaction::getCustomerId,
                                Collectors.summingDouble(Transaction::getAmount)
                        )
                ));
        var customerNames = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));

        var top3Monthly = monthlyAmounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .map(e -> new CustomerSummary(
                                        e.getKey(),
                                        customerNames.get(e.getKey()),
                                        e.getValue()
                                ))
                                .sorted(Comparator.comparing(CustomerSummary::totalAmount).reversed())
                                .limit(3)
                                .toList()
                ));

//        var result = monthlyAmounts.entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> entry.getValue().entrySet().stream()
//                                .map(e -> new CustomerSummary(
//                                        e.getKey(),
//                                        customerNames.get(e.getKey()),
//                                        e.getValue())
//                                ).sorted((c1,c2) -> Double.compare(c2.totalAmount, c1.totalAmount))
//                                .limit(3)
//                                .toList()
//                ));
        //result.forEach((key, value) -> System.out.println(key + " -> " + value));
        top3Monthly.forEach((key, value) -> System.out.println(key + " -> " + value));
        return null;

    }

    // Helper class for 4.1
    record CustomerSummary(
            Long customerId,
            String name,
            Double totalAmount
    ) {
    }

    // 4.2 Product statistics using parallel streams
    public static Map<String, ProductStats> calculateProductStatisticsParallel(List<Order> orders) {
        return null;
    }

    // Helper class for 4.2
    static class ProductStats {
        long orderCount;
        long totalQuantity;
        DoubleSummaryStatistics priceStats;

        // Constructor, getters...
    }

    // 4.3 Custom median collector and median salary by department
    public static Map<String, Double> getMedianSalaryByDepartment(List<Employee> employees) {
        return null;
    }

    // 4.4 Customers with sum > 50000 in last 30 days and no cancelled transactions
    public static Set<Customer> findQualifiedCustomers(List<Transaction> transactions,
                                                       List<Customer> customers,
                                                       double minAmount) {
        return null;
    }

    // 4.5 Department salary deviation report (employees with 20%+ deviation from median)
    public static Map<String, DepartmentReport> createSalaryDeviationReport(List<Employee> employees) {
        return null;
    }

    // Helper classes for 4.5
    static class EmployeeDeviation {
        Employee employee;
        double deviationPercent;

        // Constructor, getters...
    }

    static class DepartmentReport {
        double medianSalary;
        List<EmployeeDeviation> aboveMedian;
        List<EmployeeDeviation> belowMedian;

        // Constructor, getters...
    }




}
