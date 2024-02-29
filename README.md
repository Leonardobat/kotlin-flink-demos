# Kotlin Flink Demos

This project, named `kotlin-flink-demos`, is a collection of demos showcasing the integration of Apache Flink, Kotlin, and OpenJ9. The primary focus is leveraging Flink's power for stream processing using the expressive Kotlin language and OpenJ9 for memory optimisation.

## Project Structure

The project is organised into individual modules, each representing a different demo:

- **HelloWorld**: A simple introductory project demonstrating the basic setup.
- **FSMDemo**: A demo showcasing Finite State Machines using Flink and Kotlin.
- **KafkaDemo**: A project demonstrating integration with Apache Kafka for event streaming. It also includes a [JIT Server](https://eclipse.dev/openj9/docs/jitserver/) demonstration. 

## Dependencies

- **Flink**: The core stream processing engine.
- **Kotlin**: The modern and expressive programming language.
- **OpenJ9**: An optimised JVM implementation.
- **Podman**: An open-source, daemonless container, pod, and container image management engine.

> The recipe for the custom container image for Flink using OpenJ9 is provided on `Flink-OpenJ9`. It's based on the [original Flink image](https://github.com/apache/flink-docker/blob/578731b7a507d765c554efbfb7c6535976862d2d/1.18/scala_2.12-java17-ubuntu/Dockerfile#L19), but using [Semeru](https://hub.docker.com/_/ibm-semeru-runtimes) instead of the traditional [Temurin](https://hub.docker.com/_/eclipse-temurin) image.

## Build
Each project has a container file. You need the OpenJDK 21 and the custom Flink-OpenJ9 image to build each image.

First, you need to run the Gradle build using the über jar:
```shell
./gradlew <project_name>:shadowJar
```
Then, create the application container image:
```shell
podman build -t flink-hello-world .
```

> I will include the build directly on the container file in the future.

## Usage

Each demo project includes a Docker-Compose configuration to facilitate the usage. Run the corresponding Docker-Compose file to set up and execute the demos flawlessly.

## Contribution

Feel free to contribute to the project by submitting issues, feature requests, or pull requests. Your feedback and contributions are highly appreciated.

## License

This project uses the MIT License - see the LICENSE file for details.