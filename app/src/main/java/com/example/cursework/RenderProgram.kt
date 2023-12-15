package com.example.cursework

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class RenderProgram(vID: Int, fID: Int, context: Context) {
    private var mProgram: Int = 0
    private lateinit var mVertexS: String
    private lateinit var mFragmentS: String

    init {
        val vs = StringBuilder()
        val fs = StringBuilder()

        try {
            var inputStream: InputStream = context.resources.openRawResource(vID)
            var inStream = BufferedReader(InputStreamReader(inputStream))

            var read: String? = inStream.readLine()
            while (read != null) {
                vs.append(read).append("\n")
                read = inStream.readLine()
            }

            vs.deleteCharAt(vs.length - 1)

            inputStream = context.resources.openRawResource(fID)
            inStream = BufferedReader(InputStreamReader(inputStream))

            read = inStream.readLine()
            while (read != null) {
                fs.append(read).append("\n")
                read = inStream.readLine()
            }

            fs.deleteCharAt(fs.length - 1)
        } catch (e: Exception) {
            Log.d("RenderProgram", "Could not read shader: " + e.localizedMessage)
        }
        setup(vs.toString(), fs.toString())
    }

    private fun setup(vs: String, fs: String) {
        mVertexS = vs
        mFragmentS = fs

        if (createProgram() != 1) {
            throw RuntimeException("Error at creating shaders")
        }
    }

    private fun createProgram(): Int {
        val mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexS)
        if (mVertexShader == 0) {
            return 0
        }

        val mPixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentS)
        if (mPixelShader == 0) {
            return 0
        }

        mProgram = GLES20.glCreateProgram()
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, mVertexShader)
            GLES20.glAttachShader(mProgram, mPixelShader)
            GLES20.glLinkProgram(mProgram)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("RenderProgram", "Could not link _program: ")
                Log.e("RenderProgram", GLES20.glGetProgramInfoLog(mProgram))
                GLES20.glDeleteProgram(mProgram)
                mProgram = 0
                return 0
            }
        } else {
            Log.d("CreateProgram", "Could not create program")
        }

        return 1
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e("RenderProgram", "Could not compile shader $shaderType :")
                Log.e("RenderProgram", GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                return 0
            }
        }
        return shader
    }

    fun getProgram(): Int {
        return mProgram
    }
}
