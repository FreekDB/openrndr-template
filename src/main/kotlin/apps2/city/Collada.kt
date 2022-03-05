package apps2.city

import org.openrndr.extra.noise.Random
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import java.io.File
import java.time.LocalDateTime

/**
 * id: 7790614c-cc7e-49fb-b906-65de5ae14e7c
 * description: New sketch
 * tags: #new
 */

class Collada {
    private val geometries = mutableListOf<String>()
    private val nodes = mutableListOf<String>()

    /**
     * Converts a 2D axis aligned rectangle to a 3D rectangle
     */
    private fun Rectangle.toVertices3D(z: Double = 0.0): String {
        val (right, bottom) = arrayOf(x + width, y + height)
        return "$x $y $z $right $y $z $right $bottom $z $x $bottom $z"
    }

    /**
     * Converts a 2D axis aligned rectangle to a 3D rectangle
     */
    private fun ShapeContour.toVertices3D(z: Double = 0.0): String {
        return this.segments.joinToString(" ") { "${it.start.x} ${it.start.y} $z" }
    }

    /**
     * Converts a 2D axis aligned rectangle to a 2D rectangle
     */
    private fun Rectangle.toVertices2D(): String {
        val (x0, y0, x1, y1) = arrayOf(x, y, x + width, y + height)
        return "$x0 $y0 $x1 $y0 $x1 $y1 $x0 $y1"
    }

    /**
     * -----------------------------------------------------------------
     * Adds a list of quads under one named mesh, each rectangle with
     * two UV maps (normally you would only need one UV map)
     * The three lists should probably be the same length.
     */
    @Suppress("unused")
    fun addQuads(name: String, shapes: List<Rectangle>, uv0: List<Rectangle>, uv1: List<Rectangle>) {
        val namePositions = "${name}-mesh-positions"
        val nameNormals = "${name}-mesh-normals"
        val nameUV0 = "${name}-mesh-map-0"
        val nameUV1 = "${name}-mesh-map-1"

        addNode(name)

        // language=XML prefix="<p>" suffix="</p>"
        geometries.add(
            """
    <geometry id="$name-mesh" name="$name">
      <mesh>
        ${xmlPositions(namePositions, shapes)}
        ${xmlNormals(nameNormals)}
        ${xmlUVMap(nameUV0, uv0)}
        ${xmlUVMap(nameUV1, uv1)}
  
        <vertices id="$name-mesh-vertices">
          <input semantic="POSITION" source="#$namePositions"/>
        </vertices>
        
        <polylist count="${shapes.size}">
          <input semantic="VERTEX" source="#$name-mesh-vertices" offset="0"/>
          <input semantic="NORMAL" source="#$nameNormals" offset="1"/>
          <input semantic="TEXCOORD" source="#$nameUV0" offset="2" set="0"/>
          <input semantic="TEXCOORD" source="#$nameUV1" offset="2" set="1"/>
          <vcount>${"4 ".repeat(shapes.size)}</vcount>
          <p>${List(shapes.size * 4) { "$it 0 $it " }.joinToString("")}</p>
        </polylist>
        
      </mesh>
    </geometry>            
            """
        )
    }

    /**
     * add node
     */
    private fun addNode(name: String) {
        // language=XML prefix="<p>" suffix="</p>"
        nodes.add(
            """
      <node id="$name" name="$name" type="NODE">
        <matrix sid="transform">1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1</matrix>
        <instance_geometry url="#$name-mesh" name="$name"/>
      </node> 
            """
        )
    }

    private fun linesToShapes(lines: List<LineSegment>, width: List<Double>): List<ShapeContour> {
        return lines.mapIndexed { i, segment ->
            val amt = segment.direction.normalized * 0.5
            val extended = LineSegment(segment.start - amt, segment.end + amt)
            val offset = segment.normal * width[i] / 2.0
            val points = listOf(
                extended.start - offset,
                extended.start + offset,
                extended.end + offset,
                extended.end - offset
            )
            ShapeContour.fromPoints(points, true)
        }
    }

    /**
     * -----------------------------------------------------------------
     * Adds a list of LineSegment under one named mesh, no UVs
     */
    fun addLineSegments(name: String, lines: List<LineSegment>, width: List<Double>) {
        val shapes = linesToShapes(lines, width)
        val namePositions = "${name}-mesh-positions"
        val nameNormals = "${name}-mesh-normals"

        addNode(name)

        // language=XML prefix="<p>" suffix="</p>"
        geometries.add(
            """
    <geometry id="$name-mesh" name="$name">
      <mesh>
        ${xmlPositions(namePositions, shapes)}
        ${xmlNormals(nameNormals)}
  
        <vertices id="$name-mesh-vertices">
          <input semantic="POSITION" source="#$namePositions"/>
        </vertices>
        
        <polylist count="${shapes.size}">
          <input semantic="VERTEX" source="#$name-mesh-vertices" offset="0"/>
          <input semantic="NORMAL" source="#$nameNormals" offset="1"/>
          <vcount>${"4 ".repeat(shapes.size)}</vcount>
          <p>${List(shapes.size * 4) { "$it 0 " }.joinToString("")}</p>
        </polylist>
        
      </mesh>
    </geometry>            
            """
        )
    }

