pluginManagement {
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/maven-public")
        gradlePluginPortal()
    }
}

include("apt-utils")
include(":module-support:apt-module-support")
include(":module-support:common-module-support")
include(":module-support:ksp-module-support")
include(":module-support:xprocessing-module-support")
include(":module-support:annotations-module-support")