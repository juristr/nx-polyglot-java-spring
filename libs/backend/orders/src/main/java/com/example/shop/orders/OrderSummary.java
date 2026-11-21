package com.example.shop.orders;

import java.math.BigDecimal;

public record OrderSummary(int orderCount, int itemsSold, BigDecimal revenue) {}
