package exercise.yandex.dev.OneNew;

import java.util.List;
import java.util.Map;

public interface PaymentRepository {
    Map<String, List<Payment>> getPayments();
}
