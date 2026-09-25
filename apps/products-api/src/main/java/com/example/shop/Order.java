package com.example.shop;

import java.util.List;

public record Order(long id, List<Product> products, String status) {}
