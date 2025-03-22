rootProject.name = "MusicStreamSync"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
        val kotlincrypto by creating {
            from("org.kotlincrypto:version-catalog:0.7.0")
        }
    }
}

include(":composeApp")
include(":shared")
include(":mediaplayback")
include(":musickitauth")
include(":arkana")
include(":lastfmapi")
