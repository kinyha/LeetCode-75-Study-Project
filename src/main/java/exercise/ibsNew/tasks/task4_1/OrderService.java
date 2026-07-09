package exercise.ibsNew.tasks.task4_1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public void cancelOrder(Long id) {
        System.out.println("Order " + id + " cancelled");
    }
}
