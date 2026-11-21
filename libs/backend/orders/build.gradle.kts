import dev.nx.gradle.nx

// Business layer: builds orders on top of the catalog.
plugins {
    `java-library`
}

nx {
    set("name", "orders")
    array("tags", "type:feature", "scope:api")
}

dependencies {
    api(project(":catalog-data"))
    implementation("org.springframework:spring-context")
}
