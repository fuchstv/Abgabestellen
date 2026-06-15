package de.foodsharing.abgabestellen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import de.foodsharing.abgabestellen.data.repository.DropOffRepository
import de.foodsharing.abgabestellen.ui.screens.MainScreen
import de.foodsharing.abgabestellen.ui.theme.AbgabestellenBerlinTheme
import de.foodsharing.abgabestellen.ui.viewmodel.MainViewModel
import de.foodsharing.abgabestellen.ui.viewmodel.factory.MainViewModelFactory

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
