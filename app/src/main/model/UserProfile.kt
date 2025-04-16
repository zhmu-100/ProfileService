package src.main.model

data class UserProfile(
    val ID: Int,
    val name: String,
    val email: String,
    val imageID: Int,
    val location: Location,
    val birthday: Date,
    val weight: Float,
    val height: Float,
    val followerCount: Int,
    val followingCount: Int,
)

data class Location(
    val country: String,
    val city: String,
)

data class Date(
    val year: Int,
    val month: Int,
    val day: Int,
)