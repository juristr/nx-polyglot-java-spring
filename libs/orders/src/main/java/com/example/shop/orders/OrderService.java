package com.example.shop.orders;

import com.example.shop.catalog.Product;
import com.example.shop.catalog.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Business layer: turns raw order drafts into priced orders using the catalog. */
@Service
public class OrderService {
    private record Draft(long id, String customer, OrderStatus status, Map<Long, Integer> quantities) {}

    private static final List<Draft> DRAFTS = List.of(
        new Draft(1001, "Ada", OrderStatus.DELIVERED, Map.of(1L, 2, 5L, 4)),
        new Draft(1002, "Grace", OrderStatus.SHIPPED, Map.of(2L, 1, 3L, 1)),
        new Draft(1003, "Linus", OrderStatus.PLACED, Map.of(4L, 3))
    );

    private final ProductRepository products;

    public OrderService(ProductRepository products) {
        this.products = products;
    }

    public List<Order> orders() {
        return DRAFTS.stream().map(this::price).toList();
    }

    public OrderSummary summary() {
        List<Order> orders = orders();
        int itemsSold = orders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(OrderLine::quantity)
            .sum();
        BigDecimal revenue = orders.stream().map(Order::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderSummary(orders.size(), itemsSold, revenue);
    }

    private Order price(Draft draft) {
        List<OrderLine> lines = draft.quantities().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> line(entry.getKey(), entry.getValue()))
            .toList();
        BigDecimal total = lines.stream().map(OrderLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(draft.id(), draft.customer(), draft.status(), lines, total);
    }

    private OrderLine line(long productId, int quantity) {
        Product product = products.findById(productId)
            .orElseThrow(() -> new IllegalStateException("Unknown product " + productId));
        return new OrderLine(product, quantity, product.price().multiply(BigDecimal.valueOf(quantity)));
    }
}
