package com.example.photocapture.feature.camera

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraController (private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private val photosList = mutableListOf<Photo>()


    fun startCamera(previewView: PreviewView) {
        cameraProvider?.unbindAll()

        if (cameraProvider == null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                try {
                    cameraProvider?.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        } else {
            cameraProvider?.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, cameraSelector, preview, imageCapture)
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun switchCamera(previewView: PreviewView) {
        stopCamera()
        Log.d("CameraController", "Switch Camera Called")
        cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
        {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        else { CameraSelector.DEFAULT_BACK_CAMERA}

        startCamera(previewView)
    }

    fun capturePhoto() {
        val photoFile = createFile() ?: return
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val photo = Photo(
                        imageUri = savedUri.toString(),
                        captureDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis()),
                        imageSize = photoFile.length()
                    )
                    photosList.add(photo)
                    savePhotosListToStorage()
                }
                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun savePhotosListToStorage() {
        // Đọc danh sách ảnh hiện có từ file
        val existingPhotos = readPhotosListFromStorage().toMutableList()
        existingPhotos.addAll(photosList) // Thêm danh sách ảnh mới vào danh sách hiện có

        // Chuyển đổi danh sách thành JSON
        val gson = Gson()
        val json = gson.toJson(existingPhotos)

        // Lưu vào thư mục DOCUMENTS
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, "photos_list.json")
        file.writeText(json) // Ghi lại danh sách mới vào file
    }


    private fun createFile(): File? {
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        outputDir?.let {
            if (!it.exists()) it.mkdirs()
            val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
            return File(it, fileName)
        }
        return null
    }


    fun getLastImageUri(): Uri? {
        val photosFromStorage = readPhotosListFromStorage()
        return if (photosFromStorage.isNotEmpty()) {
            // Lấy URI của ảnh cuối cùng và ghi log
            val lastImageUri = Uri.parse(photosFromStorage.last().imageUri) // Chuyển đổi String thành Uri
            Log.d("CameraController", "Last Image: $lastImageUri")
            lastImageUri
        } else {
            // Ghi log khi không có ảnh nào
            Log.d("CameraController", "No images captured yet.")
            null
        }
    }


    private fun readPhotosListFromStorage(): List<Photo> {
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, "photos_list.json")

        return if (file.exists()) {
            val json = file.readText()
            val gson = Gson()
            val photoType = object : com.google.gson.reflect.TypeToken<List<Photo>>() {}.type
            gson.fromJson(json, photoType)
        } else {
            emptyList()
        }
    }


}