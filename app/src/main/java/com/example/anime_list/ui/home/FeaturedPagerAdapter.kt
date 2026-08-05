package com.example.anime_list.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anime_list.data.model.Anime
import com.example.anime_list.databinding.ItemFeaturedBinding

class FeaturedPagerAdapter(
    private val featuredAnime: List<Anime>,
    private val onPlayClick: (Anime) -> Unit,
    private val onInfoClick: (Anime) -> Unit
) : RecyclerView.Adapter<FeaturedPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFeaturedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeaturedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anime = featuredAnime[position]
        
        holder.binding.tvFeaturedTitle.text = anime.title
        holder.binding.tvFeaturedScore.text = "⭐ ${anime.score ?: "N/A"}  ·  ${anime.episodes ?: "?"} eps"
        
        Glide.with(holder.binding.root)
            .load(anime.images.jpg.largeImageUrl)
            .into(holder.binding.ivFeatured)
            
        holder.binding.btnFeaturedPlay.setOnClickListener { onPlayClick(anime) }
        holder.binding.btnFeaturedInfo.setOnClickListener { onInfoClick(anime) }
    }

    override fun getItemCount(): Int = featuredAnime.size
}
