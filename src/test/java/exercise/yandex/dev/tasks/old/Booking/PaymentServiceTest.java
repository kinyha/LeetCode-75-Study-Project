//package exercise.yandex.dev.tasks.transaction;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class PaymentServiceTest {
//    PaymentService paymentService;
//
//    @BeforeEach
//    void setUp() {
//        paymentService = new PaymentService();
//    }
//
//    @Test
//    @DisplayName("Happy path")
//    void happyTest() {
//        //give
//        CardLimits cardLimits = new CardLimits(new BigDecimal(500), new BigDecimal(100));
//        CardStatus cardStatus = CardStatus.ALLOWED;
//        BigDecimal amount = new BigDecimal(50);
//        Transaction tr1 = new Transaction(amount, cardLimits, cardStatus);
//
//        //when
//        var result = paymentService.check(tr1);
//
//        //than
//        assertTrue(result.result());
//    }
//
//    @Test
//    @DisplayName("Not happy, transactionLimits  more")
//    void transactionLimitsMoreThanAmount() {
//        //give
//        CardLimits cardLimits = new CardLimits(new BigDecimal(500), new BigDecimal(100));
//        CardStatus cardStatus = CardStatus.ALLOWED;
//        BigDecimal amount = new BigDecimal(120);
//        Transaction tr1 = new Transaction(amount, cardLimits, cardStatus);
//
//        //when
//        var result = paymentService.check(tr1);
//
//        //than
//        assertFalse(result.result());
//        assertEquals("Amount 120 more than transactionLimits 100", result.message());
//        System.out.println(result);
//    }
//
//    @Test
//    @DisplayName("Not happy, dayLimits more")
//    void dayLimitsMoreThanAmount() {
//        //give
//        CardLimits cardLimits = new CardLimits(new BigDecimal(500), new BigDecimal(100));
//        CardStatus cardStatus = CardStatus.ALLOWED;
//        BigDecimal amount = new BigDecimal(300);
//        Transaction tr1 = new Transaction(amount, cardLimits, cardStatus);
//
//        //when
//        var result = paymentService.check(tr1);
//
//        //than
//        assertFalse(result.result());
//        System.out.println(result);
//    }
//}