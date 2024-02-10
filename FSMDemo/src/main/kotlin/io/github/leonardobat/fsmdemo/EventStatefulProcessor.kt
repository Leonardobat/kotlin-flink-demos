package io.github.leonardobat.fsmdemo

import kotlinx.coroutines.runBlocking
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger

class EventStatefulProcessor(
    private val fsm: SimpleFSM,
) : RichFlatMapFunction<Event, State>() {
    @Transient
    private lateinit var logger: KotlinLogger

    @Transient
    private lateinit var state: ValueState<State?>

    @Override
    override fun flatMap(
        event: Event,
        output: Collector<State>,
    ) {
        runBlocking {
            val currentState = state.value() ?: State.UNKNOWN
            val newState = fsm.transition(event, currentState)
            state.update(newState)
            logger.info("$event, $newState")
            output.collect(newState)
        }
    }

    override fun open(config: Configuration?) {
        val descriptor: ValueStateDescriptor<State?> =
            ValueStateDescriptor(
                "fsmState",
                TypeInformation.of(State::class.java),
            )
        state = runtimeContext.getState(descriptor)

        if (!(this::logger.isInitialized)) {
            logger = logger()
        }
    }
}
