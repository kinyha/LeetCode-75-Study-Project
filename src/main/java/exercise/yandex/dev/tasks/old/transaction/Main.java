package exercise.yandex.dev.tasks.transaction;




/*
*
* Задача 3: Проверка платежа
Платёжный сервис. Проверить возможность платежа. Проверки: статус карты, лимит на операцию, дневной лимит.

    PaymentResult check(PaymentRequest request)
    void confirm(String paymentId)
*
* r(PaymentResult)(bool,String)
* e(CardStatus)
* r(Limits) (BidDecimal dayLimit, transactionLimit,
* r(Transaction) (BigDecimal amount, CardLimits,CardStatus)
* i(TransactionRepository) findAllTransaction()
*
*
*
*
*
* */

import java.math.BigDecimal;

public class Main {
    static void main() {
        System.out.println(123);
        CardLimits cardLimits = new CardLimits(new BigDecimal(100),new BigDecimal(200));

        System.out.println(cardLimits.isDayLimitsAccept(new BigDecimal(99)));
        System.out.println(cardLimits.isTransactionLimitsAccept((new BigDecimal(200))));


    }
}
