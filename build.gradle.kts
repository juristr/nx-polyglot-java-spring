plugins {
    id("dev.nx.gradle.project-graph") version("0.1.25")
    id("org.springframework.boot") version "3.5.16" apply false
}

subprojects {
    group = "com.example.shop"
    version = "0.0.1-SNAPSHOT"

    repositories { mavenCentral() }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain { languageVersion = JavaLanguageVersion.of(21) }
        }
        dependencies {
            "implementation"(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
            "testImplementation"("org.springframework.boot:spring-boot-starter-test")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }
        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-parameters")
        }
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
    }

    dependencyLocking { lockAllConfigurations() }
}

allprojects {
    apply {
        plugin("dev.nx.gradle.project-graph")
    }
}