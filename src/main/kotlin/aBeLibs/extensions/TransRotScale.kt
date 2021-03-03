package aBeLibs.extensions

import org.openrndr.Extension
import org.openrndr.MouseButton
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.transform

/**
 * A simple OPENRNDR that provides rotation, translation and scaling
 * by dragging the mouse with left / right buttons + scroll wheel.
 *
 * Add `extend(TransRotScale())` to your OPENRNDR program to use.
 */

class TransRotScale : Extension {
    override var enabled: Boolean = true

    var transform = Matrix44.IDENTITY
    var clickPos = Vector2.ZERO

    override fun setup(program: Program) {
        program.mouse.buttonDown.listen { clickPos = it.position }
        program.mouse.dragged.listen {
            transform = if(it.button == MouseButton.LEFT) {
                transform {
                    translate(it.dragDisplacement)
                }
            } else {
                transform {
                    translate(clickPos)
                    rotate(Vector3.UNIT_Z, it.dragDisplacement.y)
                    translate(-clickPos)
                }
            } * transform
        }
        program.mouse.scrolled.listen {
            transform = transform {
                translate(it.position)
                scale(1.0 + 0.1 * it.rotation.y)
                translate(-it.position)
            } * transform
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        drawer.view *= transform
    }
}