package io.github.leonardobat.fsmdemo

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object Application {
    operator fun invoke(): Module {
        return module {
            singleOf(::SimpleFSM)
            singleOf(::EventStatefulProcessor)
        }
    }
}

fun main() {
    val logger = logger("main")
    initializeApplication(logger)
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    val stream =
        env.fromElements(
            Event("1", Event.Action.TURN_ON),
            Event("2", Event.Action.TURN_OFF),
            Event("1", Event.Action.TURN_OFF),
        )

    val processor = getClass<EventStatefulProcessor>()

    stream.keyBy {
        it.lightId
    }.flatMap(processor)

    env.execute("Flink FSM Demo Job")
}

private fun initializeApplication(logger: KotlinLogger) {
    logger.debug("Starting all classes for koin application")
    startKoin {
        modules(Application())
    }
    logger.debug("Started koin application")
}

private fun closeApplication(logger: KotlinLogger) {
    stopKoin()
    logger.debug("Deconstructed all classes from koin application")
}

private inline fun <reified T : Any> getClass(): T {
    return get().get<T>()
}
