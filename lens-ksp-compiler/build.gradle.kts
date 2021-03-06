val kspVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")

    implementation(project(":lens-core"))
}
