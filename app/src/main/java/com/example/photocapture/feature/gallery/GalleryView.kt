package com.example.photocapture.feature.gallery

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun GalleryView(imageUris: List<Uri>, navController: NavController) {
    Scaffold { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(imageUris.size) { index ->
                val uri = imageUris[index]
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                        .clickable {
                            navController.navigate("imageDetail/${Uri.encode(uri.toString())}")
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryViewPreView() {
    val context = LocalContext.current
    val galleryController = remember { GalleryController(context) }
    val imageUris = remember { galleryController.getSavedImages() }

    GalleryView(imageUris, rememberNavController())
}