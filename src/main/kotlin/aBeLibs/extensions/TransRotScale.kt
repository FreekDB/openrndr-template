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
 * A simple OPENRNDR that provides zoom, translation and rotation
 * by dragging the mouse with left / right buttons + scroll wheel.
 *
 * I created this because sometimes my generative designs
 * turn out too large or too small for the window.
 *
 * I think it's also a beautiful example for how much one can do
 * with very few lines of code.
 *
 * Usage: `extend(TransRotScale())`
 */

class TransRotScale : Extension {
    override var enabled: Boolean = true

    var viewMat = Matrix44.IDENTITY
    var dragStart = Vector2.ZERO

    override fun setup(program: Program) {
        program.mouse.buttonDown.listen { dragStart = it.position }
        program.mouse.dragged.listen {
            viewMat = transform {
                if (it.button == MouseButton.LEFT) {
                    translate(it.dragDisplacement)
                } else {
                    translate(dragStart)
                    rotate(Vector3.UNIT_Z, it.dragDisplacement.y)
                    translate(-dragStart)
                }
            } * viewMat
        }
        program.mouse.scrolled.listen {
            viewMat = transform {
                translate(it.position)
                scale(1.0 + 0.1 * it.rotation.y)
                translate(-it.position)
            } * viewMat
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        drawer.view *= viewMat
    }
}