package com.example.memp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memp.data.local.AppDatabase
import com.example.memp.data.repository.MemoRepositoryImpl
import com.example.memp.ui.screen.MemoListScreen
import com.example.memp.ui.viewmodel.MemoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MemoViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = MemoRepositoryImpl(database.memoDao())
                return MemoViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoListScreen(viewModel = viewModel)
                }
            }
        }
    }
}