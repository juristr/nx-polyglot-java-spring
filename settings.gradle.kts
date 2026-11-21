rootProject.name = "nx-polyglot-java"

// Gradle modules live next to the TypeScript projects. Each one gets a flat
// project name so @nx/gradle infers `products-api`, `catalog-data` and `orders`.
include("products-api", "catalog-data", "orders")
project(":products-api").projectDir = file("apps/products-api")
project(":catalog-data").projectDir = file("libs/backend/catalog-data")
project(":orders").projectDir = file("libs/backend/orders")
