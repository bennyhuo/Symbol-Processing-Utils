plugins {
    kotlin("jvm") version "1.8.20" apply false
    id("org.jetbrains.dokka") version "1.7.10" apply false
    id("com.github.gmazzo.buildconfig") version "2.1.0" apply false
    id("com.vanniktech.maven.publish") version "0.22.0" apply false
}

subprojects {
    repositories {
        mavenCentral()
        google()
    }
    
    if (!name.startsWith("sample") && parent?.name?.startsWith("sample") != true) {
        group = property("GROUP").toString()
        version = property("VERSION_NAME").toString()

        apply(plugin = "com.vanniktech.maven.publish")
    }

    pluginManager.withPlugin("java") {
        extensions.getByType<JavaPluginExtension>().sourceCompatibility = JavaVersion.VERSION_1_8
    }
}