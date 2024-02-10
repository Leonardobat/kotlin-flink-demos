package io.github.leonardobat.fsmdemo

import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.streaming.api.operators.StreamFlatMap
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EventStatefulProcessorTest {
    private lateinit var processor: EventStatefulProcessor

    private lateinit var testHarness: KeyedOneInputStreamOperatorTestHarness<String, Event, State>

    @BeforeEach
    fun setUp() {
        processor = EventStatefulProcessor(SimpleFSM())

        testHarness =
            KeyedOneInputStreamOperatorTestHarness(
                StreamFlatMap(processor),
                { it: Event -> it.lightId },
                Types.STRING,
            )

        testHarness.open()
    }

    @Test
    fun testEventStatefulProcessor() {
        assertEquals(testHarness.numKeyedStateEntries(), 0)

        val testElements =
            listOf(
                Event("1", Event.Action.TURN_ON),
                Event("2", Event.Action.TURN_OFF),
                Event("1", Event.Action.TURN_OFF),
            )

        val expectedElements =
            listOf(
                State.ON,
                State.OFF,
                State.OFF,
            )

        testElements.forEachIndexed { index, event ->
            testHarness.processElement(event, index.toLong())
        }

        val result = testHarness.extractOutputValues()

        assertEquals(expectedElements, result)
        assertEquals(testHarness.numKeyedStateEntries(), 2)
    }
}
