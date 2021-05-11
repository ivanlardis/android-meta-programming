pluginManagement {

    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "MetaProgrammingSample"
include(":app")
include(":lens-ksp-compiler")
include(":lens-core")
include(":lens-reflect")
include(":lens-apt-compiler")
include(":lens-kotlin-compiler-plugin")
include(":lens-kotlin-compiler-plugin-kapt")
