package com.example.photocapture.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin


fun slideInFromTop(): EnterTransition = slideInVertically(
    animationSpec = tween(500),
    initialOffsetY = { it }
)

fun slideOutFromTop(): ExitTransition = slideOutVertically(
    animationSpec = tween(300),
    targetOffsetY = { it }
)

fun scaleInAnimation(): EnterTransition = scaleIn(
    animationSpec = tween(500), // Thời gian animation 300ms
    initialScale = 0f // Tỷ lệ ban đầu là 0.5 (nhỏ hơn kích thước gốc)
)

/*fun scaleOutAnimation(): ExitTransition = scaleOut(
    animationSpec = tween(500), // Thời gian animation 300ms
    targetScale = 0f // Tỷ lệ đích là 0.5 (nhỏ hơn kích thước gốc)
)

fun slideInFromLeft(): EnterTransition = slideInHorizontally(
    animationSpec = tween(300), // Thời gian animation 300ms
    initialOffsetX = { -it } // Trượt vào từ cạnh trái
)

fun slideOutToRight(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(300), // Thời gian animation 300ms
    targetOffsetX = { it } // Trượt ra khỏi màn hình theo hướng phải
)*/

fun fadeOutAndZoomOut(): ExitTransition = fadeOut(
    animationSpec = tween(300) // Thời gian animation 300ms
) + scaleOut(
    animationSpec = tween(300), // Thời gian animation 300ms
    targetScale = 0f, // Thu nhỏ về 0
    transformOrigin = TransformOrigin(0.5f, 0.5f) // Zoom từ trung tâm
)

/*
fun fadeInAndZoomIn(): EnterTransition = fadeIn(
    animationSpec = tween(300) // Thời gian animation 300ms
) + scaleIn(
    animationSpec = tween(300), // Thời gian animation 300ms
    initialScale = 0f, // Bắt đầu từ scale 0 (không hiển thị)
    transformOrigin = TransformOrigin(0.5f, 0.5f) // Zoom từ trung tâm
)*/
