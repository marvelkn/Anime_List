package com.example.anime_list.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anime_list.data.model.Anime
import com.example.anime_list.data.remote.ApiClient
import com.example.anime_list.databinding.ActivityDetailBinding
import com.example.anime_list.util.MyListManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var currentAnime: Anime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        @Suppress("DEPRECATION")
        val anime = intent.getSerializableExtra("ANIME") as? Anime
        currentAnime = anime
        anime?.let {
            displayAnimeDetails(it)
            loadReviews(it.malId)
            loadRecommendations(it.malId)
        }
    }

    private fun displayAnimeDetails(anime: Anime) {
        binding.tvTitle.text = anime.title
        binding.tvSynopsis.text = anime.synopsis ?: "No synopsis available."
        binding.tvScore.text = "⭐ Score: ${anime.score ?: "N/A"}"
        binding.tvEpisodes.text = "${anime.episodes ?: "?"} eps"
        binding.tvRankBadge.text = if (anime.rank != null) "RANK #${anime.rank}" else "TOP ANIME"

        Glide.with(this).load(anime.images.jpg.largeImageUrl).into(binding.ivBackdrop)

        // My List button state
        updateMyListButton(anime)

        binding.btnPlay.setOnClickListener {
            Toast.makeText(this, "▶ Playing \"${anime.title}\"...", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddList.setOnClickListener {
            val ctx = this
            if (MyListManager.isInList(ctx, anime.malId)) {
                MyListManager.removeAnime(ctx, anime.malId)
                Toast.makeText(ctx, "Removed from My List", Toast.LENGTH_SHORT).show()
            } else {
                MyListManager.addAnime(ctx, anime)
                Toast.makeText(ctx, "✅ Added to My List", Toast.LENGTH_SHORT).show()
            }
            updateMyListButton(anime)
        }

        // User rating
        val savedRating = MyListManager.getUserRating(this, anime.malId)
        binding.ratingBar.rating = savedRating
        updateRatingLabel(savedRating)

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                MyListManager.saveUserRating(this, anime.malId, rating)
                updateRatingLabel(rating)
            }
        }
    }

    private fun updateMyListButton(anime: Anime) {
        val inList = MyListManager.isInList(this, anime.malId)
        binding.btnAddList.text = if (inList) "✓  In My List" else "＋  My List"
    }

    private fun updateRatingLabel(rating: Float) {
        binding.tvUserRatingLabel.text = if (rating == 0f) "Tap to rate"
        else "Your rating: $rating / 5"
    }

    private fun loadReviews(animeId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            var retryCount = 0
            while (retryCount < 3) {
                try {
                    val response = ApiClient.instance.getAnimeReviews(animeId, preliminary = true)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val reviews = response.body()?.data ?: emptyList()
                            binding.tvReviewsLoading.visibility = View.GONE
                            if (reviews.isNotEmpty()) {
                                binding.rvReviews.visibility = View.VISIBLE
                                binding.rvReviews.layoutManager = LinearLayoutManager(this@DetailActivity)
                                binding.rvReviews.adapter = ReviewAdapter(reviews.take(5))
                            } else {
                                binding.tvReviewsLoading.text = "No reviews yet."
                                binding.tvReviewsLoading.visibility = View.VISIBLE
                            }
                            retryCount = 3 // Success, exit loop
                        } else if (response.code() == 429) {
                            // Rate limited, wait and retry
                            if (retryCount == 2) {
                                binding.tvReviewsLoading.text = "API Limit (Too Many Requests)."
                            } else {
                                binding.tvReviewsLoading.text = "Server busy, retrying..."
                            }
                        } else {
                            binding.tvReviewsLoading.text = "Could not load reviews."
                            retryCount = 3 // Other error, exit loop
                        }
                    }
                    if (response.code() == 429) {
                        retryCount++
                        kotlinx.coroutines.delay(2000)
                    }
                } catch (e: Exception) {
                    Log.e("DetailActivity", "Review load error", e)
                    withContext(Dispatchers.Main) {
                        binding.tvReviewsLoading.text = "Could not load reviews."
                    }
                    break
                }
            }
        }
    }

    private fun loadRecommendations(animeId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Delay slightly so it doesn't hit the API at the exact same millisecond as loadReviews
            kotlinx.coroutines.delay(400)
            
            var retryCount = 0
            while (retryCount < 3) {
                try {
                    val response = ApiClient.instance.getAnimeRecommendations(animeId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val recs = response.body()?.data ?: emptyList()
                            binding.tvRecsLoading.visibility = View.GONE
                            if (recs.isNotEmpty()) {
                                binding.rvRecommendations.visibility = View.VISIBLE
                                binding.rvRecommendations.layoutManager = LinearLayoutManager(
                                    this@DetailActivity, RecyclerView.HORIZONTAL, false
                                )
                                // Take top 10 recommendations
                                binding.rvRecommendations.adapter = RecommendationAdapter(recs.take(10)) { clickedAnime ->
                                    startActivity(Intent(this@DetailActivity, DetailActivity::class.java).apply {
                                        putExtra("ANIME", clickedAnime)
                                    })
                                }
                            } else {
                                binding.tvRecsLoading.text = "No recommendations yet."
                                binding.tvRecsLoading.visibility = View.VISIBLE
                            }
                            retryCount = 3 // Success, exit loop
                        } else if (response.code() == 429) {
                            // Rate limited, wait and retry
                            if (retryCount == 2) {
                                binding.tvRecsLoading.text = "API Limit (Too Many Requests)."
                            } else {
                                binding.tvRecsLoading.text = "Server busy, retrying..."
                            }
                        } else {
                            binding.tvRecsLoading.text = "Could not load recommendations."
                            retryCount = 3 // Other error, exit loop
                        }
                    }
                    if (response.code() == 429) {
                        retryCount++
                        kotlinx.coroutines.delay(2000)
                    }
                } catch (e: Exception) {
                    Log.e("DetailActivity", "Recs load error", e)
                    withContext(Dispatchers.Main) {
                        binding.tvRecsLoading.text = "Could not load recommendations."
                    }
                    break
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
