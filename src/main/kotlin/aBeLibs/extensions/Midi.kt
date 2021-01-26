package aBeLibs.extensions

import aBeLibs.math.Interpolator
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty

data class MidiActionFun(val name: String, val obj: Any, val method: KCallable<*>)
data class MidiSignedVar(val name: String, val obj: Any, val prop: KMutableProperty<*>)
data class MidiDoubleVar(val name: String, val obj: Any, val prop: KMutableProperty<*>)
data class MidiSignedVarSmooth(val name: String, val iptor: Interpolator)
data class MidiDoubleVarSmooth(val name: String, val iptor: Interpolator)
