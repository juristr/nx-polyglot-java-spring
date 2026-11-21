import dev.nx.gradle.nx

// Data layer: the product catalog. Hard-coded for the example, a real app
// would put a Spring Data repository here.
plugins {
    `java-library`
}

nx {
    set("name", "catalog-data")
    array("tags", "type:data", "scope:api")
}

dependencies {
    implementation("org.springframework:spring-context")
}
