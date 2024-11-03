package com.example.photocapture.feature.photoedit

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun EditPhotoView(photoUri: Uri, controller: EditController, navController: NavController) {
    val bitmap = remember { controller.loadPhotoFromUri(photoUri) }
    var photoEditedBitmap by remember { mutableStateOf(bitmap) }
    var isCancelDialogOpen = remember { mutableStateOf(false) }


    BackHandler {
        if (controller.history.isNotEmpty()) {
            isCancelDialogOpen.value = true
        } else {
            navController.popBackStack()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 10.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if(controller.history.isNotEmpty()) {
                            isCancelDialogOpen.value = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outline
                    ),

                    ) {
                    Text("Huỷ")
                }

                Button(
                    onClick = {
                        photoEditedBitmap?.let {
                            controller.saveEditedPhoto(it, photoUri)
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Text("Xong")
                }
            }

            photoEditedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .background(Color.Transparent)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoEditedBitmap = controller.undo() ?: photoEditedBitmap
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Button"
                    )
                }

                IconButton(
                    onClick = {
                        photoEditedBitmap = controller.redo() ?: photoEditedBitmap
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo Button",
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .width(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(0.1.dp, Color.Gray, shape = RoundedCornerShape(30.dp))
                    .padding(vertical = 7.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoEditedBitmap = photoEditedBitmap?.let { updatedBitmap ->
                            val newBitmap = controller.rotateBitmap(updatedBitmap, 90f)
                            newBitmap
                        }
                    },
                    modifier = Modifier
                        .size(38.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate Button",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                IconButton(
                    onClick = {
                        photoEditedBitmap = photoEditedBitmap?.let { updatedBitmap ->
                            val newBitmap = controller.flipPhoto(updatedBitmap)
                            newBitmap
                        }
                    },
                    modifier = Modifier
                        .size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Rotate Button",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }

    if (isCancelDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isCancelDialogOpen.value = false },
            title = { Text("Huỷ bỏ thay đổi") },
            text = { Text("Bạn có chắc chắn muốn huỷ bỏ tất cả các thay đổi không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isCancelDialogOpen.value = false
                    }
                ) {
                    Text("Không")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isCancelDialogOpen.value = false
                        navController.popBackStack()}
                ) {
                    Text("Huỷ bỏ")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditPhotoViewPreview() {
    val imageUri = Uri.EMPTY
    val context = LocalContext.current
    val controller = EditController(context)

    EditPhotoView(imageUri, controller, rememberNavController())
}