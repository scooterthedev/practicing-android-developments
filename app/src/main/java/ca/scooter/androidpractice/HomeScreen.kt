package ca.scooter.androidpractice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ca.scooter.androidpractice.ui.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeScreen : ComponentActivity() {

    private lateinit var mAuth: FirebaseAuth
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }

        setContent {
            MaterialTheme {
                AppContent(homeViewModel = homeViewModel, onLogout = ::handleLogout)
            }
        }
    }

    private fun handleLogout() {
        mAuth.signOut()
        Toast.makeText(this, "You've been signed out!", Toast.LENGTH_LONG).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this@HomeScreen, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun AppContent(homeViewModel: HomeViewModel, onLogout: () -> Unit) {
    MainAppScreen(
        homeViewModel = homeViewModel,
        onLogout = onLogout
    )
}