    /**
     * -----------------------------------------------------------------
     * Adds a list of LineSegment under one named mesh with 2 UV maps
     */
    fun addLineSegments(
        name: String,
        lines: List<LineSegment>,
        width: List<Double>,
        uv0: List<Rectangle>
    ) {
        val shapes = linesToShapes(lines, width)
        val namePositions = "${name}-mesh-positions"
        val nameNormals = "${name}-mesh-normals"
        val nameUV0 = "${name}-mesh-map-0"

        addNode(name)

        // language=XML prefix="<p>" suffix="</p>"
        geometries.add(
            """
    <geometry id="$name-mesh" name="$name">
      <mesh>
        ${xmlPositions(namePositions, shapes)}
        ${xmlNormals(nameNormals)}
        ${xmlUVMap(nameUV0, uv0)}

        <vertices id="$name-mesh-vertices">
          <input semantic="POSITION" source="#$namePositions"/>
        </vertices>
        
        <polylist count="${shapes.size}">
          <input semantic="VERTEX" source="#$name-mesh-vertices" offset="0"/>
          <input semantic="NORMAL" source="#$nameNormals" offset="1"/>
          <input semantic="TEXCOORD" source="#$nameUV0" offset="2" set="0"/>
          <vcount>${"4 ".repeat(shapes.size)}</vcount>
          <p>${List(shapes.size * 4) { "$it 0 $it " }.joinToString("")}</p>
        </polylist>
        
      </mesh>
    </geometry>            
            """
        )
    }


    /**
     * -----------------------------------------------------------------
     * Adds a list of LineSegment under one named mesh with 2 UV maps
     */
    fun addLineSegments(
        name: String,
        lines: List<LineSegment>,
        width: List<Double>,
        uv0: List<Rectangle>,
        uv1: List<Rectangle>
    ) {
        val shapes = linesToShapes(lines, width)
        val namePositions = "${name}-mesh-positions"
        val nameNormals = "${name}-mesh-normals"
        val nameUV0 = "${name}-mesh-map-0"
        val nameUV1 = "${name}-mesh-map-1"

        addNode(name)

        // language=XML prefix="<p>" suffix="</p>"
        geometries.add(
            """
    <geometry id="$name-mesh" name="$name">
      <mesh>
        ${xmlPositions(namePositions, shapes)}
        ${xmlNormals(nameNormals)}
        ${xmlUVMap(nameUV0, uv0)}
        ${xmlUVMap(nameUV1, uv1)}

        <vertices id="$name-mesh-vertices">
          <input semantic="POSITION" source="#$namePositions"/>
        </vertices>
        
        <polylist count="${shapes.size}">
          <input semantic="VERTEX" source="#$name-mesh-vertices" offset="0"/>
          <input semantic="NORMAL" source="#$nameNormals" offset="1"/>
          <input semantic="TEXCOORD" source="#$nameUV0" offset="2" set="0"/>
          <input semantic="TEXCOORD" source="#$nameUV1" offset="2" set="1"/>
          <vcount>${"4 ".repeat(shapes.size)}</vcount>
          <p>${List(shapes.size * 4) { "$it 0 $it " }.joinToString("")}</p>
        </polylist>
        
      </mesh>
    </geometry>            
            """
        )
    }

    private fun xmlPositions(name: String, positions: List<Any>): String {
        val vertices = positions.joinToString(" ") {
            when (it) {
                is Rectangle -> it.toVertices3D(Random.double0(1.0))
                is ShapeContour -> it.toVertices3D(Random.double0(1.0))
                else -> ""
            }
        }
        return """
        <source id="$name">
          <float_array id="$name-array" count="${positions.size * 12}">$vertices</float_array>
          <technique_common>
            <accessor source="#$name-array" count="${positions.size * 4}" stride="3">
              <param name="X" type="float"/>
              <param name="Y" type="float"/>
              <param name="Z" type="float"/>
            </accessor>
          </technique_common>
        </source>            
        """
    }

    private fun xmlUVMap(name: String, uvs: List<Rectangle>): String {
        return """
        <source id="$name">
          <float_array id="$name-array" count="${uvs.size * 8}">${uvs.joinToString(" ") { it.toVertices2D() }}</float_array>
          <technique_common>
            <accessor source="#$name-array" count="${uvs.size * 4}" stride="2">
              <param name="S" type="float"/>
              <param name="T" type="float"/>
            </accessor>
          </technique_common>
        </source>
        """
    }

    private fun xmlNormals(name: String): String {
        // language=XML prefix="<p>" suffix="</p>"
        return """
        <source id="$name">
          <float_array id="$name-array" count="3">0 0 1</float_array>
          <technique_common>
            <accessor source="#$name-array" count="1" stride="3">
              <param name="X" type="float"/>
              <param name="Y" type="float"/>
              <param name="Z" type="float"/>
            </accessor>
          </technique_common>
        </source>
        """
    }

    /**
     * Saves the collada file. You should probably use .dae as file extension
     */
    fun save(file: File) {
        val time = LocalDateTime.now().toString()

        // language=XML prefix="<p>" suffix="</p>"
        val xml = """<?xml version="1.0" encoding="utf-8"?>
<COLLADA xmlns="http://www.collada.org/2005/11/COLLADASchema" version="1.4.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <asset>
    <contributor>
      <author>aBe</author>
      <authoring_tool>OPENRNDR</authoring_tool>
    </contributor>
    <created>$time</created>
    <modified>$time</modified>
    <unit name="meter" meter="0.01"/>
    <up_axis>Z_UP</up_axis>
  </asset>
  <library_images/>
  <library_geometries>
        ${geometries.joinToString("\n")}
  </library_geometries>
  <library_visual_scenes>
    <visual_scene id="Scene" name="Scene">
        ${nodes.joinToString("\n")}
    </visual_scene>
  </library_visual_scenes>
  <scene>
    <instance_visual_scene url="#Scene"/>
  </scene>
</COLLADA> 
"""
        file.writeText(xml)
    }
}
