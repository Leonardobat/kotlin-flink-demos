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
    /**
     * Creates a Koin [module][org.koin.core.module.Module] for the application.
     *
     * @return the application module
     */
    operator fun invoke(): Module {
        return module {
            /**
             * Binds the [SimpleFSM] class as a single instance.
             */
            singleOf(::SimpleFSM)

            /**
             * Binds the [EventStatefulProcessor] class as a single instance.
             */
            singleOf(::EventStatefulProcessor)
        }
    }
}

/**
 * Main function of the application.
 */
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

    stream.keyBy { it.lightId }.flatMap(processor)

    env.execute("Flink FSM Demo Job")
}

/**
 * Initialize the application by starting the Koin application and injecting dependencies.
 *
 * @param logger the logger instance to use
 */
private fun initializeApplication(logger: KotlinLogger) {
    logger.debug("Starting all classes for koin application")
    startKoin {
        modules(Application())
    }
    logger.debug("Started koin application")
}

/**
 * Close the Koin application and deconstruct all classes.
 *
 * @param logger the logger instance to use
 */
private fun closeApplication(logger: KotlinLogger) {
    stopKoin()
    logger.debug("Deconstructed all classes from koin application")
}

/**
 * Get an instance of a class from the Koin application context.
 *
 * @param T the type of the class to get
 * @return an instance of the class
 */
private inline fun <reified T : Any> getClass(): T {
    return get().get<T>()
}
