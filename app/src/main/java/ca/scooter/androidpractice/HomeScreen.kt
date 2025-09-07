package ca.scooter.androidpractice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserData(
    val uid: String,
    val name: String?,
    val email: String?,
    val avatarUrl: String?
)

class UserDataViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserData(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val documentSnapshot = db.collection("users").document(firebaseUser.uid).get().await()
                if (documentSnapshot.exists()) {
                    _userData.value = UserData(
                        uid = firebaseUser.uid,
                        name = documentSnapshot.getString("name"),
                        email = documentSnapshot.getString("email"),
                        avatarUrl = documentSnapshot.getString("avatarUrl")
                    )
                } else {
                     _userData.value = UserData(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName,
                        email = firebaseUser.email,
                        avatarUrl = firebaseUser.photoUrl?.toString()
                    )
                }
            } catch (e: Exception) {
                _userData.value = UserData(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName,
                    email = firebaseUser.email,
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
            }
            _isLoading.value = false
        }
    }
}

class HomeScreen : ComponentActivity() {

    private lateinit var mAuth: FirebaseAuth
    private val userDataViewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }

        if (userDataViewModel.userData.value?.uid != currentUser.uid) {
             userDataViewModel.loadUserData(currentUser)
        }

        setContent {
            MaterialTheme {
                val user by userDataViewModel.userData.collectAsState()
                MainAppScreen(
                    userName = user?.name,
                    userEmail = user?.email,
                    userAvatarUrl = user?.avatarUrl,
                    onLogout = {
                        mAuth.signOut()
                        Toast.makeText(this, "You've been signed out!", Toast.LENGTH_LONG).show()
                        redirectToLogin()
                    }
                )
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this@HomeScreen, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
