package io.github.leonardobat.study.kafkademo

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger

class EventStatefulProcessor : RichFlatMapFunction<Event, Event>() {
    @Transient
    private lateinit var logger: KotlinLogger

    @Transient
    private lateinit var count: ValueState<Int?>

    @Override
    override fun flatMap(
        event: Event,
        output: Collector<Event>,
    ) {
        val currentCount = count.value() ?: 0
        count.update(currentCount + 1)
        logger.info("$event, ${count.value()}")
        output.collect(event)
    }

    override fun open(config: Configuration?) {
        val descriptor: ValueStateDescriptor<Int?> =
            ValueStateDescriptor(
                "fsmState",
                TypeInformation.of(Int::class.java),
            )
        count = runtimeContext.getState(descriptor)

        if (!(this::logger.isInitialized)) {
            logger = logger()
        }
    }
}
