package com.example.shop;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ShopController {
    private static final List<Product> PRODUCTS = List.of(
        new Product(1, "Coffee", new BigDecimal("8.50")),
        new Product(2, "Tea", new BigDecimal("6.00"))
    );

    @GetMapping("/products")
    public List<Product> products() {
        return PRODUCTS;
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable("id") long id) {
        return PRODUCTS.stream().filter(product -> product.id() == id).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/orders")
    public List<Order> orders() {
        return List.of(new Order(100, PRODUCTS, "PLACED"));
    }
}
