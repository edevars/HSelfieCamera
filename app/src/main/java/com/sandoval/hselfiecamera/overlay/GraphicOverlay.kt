package com.sandoval.hselfiecamera.overlay

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.sandoval.hselfiecamera.camera.CameraConfiguration

class GraphicOverlay(
    context: Context,
    atts: AttributeSet?
) : View(context, atts) {

    private val lock = Any()
    private var previewWidth = 0
    private var previewHeight = 0
    var widtScaleValue = 1.0f
        private set
    var heightScaleValue = 1.0f
        private set
    var cameraFacing = CameraConfiguration.CAMERA_FACING_FRONT
        private set

    fun addGraphic(){

    }

    fun clear(){

    }

    fun setCameraInfo (width: Int, height: Int, facing: Int){
        synchronized(lock){
            previewWidth = width
            previewHeight = height
            cameraFacing = facing
        }
        this.postInvalidate()
    }
}