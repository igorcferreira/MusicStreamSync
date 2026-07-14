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

// The canonical API document lives at server/openapi.yaml (spec/AGENT.md mandate).
// It is generated from the route documentation (see the OpenApi plugin in Application.kt)
// rather than hand-authored: the routes are the single source of truth and the served
// GET /openapi.yaml renders the document at request time, so it is not packaged as a
// resource. `openApiSnapshot` points both the drift guard (:server:test) and the
// regeneration task at the checked-in snapshot.
val openApiSnapshot = layout.projectDirectory.file("openapi.yaml")

tasks.withType<Test>().configureEach {
    systemProperty("openapi.file", openApiSnapshot.asFile.absolutePath)
}

// Regenerate server/openapi.yaml from the code: runs only the snapshot test with the
// write flag on. `./gradlew :server:generateOpenApi`.
tasks.register<Test>("generateOpenApi") {
    description = "Regenerates server/openapi.yaml from the documented routes."
    group = "documentation"
    val testTask = tasks.named<Test>("test").get()
    testClassesDirs = testTask.testClassesDirs
    classpath = testTask.classpath
    systemProperty("openapi.generate", "true")
    filter { includeTestsMatching("dev.igorcferreira.musicstreamsync.server.OpenApiDocumentTest") }
    outputs.upToDateWhen { false }
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
    implementation(libs.ktor.openapi)
    // Kotlinx-serialization-backed schema/example generation for the OpenApi plugin; these
    // schema-kenerator modules are implementation-scoped inside ktor-openapi, so they are
    // declared here to make SchemaGenerator.kotlinx()/ExampleEncoder.kotlinx() resolvable.
    implementation(libs.schemakenerator.core)
    implementation(libs.schemakenerator.serialization)
    implementation(libs.schemakenerator.swagger)
    implementation(libs.logback.classic)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.server.test.host)
}
