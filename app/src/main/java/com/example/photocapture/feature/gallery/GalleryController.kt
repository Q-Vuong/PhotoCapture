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

    /*fun getPhotoByUri(uri: Uri): Photo? {
        // Đọc danh sách ảnh từ file
        val photosFromStorage = readPhotosListFromStorage()

        // Tìm kiếm Photo có imageUri tương ứng với uri được truyền vào
        return photosFromStorage.find { it.imageUri == uri.toString() }
    }*/

    fun getSavedImages(): List<Uri> {
        // Đọc danh sách ảnh từ file photos_list.json
        val photosFromStorage = readPhotosListFromStorage()
        Log.d("GalleryController", "List: $photosFromStorage")
        return photosFromStorage.map { Uri.parse(it.imageUri) }
    }

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


    fun deleteImage(uri: Uri): Boolean {
        return try {
            val path = uri.path?.replace("file://", "") ?: return false
            val file = File(path)

            // Kiểm tra nếu file tồn tại và xóa
            if (file.exists()) {
                val isDeleted = file.delete()
                if (isDeleted) {
                    Log.d("GalleryController", "File deleted successfully: $path")

                    // Cập nhật file JSON sau khi xóa ảnh
                    updatePhotosListAfterDelete(uri)
                } else {
                    Log.e("GalleryController", "Failed to delete file: $path")
                }
                isDeleted
            } else {
                Log.e("GalleryController", "File not found at path: $path")
                false
            }
        } catch (e: Exception) {
            Log.e("GalleryController", "Error deleting file: ${e.message}", e)
            false
        }
    }

    // Cập nhật danh sách ảnh trong file JSON
    private fun updatePhotosListAfterDelete(uri: Uri) {
        // Đọc danh sách ảnh hiện tại từ file
        val existingPhotos = readPhotosListFromStorage().toMutableList()

        // Loại bỏ ảnh có URI tương ứng
        existingPhotos.removeIf { it.imageUri == uri.toString() }

        // Lưu danh sách đã cập nhật lại vào file JSON
        val gson = Gson()
        val json = gson.toJson(existingPhotos)

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, "photos_list.json")
        file.writeText(json) // Ghi lại danh sách đã cập nhật vào file
    }

}