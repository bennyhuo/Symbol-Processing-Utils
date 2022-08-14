plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":module-support:common-module-support"))

    compileOnly("com.squareup:javapoet:1.13.0")
}