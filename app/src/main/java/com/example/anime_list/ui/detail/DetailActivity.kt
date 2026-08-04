package com.example.anime_list.ui.detail

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.anime_list.data.model.Anime
import com.example.anime_list.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val anime = intent.getSerializableExtra("ANIME") as? Anime
        anime?.let { displayAnimeDetails(it) }
    }

    private fun displayAnimeDetails(anime: Anime) {
        binding.tvTitle.text = anime.title
        binding.tvSynopsis.text = anime.synopsis ?: "No synopsis available."
        binding.tvScore.text = "Score: ${anime.score ?: "N/A"}"
        binding.tvEpisodes.text = "Episodes: ${anime.episodes ?: "N/A"}"

        Glide.with(this)
            .load(anime.images.jpg.largeImageUrl)
            .into(binding.ivBackdrop)

        binding.btnPlay.setOnClickListener {
            Toast.makeText(this, "Playing ${anime.title}...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
