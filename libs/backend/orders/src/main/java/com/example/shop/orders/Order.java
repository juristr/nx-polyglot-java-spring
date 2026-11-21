package com.example.shop.orders;

import java.math.BigDecimal;
import java.util.List;

public record Order(long id, String customer, OrderStatus status, List<OrderLine> lines, BigDecimal total) {}
