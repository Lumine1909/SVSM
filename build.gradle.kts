plugins {
    java
    id("com.gradleup.shadow") version "9.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "io.github.lumine1909"
version = "1.1.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    implementation("io.github.lumine1909:reflexion:0.3.1")
}

tasks.shadowJar {
    archiveClassifier.set("")
}