package axi

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener

fun main() {
    val a = Axidraw()
//    if(!a.connected) {
//        return
//    }
//    println(a.version)

    val ports = SerialPort.getCommPorts()
    val axidraws = ports.filter {
        it.descriptivePortName.startsWith("EiBotBoard")
    }

    if (axidraws.isEmpty()) {
        println("No plotters found")
        return
    }

    println("Found ${axidraws.size} plotters")
    axidraws.forEachIndexed { i, it ->
        println("[$i] ${it.descriptivePortName}")
    }

    val axi = axidraws[0]
    axi.setComPortParameters(38400, 8, 1, 0)
    axi.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0)

    if (axi.openPort()) {
        println("Connection established to '${axi.descriptivePortName}'")
    } else {
        println("Connection failed")
        return
    }

    axi.addDataListener(object : SerialPortMessageListener{
        override fun getListeningEvents() = SerialPort.LISTENING_EVENT_DATA_RECEIVED
        override fun getMessageDelimiter() = "\r\n".toByteArray()
        override fun delimiterIndicatesEndOfMessage() = true
        override fun serialEvent(ev: SerialPortEvent) =
            println("Received: ${ev.receivedData}")
    })

    fun version() {
        axi.outputStream.write("V\r".toByteArray())
    }

    version()
    //axi.outputStream.flush()

    Thread.sleep(5000)
    axi.removeDataListener()

    if (axi.closePort()) {
        println("Connection closed")
    } else {
        println("Connection closing failed")
    }
}

class Axidraw {

}
