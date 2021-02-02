package aBeLibs.extensions

import aBeLibs.math.Interpolator
import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

annotation class FFAction(val name: String, val ch: Int, val ccnum: Int)
annotation class FFSigned(val name: String, val ch: Int, val ccnum: Int)
annotation class FFDouble(val name: String, val ch: Int, val ccnum: Int)

class FaderFox : Extension {
    override var enabled: Boolean = true

    private val targets = mutableMapOf<Int, Any>()
    private val iptors = mutableListOf<Interpolator>()

    private val controller = try {
        MidiDeviceDescription.list().forEach { println(it) }
        MidiDeviceDescription.list().firstOrNull { it.name.contains("EC4") }?.run {
            MidiTransceiver.fromDeviceVendor(name, vendor)
        }
    } catch (e: IllegalArgumentException) {
        null
    }

    init {
        controller?.controlChanged?.postpone = true
        controller?.noteOn?.listen {
            println(it)
        }
        controller?.controlChanged?.listen {
            println("${it.channel} ${it.control} ${it.value}")

            when (val target = targets[it.channel * 256 + it.control]) {
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
                    is FFAction -> {
                        targets[a.ch * 256 + a.ccnum] = MidiActionFun(a.name, obj, it)
                    }
                    is FFSigned -> {
                        if (it is KMutableProperty<*>) {
                            val v = it.getter.call(obj)
                            if (v is Double) {
                                targets[a.ch * 256 + a.ccnum] = MidiSignedVar(a.name, obj, it)
                                sendValue(a.ch, a.ccnum, (v * 63.5 + 63.5).toInt())
                            }
                        } else if (it is KProperty) {
                            val v = it.getter.call(obj)
                            if (v is Interpolator) {
                                iptors.add(v)
                                targets[a.ch * 256 + a.ccnum] = MidiSignedVarSmooth(a.name, v)
                                sendValue(a.ch, a.ccnum, (v * 63.5 + 63.5).toInt())
                            }
                        }
                    }
                    is FFDouble -> {
                        if (it is KMutableProperty<*>) {
                            val v = it.getter.call(obj)
                            if (v is Double) {
                                targets[a.ch * 256 + a.ccnum] = MidiDoubleVar(a.name, obj, it)
                                sendValue(a.ch, a.ccnum, (v * 127).toInt())
                            } else {
                                println("Unknown var ${a.name} annotation $v")
                            }
                        } else if (it is KProperty) {
                            val v = it.getter.call(obj)
                            if (v is Interpolator) {
                                iptors.add(v)
                                targets[a.ch * 256 + a.ccnum] = MidiDoubleVarSmooth(a.name, v)
                                sendValue(a.ch, a.ccnum, (v * 127).toInt())
                            } else {
                                println("Unknown val ${a.name} annotation $v")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendValue(ch: Int, ccnum: Int, v: Int) {
        controller?.controlChange(ch, ccnum, v)
    }
}
