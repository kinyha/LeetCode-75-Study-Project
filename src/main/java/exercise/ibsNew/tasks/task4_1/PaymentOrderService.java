package exercise.ibsNew.tasks.task4_1;

import org.springframework.stereotype.Service;

@Service
public class PaymentOrderService {
    private final OrderService orderService;
    private final PaymentService paymentService;


    public PaymentOrderService(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public void placeOrder(Order order) {
        paymentService.charge(order);
    }

    public void onPaymentFailed(Long orderId) {
        orderService.cancelOrder(orderId);
    }

}


