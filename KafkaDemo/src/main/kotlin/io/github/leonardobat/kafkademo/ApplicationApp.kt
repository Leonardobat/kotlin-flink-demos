package io.github.leonardobat.kafkademo

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.Logging
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class ApplicationApp : Logging {
    fun run(config: Config = ConfigFactory.load()) {
        initializeModule(logger)
        val env = StreamExecutionEnvironment.getExecutionEnvironment()

        val processor = getClass<EventStatefulProcessor>()
        env.fromSource(Source.getKafkaSource(config), WatermarkStrategy.noWatermarks(), "Kafka Source")
            .keyBy {
                it.lightId
            }.flatMap(processor)
            .sinkTo(Sink.getKafkaSink(config))
            .uid("test")

        env.execute("Kafka Demo Flink Job")
    }

    private fun initializeModule(logger: KotlinLogger) {
        logger.debug("Starting all classes for koin application")
        val appModule =
            module {
                singleOf(::EventStatefulProcessor)
            }
        GlobalContext.startKoin {
            modules(appModule)
        }
        logger.debug("Started koin application")
    }

    fun closeModule(logger: KotlinLogger) {
        GlobalContext.stopKoin()
        logger.debug("Deconstructed all classes from koin application")
    }

    private inline fun <reified T : Any> getClass(): T {
        return GlobalContext.get().get<T>()
    }
}
