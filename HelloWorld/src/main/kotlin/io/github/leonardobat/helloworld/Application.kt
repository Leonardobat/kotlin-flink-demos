package io.github.leonardobat.helloworld

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

fun main(args: Array<String>) {
    // Set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    // Create a simple Flink stream with "Hello, World!" as the output
    val stream = env.fromElements("Hello, World!")

    // Print the stream to the console
    stream.print()

    // Execute the job
    env.execute("Hello World Flink Job")
}
