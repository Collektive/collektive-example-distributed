package it.unibo.collektive.network

import io.github.oshai.kotlinlogging.KotlinLogging
import it.nicolasfarabegoli.mktt.MkttClient
import it.nicolasfarabegoli.mktt.MqttQoS
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.SerializedMessage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * MQTT-based mailbox implementation for distributed device communication.
 *
 * This mailbox uses MQTT protocol to enable devices to discover neighbors and exchange
 * aggregate computation messages. Each device publishes its presence on a topic and subscribes
 * to topics for receiving messages from other devices.
 *
 * The mailbox manages:
 * - Device neighbor discovery through MQTT topic subscription
 * - Message publishing and receiving via MQTT
 * - Connection lifecycle to the MQTT broker
 *
 * @param deviceId The unique identifier of this device.
 * @param host The MQTT broker hostname or IP address.
 * @param port The MQTT broker port. Default is 1883 (standard MQTT port).
 * @param serializer The serialization format for encoding/decoding messages. Default is JSON.
 * @param retentionTime How long to retain received messages. Default is 5 seconds.
 * @param dispatcher The coroutine dispatcher for executing network operations. Default is Dispatchers.Default.
 */
@OptIn(ExperimentalTime::class)
class MqttMailbox private constructor(
    private val deviceId: Int,
    host: String,
    port: Int = 1883,
    private val serializer: SerialFormat = Json,
    retentionTime: Duration = 5.seconds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AbstractSerializableMailbox<Int>(deviceId, serializer, retentionTime) {
    private val logger = KotlinLogging.logger("${MqttMailbox::class.simpleName!!}@$deviceId")
    private val internalScope: CoroutineScope = CoroutineScope(dispatcher)
    private val client =
        MkttClient(dispatcher) {
            brokerUrl = host
            this.port = port
        }

    private suspend fun initializeMqttClient() {
        client.connect()
        logger.info { "Device $deviceId with clientId connected to MQTT Broker" }
        internalScope.launch(dispatcher) {
            client.subscribe("drone/+").collect {
                val neighborDeviceId =
                    it.topic
                        .split("/")
                        .last()
                        .toInt()
                addNeighbor(neighborDeviceId)
                logger.debug { "Device $neighborDeviceId registered as neighbor of $deviceId" }
            }
        }
        internalScope.launch(dispatcher) {
            while (true) {
                client.publish("drone/$deviceId", byteArrayOf())
                delay(5.seconds)
            }
        }
        logger.info { "Complete MQTT initialization for device $deviceId" }
    }

    private suspend fun registerReceiverListener(): Unit = coroutineScope {
        initializeMqttClient()
        // Listen for incoming messages
        internalScope.launch(dispatcher) {
            client.subscribe("drone/$deviceId/neighbors").collect {
                try {
                    val deserialized = serializer.decode(it.payload)
                    logger.debug { "Received new message from ${deserialized.senderId} to $deviceId" }
                    deliverableReceived(deserialized)
                } catch (exception: SerializationException) {
                    logger.error { "Error decoding message from ${it.topic}: ${exception.message}" }
                }
            }
        }
    }

    override suspend fun close() {
        internalScope.cancel()
        client.disconnect()
        logger.info { "$deviceId disconnected from HiveMQ" }
    }

    override fun onDeliverableReceived(receiverId: Int, message: Message<Int, Any?>) {
        internalScope.launch {
            logger.debug { "From device ${message.senderId} to $receiverId: $message" }
            val payload = serializer.encode(message as SerializedMessage<Int>)
            client.publish(
                topic = "drone/$receiverId/neighbors",
                qos = MqttQoS.AtLeastOnce,
                message = payload,
            )
        }
    }

    /**
     * Companion object providing factory methods for creating MqttMailbox instances.
     */
    companion object {
        /**
         * Creates and initializes a new MqttMailbox instance.
         *
         * This factory method constructs an MqttMailbox, establishes the MQTT connection,
         * and sets up all necessary subscriptions for neighbor discovery and message receiving.
         *
         * @param deviceId The unique identifier of this device.
         * @param host The MQTT broker hostname or IP address.
         * @param port The MQTT broker port. Default is 1883.
         * @param serializer The serialization format for messages. Default is JSON.
         * @param retentionTime How long to retain received messages. Default is 5 seconds.
         * @param dispatcher The coroutine dispatcher for network operations. Default is Dispatchers.Default.
         * @return A fully initialized MqttMailbox ready for communication.
         */
        suspend operator fun invoke(
            deviceId: Int,
            host: String,
            port: Int = 1883,
            serializer: SerialFormat = Json,
            retentionTime: Duration = 5.seconds,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
        ): MqttMailbox = coroutineScope {
            MqttMailbox(deviceId, host, port, serializer, retentionTime, dispatcher).apply {
                registerReceiverListener()
            }
        }
    }
}
