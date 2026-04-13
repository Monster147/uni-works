package pt.isel.pc.assignment1

import java.io.*
import java.net.Socket
import kotlin.test.*
import kotlin.test.Test

class TCPServerTest {
    private lateinit var server: TCPServer
    private val ports = listOf(9000, 9001)
    private val capacity = 10

    @BeforeTest
    fun setup() {
        server = TCPServer(ports, capacity)
        server.run()
    }

    @AfterTest
    fun tearDown() {
        server.close()
    }

    @Test
    fun `basic functional test - single client`() {
        val clientSocket = Socket("localhost", ports[0])
        val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

        val welcomeMessage = reader.readLine()
        assertTrue(welcomeMessage.contains("Welcome to the server!"))

        writer.write("Hello from client 1\n")
        writer.flush()

        val broadcastedMessage = reader.readLine()
        assertEquals("[1]: Hello from client 1", broadcastedMessage)

        clientSocket.close()
    }

    @Test
    fun `multiple clients can connect and communicate`() {
        val client1 = Socket("localhost", ports[0])
        val client2 = Socket("localhost", ports[1])

        val reader1 = BufferedReader(InputStreamReader(client1.getInputStream()))
        val writer1 = BufferedWriter(OutputStreamWriter(client1.getOutputStream()))
        val reader2 = BufferedReader(InputStreamReader(client2.getInputStream()))
        val writer2 = BufferedWriter(OutputStreamWriter(client2.getOutputStream()))

        reader1.readLine()
        reader2.readLine()

        writer1.write("Hello from client 1\n")
        writer1.flush()
        Thread.sleep(100)

        val welcomeMessageClient11 = reader1.readLine()
        val welcomeMessageClient12 = reader2.readLine()

        assertEquals("[1]: Hello from client 1", welcomeMessageClient11)
        assertEquals("[1]: Hello from client 1", welcomeMessageClient12)

        writer2.write("Hello from client 2\n")
        writer2.flush()
        Thread.sleep(100)

        val welcomeMessageClient21 = reader1.readLine()
        val welcomeMessageClient22 = reader2.readLine()

        assertEquals("[2]: Hello from client 2", welcomeMessageClient21)
        assertEquals("[2]: Hello from client 2", welcomeMessageClient22)

        client1.close()
        client2.close()
    }
}
