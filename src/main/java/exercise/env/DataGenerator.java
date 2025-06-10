package exercise.env;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataGenerator {
    private static final Random random = new Random();
    private static final ThreadLocalRandom tlr = ThreadLocalRandom.current();

    // English names
    private static final String[] FIRST_NAMES = {
            "James", "John", "Robert", "Michael", "William", "David", "Richard",
            "Joseph", "Thomas", "Daniel", "Mary", "Patricia", "Jennifer", "Linda",
            "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen", "Nancy",
            "Lisa", "Betty", "Helen", "Sandra", "Margaret", "Ashley", "Kimberly"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Anderson", "Taylor", "Thomas", "Moore",
            "Jackson", "Martin", "Lee", "Thompson", "White", "Harris", "Clark"
    };

    private static final String[] DEPARTMENTS = {
            "Engineering", "Sales", "Marketing", "HR", "Finance", "Operations",
            "Customer Support", "Product", "Legal", "R&D"
    };

    private static final String[] POSITIONS = {
            "Junior Developer", "Software Engineer", "Senior Engineer", "Tech Lead",
            "Engineering Manager", "Product Manager", "Sales Representative",
            "Marketing Specialist", "HR Manager", "Financial Analyst", "DevOps Engineer"
    };

    private static final String[] CITIES = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
            "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
            "Austin", "Seattle", "Denver", "Boston", "Miami"
    };

    private static final String[] CATEGORIES = {
            "Groceries", "Transportation", "Entertainment", "Clothing", "Electronics",
            "Restaurants", "Healthcare", "Education", "Utilities", "Home & Garden",
            "Sports", "Travel", "Insurance", "Software", "Books"
    };

    private static final String[] PRODUCTS = {
            "Laptop", "Smartphone", "Headphones", "Monitor", "Keyboard", "Mouse",
            "Tablet", "Smartwatch", "Camera", "Printer", "Router", "SSD Drive",
            "Webcam", "Microphone", "Chair", "Desk", "Backpack", "Power Bank"
    };

    // Generate employees with realistic salaries
    public static List<Employee> generateEmployees(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String dept = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
                    String position = POSITIONS[random.nextInt(POSITIONS.length)];
                    double baseSalary = getBaseSalaryUSD(position);
                    // Add 0-40% variation
                    double salary = baseSalary * (1 + random.nextDouble() * 0.4);

                    // Hire date: 0-7 years ago
                    LocalDate hireDate = LocalDate.now()
                            .minusDays(random.nextInt(365 * 7));

                    return new Employee(
                            (long) i,
                            FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " +
                                    LAST_NAMES[random.nextInt(LAST_NAMES.length)],
                            dept,
                            position,
                            roundToTwoDecimals(salary),
                            hireDate,
                            CITIES[random.nextInt(CITIES.length)]
                    );
                })
                .collect(Collectors.toList());
    }

    // Generate customers
    public static List<Customer> generateCustomers(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                    String email = firstName.toLowerCase() + "." + lastName.toLowerCase() +
                            i + "@email.com";

                    // Registration: 0-4 years ago
                    LocalDate regDate = LocalDate.now()
                            .minusDays(random.nextInt(365 * 4));

                    // Distribution: 60% Regular, 30% Premium, 10% VIP
                    CustomerType type;
                    double rand = random.nextDouble();
                    if (rand < 0.6) type = CustomerType.REGULAR;
                    else if (rand < 0.9) type = CustomerType.PREMIUM;
                    else type = CustomerType.VIP;

                    boolean isActive = random.nextDouble() > 0.15; // 85% active

                    return new Customer(
                            (long) i,
                            firstName + " " + lastName,
                            email,
                            regDate,
                            CITIES[random.nextInt(CITIES.length)],
                            type,
                            isActive
                    );
                })
                .collect(Collectors.toList());
    }

    // Generate transactions with realistic amounts
    public static List<Transaction> generateTransactions(int count, int customerCount) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    TransactionType type = TransactionType.values()[random.nextInt(4)];

                    // Realistic amounts based on type
                    double amount = switch (type) {
                        case PAYMENT -> 10 + random.nextDouble() * 490;      // $10-500
                        case WITHDRAWAL -> 20 + random.nextDouble() * 480;   // $20-500
                        case TRANSFER -> 50 + random.nextDouble() * 1950;    // $50-2000
                        case REFUND -> 5 + random.nextDouble() * 195;        // $5-200
                    };

                    // Date: 0-120 days ago
                    LocalDateTime dateTime = LocalDateTime.now()
                            .minusMinutes(random.nextInt(60 * 24 * 480));

                    // 92% completed, 3% pending, 3% failed, 2% cancelled
                    TransactionStatus status;
                    double rand = random.nextDouble();
                    if (rand < 0.88) status = TransactionStatus.COMPLETED;
                    else if (rand < 0.95) status = TransactionStatus.PENDING;
                    else if (rand < 0.98) status = TransactionStatus.FAILED;
                    else status = TransactionStatus.CANCELLED;

                    return new Transaction(
                            (long) i,
                            (long) (1 + random.nextInt(customerCount)),
                            roundToTwoDecimals(amount),
                            type,
                            status,
                            dateTime,
                            CATEGORIES[random.nextInt(CATEGORIES.length)]
                    );
                })
                .collect(Collectors.toList());
    }

    // Generate orders with realistic prices
    public static List<Order> generateOrders(int count, int customerCount) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    // 1-4 items per order (weighted: more likely to have fewer items)
                    int itemCount = random.nextDouble() < 0.7 ?
                            1 + random.nextInt(2) : 3 + random.nextInt(2);

                    List<OrderItem> items = IntStream.range(0, itemCount)
                            .mapToObj(j -> {
                                String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
                                int quantity = 1 + random.nextInt(3); // 1-3 items
                                double price = getProductPrice(product);

                                return new OrderItem(
                                        (long) (j + 1),
                                        product,
                                        quantity,
                                        roundToTwoDecimals(price)
                                );
                            })
                            .collect(Collectors.toList());

                    // Order date: 0-6 months ago
                    LocalDateTime orderDate = LocalDateTime.now()
                            .minusDays(random.nextInt(180));

                    // Status distribution
                    OrderStatus status;
                    double rand = random.nextDouble();
                    if (rand < 0.7) status = OrderStatus.DELIVERED;
                    else if (rand < 0.85) status = OrderStatus.SHIPPED;
                    else if (rand < 0.93) status = OrderStatus.PROCESSING;
                    else if (rand < 0.98) status = OrderStatus.NEW;
                    else status = OrderStatus.CANCELLED;

                    return new Order(
                            (long) i,
                            (long) (1 + random.nextInt(customerCount)),
                            items,
                            orderDate,
                            status
                    );
                })
                .collect(Collectors.toList());
    }

    // Realistic base salaries in USD
    private static double getBaseSalaryUSD(String position) {
        return switch (position) {
            case "Junior Developer" -> 65000;
            case "Software Engineer" -> 95000;
            case "Senior Engineer" -> 135000;
            case "Tech Lead" -> 155000;
            case "Engineering Manager" -> 175000;
            case "Product Manager" -> 145000;
            case "Sales Representative" -> 75000;
            case "Marketing Specialist" -> 85000;
            case "HR Manager" -> 105000;
            case "Financial Analyst" -> 95000;
            case "DevOps Engineer" -> 125000;
            default -> 80000;
        };
    }

    // Realistic product prices
    private static double getProductPrice(String product) {
        double basePrice = switch (product) {
            case "Laptop" -> 800 + random.nextDouble() * 1700;        // $800-2500
            case "Smartphone" -> 300 + random.nextDouble() * 900;     // $300-1200
            case "Headphones" -> 50 + random.nextDouble() * 350;      // $50-400
            case "Monitor" -> 200 + random.nextDouble() * 600;        // $200-800
            case "Keyboard" -> 30 + random.nextDouble() * 170;        // $30-200
            case "Mouse" -> 20 + random.nextDouble() * 130;           // $20-150
            case "Tablet" -> 200 + random.nextDouble() * 800;         // $200-1000
            case "Smartwatch" -> 150 + random.nextDouble() * 450;     // $150-600
            case "Camera" -> 400 + random.nextDouble() * 2100;        // $400-2500
            case "Printer" -> 100 + random.nextDouble() * 400;        // $100-500
            case "Router" -> 50 + random.nextDouble() * 250;          // $50-300
            case "SSD Drive" -> 50 + random.nextDouble() * 250;       // $50-300
            case "Webcam" -> 30 + random.nextDouble() * 170;          // $30-200
            case "Microphone" -> 50 + random.nextDouble() * 250;      // $50-300
            case "Chair" -> 150 + random.nextDouble() * 850;          // $150-1000
            case "Desk" -> 200 + random.nextDouble() * 800;           // $200-1000
            case "Backpack" -> 30 + random.nextDouble() * 120;        // $30-150
            case "Power Bank" -> 20 + random.nextDouble() * 80;       // $20-100
            default -> 50 + random.nextDouble() * 200;
        };
        return basePrice;
    }

    // Round money to 2 decimal places
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}