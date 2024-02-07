package io.github.leonardobat.study.kafkademo

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.flink.runtime.client.JobCancellationException
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.test.junit5.InjectMiniCluster
import org.apache.flink.test.junit5.MiniClusterExtension
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Testcontainers
@ExtendWith(MiniClusterExtension::class)
class ApplicationAppTest {
    private lateinit var producer: Producer<ByteArray, ByteArray>
    private lateinit var consumer: Consumer<ByteArray, ByteArray>

    private val logger = KotlinLogger.logger()

    @BeforeEach
    fun setUp() {
        val producerProps =
            Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer::class.java.name)
            }

        producer = createProducer(producerProps)

        val consumerProps =
            Properties().apply {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
                put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java.name)
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java.name)
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            }

        consumer = createConsumer(consumerProps)
    }

    @AfterEach
    fun tearDown() {
        producer.close()
        consumer.close()
    }

    @Test
    fun `should serialize and deserialize kafka values properly`(
        @InjectMiniCluster miniCluster: MiniCluster,
    ) {
        val event = Event("1", Event.Action.TURN_ON)
        val serializedEvent = EventSerializer().serialize(event)

        producer.send(ProducerRecord(KAFKA_SOURCE_TOPIC, serializedEvent)).get()
        producer.flush()
        consumer.subscribe(listOf(KAFKA_SINK_TOPIC))
        logger.info("Starting Job")

        runBlocking(Dispatchers.Default) {
            runFlinkJob()

            await untilAsserted {
                val records = consumer.poll(Duration.ofSeconds(1))
                assertFalse(records.isEmpty)
                val record = records.asIterable().first()
                val deserializedEvent = EventDeserializer().deserialize(record.value())
                assertEquals(event, deserializedEvent)
            }

            stopRunningJobs(miniCluster)
        }
    }

    context(CoroutineScope)
    private fun runFlinkJob() {
        val configString =
            """
            app.kafka.source.topic = "$KAFKA_SOURCE_TOPIC"
            app.kafka.sink.topic = "$KAFKA_SINK_TOPIC"
            app.kafka.bootstrap-servers = "${kafkaContainer.bootstrapServers}"
            """.trimIndent()

        val config = ConfigFactory.parseString(configString).withFallback(ConfigFactory.load())
        launch {
            try {
                ApplicationApp().run(config)
            } catch (e: JobCancellationException) {
                logger.info("Stopped FlinkJob Successfully")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun stopRunningJobs(miniCluster: MiniCluster) {
        withContext(Dispatchers.IO) {
            miniCluster.listJobs().get()
        }.forEach {
            miniCluster.cancelJob(it.jobId)
        }
    }

    private fun createProducer(properties: Properties): Producer<ByteArray, ByteArray> {
        return KafkaProducer(properties)
    }

    private fun createConsumer(properties: Properties): Consumer<ByteArray, ByteArray> {
        return KafkaConsumer(properties)
    }

    companion object {
        private const val KAFKA_IMAGE = "confluentinc/cp-kafka:latest"
        private const val KAFKA_SOURCE_TOPIC = "input-topic"
        private const val KAFKA_SINK_TOPIC = "output-topic"

        @BeforeAll
        @JvmStatic
        fun setup() {
            val bootstrapServers = kafkaContainer.bootstrapServers

            val adminProperties =
                Properties().apply {
                    put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                }

            val adminClient = AdminClient.create(adminProperties)

            val topics =
                listOf(
                    NewTopic(KAFKA_SOURCE_TOPIC, 1, 1),
                    NewTopic(KAFKA_SINK_TOPIC, 1, 1),
                )

            adminClient.createTopics(topics)
        }

        @AfterAll
        @JvmStatic
        fun cleanUp() {
            val bootstrapServers = kafkaContainer.bootstrapServers

            val adminProperties =
                Properties().apply {
                    put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                }

            val adminClient = AdminClient.create(adminProperties)

            adminClient.deleteTopics(listOf(KAFKA_SOURCE_TOPIC, KAFKA_SINK_TOPIC))
        }

        @Container
        private val kafkaContainer =
            KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
                .withNetwork(Network.newNetwork())
                .withKraft()
    }
}
