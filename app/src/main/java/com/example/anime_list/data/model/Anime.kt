package com.example.anime_list.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnimeResponse(
    @SerializedName("data") val data: List<Anime>,
    @SerializedName("pagination") val pagination: Pagination?
)

data class Pagination(
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_visible_page") val lastVisiblePage: Int
)

data class Anime(
    @SerializedName("mal_id")  val malId: Int,
    @SerializedName("title")   val title: String,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("images")  val images: AnimeImages,
    @SerializedName("score")   val score: Double?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("rank")    val rank: Int?
) : Serializable

data class AnimeImages(
    @SerializedName("jpg") val jpg: JpgImages
) : Serializable

data class JpgImages(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("large_image_url") val largeImageUrl: String
) : Serializable

// ─── Reviews ───────────────────────────────────────────────────
data class ReviewResponse(
    @SerializedName("data") val data: List<Review>
)

data class Review(
    @SerializedName("mal_id")       val malId: Int?,
    @SerializedName("review")       val review: String?,
    @SerializedName("score")        val score: Int?,
    @SerializedName("date")         val date: String?,
    @SerializedName("is_spoiler")   val isSpoiler: Boolean?,
    @SerializedName("is_preliminary") val isPreliminary: Boolean?,
    @SerializedName("user")         val user: ReviewUser?
)

data class ReviewUser(
    @SerializedName("username") val username: String?
)

// ─── Recommendations ───────────────────────────────────────────
data class RecommendationResponse(
    @SerializedName("data") val data: List<RecommendationWrapper>
)

data class RecommendationWrapper(
    @SerializedName("entry") val entry: RecommendationEntry?,
    @SerializedName("votes") val votes: Int
)

data class RecommendationEntry(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("title")  val title: String,
    @SerializedName("images") val images: AnimeImages
)
