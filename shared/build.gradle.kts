import com.rickclephas.kmp.nativecoroutines.gradle.ExposedSeverity
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.swiftklib)
    alias(libs.plugins.mokkery)
    `maven-publish`
    alias(libs.plugins.nativecoroutines)
}

val frameworkName = "MusicStream"

@Suppress("LocalVariableName", "unused")
swiftklib {
    val MusicKitBridge by creating {
        path = file(layout.projectDirectory.file("src/native/MusicKitBridge/Sources/MusicKitBridge"))
        minIos = 26
        packageName("dev.igorcferreira.os.bridge")
    }
    val OSLogger by creating {
        path = file(layout.projectDirectory.file("src/native/OSLogger/Sources/OSLogger"))
        minIos = 26
        packageName("dev.igorcferreira.os.logger")
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsApi")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
    }

    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        namespace = "dev.igorcferreira.musicstreamsync.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        // Opt-in to enable and configure host-side (unit) tests
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    val xcf = XCFramework(frameworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkName
            isStatic = true
            xcf.add(this)
        }
        @Suppress("LocalVariableName", "unused")
        val main by iosTarget.compilations.getting {
            val MediaPlayer by cinterops.creating {
                definitionFile.set(project.file("src/native/OS/MediaPlayer.def"))
            }
            val OSLogger by cinterops.creating {}
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
            compileOnly(project(":arkana"))
            api(project(":arkana"))
            compileOnly(project(":lastfmapi"))
            api(project(":lastfmapi"))
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)
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
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.media)
            api(libs.jjwt.api)
            runtimeOnly(libs.jjwt.impl)
            runtimeOnly("io.jsonwebtoken:jjwt-orgjson:${libs.versions.jjwtApi.get()}") {
                exclude(group = "org.json", module = "json") //provided by Android natively
            }
        }

        @Suppress("unused")
        val androidHostTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

nativeCoroutines {
    exposedSeverity = ExposedSeverity.NONE
}
