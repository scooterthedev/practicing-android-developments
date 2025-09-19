package ca.scooter.androidpractice.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val _repos = MutableStateFlow<List<Repository>>(emptyList())
    val repos: StateFlow<List<Repository>> = _repos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _displayName = MutableStateFlow<String?>(null)
    val displayName: StateFlow<String?> = _displayName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl.asStateFlow()

    private val _githubUserName = MutableStateFlow<String?>(null)
    val githubUserName: StateFlow<String?> = _githubUserName.asStateFlow()

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val userId = currentUser?.uid

            if (userId == null) {
                _error.value = "User not logged in."
                _isLoading.value = false
                return@launch
            }

            var reposLoadedFromCache = false
            try {
                val snapshot = db.collection("users").document(userId)
                    .collection("repositories")
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    val cachedRepos = snapshot.documents.mapNotNull { it.toObject(Repository::class.java) }
                    if (cachedRepos.isNotEmpty()) {
                        _repos.value = cachedRepos
                        reposLoadedFromCache = true
                    }
                }
            } catch (e: Exception) {
            }

            var currentGithubUsername: String? = null
            try {
                val userDocumentSnapshot = db.collection("users").document(userId).get().await()
                if (userDocumentSnapshot.exists()) {
                    _displayName.value = userDocumentSnapshot.getString("name")
                    _userEmail.value = userDocumentSnapshot.getString("email")
                    _userAvatarUrl.value = userDocumentSnapshot.getString("avatarUrl")
                    currentGithubUsername = userDocumentSnapshot.getString("githubUsername")
                    _githubUserName.value = currentGithubUsername
                } else {
                    if (!reposLoadedFromCache) _error.value = "User details not found."
                    _isLoading.value = false
                    return@launch
                }
            } catch (e: Exception) {
                if (!reposLoadedFromCache) _error.value = "Failed to fetch user details: ${e.message}"
                _isLoading.value = false
                return@launch
            }

            if (!currentGithubUsername.isNullOrBlank()) {
                try {
                    val response = RetrofitClient.githubApiService.listPublicRepos(currentGithubUsername)
                    if (response.isSuccessful) {
                        val fetchedReposFromApi = response.body()
                        if (fetchedReposFromApi != null) {
                            val userReposCollection = db.collection("users").document(userId)
                                .collection("repositories")
                            val batch = db.batch()
                            fetchedReposFromApi.forEach { repo ->
                                val docRef = userReposCollection.document(repo.id.toString())
                                batch.set(docRef, repo)
                            }
                            batch.commit().await()
                            _repos.value = fetchedReposFromApi
                        } else {
                             if (!reposLoadedFromCache) _repos.value = emptyList()
                        }
                    } else {
                        if (!reposLoadedFromCache) {
                            _error.value = "Error fetching from GitHub: ${response.code()} ${response.message()}"
                            _repos.value = emptyList()
                        }
                    }
                } catch (e: Exception) {
                    if (!reposLoadedFromCache) {
                        _error.value = "Failed to fetch GitHub repositories: ${e.message}"
                        _repos.value = emptyList()
                    }
                }
            } else {
                if (!reposLoadedFromCache) {
                    _error.value = "GitHub username not found in user details."
                    _repos.value = emptyList()
                }
            }
            _isLoading.value = false
        }
    }
}
