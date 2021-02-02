package aBeLibs.extensions

import aBeLibs.math.Interpolator
import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

annotation class Description(val name: String)
annotation class MFAction(val name: String, val ccnum: Int, val color: MidiFighter.Color)
annotation class MFSigned(val name: String, val ccnum: Int, val style: Int = 0)
annotation class MFDouble(val name: String, val ccnum: Int, val style: Int = 0)

class MidiFighter : Extension {
    override var enabled: Boolean = true

    private val targets = mutableMapOf<Int, Any>()
    private val iptors = mutableListOf<Interpolator>()

    @Suppress("unused")
    private enum class Type(val id: Int) {
        RING(0),
        BUTTON(1),
        RING_STYLE(2), // Ch. 3 RGB?
        SYS(3),
        RING_SHIFT(4),
        BUTTON_STYLE(5), // Ch. 6 RING?
        SEQ(7)
    }

    @Suppress("unused")
    enum class Color(val hue: Int, val bri: Int) {
        INDIGO(1, 35),
        BLUE(14, 40),
        AQUA(18, 47),
        FOREST_GREEN(43, 28),
        GREEN(47, 37),
        LIME(51, 47),
        LEMON(60, 43),
        GOLD(63, 31),
        YELLOW(63, 45),
        ORANGE(73, 47),
        CRIMSON(83, 29),
        RED(83, 41),
        MAGENTA(109, 34),
        PINK(108, 47),
        PURPLE(115, 34),
        VIOLET(116, 47),
        OFF(1, 17)
    }

    private val controller = try {
        MidiDeviceDescription.list().firstOrNull { it.name.contains("Twister") }?.run {
            MidiTransceiver.fromDeviceVendor(name, vendor)
        }
    } catch (e: IllegalArgumentException) {
        null
    }

    init {
        controller?.controlChanged?.postpone = true
        controller?.controlChanged?.listen {
            //println("${it.channel} ${it.control} ${it.value}")

            when (val target = targets[it.control]) {
                is MidiActionFun ->
                    if (it.value == 127) {
                        target.method.call(target.obj)
                    }
                is MidiSignedVar ->
                    target.prop.setter.call(target.obj, (it.value / 127.0) * 2 - 1)
                is MidiDoubleVar ->
                    target.prop.setter.call(target.obj, (it.value / 127.0))
                is MidiDoubleVarSmooth ->
                    target.iptor.targetValue = it.value / 127.0
                is MidiSignedVarSmooth ->
                    target.iptor.targetValue = (it.value / 127.0) * 2 - 1
            }
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        controller?.controlChanged?.deliver()
        iptors.forEach { it.getNext() }
    }

    fun add(obj: Any) {
        // Here we receive an object with properties and methods.
        // Both have annotations.

        // Anotations in methods include name, which ccnum is asociated with
        // that method, color and style in the midi controller for that knob.

        // Annotations in properties include name, associated ccnum and style.

        // println(obj::class.annotations) // description for obj
        obj::class.members.forEach {
            it.annotations.forEach { a ->
                when (a) {
                    // later when I receive a.ccnum
                    // a.name may be useful to create a web based gui
                    is MFAction -> {
                        targets[a.ccnum] = MidiActionFun(a.name, obj, it)
                        sendButtonColor(a.ccnum, a.color.hue)
                        sendButtonStyle(a.ccnum, a.color.bri)
                    }
                    is MFSigned -> {
                        if (it is KMutableProperty<*>) {
                            val v = it.getter.call(obj)
                            if (v is Double) {
                                targets[a.ccnum] = MidiSignedVar(a.name, obj, it)
                                sendValue(a.ccnum, (v * 63.5 + 63.5).toInt())
                                sendRingStyle(a.ccnum, a.style)
                            }
                        } else if (it is KProperty) {
                            val v = it.getter.call(obj)
                            if (v is Interpolator) {
                                iptors.add(v)
                                targets[a.ccnum] = MidiSignedVarSmooth(a.name, v)
                                sendValue(a.ccnum, (v * 63.5 + 63.5).toInt())
                                sendRingStyle(a.ccnum, a.style)
                            }
                        }
                    }
                    is MFDouble -> {
                        if (it is KMutableProperty<*>) {
                            val v = it.getter.call(obj)
                            if (v is Double) {
                                targets[a.ccnum] = MidiDoubleVar(a.name, obj, it)
                                sendValue(a.ccnum, (v * 127).toInt())
                                sendRingStyle(a.ccnum, a.style)
                            } else {
                                println("Unknown var ${a.name} annotation $v")
                            }
                        } else if (it is KProperty) {
                            val v = it.getter.call(obj)
                            if (v is Interpolator) {
                                iptors.add(v)
                                targets[a.ccnum] = MidiDoubleVarSmooth(a.name, v)
                                sendValue(a.ccnum, (v * 127).toInt())
                                sendRingStyle(a.ccnum, a.style)
                            } else {
                                println("Unknown val ${a.name} annotation $v")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendRingStyle(ccnum: Int, style: Int) {
        controller?.controlChange(Type.RING_STYLE.id, ccnum, style)
    }

    private fun sendButtonStyle(ccnum: Int, style: Int) {
        controller?.controlChange(Type.BUTTON_STYLE.id, ccnum, style)
    }

    private fun sendButtonColor(ccnum: Int, color: Int) {
        controller?.controlChange(Type.BUTTON.id, ccnum, color)
    }

    private fun sendValue(ccnum: Int, v: Int) {
        controller?.controlChange(Type.RING.id, ccnum, v)
    }

    fun setPage(page: Int) {
        controller?.controlChange(Type.SYS.id, page, 127)
    }
}
