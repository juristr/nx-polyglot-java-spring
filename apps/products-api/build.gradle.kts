import dev.nx.gradle.nx

plugins {
    java
    id("org.springframework.boot")
}

nx {
    set("name", "products-api")
    array("tags", "type:app", "scope:api")
}

dependencies {
    implementation(project(":orders"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:" +
        providers.gradleProperty("springdocVersion").get())
}

// The OpenAPI contract is written by a JUnit test in its own source set, so
// ordinary `test` runs stay fast and `generateOpenApi` can be cached on its own.
val openapi by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[openapi.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[openapi.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

val generateOpenApi by tasks.registering(Test::class) {
    group = "documentation"
    description = "Write the OpenAPI contract without opening a server port"
    dependsOn(tasks.named(openapi.classesTaskName))
    testClassesDirs = openapi.output.classesDirs
    classpath = openapi.runtimeClasspath
    filter { includeTestsMatching("com.example.shop.api.OpenApiSpecTest") }
    workingDir(projectDir)
    systemProperty("openapi.output", "build/openapi/openapi.json")
    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
    systemProperty("user.timezone", "UTC")
    outputs.file(layout.buildDirectory.file("openapi/openapi.json"))
        .withPropertyName("openApiSpec")
}

tasks.named("check") { dependsOn(generateOpenApi) }
