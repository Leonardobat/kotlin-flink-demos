plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "kotlin-flink-demos"
include("HelloWorld")
include("FSMDemo")
include("KafkaDemo")
include("WindowDemo")
