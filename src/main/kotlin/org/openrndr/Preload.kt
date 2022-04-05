package org.openrndr

import org.openrndr.extensions.Screenshots
import org.openrndr.ffmpeg.ScreenRecorder
    
/**
 * id: 89746068-c110-4131-bbec-1b6dd2f18a07
 * description: New sketch
 * tags: #new
 */    

class Preload : ApplicationPreload() {
//    override fun onConfiguration(configuration: Configuration) {
//        configuration.width = 1200
//        configuration.height = 600
//    }

    override fun onProgramSetup(program: Program) {
        program.apply {
            extend(Screenshots())
            // TODO: add Screenshots() to all programs (key = space)

            //extend(ScreenRecorder())
            // TODO: add Screenrecorder() to all programs
            // how to start, pause, end? with high frameSkip value, which we toggle
            // by pressing the v key. Would be nice to show a red circle when recording.

            keyboard.keyDown.listen {
                when (it.key) {
                    KEY_ESCAPE -> program.application.exit()
                }
            }

        }
    }
}
