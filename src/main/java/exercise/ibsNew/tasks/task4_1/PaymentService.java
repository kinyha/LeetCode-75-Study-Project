package exercise.ibsNew.tasks.task4_1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public void charge(Order order) {
        System.out.println("Charging order " + order.getId());
    }

}
