package com.example.photocapture.feature.gallery

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun GalleryView(imageUris: List<Uri>, navController: NavController) {
    BackHandler {
        navController.popBackStack() // Quay lại HomeScreen
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(imageUris) { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Saved Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp)
            )

        }
    }
}