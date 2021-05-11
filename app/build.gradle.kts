plugins {
    id("com.android.application")
    kotlin("android")
    // TOOL-2: Kapt
    id("kotlin-kapt")
    // TOOL-4: Ksp
    id("com.google.devtools.ksp")
}

val kotlinVersion: String by project

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "ru.lardis.meta"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            sourceSets {
                getByName("main") {
                    // TOOL-4: Ksp
                    java.srcDir(File("build/generated/ksp/debug/kotlin"))
                    // TOOL-3: Compiler plugin aka kapt
                    java.srcDir(File("build/tmp/kotlin-classes/debug/simple-ksp"))
                }
            }
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        // TOOL-5: Compiler plugin
        freeCompilerArgs = freeCompilerArgs +
            "-Xdump-directory=${buildDir}/ir/" +
            "-Xphases-to-dump-after=ValidateIrBeforeLowering"
    }

}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    implementation(project(":lens-core"))

    // TOOL-1: Reflection
     implementation(project(":lens-reflect"))

    // TOOL-2: Kapt
//    kapt(project(":lens-apt-compiler"))

    // TOOL-3: Compiler plugin aka kapt
//     kotlinCompilerPluginClasspath(project(":lens-kotlin-compiler-plugin-kapt"))

    // TOOL-4: Ksp
//     ksp(project(":lens-ksp-compiler"))

    // TOOL-5: Compiler plugin IR
//     kotlinCompilerPluginClasspath(project(":lens-kotlin-compiler-plugin"))
}



