package io.github.leonardobat.helloworld

import org.apache.flink.api.connector.source.*
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.base.source.reader.RecordEmitter
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds
import org.apache.flink.connector.base.source.reader.SingleThreadMultiplexSourceReaderBase
import org.apache.flink.connector.base.source.reader.splitreader.SplitReader
import org.apache.flink.connector.base.source.reader.splitreader.SplitsChange
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.logging.log4j.kotlin.Logging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

class CustomSourceSplit(var counter: Long) : SourceSplit {
    override fun splitId(): String {
        return "custom-split-id"
    }
}


class CustomRecordsWithSplitIds(private val value: Long) : RecordsWithSplitIds<Long>, Logging {
    private val wasRead = AtomicBoolean(false)
    override fun nextSplit(): String? {
        if (!wasRead.get()) {
            return "custom-split-id"
        }
        return null
    }

    override fun nextRecordFromSplit(): Long? {
        if (!wasRead.get()) {
            wasRead.set(true)
            return value
        }
        return null
    }

    override fun finishedSplits(): MutableSet<String> {
        logger.debug("Getting the finished splits")
        return mutableSetOf()
    }

}

class CustomRecordEmitter : RecordEmitter<Long, Long, Int>, Logging {
    override fun emitRecord(element: Long, output: SourceOutput<Long>, splitState: Int) {
        logger.debug("Emitting element: $element")
        val timestamp = System.currentTimeMillis()
        output.collect(element, timestamp)
    }
}

class CustomSplitReader(private val context: SourceReaderContext) : SplitReader<Long, CustomSourceSplit>, Logging {
    private lateinit var split: CustomSourceSplit

    override fun close() {
        logger.debug("Closing split reader")
    }

    override fun fetch(): RecordsWithSplitIds<Long> {
        logger.debug("Fetching data for subtask ${context.indexOfSubtask}")
        split.counter++
        sleep(1000)
        return CustomRecordsWithSplitIds(split.counter)
    }

    override fun wakeUp() {
        logger.debug("waking up the split reader")
    }

    override fun handleSplitsChanges(splitsChanges: SplitsChange<CustomSourceSplit>) {
        split = splitsChanges.splits().first()
        logger.debug("Handling the splits change adding the split: ${split.toString()} for subtask ${context.indexOfSubtask}")
    }

}


class CustomSourceReader(
    private val context: SourceReaderContext,
    private val configuration: Configuration,
    private val supplier: Supplier<SplitReader<Long, CustomSourceSplit>>
) :
    SingleThreadMultiplexSourceReaderBase<Long, Long, CustomSourceSplit, Int>(
        supplier,
        CustomRecordEmitter(),
        configuration,
        context
    ), Logging {

    override fun onSplitFinished(finishedSplitIds: MutableMap<String, Int>) {
        logger.debug("finished consuming all splits $finishedSplitIds")
    }

    override fun initializedState(split: CustomSourceSplit): Int {
        logger.debug("Initializing the state of the split")
        return 0
    }

    override fun toSplitType(splitId: String, splitState: Int): CustomSourceSplit {
        logger.debug("Converting the split id $splitId and the split state $splitState")
        return CustomSourceSplit(splitState.toLong())
    }
}

class CustomSourceEnumerator(private val context: SplitEnumeratorContext<CustomSourceSplit>) :
    SplitEnumerator<CustomSourceSplit, Long>, Logging {
    override fun close() {
        logger.debug("Closing the split enumerator")
    }

    override fun start() {
        logger.debug("Starting the split enumerator")
    }

    override fun handleSplitRequest(subtaskId: Int, requesterHostname: String?) {
        logger.debug("Handling the split request $subtaskId and $requesterHostname")
    }

    override fun addSplitsBack(splits: MutableList<CustomSourceSplit>?, subtaskId: Int) {
        logger.debug("Handling the splits back $splits and $subtaskId")
    }

    override fun addReader(subtaskId: Int) {
        context.assignSplit(CustomSourceSplit(0), subtaskId)
        logger.debug("Adding the reader $subtaskId")
    }

    override fun snapshotState(checkpointId: Long): Long {
        logger.debug("Handling doing the $checkpointId")
        return 0
    }
}


class CustomSource(private val configuration: Configuration) : Source<Long, CustomSourceSplit, Long>, Logging {

    override fun createReader(readerContext: SourceReaderContext): SourceReader<Long, CustomSourceSplit> {
        logger.debug("creating the source reader")
        val supplier = Supplier<SplitReader<Long, CustomSourceSplit>> { CustomSplitReader(readerContext) }
        return CustomSourceReader(readerContext, configuration, supplier)
    }

    override fun getBoundedness(): Boundedness =
        Boundedness.CONTINUOUS_UNBOUNDED


    override fun createEnumerator(
        enumContext: SplitEnumeratorContext<CustomSourceSplit>
    ): SplitEnumerator<CustomSourceSplit, Long> {
        return CustomSourceEnumerator(enumContext)
    }

    override fun restoreEnumerator(
        enumContext: SplitEnumeratorContext<CustomSourceSplit>,
        checkpoint: Long?
    ): SplitEnumerator<CustomSourceSplit, Long> {
        return CustomSourceEnumerator(enumContext)
    }

    override fun getSplitSerializer(): SimpleVersionedSerializer<CustomSourceSplit> {
        return object : SimpleVersionedSerializer<CustomSourceSplit> {
            override fun getVersion(): Int {
                return 1
            }

            override fun deserialize(version: Int, serialized: ByteArray): CustomSourceSplit {
                ByteArrayInputStream(serialized).use { bais ->
                    DataInputStream(bais).use {
                        val value = it.readLong()
                        return CustomSourceSplit(value)
                    }
                }
            }

            override fun serialize(obj: CustomSourceSplit): ByteArray {
                ByteArrayOutputStream().use { baos ->
                    DataOutputStream(baos).use { out ->
                        out.writeLong(obj.counter)
                        out.flush()
                        return baos.toByteArray()
                    }
                }
            }
        }
    }

    override fun getEnumeratorCheckpointSerializer(): SimpleVersionedSerializer<Long> {
        return object : SimpleVersionedSerializer<Long> {
            override fun getVersion(): Int {
                return 1
            }

            override fun deserialize(version: Int, serialized: ByteArray): Long {
                ByteArrayInputStream(serialized).use { bais ->
                    DataInputStream(bais).use {
                        val value = it.readLong()
                        return value
                    }
                }
            }

            override fun serialize(obj: Long): ByteArray {
                ByteArrayOutputStream().use { baos ->
                    DataOutputStream(baos).use { out ->
                        out.writeLong(obj)
                        out.flush()
                        return baos.toByteArray()
                    }
                }
            }
        }
    }
}