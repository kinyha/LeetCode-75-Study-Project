package exercise.ibsNew.tasks.task4_2;

import java.util.Optional;

interface OrderRepository {
    Optional<Order> findById(Long orderId);

    Order save(Order order);
}
