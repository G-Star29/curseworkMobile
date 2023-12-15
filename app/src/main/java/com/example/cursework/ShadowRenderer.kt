package com.example.cursework

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ShadowsRenderer internal constructor(
    private val mShadowsActivity: MainActivity,
    private val c: Context
) :
    GLSurfaceView.Renderer {
    private var mSimpleShadowProgram: RenderProgram? = null
    private var mDepthMapProgram: RenderProgram? = null
    private var mActiveProgram = 0
    private val mMVPMatrix = FloatArray(16)
    private val mMVMatrix = FloatArray(16)
    private val mNormalMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val mLightMvpMatrix = FloatArray(16)
    private val mLightProjectionMatrix = FloatArray(16)
    private val mLightViewMatrix = FloatArray(16)
    private val mLightPosInEyeSpace = FloatArray(16)
    private val mLightPosModel = floatArrayOf(-0.5f, -0.5f, 5.8f, 1.0f)
    private val mActualLightPosition = FloatArray(4)
    private var mDisplayWidth = 0
    private var mDisplayHeight = 0
    private var s = 0f
    private var mShadowMapWidth = 0
    private var mShadowMapHeight = 0
    private lateinit var fboId: IntArray
    private lateinit var renderTextureId: IntArray
    private var scene_mvpMatrixUniform = 0
    private var scene_mvMatrixUniform = 0
    private var scene_normalMatrixUniform = 0
    private var scene_lightPosUniform = 0
    private var scene_shadowProjMatrixUniform = 0
    private var scene_textureUniform = 0
    private var scene_mapStepXUniform = 0
    private var scene_mapStepYUniform = 0
    private var shadow_mvpMatrixUniform = 0
    private var scene_positionAttribute = 0
    private var scene_normalAttribute = 0
    private var scene_colorAttribute = 0
    private var shadow_positionAttribute = 0
    private var table: Objects? = null
    private var chair: Objects? = null
    private var comp: Objects? = null
    private val cup: Objects? = null
    private var book: Objects? = null
    private val plate: Objects? = null
    private var nizhnyayabulka: Objects? = null
    private val kartohakonchlas: Objects? = null
    private var kruzhechka: Objects? = null
    private var tel: Objects? = null
    private var screen: Objects? = null
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        table = Objects(c, floatArrayOf(0.4f, 0.2f, 0.2f, 1.0f), "table_ikea.obj")
        comp = Objects(c, floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f), "comp1.obj")
        chair = Objects(c, floatArrayOf(0.0f, 0.3f, 0.0f, 1.0f), "stulotpynoogotrudovica.obj")
        book = Objects(c, floatArrayOf(0.4f, 0.2f, 0.3f, 1.0f), "books.obj")
        //        plate = new Objects(c, new float[]{0.4f, 0.2f, 0.3f, 1.0f}, "plate.obj");
        nizhnyayabulka = Objects(c, floatArrayOf(0.0f, 5f, 3f, 1.0f), "nizhnyayabulka.obj")
        //        kartohakonchlas = new Objects(c, new float[]{1f, 0.2f, 0.3f, 1.0f}, "kartohakonchlas.obj");
        kruzhechka = Objects(c, floatArrayOf(1f, 1f, 1f, 1.0f), "kruzhechka.obj")
        tel = Objects(c, floatArrayOf(1f, 1f, 1f, 1f), "tel.obj")
        screen = Objects(c, floatArrayOf(0.0f, 0.0f, 1f, 1f), "screen.obj")
        mSimpleShadowProgram = RenderProgram(
            R.raw.depth_tex_v_with_shadow,
            R.raw.depth_tex_f_with_simple_shadow,
            mShadowsActivity
        )
        mDepthMapProgram = RenderProgram(
            R.raw.depth_tex_v_depth_map,
            R.raw.depth_tex_f_depth_map,
            mShadowsActivity
        )
        mActiveProgram = mSimpleShadowProgram!!.getProgram()
    }

    private fun generateShadowFBO() {
        mShadowMapWidth = Math.round(mDisplayWidth.toFloat())
        mShadowMapHeight = Math.round(mDisplayHeight.toFloat())
        fboId = IntArray(1)
        val depthTextureId = IntArray(1)
        renderTextureId = IntArray(1)
        GLES20.glGenFramebuffers(1, fboId, 0)
        GLES20.glGenRenderbuffers(1, depthTextureId, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0])
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER,
            GLES20.GL_DEPTH_COMPONENT16,
            mShadowMapWidth,
            mShadowMapHeight
        )
        GLES20.glGenTextures(1, renderTextureId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0])
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            mShadowMapWidth,
            mShadowMapHeight,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            GLES20.GL_UNSIGNED_INT,
            null
        )
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D,
            renderTextureId[0], 0
        )
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        mDisplayWidth = width
        mDisplayHeight = height
        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight)
        generateShadowFBO()
        val ratio = mDisplayWidth.toFloat() / mDisplayHeight
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 100.0f
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, bottom, top, near, far)
        Matrix.frustumM(
            mLightProjectionMatrix,
            0,
            -1.1f * ratio,
            1.1f * ratio,
            1.1f * bottom,
            1.1f * top,
            near,
            far
        )
    }

    override fun onDrawFrame(unused: GL10) {
        mActiveProgram = mSimpleShadowProgram!!.getProgram()
        Matrix.setLookAtM(
            mViewMatrix, 0,
            8f, 10f, 0f,
            0f, 0f, 0f,
            -5f, 0f, 0f
        )
        scene_mvpMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, "uMVPMatrix")
        scene_mvMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, "uMVMatrix")
        scene_normalMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, "uNormalMatrix")
        scene_lightPosUniform = GLES20.glGetUniformLocation(mActiveProgram, "uLightPos")
        scene_shadowProjMatrixUniform =
            GLES20.glGetUniformLocation(mActiveProgram, "uShadowProjMatrix")
        scene_textureUniform = GLES20.glGetUniformLocation(mActiveProgram, "uShadowTexture")
        scene_positionAttribute = GLES20.glGetAttribLocation(mActiveProgram, "aPosition")
        scene_normalAttribute = GLES20.glGetAttribLocation(mActiveProgram, "aNormal")
        scene_colorAttribute = GLES20.glGetAttribLocation(mActiveProgram, "aColor")
        scene_mapStepXUniform = GLES20.glGetUniformLocation(mActiveProgram, "uxPixelOffset")
        scene_mapStepYUniform = GLES20.glGetUniformLocation(mActiveProgram, "uyPixelOffset")
        val shadowMapProgram: Int = mDepthMapProgram!!.getProgram()
        shadow_mvpMatrixUniform = GLES20.glGetUniformLocation(shadowMapProgram, "uMVPMatrix")
        shadow_positionAttribute = GLES20.glGetAttribLocation(shadowMapProgram, "aShadowPosition")
        val basicMatrix = FloatArray(16)
        Matrix.setIdentityM(basicMatrix, 0)
        Matrix.multiplyMV(mActualLightPosition, 0, basicMatrix, 0, mLightPosModel, 0)
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.setLookAtM(
            mLightViewMatrix, 0,
            mActualLightPosition[0], mActualLightPosition[1], mActualLightPosition[2],
            mActualLightPosition[0], -mActualLightPosition[1], mActualLightPosition[2],
            -mActualLightPosition[0], 0f, -mActualLightPosition[2]
        )
        GLES20.glCullFace(GLES20.GL_FRONT)
        s += 0.3f
        if (s >= 360) s -= 360f
        Matrix.rotateM(mModelMatrix, 0, 25f, 0f, 1f, 0f)
        renderShadowMap()
        GLES20.glCullFace(GLES20.GL_BACK)
        renderScene()
    }

    private fun renderShadowMap() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0])
        GLES20.glViewport(0, 0, mShadowMapWidth, mShadowMapHeight)
        GLES20.glClearColor(1f, 1f, 1f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        mDepthMapProgram?.let { GLES20.glUseProgram(it.getProgram()) }
        val tempResultMatrix = FloatArray(16)
        Matrix.multiplyMM(mLightMvpMatrix, 0, mLightViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix, 0)
        System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix, 0, 16)
        GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix, 0)
        table?.render(shadow_positionAttribute, 0, 0, true)
        chair?.render(shadow_positionAttribute, 0, 0, true)
        comp?.render(shadow_positionAttribute, 0, 0, true)
        book?.render(shadow_positionAttribute, 0, 0, true)
        //        plate.render(shadow_positionAttribute, 0, 0, true);
        nizhnyayabulka?.render(shadow_positionAttribute, 0, 0, true)
        //        kartohakonchlas.render(shadow_positionAttribute, 0, 0, true);
        kruzhechka?.render(shadow_positionAttribute, 0, 0, true)
        tel?.render(shadow_positionAttribute, 0, 0, true)
        screen?.render(shadow_positionAttribute, 0, 0, true)
    }

    private fun renderScene() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mActiveProgram)
        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight)
        GLES20.glUniform1f(scene_mapStepXUniform, (1.0 / mShadowMapWidth).toFloat())
        GLES20.glUniform1f(scene_mapStepYUniform, (1.0 / mShadowMapHeight).toFloat())
        val tempResultMatrix = FloatArray(100)
        val bias = floatArrayOf( // коорд глубины, тень
            0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f
        )
        val depthBiasMVP = FloatArray(16) // смещение глубины
        Matrix.multiplyMM(tempResultMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        System.arraycopy(tempResultMatrix, 0, mMVMatrix, 0, 16)
        GLES20.glUniformMatrix4fv(scene_mvMatrixUniform, 1, false, mMVMatrix, 0)
        Matrix.invertM(tempResultMatrix, 0, mMVMatrix, 0)
        Matrix.transposeM(mNormalMatrix, 0, tempResultMatrix, 0)
        GLES20.glUniformMatrix4fv(scene_normalMatrixUniform, 1, false, mNormalMatrix, 0)
        Matrix.multiplyMM(tempResultMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)
        System.arraycopy(tempResultMatrix, 0, mMVPMatrix, 0, 16)
        GLES20.glUniformMatrix4fv(scene_mvpMatrixUniform, 1, false, mMVPMatrix, 0)
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mActualLightPosition, 0)
        GLES20.glUniform3f(
            scene_lightPosUniform,
            mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]
        )
        Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix, 0)
        System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix, 0, 16)
        GLES20.glUniformMatrix4fv(scene_shadowProjMatrixUniform, 1, false, mLightMvpMatrix, 0)
        table?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
        chair?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
        comp?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
        book?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
        //        plate.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
        nizhnyayabulka?.render(
            scene_positionAttribute,
            scene_normalAttribute,
            scene_colorAttribute,
            false
        )
        //        kartohakonchlas.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
        kruzhechka!!.render(
            scene_positionAttribute,
            scene_normalAttribute,
            scene_colorAttribute,
            false
        )
        tel?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
        screen?.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false)
    }
}