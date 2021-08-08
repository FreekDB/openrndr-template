package aBeLibs.extensions

import org.openrndr.Extension
import org.openrndr.KeyModifier
import org.openrndr.Program
import org.openrndr.extensions.Screenshots

/**
 * OPENRNDR extension to enable
 * CTRL+C to copy the visible content to the clipboard on Linux or
 * other OSes providing the `xclip` command line program.
 *
 * Usage: `extend(ClipboardScreenshot())`
 * then press CTRL+C to copy what you see, paste the image somewhere.
 */

class ClipboardScreenshot : Extension {
    override var enabled: Boolean = true

    val screenshots = Screenshots().also {
        it.name = "/tmp/openrndr-screenshot.png"
        it.async = false
        it.afterScreenshot.listen { _ ->
            Runtime.getRuntime().exec(
                "xclip -selection clip -t image/png ${it.name}"
            )
        }
    }

    override fun setup(program: Program) {
        program.extend(screenshots)
        println("Listening to CTRL+C")
        program.keyboard.keyDown.listen {
            if (it.modifiers.contains(KeyModifier.CTRL) &&
                it.name == "c"
            ) screenshots.trigger()
        }
    }
}