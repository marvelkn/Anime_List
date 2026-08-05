package com.example.anime_list.ui.detail

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anime_list.data.model.Anime
import com.example.anime_list.data.model.RecommendationWrapper
import com.example.anime_list.databinding.ItemAnimeBinding

class RecommendationAdapter(
    private val recommendations: List<RecommendationWrapper>,
    private val onItemClick: (Anime) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAnimeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // Make the cards slightly smaller for the horizontal scroller
        val layoutParams = binding.root.layoutParams
        layoutParams.width = 300
        binding.root.layoutParams = layoutParams
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recommendations[position]
        val entry = item.entry ?: return
        
        holder.binding.tvAnimeTitle.text = entry.title
        Glide.with(holder.binding.root)
            .load(entry.images.jpg.largeImageUrl)
            .into(holder.binding.ivAnimeCover)

        holder.binding.tvAnimeScore.text = "👍 ${item.votes} votes"

        holder.binding.root.setOnClickListener {
            // We map the RecommendationEntry to a basic Anime object to open DetailActivity
            val simpleAnime = Anime(
                malId = entry.malId,
                title = entry.title,
                images = entry.images,
                synopsis = "No synopsis available for recommendations yet. Open in full to see details.",
                score = 0.0,
                episodes = 0,
                rank = 0
            )
            onItemClick(simpleAnime)
        }
    }

    override fun getItemCount(): Int = recommendations.size
}
