//package exercise.yandex;
//
//
//import exercise.yandex.dev.*;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class Task1Test {
//
//    @Mock
//    PaymentRepository paymentRepo;
//    @Mock
//    UserLimitRepository limitRepo;
//
//    @Test
//    void shouldPassWhenWithinLimits() {
//        // Arrange
//        UserLimits limits = new UserLimits(new BigDecimal("1000"), new BigDecimal("500"));
//        when(limitRepo.getLimits("user1")).thenReturn(limits);
//
//        // Настраиваем так, что история пуста
//        when(paymentRepo.findPaymentsByUserIdAndDateRange(any(), any(), any()))
//                .thenReturn(Collections.emptyList());
//
//        List<LimitRule> rules = List.of(new MaxAmountLimitRule(), new DailyLimitRule());
//        LimitCheckService service = new LimitCheckService(limitRepo, paymentRepo, rules);
//
//        Payment payment = new Payment("user1", new BigDecimal("100"), Instant.now());
//
//        // Act
//        CheckResult result = service.checkLimit(payment);
//
//        // Assert
//        assertTrue(result.allowed());
//    }
//}
