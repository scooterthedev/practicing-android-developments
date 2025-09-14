package ca.scooter.androidpractice.ui.home

import com.google.firebase.firestore.PropertyName

data class Repository(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    @get:PropertyName("html_url") @set:PropertyName("html_url") var htmlUrl: String = "",
    val stars: Int = 0,
    val forks: Int = 0,
)
