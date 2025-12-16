package exercise.yandex.dev.tasks.paymentLimit;

import java.util.List;
import java.util.Map;

public interface PaymentRepository {
    Map<String, List<Payment>> getPayments();
}
