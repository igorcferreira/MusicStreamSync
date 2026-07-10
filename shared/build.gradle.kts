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
    alias(libs.plugins.ktlint)
}

val frameworkName = "MusicStream"

@Suppress("LocalVariableName", "unused")
swiftklib {
    val MusicKitBridge =
        create("MusicKitBridge") {
            path = file(layout.projectDirectory.file("src/native/MusicKitBridge/Sources/MusicKitBridge"))
            minIos =
                libs.versions.ios.minSdk
                    .get()
                    .toInt()
            packageName("dev.igorcferreira.os.bridge")
        }
    val OSLogger =
        create("OSLogger") {
            path = file(layout.projectDirectory.file("src/native/OSLogger/Sources/OSLogger"))
            minIos =
                libs.versions.ios.minSdk
                    .get()
                    .toInt()
            packageName("dev.igorcferreira.os.logger")
        }
}

kotlin {
    // The custom jvmCommon source set below adds manual dependsOn edges, which would
    // otherwise disable the default hierarchy template (and with it appleMain/iosMain).
    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsApi")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
    }

    android {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        namespace = "dev.igorcferreira.musicstreamsync.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        // Opt-in to enable and configure host-side (unit) tests
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
        val main =
            iosTarget.compilations.getByName("main") {
                val MediaPlayer =
                    cinterops.create("MediaPlayer") {
                        definitionFile.set(project.file("src/native/OS/MediaPlayer.def"))
                    }
                // clang 21 (bundled with Kotlin 2.4.0+) only discovers module.modulemap directly inside
                // -I directories, while swiftklib 0.6.4 points -I one level above the include/ dir
                // SwiftPM emits. Add the include/ dir explicitly until swiftklib ships a fix.
                val swiftklibArch = if (iosTarget.name == "iosX64") "x86_64" else "arm64"

                fun swiftklibIncludeDir(name: String): String {
                    val swiftBuildDir = "swiftklib/$name/${iosTarget.name}/swiftBuild/.build"
                    return layout.buildDirectory
                        .dir("$swiftBuildDir/$swiftklibArch-apple-macosx/release/$name.build/include")
                        .get()
                        .asFile
                        .absolutePath
                }

                val OSLogger =
                    cinterops.create("OSLogger") {
                        compilerOpts("-I${swiftklibIncludeDir("OSLogger")}")
                    }
                val MusicKitBridge =
                    cinterops.create("MusicKitBridge") {
                        compilerOpts("-I${swiftklibIncludeDir("MusicKitBridge")}")
                    }
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

        // JVM-compatible code shared between the Android and plain-JVM targets
        // (JWTTokenSigner and friends).
        val jvmCommon by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.jjwt.api)
            }
        }

        jvmMain {
            dependsOn(jvmCommon)
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.slf4j.api)
                runtimeOnly(libs.jjwt.impl)
                runtimeOnly("io.jsonwebtoken:jjwt-orgjson:${libs.versions.jjwtApi.get()}")
            }
        }

        androidMain.get().dependsOn(jvmCommon)
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
            runtimeOnly(libs.jjwt.impl)
            runtimeOnly("io.jsonwebtoken:jjwt-orgjson:${libs.versions.jjwtApi.get()}") {
                exclude(group = "org.json", module = "json") // provided by Android natively
            }
        }

        @Suppress("unused")
        val androidHostTest =
            getByName("androidHostTest") {
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
