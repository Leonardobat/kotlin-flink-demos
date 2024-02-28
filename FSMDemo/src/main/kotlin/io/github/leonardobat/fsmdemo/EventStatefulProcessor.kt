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

/**
 * A [RichFlatMapFunction] that processes events using a finite state machine (FSM).
 *
 * The FSM is defined in the [fsm] property, and the current state of the FSM is maintained in the [state]
 * [ValueState]. The FSM is updated based on the incoming [Event], and the new state is written to the state
 * [ValueState]. The updated FSM state is then emitted as an output.
 *
 * The [open] method initializes the [state] [ValueState] and sets up the [logger].
 */
class EventStatefulProcessor(
    private val fsm: SimpleFSM,
) : RichFlatMapFunction<Event, State>() {
    @Transient
    private lateinit var logger: KotlinLogger

    @Transient
    private lateinit var state: ValueState<State?>

    /**
     * Transitions the FSM based on the incoming [event] and the current [state]. The updated FSM state is
     * emitted as an output.
     *
     * This function is implemented using [runBlocking] to ensure that the FSM transition is executed
     * synchronously.
     *
     * @param event the incoming event
     * @param output the collector for emitting output
     */
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

    /**
     * Initializes the [state] [ValueState] and sets up the [logger].
     *
     * The [ValueState] is initialized with a default value of `null`, which represents the initial
     * state of the FSM. The [logger] is initialized using the [logger] function from the
     * [org.apache.logging.log4j.kotlin] package.
     */
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
