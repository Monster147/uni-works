import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

fun main() {

    val serverName = "localhost"
    val serverPort = 12000

    val s = DatagramSocket()

    while(true) {

        val message = readln().toByteArray()
        val packet = DatagramPacket(message, message.size, InetSocketAddress(serverName, serverPort))
        s.send(packet)

        s.receive(packet)
        val modifiedMessage = String(packet.data)
        println("Received the following answer: " + modifiedMessage)
        println("Server address: " + packet.socketAddress)

    }

    s.close()
}