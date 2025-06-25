package exercise.concurency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PaymentProcessingExample {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        processPayment("user123", "card_456", 99.99);
    }
    
    public static void processPayment(String userId, String cardId, double amount) 
            throws ExecutionException, InterruptedException {
        
        System.out.println("Starting payment processing for $" + amount);
        
        CompletableFuture<String> paymentResult = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Step 1: Validating user and card...");
                    sleepQuietly(300);
                    return new ValidationResult(userId, cardId, true);
                })
                .thenApply(validation -> {
                    if (!validation.isValid) {
                        throw new RuntimeException("Validation failed");
                    }
                    System.out.println("Step 2: Charging card...");
                    sleepQuietly(500);
                    return new ChargeResult("txn_789", amount, "SUCCESS");
                })
                .thenApply(charge -> {
                    System.out.println("Step 3: Updating user balance...");
                    sleepQuietly(200);
                    return new BalanceUpdate(userId, charge.amount, "COMPLETED");
                })
                .thenApply(balance -> {
                    System.out.println("Step 4: Sending confirmation...");
                    sleepQuietly(100);
                    return "Payment of $" + balance.amount + " processed successfully for " + balance.userId;
                })
                .exceptionally(ex -> {
                    System.err.println("Payment failed: " + ex.getMessage());
                    return "Payment failed - please try again";
                });
        
        System.out.println("Result: " + paymentResult.get());
    }
    
    private static void sleepQuietly(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    record ValidationResult(String userId, String cardId, boolean isValid) {
    }

    record ChargeResult(String transactionId, double amount, String status) {
    }

    record BalanceUpdate(String userId, double amount, String status) {
    }
}