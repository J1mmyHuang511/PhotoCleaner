package com.jimmy.photocleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jimmy.photocleaner.ui.screen.HistoryScreen
import com.jimmy.photocleaner.ui.screen.PhotoCleanerScreen
import com.jimmy.photocleaner.ui.screen.SettingsScreen
import com.jimmy.photocleaner.ui.theme.PhotoCleanerTheme
import com.jimmy.photocleaner.viewmodel.PhotoCleanerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoCleanerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // 三个页面共享同一个数据层，保证设置瞬间生效
                    val viewModel: PhotoCleanerViewModel = hiltViewModel()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            PhotoCleanerScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToSettings = { navController.navigate("settings") } // 👈 新增设置跳转
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}