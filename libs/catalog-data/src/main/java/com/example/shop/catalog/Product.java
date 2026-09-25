package com.example.shop.catalog;

import java.math.BigDecimal;

/** A product in the catalog. This record is the contract the storefront sees. */
public record Product(long id, String name, String description, String category, BigDecimal price) {}
