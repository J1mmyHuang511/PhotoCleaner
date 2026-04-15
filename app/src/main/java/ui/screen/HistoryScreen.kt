package com.jimmy.photocleaner.ui.screen

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jimmy.photocleaner.data.model.OperationHistory
import com.jimmy.photocleaner.viewmodel.PhotoCleanerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: PhotoCleanerViewModel,
    onBack: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    // 核心功能：处理系统弹出的“批量删除确认”
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.clearDeletedRecords()
            Toast.makeText(context, "彻底清理成功！空间已释放", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("操作记录", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        // 👇 这里就是新加的“一键清理”悬浮大按钮
        floatingActionButton = {
            if (historyList.any { it.operation == "DELETED" }) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val deletedItems = historyList.filter { it.operation == "DELETED" }
                        val deletedUris = deletedItems.map { Uri.parse(it.photoUri) }

                        // 呼叫安卓系统自带的批量删除对话框
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, deletedUris)
                            deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent).build())
                        } else {
                            viewModel.deletePermanentlyLegacy(deletedItems.map { it.photoPath })
                            Toast.makeText(context, "彻底清理成功！空间已释放", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Default.DeleteSweep, "一键清理") },
                    text = {
                        val count = historyList.count { it.operation == "DELETED" }
                        Text("一键彻底删除 ($count) 张")
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无记录", color = Color.Gray) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), // 留出底部空间给按钮
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { history -> HistoryItem(history) }
            }
        }
    }
}

@Composable
private fun HistoryItem(history: OperationHistory) {
    val isDeleted = history.operation == "DELETED"
    val statusColor = if (isDeleted) Color.Red else Color(0xFF4CAF50)
    val statusText = if (isDeleted) "待删除" else "已保留"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = history.photoUri, contentDescription = null, modifier = Modifier.size(64.dp).padding(end = 12.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = history.photoName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = history.photoPath, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(history.timestamp)), fontSize = 11.sp, color = Color.Gray)
            }
            Text(text = statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}