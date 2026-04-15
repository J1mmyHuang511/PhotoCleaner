package com.jimmy.photocleaner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jimmy.photocleaner.data.model.Photo
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoCard(
    photo: Photo,
    onSwipeLeft: (Photo) -> Unit, // 左滑删除
    onSwipeRight: (Photo) -> Unit, // 右滑保留
    modifier: Modifier = Modifier
) {
    var offsetX by remember(photo.id) { mutableStateOf(0f) }
    var isVisible by remember(photo.id) { mutableStateOf(true) }

    if (!isVisible) return

    val screenWidth = 400f
    val swipeThreshold = screenWidth * 0.3f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
            // 👇 核心修复点：将 Unit 改为 photo.id。强制手势监听器跟随照片更新！
            .pointerInput(photo.id) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                        offsetX = offsetX.coerceIn(-screenWidth, screenWidth)
                    },
                    onDragEnd = {
                        when {
                            offsetX > swipeThreshold -> {
                                isVisible = false
                                onSwipeRight(photo) // 向右拖拽，保留
                            }
                            offsetX < -swipeThreshold -> {
                                isVisible = false
                                onSwipeLeft(photo) // 向左拖拽，删除
                            }
                            else -> offsetX = 0f
                        }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (offsetX / 50).dp)
                .alpha(1 - (kotlin.math.abs(offsetX) / screenWidth * 0.3f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = photo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                ).align(Alignment.TopCenter))

                PhotoInfo(photo = photo, modifier = Modifier.align(Alignment.TopStart))
                SwipeHints(offsetX = offsetX, screenWidth = screenWidth)
                BottomStats(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp))
            }
        }
    }
}

@Composable
private fun PhotoInfo(photo: Photo, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp).fillMaxWidth(0.8f)) {
        Text(photo.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(formatFileSize(photo.size), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Text(formatDate(photo.dateModified), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun SwipeHints(offsetX: Float, screenWidth: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 向右拖拽出现绿色保留
        if (offsetX > 20) {
            Text("✓ 保留", color = Color.Green.copy(alpha = (offsetX / screenWidth * 0.8f)), fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp))
        }
        // 向左拖拽出现红色删除
        if (offsetX < -20) {
            Text("✕ 删除", color = Color.Red.copy(alpha = (kotlin.math.abs(offsetX) / screenWidth * 0.8f)), fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp))
        }
    }
}

@Composable
private fun BottomStats(modifier: Modifier = Modifier) {
    Row(modifier = modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("← 左滑删除", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("→ 右滑保留", color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024f * 1024f))
    bytes >= 1024 -> String.format("%.2f KB", bytes / 1024f)
    else -> "$bytes B"
}

private fun formatDate(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp * 1000))