plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "io.github.leonardobat.study"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val flinkVersion = "1.18.1"

dependencies {
    compileOnly("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
}
