package com.example.photocapture.feature.photoedit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

import android.net.Uri
import android.util.Log
import java.util.Stack

class EditController(private val context: Context) {
    val history = Stack<Bitmap>()
    private val redoStack = Stack<Bitmap>()

    fun loadPhotoFromUri(uri: Uri): Bitmap? {
        // Mở inputStream để lấy Exif và decode Bitmap
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // Đọc thông tin orientation từ metadata của ảnh
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // Mở lại inputStream để decode bitmap (do inputStream trước đó đã tiêu thụ)
            context.contentResolver.openInputStream(uri)?.use { decodeStream ->
                val bitmap = BitmapFactory.decodeStream(decodeStream)
                // Xoay ảnh dựa trên thông tin orientation nếu cần thiết
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            }
        }
        return null
    }


    // Xoay Photo và lưu trạng thái sau khi cập nhật
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        val updatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        saveToHistory(updatedBitmap)
        Log.d("EditController", "Rotated and saved to history: ${history.size}")
        return updatedBitmap
    }

    // Lật ảnh theo chiều ngang và lưu trạng thái sau khi cập nhật
    fun flipPhoto(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { preScale(-1.0f, 1.0f) }
        val updatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        saveToHistory(updatedBitmap)
        Log.d("EditController", "Flipped horizontally and saved to history: ${history.size}")
        return updatedBitmap
    }

    // Lưu trạng thái hiện tại vào lịch sử
    private fun saveToHistory(bitmap: Bitmap) {
        bitmap.config?.let { config ->
            history.push(bitmap.copy(config, true))
            redoStack.clear() // Xóa redoStack khi có thay đổi mới
            Log.d("EditController", "Saved to history: ${history.size}")
        }
    }

    // Undo thay đổi
    fun undo(): Bitmap? {
        return if (history.isNotEmpty()) {
            val previousState = history.pop()
            val config = previousState.config
                ?: Bitmap.Config.ARGB_8888 // Sử dụng cấu hình mặc định nếu config là null
            redoStack.push(previousState.copy(config, true))
            Log.d(
                "EditController", "Undo: history size ${history.size}, redo size ${redoStack.size}"
            )
            previousState
        } else {
            Log.d("EditController", "Undo: No history available")
            null
        }
    }

    // Redo thay đổi
    fun redo(): Bitmap? {
        return if (redoStack.isNotEmpty()) {
            val redoState = redoStack.pop()
            val config = redoState.config ?: Bitmap.Config.ARGB_8888 // Sử dụng cấu hình mặc định nếu config là null
            history.push(redoState.copy(config, true))
            Log.d("EditController", "Redo: history size ${history.size}, redo size ${redoStack.size}")
            redoState
        } else {
            Log.d("EditController", "Redo: No redo available")
            null
        }
    }

    // Lưu ảnh chỉnh sửa lại vào URI cũ
    fun saveEditedPhoto(bitmap: Bitmap, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
            Log.d("EditController", "Image saved successfully to $uri")
            true
        } catch (e: Exception) {
            Log.e("EditController", "Failed to save image to $uri", e)
            false
        }
    }
}
