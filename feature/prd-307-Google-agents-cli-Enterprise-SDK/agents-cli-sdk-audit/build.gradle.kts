plugins {
    kotlin("jvm")
}

group = "com.gcp.agents.cli"
version = "1.0.0"

dependencies {
    implementation(project(":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-core"))
    implementation(project(":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-iam"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("io.mockk:mockk:1.13.10")
}
