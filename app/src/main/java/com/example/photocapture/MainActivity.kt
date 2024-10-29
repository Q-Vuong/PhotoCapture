package com.example.photocapture

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.photocapture.feature.camera.Camera
import com.example.photocapture.feature.camera.CameraController
import com.example.photocapture.feature.camera.CameraView
import android.Manifest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photocapture.feature.gallery.GalleryView

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo launcher để yêu cầu quyền
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Quyền được cấp, có thể tiếp tục sử dụng camera và lưu trữ
                navigateToCameraView() // Chuyển sang Camera View
            } else {
                // Quyền không được cấp, hiển thị thông báo
                showPermissionDeniedMessage()
            }
        }

        // Kiểm tra quyền khi ứng dụng được tải về
        checkPermissions()

        setContent {
            MyNavigation()
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        // Uncomment this line if you need storage permission
        // val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED /* || storagePermission != PackageManager.PERMISSION_GRANTED */) {
            // Nếu chưa được cấp quyền, yêu cầu quyền
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            navigateToCameraView() // Chuyển sang Camera View nếu đã cấp quyền
        }
    }

    private fun showPermissionDeniedMessage() {
        // Hiển thị thông báo cho người dùng biết quyền không được cấp
        Toast.makeText(this, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCameraView() {
        // Chuyển sang Camera View sau khi đã cấp quyền
        setContent {
            MyNavigation() // Gọi lại navigation để chuyển đến Camera View
        }
    }
}

@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "cameraView") {
        composable("cameraView") { SetupCameraView(navController) }
        composable("galleryView") { SetupGalleryView(navController) }
    }
}

@Composable
fun SetupCameraView(navController: NavController) {
    val context = LocalContext.current
    val cameraModel = remember { Camera() }
    val cameraController = remember { CameraController(context, cameraModel) }

    // Kiểm tra quyền camera
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        cameraController.bindCamera(previewView = PreviewView(context))
    } else {
        // Nếu quyền chưa được cấp, hiển thị thông báo
        Toast.makeText(context, "Vui lòng cấp quyền camera", Toast.LENGTH_SHORT).show()
    }

    CameraView(controller = cameraController, navController = navController)
}

@Composable
fun SetupGalleryView(navController: NavController) {
    val context = LocalContext.current
    val cameraModel = remember { Camera() }
    val cameraController = remember { CameraController(context, cameraModel) }
    val imageUris = remember { cameraController.getSavedImages() }
    GalleryView(imageUris, navController)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Text(text = "Hello, Preview!")
}
