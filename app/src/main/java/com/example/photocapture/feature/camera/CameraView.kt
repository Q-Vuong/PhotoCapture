package com.example.photocapture.feature.camera

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.photocapture.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraView(controller: CameraController, navController: NavController) {
    var isCameraAvailable by remember { mutableStateOf(true) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var lastImageUri by remember { mutableStateOf<Uri?>(null) }

    // Dừng camera khi view không còn hiện
    DisposableEffect(Unit) {
        onDispose {
            controller.stopCamera()
        }
    }
    LaunchedEffect(Unit) {
        lastImageUri = controller.getLastPhotoUri()
    }


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (isCameraAvailable) {
                CameraPreviewView(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(4f / 3f)
                ) { view ->
                    if (previewView == null) {
                        previewView = view
                        Log.d("CameraView", "previewView has value")
                        try {
                            controller.startCamera(previewView!!)
                        } catch (e: Exception) {
                            isCameraAvailable = false
                            Log.e("CameraView", "Error starting camera: ${e.message}", e)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        //.fillMaxSize()
                        .aspectRatio(4f / 3f)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Camera không khả dụng", color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                lastImageUri?.let {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clickable { navController.navigate("galleryView") }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painterResource(R.drawable.icon_cover_circle),
                            contentDescription = "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("galleryView") }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_cover_circle),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(84.dp),
                    contentAlignment = Alignment.Center

                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_cover_circle),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Button(
                        onClick = {
                            controller.capturePhoto { savedUri ->
                                lastImageUri = savedUri
                            }},
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {}
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable {
                            Log.d("CameraView", "Switch camera clicked")
                            previewView?.let {
                                Log.d("CameraView", "Switching camera")
                                controller.switchCamera(it)
                            } ?: run {
                                Log.d("CameraView", "previewView is null")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_cover_circle),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Image(
                        painter = painterResource(R.drawable.icon_camera_switch),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
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
) {
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
    val cameraController = remember { CameraController(context) }
    CameraView(controller = cameraController, navController = rememberNavController())
}