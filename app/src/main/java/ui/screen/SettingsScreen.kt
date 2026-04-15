package com.jimmy.photocleaner.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimmy.photocleaner.viewmodel.PhotoCleanerViewModel
import com.jimmy.photocleaner.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PhotoCleanerViewModel,
    onBack: () -> Unit
) {
    val statistics by viewModel.statistics.collectAsState()
    val currentOrder by viewModel.currentSortOrder.collectAsState()
    val currentMinSize by viewModel.minSizeFilterMB.collectAsState()

    // 👇 新增：用于打开外部网页链接的处理器
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置与统计", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("数据统计", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem("已删除", statistics.first, Color.Red)
                        StatItem("已保留", statistics.second, Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.reset(); onBack() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("清空并重置所有记录", color = Color.White)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("照片展示顺序", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    SortOrder.values().forEach { order ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(
                                selected = currentOrder == order,
                                onClick = { viewModel.updateSettings(order, currentMinSize) }
                            )
                            Text(order.displayName)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("跳过小文件 (只看大于 ${currentMinSize} MB 的图)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = currentMinSize.toFloat(),
                        onValueChange = { viewModel.updateSettings(currentOrder, it.toInt()) },
                        valueRange = 0f..50f,
                        steps = 50
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0 MB", fontSize = 12.sp, color = Color.Gray)
                        Text("50+ MB", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // 👇 新增：开发者信息与 GitHub 仓库模块
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("开发者: JimmyHuang", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { uriHandler.openUri("https://github.com/J1mmyHuang511/PhotoCleaner") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("⭐ 访问 GitHub 开源仓库", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = count.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
    }
}