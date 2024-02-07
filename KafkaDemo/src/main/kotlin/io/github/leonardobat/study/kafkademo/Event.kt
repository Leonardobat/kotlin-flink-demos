package io.github.leonardobat.study.kafkademo

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema

data class Event(var lightId: String = "", var action: Action = Action.TURN_ON) {
    enum class Action {
        TURN_ON,
        TURN_OFF,
    }
}

class EventDeserializer : AbstractDeserializationSchema<Event>() {
    private val objectMapper = ObjectMapper()

    @Override
    override fun deserialize(message: ByteArray): Event {
        return objectMapper.readValue(message, Event::class.java)
    }
}

class EventSerializer : SerializationSchema<Event> {
    private val objectMapper = ObjectMapper()

    @Override
    override fun serialize(element: Event): ByteArray {
        return objectMapper.writeValueAsBytes(element)
    }
}
