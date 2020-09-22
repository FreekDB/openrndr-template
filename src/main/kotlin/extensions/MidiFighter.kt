package extensions

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.instanceParameter

annotation class Description(val name: String)
annotation class MidiAction(val name: String, val ccnum: Int, val color: Int, val style: Int)
annotation class MidiSigned(val name: String, val ccnum: Int, val detent: Boolean, val style: Int)
annotation class MidiDouble(val name: String, val ccnum: Int)

data class MidiActionFun(val name: String, val obj: Any, val method: KCallable<*>)
data class MidiSigneds(val name:String, val obj: Any, val method: KCallable<*>)

class MidiFighter : Extension {
    override var enabled: Boolean = true

    private val midiActions = mutableMapOf<Int, MidiActionFun>()
    private val midiSigned = mutableMapOf<Int, MidiSigneds>()

    private val mf = try {
        MidiDeviceDescription.list().firstOrNull() { it.name.contains("Twister") }?.run {
            MidiTransceiver.fromDeviceVendor(name, vendor)
        }
    } catch (e: IllegalArgumentException) {
        null
    }

    init {
        mf?.controlChanged?.postpone(true)
        mf?.controlChanged?.listen {
            // TODO: read the stored information and react
            // for example, if channel = 3, control = 22 and value = 127,
            // call a stored function here.
            // We can do 3 things: call a method, update a variable, or update
            // the target of an interpolator.
            println("${it.channel} ${it.control} ${it.value}")
            midiActions[it.control]?.run {
                if(it.value == 127) {
                    method.call(obj)
                }
            }
            midiSigned[it.control]?.run {
                println(it.value / 127.0)
                //var d = method.instanceParameter as KMutableProperty1<Any, Double>
                //d.set(obj, it.value / 127.0)
                //val property: KMutableProperty1<out Any, Any?>?
                //val p = parameter.property as KMutableProperty1<Any, Double>
                //::method.
                // https://kotlinlang.org/docs/reference/reflection.html
                println("$name ${method.parameters}")
            }
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        mf?.controlChanged?.deliver()
    }

    fun add(obj: Any) {
        // Here we receive an object with properties.
        // The properties have annotations indicating how we want
        // to interpret (ranges) the midi values. We want to change
        // those properties, either immediately, or by interpolating
        // TODO: store the received information in local collections that are
        // accessed when knobs are turned, in controlChanged above
        // println(obj::class.annotations) // description for obj
        obj::class.members.forEach { method ->
            method.annotations.forEach { a ->
                when (a) {
                    // later when I receive a.ccnum, call member
                    // a.name may be useful to create a web based gui
                    is MidiAction -> {
                        midiActions[a.ccnum] = MidiActionFun(a.name, obj, method)
                        // TODO: SEND a.style and a.color to controller
                    }
                    is MidiSigned -> {
                        midiSigned[a.ccnum] = MidiSigneds(a.name, obj, method)
                        //println("signed: ${a.name} ${a.ccnum} ${a.style} ${a.detent} $method")
                    }
                }
            }
        }
    }

    fun setPage(page: Int) {
        // TODO: send a message to the midi controller
    }
}
