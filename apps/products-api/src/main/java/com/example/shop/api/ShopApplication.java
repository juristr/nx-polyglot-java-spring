package com.example.shop.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.TreeSet;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.example.shop")
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    OpenAPI shopOpenApi() {
        return new OpenAPI()
            .info(new Info().title("Shop API").version("1.0.0"))
            .addServersItem(new Server().url("/"));
    }

    /**
     * Java records in this API never carry null fields, so mark every property
     * required. Without this the generated TypeScript makes every field optional.
     */
    @Bean
    OpenApiCustomizer requireRecordComponents() {
        return openApi -> {
            for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
                if (schema.getProperties() != null) {
                    schema.setRequired(new ArrayList<>(new TreeSet<>(schema.getProperties().keySet())));
                }
            }
        };
    }
}
