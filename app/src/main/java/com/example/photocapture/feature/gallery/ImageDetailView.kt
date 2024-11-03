package com.example.photocapture.feature.gallery

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDownCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.photocapture.animations.slideInFromTop
import com.example.photocapture.animations.slideOutFromTop
import androidx.compose.runtime.getValue

@Composable
fun ImageDetailView(imageUri: Uri, controller: GalleryController, imageUriList: List<Uri>, navController: NavController) {
    val imageUriListState = rememberSaveable { mutableStateOf(imageUriList) }

    val initialIndex = imageUriListState.value.indexOf(imageUri).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { imageUriListState.value.size })

    val uriRemoveImage = rememberSaveable { mutableStateOf(imageUri) }
    val selectedImageIndex = rememberSaveable { mutableStateOf(initialIndex) }

    val showInfo = remember { mutableStateOf(false) }
    var isDeleteDialogOpen = remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (showInfo.value) 0.8f else 1f, // Scale xuống 0.8 khi showInfo.value là true
        animationSpec = tween(600) // Thời gian animation 300ms
    )

    LaunchedEffect(selectedImageIndex.value) {
        pagerState.scrollToPage(selectedImageIndex.value)
    }

    LaunchedEffect(pagerState.currentPage) {
        uriRemoveImage.value = imageUriListState.value.getOrNull(pagerState.currentPage) ?: Uri.EMPTY
        selectedImageIndex.value = pagerState.currentPage
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
                    contentDescription = "Cancel Icon",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val imageUri = imageUriListState.value[page]
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            if (!showInfo.value) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    reverseLayout = true
                ) {
                    items(imageUriListState.value.size) { index ->
                        val uri = imageUriListState.value[imageUriListState.value.lastIndex - index]
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Saved Image",
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(50.dp)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedImageIndex.value = imageUriListState.value.indexOf(uri)
                                    uriRemoveImage.value = uri
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (uriRemoveImage.value != Uri.EMPTY) {
                Spacer(modifier = Modifier.height(8.dp))
                BottomView(
                    controller,
                    uriRemoveImage.value,
                    navController,
                    showInfo,
                    isDeleteDialogOpen
                ) { removedUri ->

                    imageUriListState.value = imageUriListState.value.filter { it != removedUri }

                    Log.d("ImageDetailView", "Update list photo")
                    if (imageUriListState.value.isNotEmpty()) {
                        selectedImageIndex.value = selectedImageIndex.value.coerceAtMost(imageUriListState.value.lastIndex)
                        uriRemoveImage.value = imageUriListState.value[selectedImageIndex.value.coerceAtMost(imageUriListState.value.lastIndex)]
                    } else {
                        uriRemoveImage.value = Uri.EMPTY
                        navController.popBackStack()
                    }
                }
            }

            /*if (showInfo.value) {
                InfoView(controller, uriRemoveImage.value, showInfo)
            }*/
            AnimatedVisibility(
                visible = showInfo.value,
                enter = slideInFromTop(),
                exit = slideOutFromTop()
            ) {
                InfoView(controller, uriRemoveImage.value, showInfo)
            }
        }
    }
}

@Composable
fun BottomView(
    controller: GalleryController,
    uri: Uri, navController: NavController,
    showInfo: MutableState<Boolean>,
    isDeleteDialogOpen: MutableState<Boolean>,
    onImageRemoved: (Uri) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .width(220.dp)
            .background(MaterialTheme.colorScheme.surfaceDim)
            .border(0.1.dp, Color.Gray, shape = RoundedCornerShape(30.dp))
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                navController.navigate("editPhoto/${Uri.encode(uri.toString())}")
            },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Button",
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = {
                if (showInfo.value) {
                    showInfo.value = false
                } else {
                    showInfo.value = true
                }
            },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (!showInfo.value) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info Button",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info Button",
                    modifier = Modifier.fillMaxSize()
                )
            }

        }

        IconButton(
            onClick = { isDeleteDialogOpen.value = true },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Icon",
                modifier = Modifier.fillMaxSize()
            )
        }

    }

    if (isDeleteDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen.value = false },
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn có chắc chắn muốn xoá ảnh này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finishDelete = controller.deletePhoto(uri)
                        if (finishDelete) {
                            Log.d("ImageDetailView", "Finished delete uri: $uri")
                            onImageRemoved(uri)
                        } else {
                            Log.e("ImageDetailView", "Error delete uri: $uri")
                        }
                        isDeleteDialogOpen.value = false
                    }
                ) {
                    Text("Xoá")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDeleteDialogOpen.value = false }
                ) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
fun InfoView(controller: GalleryController, uri: Uri, showInfo: MutableState<Boolean>) {
    val photoDetails = controller.getPhotoDetailsByUri(uri)

    photoDetails?.let {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                Text(text = "URI: ${it.imageUri}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Capture Date: ${it.captureDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Image Size: ${it.imageSize} bytes",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = { showInfo.value = false },
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.End),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDownCircle,
                        contentDescription = "Close Infor",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    } ?: run {
        Log.e("InfoView", "No details found for URI: $uri")
    }
}


@Preview(showBackground = true)
@Composable
fun ImageDetailViewPreView() {
    val context = LocalContext.current
    val galleryController = remember { GalleryController(context) }
    val imageUris = remember { galleryController.getListPhotos() }
    val uri = Uri.EMPTY
    //val showInfo = remember { mutableStateOf(false) }
    //val isDeleteDialogOpen = remember { mutableStateOf(false) }

    ImageDetailView(uri, galleryController, imageUris, rememberNavController())

    //BottomView(galleryController, uri, rememberNavController(), showInfo, isDeleteDialogOpen) {}

    //InfoView(galleryController, uri, showInfo)
}
