plugins {
    alias(deps.plugins.kotlin.jvm)
    id("configuration")
}

dependencies {
    /* Project - Domain */
    implementation(projects.domain.core)
    implementation(projects.domain.blockaid.models)

    /* Other */
    implementation(deps.moshi.adapters)
}
