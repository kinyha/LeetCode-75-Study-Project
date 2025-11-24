package exercise.yandex.dev.OneNew;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentRepInMemory implements PaymentRepository {
    @Override
    public Map<String, List<Payment>> getPayments() {
        return new HashMap<>();
    }
}
