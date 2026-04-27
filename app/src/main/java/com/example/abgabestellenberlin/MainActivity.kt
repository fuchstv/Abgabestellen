package com.example.abgabestellenberlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.abgabestellenberlin.data.remote.GoogleSheetsService
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.example.abgabestellenberlin.logic.AuthManager
import com.example.abgabestellenberlin.ui.screens.MainScreen
import com.example.abgabestellenberlin.ui.theme.AbgabestellenBerlinTheme
import com.example.abgabestellenberlin.ui.viewmodel.MainViewModel
import com.example.abgabestellenberlin.ui.viewmodel.factory.MainViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(DropOffRepository(GoogleSheetsService(this)))
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            viewModel.setUserAccount(account)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Sign-in failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        
        // Check for existing account
        viewModel.setUserAccount(authManager.getLastSignedInAccount())

        enableEdgeToEdge()
        setContent {
            AbgabestellenBerlinTheme {
                MainScreen(
                    viewModel = viewModel,
                    onSignInClick = { signInLauncher.launch(authManager.getSignInIntent()) }
                )
            }
        }
    }
}
