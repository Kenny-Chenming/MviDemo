plugins {
    kotlin("jvm")
}

group = "com.gcp.agents.cli"
version = "1.0.0"

dependencies {
    // OkHttp for HTTP requests / process execution
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Google Auth Library for Java/Kotlin (optional, for GCP authentication)
    // implementation("com.google.auth:google-auth-library-oauth2-http:1.20.0")

    // Kotlin YAML parsing
    implementation("org.yaml:snakeyaml:2.2")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("io.mockk:mockk:1.13.10")
}
