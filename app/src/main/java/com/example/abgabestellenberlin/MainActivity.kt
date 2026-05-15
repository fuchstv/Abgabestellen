package com.example.abgabestellenberlin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.example.abgabestellenberlin.logic.AuthManager
import com.example.abgabestellenberlin.ui.screens.MainScreen
import com.example.abgabestellenberlin.ui.theme.AbgabestellenBerlinTheme
import com.example.abgabestellenberlin.ui.viewmodel.MainViewModel
import com.example.abgabestellenberlin.ui.viewmodel.factory.MainViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(DropOffRepository())
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { signInWithFirebase(it) }
        } catch (e: Exception) {
            Log.e("MainActivity", "Sign-in failed", e)
        }
    }

    private fun signInWithFirebase(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    viewModel.updateFirebaseUser(user)
                } else {
                    Log.e("MainActivity", "Firebase sign-in failed", task.exception)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        
        // Update ViewModel with current Firebase user
        viewModel.updateFirebaseUser(FirebaseAuth.getInstance().currentUser)

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
