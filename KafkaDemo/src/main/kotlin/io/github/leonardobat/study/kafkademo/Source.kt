package io.github.leonardobat.study.kafkademo

import com.typesafe.config.Config
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer

object Source {
    fun getKafkaSource(config: Config): KafkaSource<Event> {
        val topic = config.getString("app.kafka.source.topic")
        val consumerGroup = config.getString("app.kafka.source.topic")
        val bootstrapServers = config.getString("app.kafka.bootstrap-servers")

        return KafkaSource.builder<Event>()
            .setBootstrapServers(bootstrapServers)
            .setTopics(topic)
            .setGroupId(consumerGroup)
            .setStartingOffsets(offsetMode())
            .setValueOnlyDeserializer(EventDeserializer())
            .build()
    }

    private fun offsetMode(): OffsetsInitializer =
        if (true) {
            OffsetsInitializer.earliest()
        } else {
            OffsetsInitializer.latest()
        }
}
