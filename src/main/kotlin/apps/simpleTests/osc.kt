package apps.simpleTests

import org.openrndr.application
import org.openrndr.extra.osc.OSC

/**
 * id: 26b30568-5144-4121-b25b-b6217e007261
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    configure {
        width = 600
        height = 150
        hideWindowDecorations = true
    }

    program {
        val osc = OSC(portIn = 9996, portOut = 9997)

        osc.listen("/speed") { it, _ ->
            println(it)
        }

        extend {
            //osc.send("/speed", Random.double0())
        }
    }
}
