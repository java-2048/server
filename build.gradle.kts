plugins {
    id("java")
}

group = "io.github.java_2048"
version = "1.0-SNAPSHOT"

java {
    toolchain{
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar{
    manifest.attributes["Main-Class"] = "io.github.java_2048.server.Server"
}