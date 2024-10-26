package com.example.photocapture.feature.camera

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberImagePainter
import com.example.photocapture.R

@Composable
fun CameraView(controller: CameraController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isCameraAvailable by remember { mutableStateOf(true) }
    var previewView: PreviewView? = null

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isCameraAvailable) {
                CameraPreviewView(modifier = Modifier.weight(1f)
                ) { view ->
                    previewView = view
                    try {
                        controller.bindCamera(view)
                    } catch (e: Exception) {
                        isCameraAvailable = false
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Camera không khả dụng", color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                imageUri?.let {
                    Box(modifier = Modifier.size(44.dp)) {
                        Image(
                            painter = rememberImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        Image(
                            painter = painterResource(R.drawable.icon_cover_circle),
                            contentDescription = "",
                            modifier = Modifier.fillMaxSize()
                        )

                    }

                } ?: run {
                    Image(
                        painter = painterResource(R.drawable.icon_cover_circle),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_cover_circle),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Button(
                        onClick = {controller.captureImage { uri -> imageUri = uri }},
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {}
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable { previewView?.let { controller.switchCamera(it) } },
                    contentAlignment = Alignment.Center
                ) {
                    // Hình nền cho nút
                    Image(
                        painter = painterResource(R.drawable.icon_cover_circle),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Icon xoay camera
                    Image(
                        painter = painterResource(R.drawable.icon_camera_switch),
                        contentDescription = "Switch Camera",
                        modifier = Modifier.size(20.dp)
                    )
                }

            }
        }
    }

}

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onUseSurfaceProvider: (PreviewView) -> Unit
)
{
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onUseSurfaceProvider(this)
            }
        },
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun CameraViewPreView() {
    val context = LocalContext.current
    val cameraModel = remember { Camera() }
    val cameraController = remember { CameraController(context, cameraModel) }
    CameraView(controller = cameraController)
}