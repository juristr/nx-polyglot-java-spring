package com.example.shop.api;

import com.example.shop.orders.Order;
import com.example.shop.orders.OrderService;
import com.example.shop.orders.OrderSummary;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/orders")
    public List<Order> orders() {
        return orders.orders();
    }

    @GetMapping("/orders/summary")
    public OrderSummary summary() {
        return orders.summary();
    }
}
