package com.example.abgabestellenberlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.example.abgabestellenberlin.ui.screens.MainScreen
import com.example.abgabestellenberlin.ui.theme.AbgabestellenBerlinTheme
import com.example.abgabestellenberlin.ui.viewmodel.MainViewModel
import com.example.abgabestellenberlin.ui.viewmodel.factory.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(DropOffRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbgabestellenBerlinTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
