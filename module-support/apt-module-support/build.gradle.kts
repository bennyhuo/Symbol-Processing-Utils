import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":module-support:common-module-support"))

    compileOnly("com.squareup:javapoet:1.13.0")
}