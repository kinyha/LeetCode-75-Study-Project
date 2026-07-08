package exercise.ibsNew;

import java.math.BigDecimal;

record OrderItem(Long productId, BigDecimal price, int quantity) {
}
