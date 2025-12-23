package it.unibo.collektive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Entry point for the JVM platform of the Collektive distributed example.
 *
 * Starts 3 devices with IDs starting from 0, using the IO dispatcher for coroutine execution.
 * The devices communicate via MQTT to discover neighbors and exchange aggregate computation results.
 */
@Suppress("InjectDispatcher")
fun main() = runBlocking {
    mainEntrypoint(
        startDeviceId = 0,
        deviceCount = 3,
        dispatcher = Dispatchers.IO,
    )
}
