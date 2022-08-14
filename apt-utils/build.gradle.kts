plugins {
    id("java")
    kotlin("jvm")
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

dependencies {
    implementation("com.squareup:javapoet:1.9.0")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}