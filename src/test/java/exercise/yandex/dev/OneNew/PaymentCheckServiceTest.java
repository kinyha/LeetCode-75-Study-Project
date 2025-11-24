package exercise.yandex.dev.OneNew;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Мои тесты")
class PaymentCheckServiceTest {

    private Map<String, List<Payment>> paymentsHistory;
    private Map<String, User> users;


    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private PaymentCheckService service;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        paymentsHistory = new HashMap<>();
        users = new HashMap<>();

        // Настраиваем моки
        when(paymentRepository.getPayments()).thenReturn(paymentsHistory);
        when(userRepository.getUsers()).thenAnswer(invocation -> new
                ArrayList<>(users.values()));
        service = new PaymentCheckService(paymentRepository, userRepository);
    }

    private void setupUser(BigDecimal dayLimit, BigDecimal txLimit) {
        users.put(USER_ID, new User(USER_ID, new UserLimits(dayLimit, txLimit)));
    }

    private void setUpPayment(BigDecimal amount) {
        Payment payment = new Payment(USER_ID, amount, LocalDateTime.now().minusHours(1));
        paymentsHistory.computeIfAbsent(USER_ID, k -> new ArrayList<>()).add(payment);
    }


    @Test
    @DisplayName("Платёж проходит при соблюдении всех лимитов")
    void shouldApprove_whenAllLimitsOk() {
        //given
        setupUser(new BigDecimal(1000), new BigDecimal(300));
        setUpPayment(new BigDecimal(100));
        Payment payment = new Payment(USER_ID, new BigDecimal(300), LocalDateTime.now());
        //when
        var result = service.check(USER_ID, payment);
        //then
        assertTrue(result.access());

    }
}
