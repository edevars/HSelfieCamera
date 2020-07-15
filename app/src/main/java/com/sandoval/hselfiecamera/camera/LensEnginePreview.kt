package com.sandoval.hselfiecamera.camera

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.huawei.hms.common.size.Size
import com.huawei.hms.mlsdk.common.LensEngine
import com.sandoval.hselfiecamera.overlay.GraphicOverlay
import java.io.IOException

class LensEnginePreview(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val mContext: Context = context
    private val mSurfaceView: SurfaceView
    private var mStartRequested: Boolean
    private var mSurfaceAvailable: Boolean
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null

    init {
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        // Nuestra vista va a pedir un callback para agregar el lente
        this.addView(mSurfaceView)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?) {
        mOverlay = overlay
        start(lensEngine)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            // Vamos a crear una funcion que nos va a decir
            //si la camara esta lista
        }
    }

    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    fun release() {
        if (mLensEngine != null) {
            mLensEngine!!.release()
            mLensEngine = null
        }
    }

    @Throws(IOException::class)
    fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (overlay != null) {
                val size: Size = mLensEngine!!.displayDimension
                val min: Int = size.width.coerceAtMost(size.height)
                val max: Int = size.width.coerceAtLeast((size.height))
                if (Configuration.ORIENTATION_PORTRAIT == mContext.resources.configuration.orientation) {
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {


        override fun surfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mSurfaceAvailable = false
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e("Error: ", "No pudimos iniciar la camra $e")
            }
        }

    }

}