    
/**
 * id: c8ec3faf-e02f-4127-9075-56e62c975331
 * description: New sketch
 * tags: #new
 */    
package apps.simpleTests

//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonConfiguration

/**
 * Interesting that kotlinx.serialization runs init {} when deserializing.
 * That is useful.
 * But I can't figure out how to serialize more complex objects, like a list
 * of Vector2 or `lateinit var guiState: Map<String, Map<String, aBeLibs.gui.GUI.ParameterValue>>`
 * So I'll leave this for now.
 */

//@Serializable
//data class Data(val a: Int, val b: String = "42") {
//    var pieces = mutableListOf<Double>()
//    init {
//        println("init")
//    }
//}
//
//fun main() {
//    val json = Json(JsonConfiguration.Stable)
//
//    var d = Data(42)
//    d.pieces.add(3.14)
//    d.pieces.add(7.29)
//
//    val jsonData = json.stringify(Data.serializer(), d)
//    println(jsonData)
//
//    val d2 = json.parse(Data.serializer(), """{"a":42}""") // b is optional since it has default value
//    println(d2)
//}