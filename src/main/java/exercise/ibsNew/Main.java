package exercise.ibsNew;

import exercise.ibsNew.tasks.task4_2.Order;
import exercise.ibsNew.tasks.task4_2.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

enum OrderStatus {UNPAID, PAID, PENDING, IN_PROGRESS, COMPLETED, CANCELLED}

public class Main {
    public static void main(String[] args) {
        System.out.println(123);
        //some fill
        List<OrderItem> items = List.of(
                new OrderItem(1L, BigDecimal.valueOf(10.0), 2),
                new OrderItem(2L, BigDecimal.valueOf(20.0), 11)
//                new OrderItem(3L, BigDecimal.valueOf(20.0), 1)
        );
        List<Order> orders = List.of(
                new Order(1L, 1L, OrderStatus.UNPAID, items),
                new Order(2L, 2L, OrderStatus.PAID, items),
                new Order(3L, 3L, OrderStatus.COMPLETED, items),
                new Order(4L, 4L, OrderStatus.CANCELLED, items),
                new Order(5L, 5L, OrderStatus.IN_PROGRESS, items)
        );
        Map<Long, Integer> stock = Map.of(
                1L, 10,
                2L, 5
        );

//        //2.1 groupByStatu
        var a = groupByStatus(orders);
        a.forEach((orderStatus, orders1) -> {
            System.out.println(orderStatus + "  n" + orders1);
            //orders1.forEach(System.out::println);
        });
        System.out.println(canFulFill(orders.get(1), stock));
    }

    static Map<OrderStatus, List<Order>> groupByStatus(List<Order> orders) {
        var a = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, TreeMap::new, Collectors.toList()));
        return a;
    }

    //2.2
    static boolean canFulFill(Order order, Map<Long, Integer> stock) {
        Map<Long, Integer> required = order.items.stream()
                .collect(Collectors.groupingBy(OrderItem::productId, Collectors.summingInt(OrderItem::quantity)));

        return required.entrySet().stream()
                .allMatch(reqq -> stock.getOrDefault(reqq.getKey(), 0) >= reqq.getValue());

    }

    //2.3
    /*
    * public interface StockRepository extends JpaRepository<Stck, Long> {
    *   List<Stck> findByProductId(Collection<Long> productsIds);
    * }
    *
    *
    *
    * */
}


