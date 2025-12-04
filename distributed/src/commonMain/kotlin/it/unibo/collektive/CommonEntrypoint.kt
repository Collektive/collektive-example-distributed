package it.unibo.collektive

import io.github.oshai.kotlinlogging.KotlinLogging
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.ids
import it.unibo.collektive.network.MqttMailbox
import it.unibo.collektive.networking.Mailbox
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEFAULT_DEVICE_COUNT = 50
private val DEFAULT_ROUND_TIME = 1.seconds
private val DEFAULT_EXECUTE_FOR = 60.seconds

/**
 * Defines the aggregate program logic for each device in the distributed system.
 *
 * This program collects the IDs of all neighboring devices that are discovered through
 * the MQTT network communication.
 *
 * @param id The unique identifier of this device.
 * @param network The mailbox used for network communication with other devices.
 * @return A Collektive instance that computes and returns the set of neighbor IDs.
 */
fun aggregateProgram(id: Int, network: Mailbox<Int>): Collektive<Int, Collection<Int>> = Collektive(id, network) {
    neighborhood().neighbors.ids.set
}

/**
 * Main entry point for running a distributed Collektive system with multiple devices.
 *
 * Creates and manages a network of devices that communicate via MQTT. Each device runs
 * the aggregate program in a loop, either synchronously with a fixed round time or
 * asynchronously triggered by incoming network messages.
 *
 * @param roundTime The duration between aggregate computation rounds when running synchronously.
 *                  Default is 1 second.
 * @param executeFor The total duration for which the system should run before shutting down.
 *                   Default is 60 seconds.
 * @param startDeviceId The ID of the first device. Subsequent devices will have sequential IDs.
 *                      Default is 0.
 * @param deviceCount The total number of devices to create in the network. Default is 50.
 * @param asyncNetwork If true, devices run aggregate computations asynchronously when messages
 *                     are received. If false, devices run at fixed intervals defined by [roundTime].
 *                     Default is false.
 * @param dispatcher The coroutine dispatcher to use for executing device computations.
 */
suspend fun mainEntrypoint(
    roundTime: Duration = DEFAULT_ROUND_TIME,
    executeFor: Duration = DEFAULT_EXECUTE_FOR,
    startDeviceId: Int = 0,
    deviceCount: Int = DEFAULT_DEVICE_COUNT,
    asyncNetwork: Boolean = false,
    dispatcher: CoroutineDispatcher,
) = coroutineScope {
    val logger = KotlinLogging.logger("Entrypoint")
    logger.info { "Starting Collektive with $deviceCount devices" }
    val jobRefs = mutableSetOf<Job>()
    val networks = mutableSetOf<MqttMailbox>()
    // Create a network of devices
    for (id in startDeviceId until (startDeviceId + deviceCount)) {
        val job =
            launch {
                val network = MqttMailbox(id, "test.mosquitto.org", dispatcher = dispatcher)
                logger.info { "Fdfdfdfdf" }
                networks.add(network)
                val program = aggregateProgram(id, network)
                when (asyncNetwork) {
                    true ->
                        network.neighborsMessageFlow().collect {
                            val result = program.cycle()
                            logger.info { "For device $id: $result" }
                        }

                    false -> {
                        while (true) {
                            val result = program.cycle()
                            logger.info { "For device $id: $result" }
                            delay(roundTime)
                        }
                    }
                }
            }
        jobRefs.add(job)
    }
    delay(executeFor)
    // Gracefully shutdown
    jobRefs.forEach { it.cancel() }
    networks.forEach { it.close() }
    logger.info { "Bye, Collektive!" }
}
