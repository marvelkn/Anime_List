package com.example.anime_list.data.remote

import com.example.anime_list.data.model.AnimeResponse
import com.example.anime_list.data.model.RecommendationResponse
import com.example.anime_list.data.model.ReviewResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1
    ): Response<AnimeResponse>

    @GET("anime/{id}/reviews")
    suspend fun getAnimeReviews(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("preliminary") preliminary: Boolean = false
    ): Response<ReviewResponse>

    @GET("anime/{id}/recommendations")
    suspend fun getAnimeRecommendations(
        @Path("id") id: Int
    ): Response<RecommendationResponse>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("order_by") orderBy: String = "score",
        @Query("sort") sort: String = "desc",
        @Query("limit") limit: Int = 25,
        @Query("genres") genres: String? = null
    ): Response<AnimeResponse>
}

