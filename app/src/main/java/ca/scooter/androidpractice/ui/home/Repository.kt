package ca.scooter.androidpractice.ui.home

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Repository(
    var id: Long = 0L,
    var name: String = "",
    var html_url: String = "",
    var description: String? = null,
    var stargazers_count: Int = 0
) {
    constructor() : this(0L, "", "", null, 0)
}
