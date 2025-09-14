package ca.scooter.androidpractice.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface RepoService {
    //need to fix later lol
    suspend fun fetchRepos(user: FirebaseUser?): Result<List<Repository>>
}
class GithubRepoService : RepoService {
    override suspend fun fetchRepos(user: FirebaseUser?): Result<List<Repository>> {
        return if (user != null){
            Result.success(
                listOf(
                    Repository(1, "Test", "Test", "Test")
                )
            )
        } else {
            Result.failure(Exception("Uhhh, somehow your not logged in!"))
        }
    }
}

class HomeViewModel(
    private val repoService: RepoService = GithubRepoService()
) : ViewModel() {
    private val _repos = MutableStateFlow<List<Repository>>(emptyList())
    val repos: StateFlow<List<Repository>> = _repos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUser = FirebaseAuth.getInstance().currentUser

    init {
        fetchRepos()
    }
    fun fetchRepos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _repos.value = repoService.fetchRepos(currentUser).getOrThrow()
            } catch (e: Exception){
                _error.value = "${e.message}"
                _repos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}