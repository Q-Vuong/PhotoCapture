package com.example.photocapture

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.photocapture.feature.camera.CameraController
import com.example.photocapture.feature.camera.CameraView
import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photocapture.animations.fadeOutAndZoomOut
import com.example.photocapture.animations.scaleInAnimation
import com.example.photocapture.feature.gallery.GalleryController
import com.example.photocapture.feature.gallery.GalleryView
import com.example.photocapture.feature.gallery.ImageDetailView
import com.example.photocapture.feature.photoedit.EditController
import com.example.photocapture.feature.photoedit.EditPhotoView
import com.example.photocapture.ui.theme.PhotoCaptureTheme

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Khởi tạo launcher để yêu cầu quyền
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCameraView()
            } else {
                showPermissionDeniedMessage()
            }
        }
        // Kiểm tra quyền khi ứng dụng được tải về
        checkPermissions()

        setContent {
            PhotoCaptureTheme {
                MyNavigation()
            }
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED /* || storagePermission != PackageManager.PERMISSION_GRANTED */) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            navigateToCameraView()
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCameraView() {
        setContent {
            MyNavigation()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "cameraView") {
        composable(
            route = "cameraView",
            enterTransition = { scaleInAnimation() } ,
            popExitTransition = { fadeOutAndZoomOut() },
            exitTransition = { fadeOutAndZoomOut() },
            popEnterTransition = { scaleInAnimation() },
        ) { SetupCameraView(navController) }

        composable(
            route = "galleryView",
            enterTransition = { scaleInAnimation() } ,
            popExitTransition = { fadeOutAndZoomOut() },
            exitTransition = { fadeOutAndZoomOut() },
            popEnterTransition = { scaleInAnimation() } // animation khi back về lại view này

        ) { SetupGalleryView(navController) }

        composable(
            route = "imageDetail/{imageUri}",
            enterTransition = { scaleInAnimation() } ,
            popExitTransition = { fadeOutAndZoomOut() },
            exitTransition = { fadeOutAndZoomOut() },
            popEnterTransition = { scaleInAnimation() }// animation khi back về lại view trước đó
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("imageUri") ?: ""
            SetupImageDetailView(Uri.parse(uriString), navController)
        }

        composable(
            route = "editPhoto/{imageUri}",
            enterTransition = { scaleInAnimation() } ,
            popExitTransition = { fadeOutAndZoomOut() },
            exitTransition = { fadeOutAndZoomOut() },
            popEnterTransition = { scaleInAnimation() }
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("imageUri") ?: ""
            SetupEditPhotoView(Uri.parse(uriString), navController)
        }
    }
}

@Composable
fun SetupCameraView(navController: NavController) {
    val context = LocalContext.current
    val cameraController = remember { CameraController(context) }

    CameraView(controller = cameraController, navController = navController)
}

@Composable
fun SetupGalleryView(navController: NavController) {
    val context = LocalContext.current
    val galleryController = remember { GalleryController(context) }
    val imageUris = remember { galleryController.getListPhotos() }

    GalleryView(imageUris, navController)
}

@Composable
fun SetupImageDetailView(uri: Uri, navController: NavController) {
    val context = LocalContext.current
    val galleryController = remember { GalleryController(context) }
    val imageUris = remember { galleryController.getListPhotos() }

    ImageDetailView(uri, galleryController, imageUris, navController)
}

@Composable
fun SetupEditPhotoView(uri: Uri, navController: NavController) {
    val context = LocalContext.current
    val editController = remember { EditController(context) }

    EditPhotoView(uri, editController, navController)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Text(text = "Hello, Preview!")
}
