package ca.scooter.androidpractice.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RepoList(repos: List<Repository>) {
    LazyColumn {
        items(repos) { repo ->
            RepoItem(repo = repo)
        }
    }
}

@Composable
fun RepoItem(repo: Repository) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
            repo.description.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepoListPreview() {
    val mockRepos = listOf(
        Repository(1, "Repo 1", "This is a sample description for repository 1.", "url", "owner", "commit", "date", "Kotlin", "123"),
        Repository(2, "Repo 2", "This is a sample description for repository 2.", "url", "owner", "commit", "date", "Java", "456")
    )
    RepoList(repos = mockRepos)
}
