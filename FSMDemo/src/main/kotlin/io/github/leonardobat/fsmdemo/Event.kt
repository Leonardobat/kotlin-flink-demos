package io.github.leonardobat.fsmdemo

data class Event(var lightId: String = "", var action: Action = Action.TURN_ON) {
    enum class Action {
        TURN_ON,
        TURN_OFF,
    }
}
