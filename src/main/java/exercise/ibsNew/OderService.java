import exercise.streamExercise.data_v1.Order;
import exercise.streamExercise.data_v1.OrderStatus;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transcational
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFountException(orderId))
        switch(order.getStatus()) {
            case OrderStatus.CANCELLED -> {
                order.setStatus("COMPLETED");
                orderRepository.save(order);
                notificationService.notifyUser(
                        order.getUserId(),
                        "Your order is completed"
                );
            }
        }
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
//enum OrderStatus {
//    "COMPLETED","CANCELLED","IN_PROGRESS"
//}
