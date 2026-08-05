package com.example.anime_list.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anime_list.data.model.Anime
import com.example.anime_list.databinding.ItemAnimeBinding

class AnimeAdapter(
    private var animeList: List<Anime>,
    private val onItemClick: (Anime) -> Unit,
    private val onMoreClick: (Anime, View) -> Unit,
    private val onItemLongClick: (Anime, View) -> Unit
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    inner class AnimeViewHolder(val binding: ItemAnimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: Anime) {
            binding.tvAnimeTitle.text = anime.title

            val score = anime.score
            if (score != null) {
                binding.tvAnimeScore.text = "⭐ $score"
                binding.tvAnimeScore.visibility = View.VISIBLE
            } else {
                binding.tvAnimeScore.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(anime.images.jpg.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivAnimeCover)

            binding.root.setOnClickListener { onItemClick(anime) }
            binding.root.setOnLongClickListener {
                onItemLongClick(anime, binding.root)
                true
            }
            binding.ivMore.setOnClickListener { onMoreClick(anime, binding.ivMore) }
        }
    }

    fun updateList(newList: List<Anime>) {
        animeList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount(): Int = animeList.size
}
