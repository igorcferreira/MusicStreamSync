import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktlint)
    application
}

kotlin {
    // Target 17 bytecode without pinning a 17 toolchain: the Docker build stage runs
    // JDK 21 (required by the generated :arkana module) and has no toolchain resolver,
    // so a jvmToolchain(17) pin could not be provisioned there.
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("dev.igorcferreira.musicstreamsync.server.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":lastfmapi"))
    implementation(project(":arkana"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.server.test.host)
}
