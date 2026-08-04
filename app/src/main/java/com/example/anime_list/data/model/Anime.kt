package com.example.anime_list.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnimeResponse(
    @SerializedName("data")
    val data: List<Anime>
)

data class Anime(
    @SerializedName("mal_id")
    val malId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("synopsis")
    val synopsis: String?,
    
    @SerializedName("images")
    val images: AnimeImages,
    
    @SerializedName("score")
    val score: Double?,
    
    @SerializedName("episodes")
    val episodes: Int?
) : Serializable

data class AnimeImages(
    @SerializedName("jpg")
    val jpg: JpgImages
) : Serializable

data class JpgImages(
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("large_image_url")
    val largeImageUrl: String
) : Serializable
