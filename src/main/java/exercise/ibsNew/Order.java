package exercise.ibsNew;

import java.math.BigDecimal;
import java.util.List;

class Order {
    Long id;
    Long customerId;
    OrderStatus status;
    List<OrderItem> items;

    public Order(Long id, Long customerId, OrderStatus status, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", status=" + status +
                ", items=" + items +
                '}';
    }
    BigDecimal getTotal() {
        return items.stream()
                .map(q -> q.price().multiply(BigDecimal.valueOf(q.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
