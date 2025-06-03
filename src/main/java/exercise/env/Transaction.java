package exercise.env;

import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long customerId;
    private Double amount;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime dateTime;
    private String category;

    public Transaction(Long id, Long customerId, Double amount,
                       TransactionType type, TransactionStatus status,
                       LocalDateTime dateTime, String category) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.dateTime = dateTime;
        this.category = category;
    }

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, customerId=%d, amount=%.2f, type=%s, status=%s}",
                id, customerId, amount, type, status);
    }
}

