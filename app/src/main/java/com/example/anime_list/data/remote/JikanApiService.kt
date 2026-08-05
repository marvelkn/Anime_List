package com.example.anime_list.data.remote

import com.example.anime_list.data.model.AnimeResponse
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
        @Query("page") page: Int = 1
    ): Response<ReviewResponse>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("order_by") orderBy: String = "score",
        @Query("sort") sort: String = "desc"
    ): Response<AnimeResponse>
}
