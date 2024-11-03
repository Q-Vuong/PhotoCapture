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
    private var originalBitmap: Bitmap? = null // Biến giữ bản sao gốc của ảnh ban đầu
    val history = Stack<Bitmap>()
    private val redoStack = Stack<Bitmap>()

    // Hàm load ảnh từ URI và lưu trạng thái ban đầu
    fun loadPhotoFromUri(uri: Uri): Bitmap? {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            context.contentResolver.openInputStream(uri)?.use { decodeStream ->
                val bitmap = BitmapFactory.decodeStream(decodeStream)
                val rotatedBitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmapIfNeeded(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmapIfNeeded(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmapIfNeeded(bitmap, 270f)
                    else -> bitmap
                }

                rotatedBitmap?.let {
                    originalBitmap = it // Lưu bản sao gốc
                    saveToHistory(it) // Lưu vào history
                }

                return rotatedBitmap
            }


        }
        return null
    }


    // Hàm xoay ảnh nhưng không lưu
    fun rotateBitmapIfNeeded(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        val updatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        return updatedBitmap
    }


    //************************************* Thuộc về Edit Ảnh *************************************************
    // Hàm xoay ảnh
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        val updatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        saveToHistory(updatedBitmap)
        Log.d("EditController", "Rotated and saved to history: ${history.size}")
        return updatedBitmap
    }

    // Hàm lật ảnh
    fun flipPhoto(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { preScale(-1.0f, 1.0f) }
        val updatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        saveToHistory(updatedBitmap)
        Log.d("EditController", "Flipped horizontally and saved to history: ${history.size}")
        return updatedBitmap
    }

    private fun saveToHistory(bitmap: Bitmap) {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888 // Giá trị mặc định khi config là null
        if (history.isEmpty() || history.peek() != bitmap) {
            history.push(bitmap.copy(config, true))
            redoStack.clear() // Xóa redoStack khi có thay đổi mới
            Log.d("EditController", "Saved to history: ${history.size}")
        }
    }

    fun undo(): Bitmap? {
        return if (history.size > 1) { // Đảm bảo ít nhất còn 1 trạng thái trong history
            val lastState = history.pop() // Lấy trạng thái hiện tại ra khỏi history
            val config = lastState.config ?: Bitmap.Config.ARGB_8888 // Giá trị mặc định khi config là null
            redoStack.push(lastState.copy(config, true)) // Đẩy vào redoStack
            Log.d("EditController", "Undo: history size ${history.size}, redo size ${redoStack.size}")
            history.peek()
        } else {
            Log.d("EditController", "Undo: No more history to undo")
            originalBitmap // Trả về ảnh gốc nếu không còn gì để undo
        }
    }

    fun redo(): Bitmap? {
        return if (redoStack.isNotEmpty()) {
            val redoState = redoStack.pop() // Lấy trạng thái từ redoStack
            val config = redoState.config ?: Bitmap.Config.ARGB_8888 // Giá trị mặc định khi config là null
            history.push(redoState.copy(config, true)) // Đẩy lại vào history
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
