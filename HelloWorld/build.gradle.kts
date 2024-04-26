plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.johnrengelman.shadow")

    java
    application
}

group = "io.github.leonardobat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val flinkVersion = "1.19.0"

application {
    mainClass = "io.github.leonardobat.helloworld.ApplicationKt"
}

dependencies {
    compileOnly("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.1"))
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}
