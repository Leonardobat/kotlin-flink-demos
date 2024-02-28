package io.github.leonardobat.fsmdemo

import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import java.io.Serializable

/**
 * An enumeration of the possible states of a light.
 */
enum class State {
    /**
     * The light is turned on.
     */
    ON,

    /**
     * The light is turned off.
     */
    OFF,

    /**
     * The state of the light is unknown.
     */
    UNKNOWN,
}

/**
 * A finite state machine (FSM) that manages the state of a light.
 */
class SimpleFSM : Serializable {
    @Transient
    private lateinit var logger: KotlinLogger

    /**
     * Transitions the FSM based on the given event and current state.
     *
     * @param event the event that triggered the state transition
     * @param currentState the current state of the FSM
     * @return the new state of the FSM
     */
    fun transition(
        event: Event,
        currentState: State,
    ): State {
        if (!(this::logger.isInitialized)) {
            logger = logger()
        }

        return when (event.action) {
            Event.Action.TURN_ON -> handleTurnOn(event, currentState)
            Event.Action.TURN_OFF -> handleTurnOff(event, currentState)
        }
    }

    /**
     * Handles a turn-on event.
     *
     * @param event the event that triggered the state transition
     * @param currentState the current state of the FSM
     * @return the new state of the FSM
     */
    private fun handleTurnOn(
        event: Event,
        currentState: State,
    ): State {
        return when (currentState) {
            State.ON -> {
                logger.debug("keeping actual state for id: ${event.lightId}")
                currentState
            }

            else -> {
                logger.info("updating state for id: ${event.lightId}")
                State.ON
            }
        }
    }

    /**
     * Handles a turn-off event.
     *
     * @param event the event that triggered the state transition
     * @param currentState the current state of the FSM
     * @return the new state of the FSM
     */
    private fun handleTurnOff(
        event: Event,
        currentState: State,
    ): State {
        return when (currentState) {
            State.OFF -> {
                logger.debug("keeping actual state for id: ${event.lightId}")
                currentState
            }

            else -> {
                logger.info("updating state for id: ${event.lightId}")
                State.OFF
            }
        }
    }
}
