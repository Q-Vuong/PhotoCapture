package com.example.photocapture.feature.gallery

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImageDetailView(imageUri: Uri, controller: GalleryController, imageUriList: List<Uri>, navController: NavController) {
    var imageUriList = remember { imageUriList.toList() }
    val initialIndex = imageUriList.indexOf(imageUri).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex) { imageUriList.size }
    val uriRemoveImage = remember { mutableStateOf(imageUri) }
    val selectedImageIndex = remember { mutableIntStateOf(initialIndex) }

    LaunchedEffect(pagerState.currentPage, selectedImageIndex.intValue) {
        uriRemoveImage.value = imageUriList.getOrNull(pagerState.currentPage) ?: Uri.EMPTY
        pagerState.scrollToPage(selectedImageIndex.intValue)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(58.dp)
                    .padding(10.dp)
                    .align(Alignment.End),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancle Icon",
                    modifier = Modifier.fillMaxSize()
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 350.dp)
                    .sizeIn(maxHeight = 620.dp)
            ) { page ->
                uriRemoveImage.value = imageUriList[page]
                Image(
                    painter = rememberAsyncImagePainter(imageUriList[page]),
                    contentDescription = "Image ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                reverseLayout = true
            ) {
                items(imageUriList.size) { index ->
                    val uri = imageUriList[imageUriList.lastIndex - index]
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Saved Image",
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(50.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedImageIndex.intValue = imageUriList.indexOf(uri)
                                uriRemoveImage.value = uri
                                Log.d("GalleryController", "Uri: ${uriRemoveImage.value}")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (uriRemoveImage.value != Uri.EMPTY ) {
                BottomView(controller, uriRemoveImage.value) { removedUri ->
                    imageUriList = imageUriList.filter { it != removedUri }
                        .toMutableList() // Cập nhật lại danh sách khi xoá ảnh
                    uriRemoveImage.value = Uri.EMPTY
                }
            }
        }
    }
}

@Composable
fun BottomView(controller: GalleryController, uri: Uri, onImageRemoved: (Uri) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .width(220.dp)
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Cancle Icon",
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Cancle Icon",
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        IconButton(
            onClick = {
                controller.deleteImage(uri)
                onImageRemoved(uri)
            },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Cancle Icon",
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageDetailViewPreView() {
    val context = LocalContext.current
    val galleryController = remember { GalleryController(context) }
    val imageUris = remember { galleryController.getSavedImages() }
    val uri = Uri.EMPTY

    ImageDetailView(uri, galleryController, imageUris, rememberNavController())
}