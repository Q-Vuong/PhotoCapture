package com.example.photocapture.feature.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraController (private val context: Context, private val model: Camera) {
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    fun bindCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun switchCamera(previewView: PreviewView) {
        cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
        {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        else { CameraSelector.DEFAULT_BACK_CAMERA}

        bindCamera(previewView)
    }

    fun captureImage(onImageCaptured: (Uri) -> Unit) {
        val photoFile = createFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    model.imageUri = savedUri
                    onImageCaptured(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun createFile(): File {
        val outputDir = context.cacheDir
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        return File(outputDir, fileName)
    }
}