package com.example.anime_list.data.remote

import com.example.anime_list.data.model.AnimeResponse
import retrofit2.Response
import retrofit2.http.GET

interface JikanApiService {
    @GET("top/anime")
    suspend fun getTopAnime(): Response<AnimeResponse>
}
