package com.example.shop.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Hard-coded catalog. Swap for a Spring Data repository in a real app. */
@Repository
public class ProductRepository {
    private static final List<Product> PRODUCTS = List.of(
        new Product(1, "Espresso Beans", "Dark roast, 1 kg bag", "Coffee", new BigDecimal("24.90")),
        new Product(2, "Pour-Over Kettle", "Gooseneck, 1 liter", "Equipment", new BigDecimal("49.00")),
        new Product(3, "Ceramic Dripper", "Size 02, fits standard filters", "Equipment", new BigDecimal("22.50")),
        new Product(4, "Sencha Green Tea", "Loose leaf, 100 g", "Tea", new BigDecimal("12.80")),
        new Product(5, "Oat Milk", "Barista edition, 1 liter", "Pantry", new BigDecimal("2.95"))
    );

    public List<Product> findAll() {
        return PRODUCTS;
    }

    public Optional<Product> findById(long id) {
        return PRODUCTS.stream().filter(product -> product.id() == id).findFirst();
    }
}
