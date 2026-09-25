package com.example.shop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
    "springdoc.api-docs.enabled=true",
    "springdoc.writer-with-order-by-keys=true",
    "springdoc.writer-with-default-pretty-printer=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("openapi")
class OpenApiSpecTest {
    @Autowired MockMvc mvc;
    @Autowired ApplicationContext context;

    @Test void writeSpec() throws Exception {
        assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
        assertThat(((WebApplicationContext) context).getServletContext())
            .isInstanceOf(MockServletContext.class);

        String json = mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.paths['/products'].get").exists())
            .andExpect(jsonPath("$.paths['/products/{id}'].get").exists())
            .andExpect(jsonPath("$.paths['/orders'].get").exists())
            .andExpect(jsonPath("$.components.schemas.Product.properties.id").exists())
            .andExpect(jsonPath("$.components.schemas.Product.properties.price").exists())
            .andExpect(jsonPath("$.components.schemas.Order.properties.products.items['$ref']")
                .value("#/components/schemas/Product"))
            .andExpect(jsonPath("$.servers[0].url").value("/"))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Path output = Path.of(System.getProperty("openapi.output"));
        Files.createDirectories(output.getParent());
        Files.writeString(output, json + "\n", StandardCharsets.UTF_8);
    }
}
