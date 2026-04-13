package pt.isel.pc.assignment1

import org.slf4j.*
import java.io.*
import java.net.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A TCP server that manages multiple client connections using structured concurrency.
 *
 * This server listens on multiple ports, accepts client connections, and handles both
 * reading and writing messages using a bounded stream. It ensures proper resource management
 * and supports graceful shutdown.
 *
 * ## Features:
 * - Supports multiple ports for accepting connections.
 * - Uses a [BoundedStream] to store and distribute messages.
 * - Manages threads using a structured [ThreadScope].
 * - Ensures proper cleanup and shutdown of resources.
 *
 * ## Lifecycle:
 * - The server is started with [run], which spawns threads to listen on the specified ports.
 * - Incoming connections are handled in [acceptLoop] and managed in separate client threads.
 * - Messages are read and written asynchronously using [readMessages] and [writeMessages].
 * - The server can be gracefully shut down using [close], ensuring all resources are released.
 *
 * @property ports The list of ports on which the server will listen for incoming connections.
 * @property capacity The capacity of the bounded message stream.
 */
class TCPServer(
    private val ports: List<Int>,
    capacity: Int,
) : Closeable {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TCPServer::class.java)
    }

    private val serverScope = ThreadScope("server", Thread.ofPlatform())
    private val boundedStream = BoundedStream<String>(capacity)
    private var isRunning = true
    private val serverSockets = mutableListOf<Socket>()
    private val connectionCounter = AtomicInteger(0)
    private val lock = ReentrantLock()

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
     */
    fun run() {
        ports.forEach {
            serverScope.startThread {
                acceptLoop(it)
            }
        }
    }

    /**
     * Accepts client connections on a specified port and assigns them to handlers.
     *
     * @param port The port number on which to accept incoming connections.
     */
    private fun acceptLoop(port: Int) {
        ServerSocket().use { serverSocket ->
            serverSocket.bind(InetSocketAddress("0.0.0.0", port))
            logger.info("Server socket bound to port {}", port)
            while (isRunning) {
                val clientSocket = serverSocket.accept()
                logger.info("Incoming connection accepted from {}", clientSocket.inetAddress.hostAddress)
                val connectionId = connectionCounter.incrementAndGet()
                lock.withLock {
                    serverSockets.add(clientSocket)
                }
                handleClient(clientSocket, connectionId)
            }
        }
    }

    /**
     * Manages a client connection by creating read and write threads.
     *
     * @param clientSocket The socket representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private fun handleClient(
        clientSocket: Socket,
        connectionId: Int,
    ) {
        val clientScope = serverScope.newChildScope("client-$connectionId") ?: return
        clientScope.startThread { readMessages(clientSocket, connectionId) }
        clientScope.startThread { writeMessages(clientSocket, connectionId) }
    }

    /**
     * Sends messages to the client, ensuring delivery of new messages stored in [BoundedStream].
     *
     * @param clientSocket The socket representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private fun writeMessages(
        clientSocket: Socket,
        connectionId: Int,
    ) {
        try {
            var lastIndex = 0
            clientSocket.getOutputStream().bufferedWriter().use { writer ->
                writer.write("Welcome to the server! You are connection number $connectionId")
                writer.newLine()
                writer.flush()
                while (isRunning) {
                    val result = boundedStream.read(lastIndex, Duration.INFINITE)
                    if (result is BoundedStream.ReadResult.Success) {
                        for (msg in result.items) {
                            writer.write(msg)
                            writer.newLine()
                            writer.flush()
                            logger.info("Client $connectionId sent: $msg")
                        }
                        lastIndex = result.startIndex.toInt() + result.items.size
                    }
                }
            }
        } catch (e: IOException) {
            if (!Thread.currentThread().isInterrupted && !clientSocket.isClosed) { // Log only unexpected errors
                logger.error("Error writing to connection {}", connectionId, e)
            }
        } finally {
            clientSocket.close()
        }
    }

    /**
     * Reads messages from the client and writes them to the shared [BoundedStream].
     *
     * @param clientSocket The socket representing the client connection.
     * @param connectionId A unique identifier for the connection.
     */
    private fun readMessages(
        clientSocket: Socket,
        connectionId: Int,
    ) {
        try {
            clientSocket.getInputStream().bufferedReader().use { reader ->
                while (isRunning) {
                    val line = reader.readLine() ?: break // End of stream
                    val message = "[$connectionId]: $line"
                    logger.info("Received from client $connectionId: $line")
                    boundedStream.write(message) // Write to the shared BoundedStream
                }
            }
        } catch (e: IOException) {
            if (!Thread.currentThread().isInterrupted && !clientSocket.isClosed) { // Log only unexpected errors
                logger.error("Error reading from connection {}", connectionId, e)
            }
        } finally {
            clientSocket.close()
        }
    }

    /**
     * Gracefully shuts down the server, closing all client connections and stopping all threads.
     */
    override fun close() {
        isRunning = false
        boundedStream.close()
        lock.withLock {
            for (socket in serverSockets) {
                try {
                    if (!socket.isClosed) {
                        socket.close()
                    }
                } catch (e: Exception) {
                    logger.warn("Error closing socket: ${e.message}")
                }
            }
            serverSockets.clear()
        }
        serverScope.cancel()
        logger.info("All threads completed.")
    }
}
