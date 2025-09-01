package ca.scooter.androidpractice.ui.home

data class Repository(
        val id: Long,
        val name: String,
        val description: String,
        val url: String,
        val owner: String,
        val last_commit: String,
        val commit_date: String,
        val lang: String,
        val stars: String
        )