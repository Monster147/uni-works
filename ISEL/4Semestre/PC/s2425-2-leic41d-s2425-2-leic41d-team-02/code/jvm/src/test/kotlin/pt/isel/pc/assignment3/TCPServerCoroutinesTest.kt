package pt.isel.pc.assignment3

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.net.Socket
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class TCPServerCoroutinesTest {
    private lateinit var server: TCPServerCoroutines
    private val ports = listOf(9000, 9001)
    private val capacity = 10
    private val testScope = CoroutineScope(Dispatchers.IO)

    @BeforeTest
    fun setup() =
        runTest {
            server = TCPServerCoroutines(ports, capacity)
            server.run()
            delay(100)
        }

    @AfterTest
    fun tearDown() =
        runTest {
            server.close()
            testScope.cancel()
        }

    @Test
    fun `basic functional test - single client`() =
        runTest {
            withTimeout(10.seconds) {
                val clientSocket = Socket("localhost", ports[0])
                val reader = clientSocket.getInputStream().bufferedReader()
                val writer = clientSocket.getOutputStream().bufferedWriter()

                val welcomeMessage = withTimeout(1.seconds) { reader.readLine() }
                assertTrue(welcomeMessage!!.contains("Welcome to the server!"))

                writer.write("Hello from client 1\n")
                writer.flush()

                val broadcastedMessage = withTimeout(1.seconds) { reader.readLine() }
                assertEquals("[1]: Hello from client 1", broadcastedMessage)

                clientSocket.close()
            }
        }

    @Test
    fun `client can send multiple messages`() =
        runTest {
            withTimeout(10.seconds) {
                val clientSocket = Socket("localhost", ports[0])
                val reader = clientSocket.getInputStream().bufferedReader()
                val writer = clientSocket.getOutputStream().bufferedWriter()

                reader.readLine() // Welcome

                writer.write("First message\n")
                writer.flush()
                delay(100)
                assertEquals("[1]: First message", reader.readLine())

                writer.write("Second message\n")
                writer.flush()
                delay(100)
                assertEquals("[1]: Second message", reader.readLine())

                clientSocket.close()
            }
        }

    @Test
    fun `server handles client disconnection gracefully`() =
        runTest {
            withTimeout(10.seconds) {
                val clientSocket = Socket("localhost", ports[0])
                val reader = clientSocket.getInputStream().bufferedReader()
                val writer = clientSocket.getOutputStream().bufferedWriter()

                reader.readLine()

                writer.write("Goodbye\n")
                writer.flush()
                assertEquals("[1]: Goodbye", reader.readLine())

                clientSocket.close()

                val newClient = Socket("localhost", ports[0])
                assertTrue(
                    newClient
                        .getInputStream()
                        .bufferedReader()
                        .readLine()
                        .contains("Welcome"),
                )
                newClient.close()
            }
        }

    @Test
    fun `stress test with many clients on same ports`() =
        runTest {
            val port = ports[0]
            val numClients = 10

            withTimeout(30.seconds) {
                val clients =
                    List(numClients) { idx ->
                        val client = Socket("localhost", port)
                        val reader = client.getInputStream().bufferedReader()
                        val writer = client.getOutputStream().bufferedWriter()
                        Triple(client, reader, writer)
                    }

                clients.forEach { (_, reader, _) ->
                    assertTrue(reader.readLine()!!.contains("Welcome"))
                }

                clients.forEachIndexed { idx, (_, _, writer) ->
                    writer.write("Hello from client ${idx + 1}\n")
                    writer.flush()
                }
                delay(500)

                clients.forEach { (_, reader, _) ->
                    repeat(numClients) {
                        val msg = reader.readLine()
                        assertNotNull(msg)
                        assertTrue(msg.startsWith("[") && msg.contains("]: Hello from client"))
                    }
                }

                clients.forEach { (client, _, _) ->
                    client.close()
                }
            }
        }
}

/*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import pt.isel.pc.utils.waitUntilListening
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test

class TCPServerCoroutinesIntegrationTest {
    data class ReceivedMessage(val clientId: Int, val message: String)

    companion object {
        const val N_OF_CLIENTS = 5
        const val N_OF_MESSAGES_PER_CLIENT = 7
    }

    @Test
    fun `clients are handled concurrently by coroutine server`() = runBlocking {
        val port = 9000
        val server = TCPServerCoroutines(listOf(port), 100)
        val serverJob = launch(Dispatchers.IO) { server.run() }
        assertTrue(waitUntilListening(InetSocketAddress("localhost", port)))

        val receivedMessages = LinkedBlockingQueue<ReceivedMessage>()

        val clientJobs = List(N_OF_CLIENTS) { clientId ->
            launch(Dispatchers.IO) {
                clientBehaviour(InetSocketAddress("localhost", port), clientId, receivedMessages)
            }
        }

        clientJobs.forEach { it.join() }

        val list = receivedMessages.toList()
        assertEquals(N_OF_CLIENTS * N_OF_MESSAGES_PER_CLIENT, list.size)

        repeat(N_OF_CLIENTS) { ix ->
            val startIx = ix * N_OF_MESSAGES_PER_CLIENT
            val sublist = list.slice(startIx until (startIx + N_OF_CLIENTS))
            val clientIdSet = sublist.map { it.clientId }.toSet()
            assertTrue(clientIdSet.size > 1)
        }

        server.close()
        serverJob.cancelAndJoin()
    }

    private fun clientBehaviour(
        serverAddress: InetSocketAddress,
        clientId: Int,
        receivedMessages: LinkedBlockingQueue<ReceivedMessage>
    ) {
        Socket().use { socket ->
            socket.connect(serverAddress)
            socket.getInputStream().bufferedReader().use { reader ->
                socket.getOutputStream().bufferedWriter().use { writer ->
                    reader.readLine()
                    repeat(N_OF_MESSAGES_PER_CLIENT) { messageIx ->
                        val msgToSend = "message - $messageIx"
                        writer.write(msgToSend + "\n")
                        writer.flush()
                        val receivedMessage = reader.readLine()
                        receivedMessages.add(ReceivedMessage(clientId, receivedMessage))
                        Thread.sleep(1)
                    }
                }
            }
        }
    }
}*/