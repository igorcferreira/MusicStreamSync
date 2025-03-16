import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.material)
            implementation(libs.androidx.constraintlayout)
            implementation(projects.shared)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.coil.compose)
            implementation(libs.coil3.coil.network.ktor3)
            implementation(libs.ktor.client.android)
        }
    }
}

android {
    namespace = "dev.igorcferreira.musicstreamsync"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.igorcferreira.musicstreamsync"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        all {
            buildConfigField(
                "String", "PRIVATE_KEY", "\"${
                    getProperty("project.privateKey") ?: ""
                }\""
            )

            buildConfigField(
                "String", "TEAM_ID", "\"${
                    getProperty("project.teamId") ?: ""
                }\""
            )

            buildConfigField(
                "String", "KEY_ID", "\"${
                    getProperty("project.keyId") ?: ""
                }\""
            )
        }

        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

fun getProperty(name: String): String? {
    val localFile = layout.projectDirectory.file("../local.properties").asFile
    if (localFile.exists()) {
        val localProperties = loadProperties(localFile.path)
        if (localProperties.containsKey(name)) {
            return localProperties.getProperty(name)
        }
    }

    if (!project.hasProperty(name)) {
        return null
    }
    return project.property(name)?.toString()
}

