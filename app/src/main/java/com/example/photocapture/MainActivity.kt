package com.example.photocapture

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.photocapture.feature.camera.Camera
import com.example.photocapture.feature.camera.CameraController
import com.example.photocapture.feature.camera.CameraView
import com.example.photocapture.ui.theme.PhotoCaptureTheme
import android.Manifest
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SetupCameraView()
        }
    }
}

@Composable
fun SetupCameraView() {
    val context = LocalContext.current
    val cameraModel = remember { Camera() }
    val cameraController = remember { CameraController(context, cameraModel) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraController.bindCamera(previewView = PreviewView(context))
        }
    }

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        cameraController.bindCamera(previewView = PreviewView(context))
    } else {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    CameraView(controller = cameraController)
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}