package exercise.ibsNew.tasks.task4_2;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).get();

        if (newStatus.equals("COMPLETED")) {
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            notificationService.notifyUser(
                    order.getUserId(),
                    "Your order is completed"
            );
        } else if (newStatus.equals("CANCELLED")) {
            order.setStatus("CANCELLED");
            order.setReason("CANCELLED reason");
            orderRepository.save(order);
            notificationService.notifyUser(
                    order.getUserId(),
                    "Your order is cancelled"
            );
        } else if (newStatus.equals("PENDING")) {
            order.setStatus("PENDING");
            orderRepository.save(order);
        } else if (newStatus.equals("IN_PROGRESS")) {
            order.setStatus("IN_PROGRESS");
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Unsupported status: " + newStatus);
        }
    }
}
