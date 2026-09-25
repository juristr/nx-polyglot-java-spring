plugins {
    java
    id("org.springframework.boot") version "3.5.16"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
repositories { mavenCentral() }

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:" +
        providers.gradleProperty("springdocVersion").get())
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val openapi by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[openapi.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[openapi.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.test { useJUnitPlatform() }

val generateOpenApi by tasks.registering(Test::class) {
    group = "documentation"
    description = "Write the OpenAPI contract without opening a server port"
    dependsOn(tasks.named(openapi.classesTaskName))
    testClassesDirs = openapi.output.classesDirs
    classpath = openapi.runtimeClasspath
    useJUnitPlatform()
    filter { includeTestsMatching("com.example.shop.OpenApiSpecTest") }
    workingDir(projectDir)
    systemProperty("openapi.output", "build/openapi/openapi.json")
    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
    systemProperty("user.timezone", "UTC")
    outputs.file(layout.buildDirectory.file("openapi/openapi.json"))
        .withPropertyName("openApiSpec")
}

tasks.named("check") { dependsOn(generateOpenApi) }

dependencyLocking { lockAllConfigurations() }
