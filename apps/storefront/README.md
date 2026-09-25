# storefront

TanStack Start app showing products, an order summary and recent orders from `products-api`. Types come from `@shop/api-types`, generated from the API's OpenAPI spec. See the [workspace README](../../README.md).

```sh
pnpm nx dev storefront        # also starts the Spring Boot API
pnpm nx typecheck storefront  # regenerates types first if the Java contract changed
pnpm nx build storefront
```
