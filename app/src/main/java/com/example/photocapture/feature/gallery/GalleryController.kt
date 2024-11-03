package com.example.photocapture.feature.gallery

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.photocapture.feature.camera.Photo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class GalleryController(private val context: Context) {

    //Đọc danh sách ảnh từ bộ nhớ
    private fun readPhotosListFromStorage(): List<Photo> {
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, "photos_list.json")
        return if (file.exists()) {
            val json = file.readText()
            val gson = Gson()
            val photoType = object : TypeToken<List<Photo>>() {}.type
            gson.fromJson(json, photoType)
        } else {
            emptyList()
        }
    }

    // Lấy danh sách ảnh từ bộ nhớ
    fun getListPhotos(): List<Uri> {
        val photosFromStorage = readPhotosListFromStorage()
        Log.d("GalleryController", "List: $photosFromStorage")
        return photosFromStorage.map { Uri.parse(it.imageUri) }
            .distinct()
    }

    // Xoá ảnh khỏi bộ nhớ
    fun deletePhoto(uri: Uri): Boolean {
        return try {
            val path = uri.path?.replace("file://", "") ?: return false
            val file = File(path)
            if (file.exists()) {
                val isDeleted = file.delete()
                if (isDeleted) {
                    Log.d("GalleryController", "File deleted successfully: $path")
                    updatePhotosListAfterDelete(uri)
                } else {
                    Log.e("GalleryController", "Failed to delete file: $path")
                }
                return isDeleted
            } else {
                Log.e("GalleryController", "File not found at path: $path")
                false
            }
        } catch (e: Exception) {
            Log.e("GalleryController", "Error deleting file: ${e.message}", e)
            false
        }
    }

    // Cập nhật danh sách ảnh sau khi xoá
    private fun updatePhotosListAfterDelete(uri: Uri) {
        val existingPhotos = readPhotosListFromStorage().toMutableList()
        existingPhotos.removeIf { it.imageUri == uri.toString() }
        writePhotosListToStorage(existingPhotos)
    }

    // Ghi danh sách ảnh vào bộ nhớ
    private fun writePhotosListToStorage(photos: List<Photo>) {
        val gson = Gson()
        val json = gson.toJson(photos)
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, "photos_list.json")
        file.writeText(json)
    }

    // lấy thông tin ảnh theo uri
    fun getPhotoDetailsByUri(uri: Uri): Photo? {
        val photosFromStorage = readPhotosListFromStorage()
        return photosFromStorage.find { it.imageUri == uri.toString() }
    }
}