package it.unibo.collektive

import kotlinx.coroutines.Dispatchers

/**
 * Entry point for the JavaScript/Node.js platform of the Collektive distributed example.
 *
 * Starts 2 devices with IDs starting from 2, using the Default dispatcher for coroutine execution.
 * The devices communicate via MQTT to discover neighbors and exchange aggregate computation results.
 */
suspend fun main() {
    mainEntrypoint(
        startDeviceId = 2,
        deviceCount = 2,
        dispatcher = Dispatchers.Default,
    )
}
