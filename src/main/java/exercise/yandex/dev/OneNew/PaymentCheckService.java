package exercise.yandex.dev.OneNew;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentCheckService {
//    private Map<String, List<Payment>> paymentsHistory;
//    private Map<String, User> users; //repository

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentCheckService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public CheckResult check(String userId, Payment payment) {
        List<User> users = userRepository.getUsers();

        User user = users.stream().filter(u -> u.id().equals(userId)).findFirst().orElseThrow();

        List<Payment> paymentList = paymentRepository.getPayments().get(userId);

        var dayPayment = payment.timestamp().toLocalDate();
        if (paymentList == null) {
            paymentList = new ArrayList<>();
        }
        BigDecimal dayPaymentSum  = paymentList.stream()
                .filter(payment1 ->payment1.timestamp().toLocalDate().equals(dayPayment))
                .map(Payment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (user.limits().transactionalLimit().compareTo(payment.amount()) < 0) {
            return CheckResult.failure(String.format("Payment (%s) more than transactional limit (%s)",payment.amount(),user.limits().transactionalLimit()));
        }

        if (user.limits().dayLimit().compareTo(dayPaymentSum.add(payment.amount())) < 0) {
            return CheckResult.failure(String.format("Payment (%s) with daySum(%s)  more than day limit (%s)",payment.amount(),dayPaymentSum,user.limits().dayLimit()));
        }


        return CheckResult.accessed();
    }
}
