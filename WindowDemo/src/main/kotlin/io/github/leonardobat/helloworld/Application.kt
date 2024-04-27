package io.github.leonardobat.helloworld

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment


/**
 * Main function of the program.
 */
fun main() {
    // Set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.parallelism = 1
    val stream = env.fromSource(CustomSource(Configuration()), WatermarkStrategy.noWatermarks(), "custom-source")
    stream
        .print()

    // Execute the job
    env.execute("Hello World Flink Job")
}
