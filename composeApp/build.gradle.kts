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
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
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
            implementation(libs.androidx.material3.android)
            implementation(project(":mediaplayback"))
            implementation(project(":musickitauth"))
        }
    }
}

android {
    namespace = "dev.igorcferreira.musicstreamsync"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = getProperty("project.applicationId", "dev.igorcferreira.musicstreamsync")
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = getProperty("project.versionCode", "1").toInt()
        versionName = getProperty("project.versionName", "1.0")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

fun getProperty(name: String, defaultValue: String): String {
    val localFile = layout.projectDirectory.file("../local.properties").asFile
    if (localFile.exists()) {
        val localProperties = loadProperties(localFile.path)
        if (localProperties.containsKey(name)) {
            return localProperties.getProperty(name)
        }
    }

    if (!project.hasProperty(name)) {
        return defaultValue
    }
    return project.property(name)?.toString() ?: defaultValue
}

