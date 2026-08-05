package com.example.anime_list.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anime_list.data.model.Review
import com.example.anime_list.databinding.ItemReviewBinding

class ReviewAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            val username = review.user?.username ?: "Anonymous"
            val score = review.score ?: 0
            val reviewText = review.review ?: "No review content."
            
            binding.tvReviewUsername.text = "👤 $username"
            binding.tvReviewScore.text = "⭐ $score/10"
            binding.tvReviewBody.text = reviewText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size
}
