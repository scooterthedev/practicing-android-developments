package ca.scooter.androidpractice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.scooter.androidpractice.ui.home.Repository
import ca.scooter.androidpractice.ui.home.RetrofitClient
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserData(
    val uid: String,
    val name: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val githubUsername: String? = null
)

class UserDataViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val githubApiService = RetrofitClient.githubApiService

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories: StateFlow<List<Repository>> = _repositories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserData(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userDocumentRef = db.collection("users").document(firebaseUser.uid)
                val documentSnapshot = userDocumentRef.get().await()

                val currentGithubUsername: String?
                if (documentSnapshot.exists()) {
                    currentGithubUsername = documentSnapshot.getString("githubUsername")
                    _userData.value = UserData(
                        uid = firebaseUser.uid,
                        name = documentSnapshot.getString("name") ?: firebaseUser.displayName,
                        email = documentSnapshot.getString("email") ?: firebaseUser.email,
                        avatarUrl = documentSnapshot.getString("avatarUrl") ?: firebaseUser.photoUrl?.toString(),
                        githubUsername = currentGithubUsername
                    )
                    loadRepos(firebaseUser.uid)
                } else {
                    currentGithubUsername = firebaseUser.providerData.find { it.providerId == "github.com" }?.uid
                    val newUser = UserData(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName,
                        email = firebaseUser.email,
                        avatarUrl = firebaseUser.photoUrl?.toString(),
                        githubUsername = currentGithubUsername
                    )
                    userDocumentRef.set(mapOf(
                        "name" to newUser.name,
                        "email" to newUser.email,
                        "avatarUrl" to newUser.avatarUrl,
                        "githubUsername" to newUser.githubUsername
                    ), SetOptions.merge()).await()
                    _userData.value = newUser
                }

                if (currentGithubUsername != null) {
                    fetchRepos(firebaseUser.uid, currentGithubUsername)
                } else {
                    Log.d("UserDataViewModel", "GitHub username not found for user ${firebaseUser.uid}")
                    _error.value = "GitHub username not found. Please link your GitHub account."
                    _repositories.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("UserDataViewModel", "Error loading user data or repositories", e)
                _error.value = "Error: ${e.localizedMessage}"
                _userData.value = UserData(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName,
                    email = firebaseUser.email,
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                _repositories.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchRepos(userId: String, githubUsername: String) {
        try {
            val response = githubApiService.listPublicRepos(githubUsername)
            if (response.isSuccessful) {
                val fetchedRepos = response.body() ?: emptyList()
                _repositories.value = fetchedRepos
                val userReposRef = db.collection("users").document(userId).collection("repositories")
                val batch = db.batch()
                userReposRef.get().await().documents.forEach { batch.delete(it.reference) }
                fetchedRepos.forEach { repo ->
                    val repoDocRef = userReposRef.document(repo.id.toString())
                    batch.set(repoDocRef, repo)
                }
                batch.commit().await()
                Log.d("UserDataViewModel", "Fetched and stored ${fetchedRepos.size} repositories.")
            } else {
                val errorMsg = "Error fetching repos: ${response.code()} - ${response.message()}"
                Log.e("UserDataViewModel", errorMsg)
                _error.value = errorMsg
                loadRepos(userId, onlyIfEmpty = false)
            }
        } catch (e: Exception) {
            Log.e("UserDataViewModel", "Exception fetching or storing repositories", e)
            _error.value = "Network error: ${e.localizedMessage}"
            loadRepos(userId, onlyIfEmpty = false)
        }
    }

    private fun loadRepos(userId: String, onlyIfEmpty: Boolean = true) {
        if (onlyIfEmpty && _repositories.value.isNotEmpty()) return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(userId).collection("repositories").get().await()
                val firestoreRepos = snapshot.toObjects(Repository::class.java)
                if (firestoreRepos.isNotEmpty()) {
                    _repositories.value = firestoreRepos
                    Log.d("UserDataViewModel", "Loaded ${firestoreRepos.size} repositories from Firestore.")
                } else if (!onlyIfEmpty || _repositories.value.isEmpty()) {
                    Log.d("UserDataViewModel", "No repositories found in Firestore for user $userId.")
                }
            } catch (e: Exception) {
                Log.e("UserDataViewModel", "Error loading repositories from Firestore", e)
                if (_error.value == null) {
                    _error.value = "Error loading cached repositories: ${e.localizedMessage}"
                }
            }
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

        if (userDataViewModel.userData.value?.uid != currentUser.uid || userDataViewModel.repositories.value.isEmpty()) {
            userDataViewModel.loadUserData(currentUser)
        }

        setContent {
            MaterialTheme {
                val user by userDataViewModel.userData.collectAsState()
                val repositories by userDataViewModel.repositories.collectAsState()
                val isLoading by userDataViewModel.isLoading.collectAsState()
                val error by userDataViewModel.error.collectAsState()
                val context = LocalContext.current

                MainAppScreen(
                    userName = user?.name,
                    userEmail = user?.email,
                    userAvatarUrl = user?.avatarUrl,
                    repositories = repositories,
                    isLoading = isLoading,
                    errorMessage = error,
                    onLogout = {
                        mAuth.signOut()
                        Toast.makeText(context, "You've been signed out!", Toast.LENGTH_LONG).show()
                        redirectToLogin()
                    },
                    onRetry = {
                        currentUser.let { userDataViewModel.loadUserData(it) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    userName: String?,
    userEmail: String?,
    userAvatarUrl: String?,
    repositories: List<Repository>,
    isLoading: Boolean,
    errorMessage: String?,
    onLogout: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitHub Repositories") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            errorMessage?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val showContent = !isLoading
            val showUserInfo = showContent && (errorMessage == null || repositories.isNotEmpty())
            val showRepos = showContent && repositories.isNotEmpty()

            if (showUserInfo) {
                UserProfileInfoCard(userName, userEmail, userAvatarUrl)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showRepos) {
                RepositoryList(repositories = repositories)
            } else if (showContent && errorMessage == null && repositories.isEmpty()) {
                Text("No repositories found or GitHub username not set.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun UserProfileInfoCard(name: String?, email: String?, avatarUrl: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        avatarUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        name?.let {
            Text(text = it, style = MaterialTheme.typography.headlineSmall)
        }
        email?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RepositoryList(repositories: List<Repository>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(repositories, key = { it.id }) { repo ->
            RepositoryItem(repo = repo)
        }
    }
}

@Composable
fun RepositoryItem(repo: Repository) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
            repo.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Link: ${repo.htmlUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
