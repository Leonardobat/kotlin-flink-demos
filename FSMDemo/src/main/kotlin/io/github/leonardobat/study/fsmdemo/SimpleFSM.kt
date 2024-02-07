package io.github.leonardobat.study.fsmdemo

import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import java.io.Serializable

enum class State {
    ON,
    OFF,
    UNKNOWN,
}

class SimpleFSM : Serializable {
    @Transient
    private lateinit var logger: KotlinLogger

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
