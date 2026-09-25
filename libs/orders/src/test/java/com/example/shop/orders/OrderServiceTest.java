package com.example.shop.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.catalog.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderServiceTest {
    private final OrderService service = new OrderService(new ProductRepository());

    @Test
    void pricesOrdersFromTheCatalog() {
        Order first = service.orders().getFirst();
        assertThat(first.lines()).hasSize(2);
        assertThat(first.total()).isEqualByComparingTo(new BigDecimal("61.60"));
    }

    @Test
    void summarizesAllOrders() {
        OrderSummary summary = service.summary();
        assertThat(summary.orderCount()).isEqualTo(3);
        assertThat(summary.itemsSold()).isEqualTo(11);
        assertThat(summary.revenue()).isEqualByComparingTo(new BigDecimal("171.50"));
    }
}
