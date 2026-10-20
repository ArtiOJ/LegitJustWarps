plugins {
    kotlin("jvm") version "2.4.21"
}

group = "dev.artifabrian"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.206-stable")
    compileOnly(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(25)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
