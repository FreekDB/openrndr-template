package apps4

import org.openrndr.application
import org.openrndr.extra.shaderphrases.ShaderPhrase
import org.openrndr.extra.shaderphrases.ShaderPhraseBook
import org.openrndr.extra.shaderphrases.preprocess

/**
 * id: a840d763-3a10-44e2-87c2-9b261bf88614
 * description: New sketch
 * tags: #new
 */

fun main() = application {
    program {
        val phrases =
            mapOf(
                "a" to "//this is a",
                "b" to "bool b() { return true; }",
            )

        val book = object : ShaderPhraseBook("csBook") {
            val white = ShaderPhrase(
                """
                vec4 white() { 
                    return vec4(1.0);
                }
                """.trimIndent()
            )
            val limit = ShaderPhrase(
                """
                vec2 limit(vec2 v, float max) {
                    if(dot(v, v) <= max * max) {
                        return v;
                    } else {
                        return normalize(v) * max;
                    }
                }                    
                """.trimIndent()
            )
        }
        book.register()

        println(
            processShaderPhrases(
                """
                    // start
                    #pragma import csBook.limit
                    #pragma phrase a
                    #pragma phrase b
                    #pragma import csBook.white
                    // end
                """.trimIndent().preprocess(), phrases
            )
        )

        extend { }
    }
}

fun processShaderPhrases(glsl: String, phrases: Map<String, String>) =
    glsl.split("\n").map {
        if (it.startsWith("#pragma phrase")) {
            val tokens = it.split(" ")
            val symbol = tokens[2].trim().replace(";", "")
            phrases[symbol]
        } else {
            it
        }
    }.joinToString("\n")
