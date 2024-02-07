package io.github.leonardobat.study.kafkademo

import com.typesafe.config.Config
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink

object Sink {
    fun getKafkaSink(config: Config): KafkaSink<Event> {
        val topic = config.getString("app.kafka.sink.topic")
        val bootstrapServers = config.getString("app.kafka.bootstrap-servers")

        return KafkaSink.builder<Event>()
            .setBootstrapServers(bootstrapServers)
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<Event>()
                    .setTopic(topic)
                    .setValueSerializationSchema(EventSerializer())
                    .build(),
            )
            .build()
    }
}
