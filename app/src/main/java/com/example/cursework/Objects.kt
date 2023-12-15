package com.example.cursework

import android.content.Context
import android.opengl.GLES20
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class Objects(c: Context, color: FloatArray, objName: String) {
    private var colorBuffer: FloatBuffer
    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var normalBuffer: FloatBuffer
    private lateinit var facesVertexBuffer: ShortBuffer
    private lateinit var facesNormalBuffer: ShortBuffer
    private var facesList: List<String> = ArrayList()

    init {
        val verticesList = ArrayList<String>()
        val normalList = ArrayList<String>()

        try {
            val scanner = Scanner(c.assets.open(objName))
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                when {
                    line.startsWith("v ") -> verticesList.add(line)
                    line.startsWith("f ") -> facesList = facesList + line
                    line.startsWith("vn ") -> normalList.add(line)
                    line.startsWith("vt ") -> continue
                }
            }

            val buffer1 = ByteBuffer.allocateDirect(verticesList.size * 3 * 4)
            buffer1.order(ByteOrder.nativeOrder())
            verticesBuffer = buffer1.asFloatBuffer()

            val buffer2 = ByteBuffer.allocateDirect(normalList.size * 3 * 4)
            buffer2.order(ByteOrder.nativeOrder())
            normalBuffer = buffer2.asFloatBuffer()

            val buffer3 = ByteBuffer.allocateDirect(facesList.size * 3 * 2)
            buffer3.order(ByteOrder.nativeOrder())
            facesVertexBuffer = buffer3.asShortBuffer()

            val buffer4 = ByteBuffer.allocateDirect(facesList.size * 3 * 2)
            buffer4.order(ByteOrder.nativeOrder())
            facesNormalBuffer = buffer4.asShortBuffer()

            for (vertex in verticesList) {
                val coords = vertex.split(" ").toTypedArray()
                val x = coords[1].toFloat()
                val y = coords[2].toFloat()
                val z = coords[3].toFloat()
                verticesBuffer.put(x).put(y).put(z)
            }
            verticesBuffer.position(0)

            for (vertex in normalList) {
                val coords = vertex.split(" ").toTypedArray()
                val x = coords[1].toFloat()
                val y = coords[2].toFloat()
                val z = coords[3].toFloat()
                normalBuffer.put(x).put(y).put(z)
            }
            normalBuffer.position(0)

            for (face in facesList) {
                val vertexIndices = face.split(" ").toTypedArray()
                val coord1: Array<String>
                val coord2: Array<String>
                val coord3: Array<String>

                coord1 = if (vertexIndices[1].contains("//")) vertexIndices[1].split("//").toTypedArray() else vertexIndices[1].split("/").toTypedArray()
                coord2 = if (vertexIndices[2].contains("//")) vertexIndices[2].split("//").toTypedArray() else vertexIndices[2].split("/").toTypedArray()
                coord3 = if (vertexIndices[3].contains("//")) vertexIndices[3].split("//").toTypedArray() else vertexIndices[3].split("/").toTypedArray()

                var vertex1 = coord1[0].toShort()
                var vertex2 = coord2[0].toShort()
                var vertex3 = coord3[0].toShort()
                facesVertexBuffer.put((vertex1 - 1).toShort()).put((vertex2 - 1).toShort()).put((vertex3 - 1).toShort())

                vertex1 = coord1[1].toShort()
                vertex2 = coord2[1].toShort()
                vertex3 = coord3[1].toShort()
                facesNormalBuffer.put((vertex1 - 1).toShort()).put((vertex2 - 1).toShort()).put((vertex3 - 1).toShort())
            }
            facesVertexBuffer.position(0)
            facesNormalBuffer.position(0)

            verticesList.clear()
            normalList.clear()

            scanner.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val colorData = FloatArray(facesList.size * 4)
        for (v in facesList.indices) {
            colorData[4 * v] = color[0]
            colorData[4 * v + 1] = color[1]
            colorData[4 * v + 2] = color[2]
            colorData[4 * v + 3] = color[3]
        }

        val bColor = ByteBuffer.allocateDirect(colorData.size * 4)
        bColor.order(ByteOrder.nativeOrder())
        colorBuffer = bColor.asFloatBuffer()
        colorBuffer.put(colorData).position(0)
    }

    fun render(positionAttribute: Int, normalAttribute: Int, colorAttribute: Int, onlyPosition: Boolean) {
        facesVertexBuffer.position(0)
        facesNormalBuffer.position(0)
        verticesBuffer.position(0)
        normalBuffer.position(0)
        colorBuffer.position(0)

        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, verticesBuffer)
        GLES20.glEnableVertexAttribArray(positionAttribute)

        if (!onlyPosition) {
            GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)
            GLES20.glEnableVertexAttribArray(normalAttribute)

            GLES20.glVertexAttribPointer(colorAttribute, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
            GLES20.glEnableVertexAttribArray(colorAttribute)
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size * 3, GLES20.GL_UNSIGNED_SHORT, facesVertexBuffer)
    }
}

