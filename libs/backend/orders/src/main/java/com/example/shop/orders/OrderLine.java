package com.example.shop.orders;

import com.example.shop.catalog.Product;
import java.math.BigDecimal;

public record OrderLine(Product product, int quantity, BigDecimal lineTotal) {}
