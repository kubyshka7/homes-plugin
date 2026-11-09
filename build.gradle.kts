plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    shadowJar {
        relocate("com.zaxxer.hikari", "org.example.homes.libs.hikari")
        relocate("org.postgresql", "org.example.homes.libs.postgresql")
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}