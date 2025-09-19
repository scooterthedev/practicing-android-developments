package ca.scooter.androidpractice.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RepoList(
    repos: List<Repository>,
    onRepoClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(8.dp)
    ) {
        items(repos, key = { it.id }) { repo ->
            RepoListItem(repo = repo, onClick = { onRepoClick(repo.id) })
        }
    }
}

@Composable
fun RepoListItem(
    repo: Repository,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = repo.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            repo.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Stars: ${repo.stargazers_count}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
