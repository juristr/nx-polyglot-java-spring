# products-api

Spring Boot 3.5 API over the `orders` and `catalog-data` modules. See the [workspace README](../../README.md) for commands and how the OpenAPI type sync works.

- `src/main`: application, `ProductController`, `OrderController`, OpenAPI bean and customizer.
- `src/openapi`: `OpenApiSpecTest`, run by the `generateOpenApi` Gradle task to write `build/openapi/openapi.json` through MockMvc without opening a port.

Spring Boot is pinned to 3.5.16 and springdoc to 2.8.17 (`gradle.properties`). Each module has a `gradle.lockfile`. After changing dependencies, run `./gradlew build --write-locks` from the workspace root.
