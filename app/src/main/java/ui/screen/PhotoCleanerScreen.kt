package com.jimmy.photocleaner.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jimmy.photocleaner.R
import com.jimmy.photocleaner.ui.component.PhotoCard
import com.jimmy.photocleaner.viewmodel.PhotoCleanerUiState
import com.jimmy.photocleaner.viewmodel.PhotoCleanerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCleanerScreen(
    viewModel: PhotoCleanerViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // 👇 修复点：提前判断权限，有权限就直接读，避免重复弹窗和刷新
    val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.loadPhotos(forceRefresh = true)
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            launcher.launch(permission)
        } else {
            viewModel.loadPhotos() // 这里的 loadPhotos 已受 ViewModel 保护，不会刷掉当前图
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "历史记录", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is PhotoCleanerUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is PhotoCleanerUiState.Empty -> EmptyState(modifier = Modifier.align(Alignment.Center))
                is PhotoCleanerUiState.ShowPhoto -> {
                    val photo = (uiState as PhotoCleanerUiState.ShowPhoto).photo
                    PhotoCard(
                        photo = photo,
                        onSwipeLeft = { viewModel.swipeLeft(photo) },
                        onSwipeRight = { viewModel.swipeRight(photo) },
                        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp)
                    )
                }
                is PhotoCleanerUiState.Error -> ErrorState((uiState as PhotoCleanerUiState.Error).message, Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "🎉", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "照片清理完毕", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "没有符合条件的照片了", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "⚠️", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "出错了", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
    }
}