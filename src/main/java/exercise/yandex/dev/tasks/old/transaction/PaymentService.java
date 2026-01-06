package exercise.yandex.dev.tasks.transaction;

public class PaymentService {

    //
    //    PaymentResult check(PaymentRequest request)
    //    void confirm(String paymentId)


    public PaymentResult check(Transaction transaction) {
        if (transaction == null) {
            return PaymentResult.failure("transaction cant be null");
        }

        if (!transaction.cardLimits().isDayLimitsAccept(transaction.amount())) {
            return PaymentResult.failure(String.format("Amount %s more than dayLimits %s",transaction.amount(),transaction.cardLimits().dayLimits()));
        }
        if (!transaction.cardLimits().isTransactionLimitsAccept(transaction.amount())) {
            return PaymentResult.failure(String.format("Amount %s more than transactionLimits %s",transaction.amount(),transaction.cardLimits().transactionLimits()));
        }
        if (transaction.cardStatus() != CardStatus.ALLOWED) {
            return PaymentResult.failure("Card status not allowed");
        }
        return PaymentResult.success();
    }
}
