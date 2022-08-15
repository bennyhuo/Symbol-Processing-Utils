import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))

    api(project(":module-support:common-module-support"))

    compileOnly("androidx.room:room-compiler-processing:2.4.0")
    compileOnly("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjvm-default=all",
            "-Xopt-in=androidx.room.compiler.processing.ExperimentalProcessingApi"
        )
    }
}