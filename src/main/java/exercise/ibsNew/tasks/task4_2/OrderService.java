package exercise.ibsNew.tasks.task4_2;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/*
* 1)fild inject
* 2)final
* 3)Enum
* 4)@transactional
* 5)Correct optional
* 6)CustomException
* 7) Сделал что-бы нотивай был только после сейф что-бы не откатило
* */
@Service
public class OrderService {
    //1 филд инджект + final
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    //@Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundOrderException(orderId));
        String message = "";
        switch (newStatus) {
            case COMPLETED -> {
                order.setStatus("COMPLETED");
                message = "Your order is completed";
            }
            case CANCELLED -> {
                order.setStatus("CANCELLED");
                order.setReason("CANCELLED reason");
                message = "Your order is canceled";
            }
            case PENDING -> {
                order.setStatus("PENDING");
            }
            case IN_PROGRESS -> {
                order.setStatus("IN_PROGRESS");
            }
            default -> throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }
        orderRepository.save(order);
        if (!message.isEmpty()) {
            notify(order, message);
        }
    }

    private void notify(Order order, String Your_order_is_completed) {
        notificationService.notifyUser(
                order.getUserId(),
                Your_order_is_completed
        );
    }
}
//enum OrderStatus {
//    "COMPLETED","CACELLED","IN_PROGRESS";
//}
