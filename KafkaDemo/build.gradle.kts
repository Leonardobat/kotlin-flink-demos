import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

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

tasks.test {
    useJUnitPlatform()

    // Podman Configuration
    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    if (os.isLinux) {
        val uid =
            ProcessBuilder("id", "-u")
                .start()
                .inputStream
                .bufferedReader()
                .use { it.readText().trim() }
        environment("DOCKER_HOST", "unix:///run/user/$uid/podman/podman.sock")
    } else if (os.isMacOsX) {
        environment("DOCKER_HOST", "unix:///tmp/podman.sock")
    }
}

application {
    mainClass = "io.github.leonardobat.kafkademo.ApplicationKt"
}

dependencies {
    compileOnly("org.apache.flink:flink-streaming-java:$flinkVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    implementation("org.apache.flink:flink-connector-kafka:3.1.0-1.18")
    implementation("org.apache.flink:flink-connector-base:$flinkVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.1"))
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-core-coroutines")
    implementation("com.typesafe:config:1.4.3")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.insert-koin:koin-test")
    testImplementation("io.insert-koin:koin-test-junit5")
    testImplementation("org.apache.flink:flink-test-utils:$flinkVersion")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.3"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.apache.kafka:kafka-clients:3.7.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
