package com.example.anime_list.util

import android.content.Context
import com.example.anime_list.data.model.Anime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MyListManager {
    private const val PREFS_NAME = "anivault_my_list"
    private const val KEY_ANIME_LIST = "anime_list"
    private val gson = Gson()

    fun addAnime(context: Context, anime: Anime) {
        val list = getAll(context).toMutableList()
        if (list.none { it.malId == anime.malId }) {
            list.add(anime)
            save(context, list)
        }
    }

    fun removeAnime(context: Context, animeId: Int) {
        val list = getAll(context).toMutableList()
        list.removeAll { it.malId == animeId }
        save(context, list)
    }

    fun getAll(context: Context): List<Anime> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ANIME_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<Anime>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun isInList(context: Context, animeId: Int): Boolean =
        getAll(context).any { it.malId == animeId }

    private fun save(context: Context, list: List<Anime>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANIME_LIST, gson.toJson(list)).apply()
    }

    fun saveUserRating(context: Context, animeId: Int, rating: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("rating_$animeId", rating).apply()
    }

    fun getUserRating(context: Context, animeId: Int): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat("rating_$animeId", 0f)
    }
}
