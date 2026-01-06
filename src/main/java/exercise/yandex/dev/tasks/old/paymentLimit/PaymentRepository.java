package exercise.yandex.dev.tasks.old.paymentLimit;

import java.util.List;
import java.util.Map;

public interface PaymentRepository {
    Map<String, List<Payment>> getPayments();
}
