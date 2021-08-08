package apps.simpleTests

import org.openrndr.applicationSynchronous
import org.openrndr.extra.osc.OSC


fun main() = applicationSynchronous {
    configure {
        width = 600
        height = 150
        hideWindowDecorations = true
    }

    program {
        val osc = OSC(portIn = 9996, portOut = 9997)

        osc.listen("/speed") {
            println(it)
        }

        extend {
            //osc.send("/speed", Random.double0())
        }
    }
}
