package pt.isel.pc.assignment3

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pt.isel.pc.assignment3.BoundedStreamSuspend.ReadResult
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

/**
 * A coroutine-based TCP server that manages multiple client connections using asynchronous socket channels.
 *
 * This server listens on multiple ports, accepts client connections, and handles both
 * reading and writing messages using a bounded stream. It leverages Kotlin coroutines
 * and Java NIO asynchronous channels to efficiently handle many connections with a constant
 * number of threads.
 *
 * ## Features:
 * - Supports multiple ports for accepting connections.
 * - Uses a [BoundedStreamSuspend] to store and distribute messages.
 * - Uses coroutines and asynchronous NIO channels for efficient, non-blocking I/O.
 * - Ensures proper cleanup and shutdown of resources.
 *
 * ## Lifecycle:
 * - The server is started with [run], which launches coroutines to listen on the specified ports.
 * - Incoming connections are accepted and handled in [acceptLoop], spawning coroutines per client.
 * - Messages are read and written asynchronously using [readMessages] and [writeMessages].
 * - The server can be gracefully shut down using [close], ensuring all resources are released.
 *
 * @property ports The list of ports on which the server will listen for incoming connections.
 * @property capacity The capacity of the bounded message stream.
 */
class TCPServerCoroutines(
    private val ports: List<Int>,
    capacity: Int,
) : Closeable {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TCPServerCoroutines::class.java)
    }

    private val serverScope = CoroutineScope(Dispatchers.IO)
    private val boundedStream = BoundedStreamSuspend<String>(capacity)
    private var isRunning = true
    private val serverChannels = mutableListOf<AsynchronousServerSocketChannel>()
    private val serverSockets = mutableListOf<AsynchronousSocketChannel>()
    private val connectionCounter = AtomicInteger(0)
    private val mutex = Mutex()

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Server shutdown hook triggered. Closing Server...")
                close()
            },
        )
    }

    /**
     * Starts the server, listening for incoming connections on all specified ports.
     *
     * Launches a coroutine per port for accepting connections.
     */
    fun run() =
        runBlocking {
            ports.forEach {
                serverScope.launch {
                    acceptLoop(it)
                }
            }
        }

    /**
     * Accepts client connections on a specified port and assigns them to handlers.
     *
     * @param port The port number on which to accept incoming connections.
     */
    private suspend fun acceptLoop(port: Int) {
        val serverChannel = AsynchronousServerSocketChannel.open().bind(InetSocketAddress(port))
        logger.info("Server socket bound to port {}", port)
        mutex.withLock {
            serverChannels.add(serverChannel)
        }
        try {
            while (isRunning) {
                val clientChannel = serverChannel.acceptAsync()
                logger.info("Incoming connection accepted from {}", clientChannel.remoteAddress)
                val connectionId = connectionCounter.incrementAndGet()
                mutex.withLock {
                    serverSockets.add(clientChannel)
                }
                serverScope.launch { handleClient(clientChannel, connectionId) }
            }
        } catch (e: AsynchronousCloseException) {
            logger.debug("Server socket on port $port closed while accepting connections")
        } catch (e: Exception) {
            logger.error("Error accepting connection on port $port", e)
        } finally {
            serverChannel.close()
            mutex.withLock {
                serverChannels.remove(serverChannel)
            }
        }
    }

    /**
     * Manages a client connection by launching read and write coroutines.
     *
     * @param clientSocket The socket representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private suspend fun handleClient(
        clientSocket: AsynchronousSocketChannel,
        connectionId: Int,
    ) {
        val readJob = serverScope.launch { readMessages(clientSocket, connectionId) }
        val writeJob = serverScope.launch { writeMessages(clientSocket, connectionId) }
        joinAll(readJob, writeJob)
        clientSocket.close()
        mutex.withLock { serverSockets.remove(clientSocket) }
    }

    /**
     * Sends messages to the client, ensuring delivery of new messages stored in [BoundedStreamSuspend].
     *
     * @param clientChannel The channel representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private suspend fun writeMessages(
        clientChannel: AsynchronousSocketChannel,
        connectionId: Int,
    ) {
        val welcomeMsg = "Welcome to the server! You are connection number $connectionId\r\n"
        suspendWrite(clientChannel, welcomeMsg)
        var lastIndex = 0
        while (isRunning && clientChannel.isOpen) {
            when (val result = boundedStream.read(lastIndex, 500.milliseconds)) {
                is ReadResult.Success -> {
                    for (msg in result.items) {
                       /* Client doenst see its own messages with this code
                        val prefix = "[$connectionId]: "
                        if (!msg.startsWith(prefix)) {
                            suspendWrite(clientChannel, "$msg\r\n")
                        }
                        */
                        suspendWrite(clientChannel, msg + "\r\n") // Client sees its own messages with this code
                        logger.info("Client {} sent: {}", connectionId, msg)
                    }
                    lastIndex = result.startIndex.toInt() + result.items.size
                }
                is ReadResult.Timeout -> continue
                is ReadResult.Closed -> break
            }
        }
    }

    /**
     * Reads messages from the client and writes them to the shared [BoundedStreamSuspend].
     *
     * @param clientSocket The channel representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private suspend fun readMessages(
        clientSocket: AsynchronousSocketChannel,
        connectionId: Int,
    ) {
        while (isRunning && clientSocket.isOpen) {
            val line = suspendReadLine(clientSocket) ?: break
            if (line.isNotBlank()) {
                val message = "[$connectionId]: $line"
                logger.info("Received from client {}: {}", connectionId, line)
                boundedStream.write(message)
            }
        }
    }

    /**
     * Gracefully shuts down the server, closing all client connections and stopping all coroutines.
     *
     * Closes all server and client channels, clears resources, and cancels the server coroutine scope.
     */
    override fun close() {
        isRunning = false
        boundedStream.close()
        synchronized(serverChannels) {
            serverChannels.forEach { channel ->
                try {
                    channel.close()
                } catch (e: IOException) {
                    logger.error("Error closing channel: {}", e.message)
                }
            }
            serverSockets.clear()
        }
        synchronized(serverSockets) {
            serverSockets.forEach { socket ->
                try {
                    socket.close()
                } catch (e: IOException) {
                    logger.error("Error closing socket: {}", e.message)
                }
            }
            serverSockets.clear()
        }
        serverScope.cancel()
        logger.info("All resources released.")
    }

    /**
     * Suspends until a client connection is accepted on this [AsynchronousServerSocketChannel].
     *
     * @receiver The server socket channel to accept connections from.
     * @return The accepted [AsynchronousSocketChannel].
     */
    private suspend fun AsynchronousServerSocketChannel.acceptAsync(): AsynchronousSocketChannel =
        suspendCoroutine { cont ->
            accept(
                null,
                object : CompletionHandler<AsynchronousSocketChannel, Nothing?> {
                    override fun completed(
                        result: AsynchronousSocketChannel,
                        attachment: Nothing?,
                    ) {
                        cont.resume(result)
                    }

                    override fun failed(
                        exc: Throwable,
                        attachment: Nothing?,
                    ) {
                        cont.resumeWithException(exc)
                    }
                },
            )
        }

    /**
     * Suspends until the entire [message] is written to the [channel].
     *
     * @param channel The channel to write to.
     * @param message The message to send.
     */
    private suspend fun suspendWrite(
        channel: AsynchronousSocketChannel,
        message: String,
    ) {
        val bytes = message.toByteArray(Charsets.UTF_8)
        val buffer = ByteBuffer.wrap(bytes)
        while (buffer.hasRemaining()) {
            suspendCoroutine { cont ->
                channel.write(
                    buffer,
                    null,
                    object : CompletionHandler<Int, Nothing?> {
                        override fun completed(
                            result: Int,
                            attachment: Nothing?,
                        ) {
                            if (buffer.hasRemaining()) {
                                channel.write(buffer, null, this)
                            } else {
                                cont.resume(Unit)
                            }
                        }

                        override fun failed(
                            exc: Throwable,
                            attachment: Nothing?,
                        ) {
                            cont.resumeWithException(exc)
                        }
                    },
                )
            }
        }
    }

    /**
     * Suspends until a line of text is read from the [channel].
     *
     * @param channel The channel to read from.
     * @return The next line of text, or null if end of stream is reached.
     */
    private suspend fun suspendReadLine(channel: AsynchronousSocketChannel): String? {
        val byteBuffer = ByteBuffer.allocate(1024)
        val lineBuffer = StringBuilder()
        val charset = Charsets.UTF_8
        while (true) {
            val bytesRead = suspendRead(channel, byteBuffer)
            if (bytesRead == -1) return if (lineBuffer.isNotEmpty()) lineBuffer.toString() else null
            byteBuffer.flip()
            val chars = charset.decode(byteBuffer).toString()
            byteBuffer.clear()
            for (ch in chars) {
                if (ch == '\n') {
                    val line = lineBuffer.toString()
                    return line
                } else {
                    lineBuffer.append(ch)
                }
            }
        }
    }

    /**
     * Suspends until bytes are read from the [channel] into the [buffer].
     *
     * @param channel The channel to read from.
     * @param buffer The buffer to fill.
     * @return The number of bytes read, or -1 if end of stream.
     */
    private suspend fun suspendRead(
        channel: AsynchronousSocketChannel,
        buffer: ByteBuffer,
    ): Int =
        suspendCoroutine { cont ->
            channel.read(
                buffer,
                null,
                object : CompletionHandler<Int, Nothing?> {
                    override fun completed(
                        result: Int,
                        attachment: Nothing?,
                    ) {
                        cont.resume(result)
                    }

                    override fun failed(
                        exc: Throwable,
                        attachment: Nothing?,
                    ) {
                        cont.resumeWithException(exc)
                    }
                },
            )
        }
}

fun main() {
    runBlocking {
        val ports = listOf(9000, 9001)
        val capacity = 100

        val server = TCPServerCoroutines(ports, capacity)
        server.run()

        println("Server started. Press Enter to stop.")
        readLine()

        server.close()
    }
}
