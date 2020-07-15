package com.sandoval.hselfiecamera.face

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLResultTrailer
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.hms.mlsdk.face.MLMaxSizeFaceTransactor
import com.sandoval.hselfiecamera.R
import com.sandoval.hselfiecamera.camera.LensEnginePreview
import com.sandoval.hselfiecamera.overlay.GraphicOverlay
import com.sandoval.hselfiecamera.overlay.LocalFaceGraphic
import java.io.IOException
import java.lang.RuntimeException

class LiveFaceActivityCamera : AppCompatActivity() {

    private var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS
    private var detectMode = 0
    private val smilingPossibility = 0.95f
    private var safeToTakePicture = false
    private var restart: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_face_camera)
        if (savedInstanceState != null) {
            lensType = savedInstanceState.getInt("lensType")
        }
        mPreview = findViewById(R.id.preview)
        val intent = this.intent
        try {
            detectMode = intent.getIntExtra("detect_mode", 1)
        } catch (e: RuntimeException) {
            Log.e("Error: ", "No pude traer el codigo de deteccion")
        }
        overlay = findViewById(R.id.face_overlay)
        restart = findViewById(R.id.restart)
        createFaceAnalyzer()
        createLensEngine()
    }

    override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine != null) {
            mLensEngine!!.release()
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putInt("lensType", lensType)
        super.onSaveInstanceState(outState)
    }

    private fun createFaceAnalyzer() {
        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
            .setMinFaceProportion(0.1f)
            .setTracingAllowed(true)
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        if (detectMode == 1003) {
            val transactor =
                MLMaxSizeFaceTransactor.Creator(analyzer, object : MLResultTrailer<MLFace?>() {
                    override fun objectCreateCallback(
                        itemId: Int,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()
                        if (obj == null) {
                            return
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceActivityCamera
                        )
                        overlay!!.addGraphic(faceGraphic)
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility) {
                            safeToTakePicture = false
                        }
                    }

                    override fun objectUpdateCallback(
                        var1: MLAnalyzer.Result<MLFace?>?,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()
                        if (obj == null) {
                            return
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceActivityCamera
                        )
                        overlay!!.addGraphic(faceGraphic)
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility && safeToTakePicture) {
                            safeToTakePicture = false
                        }
                    }

                    override fun lostCallback(result: MLAnalyzer.Result<MLFace?>?) {
                        overlay!!.clear()
                    }

                    override fun completeCallback() {
                        overlay!!.clear()
                    }
                }).create()
            analyzer!!.setTransactor(transactor)
        }
        else {
            // Validacion de rostro grupal
        }
    }

    private fun createLensEngine() {
        val context: Context = this.applicationContext
        mLensEngine = LensEngine.Creator(context, analyzer).setLensType(lensType)
            .applyDisplayDimension(640, 480)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }

    private fun startLensEngine() {
        restart!!.visibility = View.GONE
        if (mLensEngine != null) {
            try {
                if (detectMode == 1003) {
                    mPreview!!.start(mLensEngine, overlay)
                } else {
                    mPreview!!.start(mLensEngine)
                }
            } catch (e: IOException) {
                mLensEngine!!.release()
                mLensEngine = null
            }
        }
    }

    fun startPreview(view: View?) {
        mPreview!!.release()
        createFaceAnalyzer()
        createLensEngine()
        startLensEngine()
    }

}