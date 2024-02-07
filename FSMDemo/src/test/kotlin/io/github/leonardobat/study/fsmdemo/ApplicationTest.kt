package io.github.leonardobat.study.fsmdemo

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.Koin
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

internal class ApplicationTest : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create {
            modules(Application())
        }

    @Test
    fun `verify creation of modules`(koin: Koin) {
        assertNotNull(koin.getOrNull<SimpleFSM>())
        assertNotNull(koin.getOrNull<EventStatefulProcessor>())
    }
}
