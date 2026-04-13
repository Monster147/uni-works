import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

fun main() {

    val serverPort = 12000

    val s = DatagramSocket(serverPort)
    val buffer = ByteArray(8192)

    while(true) {

        var packet = DatagramPacket(buffer, buffer.size)
        s.receive(packet)
        val message = String(packet.data.sliceArray(0..packet.length - 1))
        val address = packet.socketAddress

        println("Received the following request: " + message)
        println("Client address: " + address)

        val modifiedMessage = message.uppercase(Locale.getDefault()).toByteArray()

        packet = DatagramPacket(modifiedMessage, modifiedMessage.size, address)

        s.send(packet)
    }

    s.close()
}