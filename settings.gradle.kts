rootProject.name = "MusicStreamSync"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    // Fork of ttypic/swift-klib-plugin, checked out as a git submodule, carrying the Xcode 27
    // (Swift Build engine) support that upstream lacks. Run `git submodule update --init` first.
    includeBuild("swift-klib-plugin")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        val kotlincrypto = create("kotlincrypto") {
            from("org.kotlincrypto:version-catalog:0.8.0")
        }
    }
}

include(":composeApp")
include(":shared")
include(":mediaplayback")
include(":musickitauth")
include(":arkana")
include(":lastfmapi")
