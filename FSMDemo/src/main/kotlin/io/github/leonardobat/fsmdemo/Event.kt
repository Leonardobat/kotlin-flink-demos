package io.github.leonardobat.fsmdemo

/**
 * Data class that represents an event that is sent to the state machine.
 *
 * @param lightId the ID of the light that the event is related to
 * @param action the action that should be taken for the specified light
 */
data class Event(
    val lightId: String = "",
    val action: Action = Action.TURN_ON,
) {
    /**
     * Enumeration of the possible actions that can be set for a light.
     */
    enum class Action {
        TURN_ON,
        TURN_OFF,
    }
}
