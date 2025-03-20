import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.swiftklib)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kmmdeploy)
    `maven-publish`
}

val frameworkName = "MusicStream"

@Suppress("LocalVariableName")
swiftklib {
    val KCrypto by creating {
        path = file(layout.projectDirectory.file("src/native/KCrypto/Sources"))
        minIos = 17
        minMacos = 14
        packageName("dev.igorcferreira.crypt")
    }
    val MusicKitBridge by creating {
        path = file(layout.projectDirectory.file("src/native/MusicKitBridge/Sources"))
        minIos = 17
        minMacos = 14
        packageName("dev.igorcferreira.os.bridge")
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcf = XCFramework(frameworkName)

    fun KotlinNativeTargetWithHostTests.applyMediaRemote() = apply {
        val frameworkPath = layout.projectDirectory.file("src/native/MediaRemote")

        val main by compilations.getting {
            val MediaRemote by cinterops.creating {
                definitionFile.set(project.file("src/native/MediaRemote/MediaRemote.def"))
                headers("$frameworkPath/MSMediaRemote.h", "$frameworkPath/MSCatalogItem.h")
                extraOpts("-libraryPath", "$frameworkPath")
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64().applyMediaRemote(),
        macosArm64().applyMediaRemote()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkName
            isStatic = true
            xcf.add(this)
        }
        @Suppress("LocalVariableName")
        val main by iosTarget.compilations.getting {
            val MediaPlayer by cinterops.creating {
                definitionFile.set(project.file("src/native/OS/MediaPlayer.def"))
            }
            val KCrypto by cinterops.creating {}
            val MusicKitBridge by cinterops.creating {}
        }
        iosTarget.binaries.all {
            linkerOpts("-framework", "MediaPlayer")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":arkana"))
        }
        commonTest.dependencies {
            api(libs.jjwt.api)
            runtimeOnly(libs.jjwt.impl)
            runtimeOnly("io.jsonwebtoken:jjwt-orgjson:${libs.versions.jjwtApi.get()}")
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        androidMain.dependencies {
            implementation(libs.androidx.media)
            implementation(project(":mediaplayback"))
            implementation(project(":musickitauth"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.kvault)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.ktor.client.okhttp)
            api(libs.jjwt.api)
            runtimeOnly(libs.jjwt.impl)
            runtimeOnly("io.jsonwebtoken:jjwt-orgjson:${libs.versions.jjwtApi.get()}") {
                exclude(group = "org.json", module = "json") //provided by Android natively
            }
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "dev.igorcferreira.musicstreamsync.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    ndkVersion = "27.2.12479018"
    dependencies {
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.androidx.media)
    }
}
