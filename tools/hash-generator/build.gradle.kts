plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.mvi.kenny.otpdelay"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // No external dependencies for pure Kotlin implementation
    // 纯 Kotlin 实现，无外部依赖
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

application {
    mainClass.set("com.mvi.kenny.otpdelay.MainKt")
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.mvi.kenny.otpdelay.MainKt"
    }
}

tasks.withType<com.github.jengelman.gradle.tasks.shadow.ShadowJar> {
    archiveBaseName.set("sms-retriever-hash-generator")
    archiveClassifier.set("")
    archiveVersion.set("")
}
