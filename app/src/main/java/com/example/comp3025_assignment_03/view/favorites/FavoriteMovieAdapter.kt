package com.example.comp3025_assignment_03.view.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.comp3025_assignment_03.R
import com.example.comp3025_assignment_03.databinding.ItemFavoriteMovieBinding
import com.example.comp3025_assignment_03.model.FavoriteMovie

// simplified listener
typealias OnFavoriteItemClickListener = (FavoriteMovie) -> Unit

class FavoriteMovieAdapter(private val onItemClicked: OnFavoriteItemClickListener) :
    ListAdapter<FavoriteMovie, FavoriteMovieAdapter.FavoriteViewHolder>(DiffCallback()) {

    inner class FavoriteViewHolder(private val binding: ItemFavoriteMovieBinding) :
        RecyclerView.ViewHolder(binding.root) { // item view is a card view

        // store the current movie bound to this holder
        private var currentMovie: FavoriteMovie? = null

        init {
            // set the click listener on the root view to pass the current movie when clicked
            itemView.setOnClickListener {
                currentMovie?.let { movie ->
                    onItemClicked(movie) // invoke the click listener lambda
                }
            }
        }

        fun bind(movie: FavoriteMovie) {
            currentMovie = movie // keep a reference for the click listener

            binding.tvFavoriteTitle.text = movie.title ?: "no title"
            binding.tvFavoriteStudio.text = "studio: ${movie.studio ?: "n/a"}"
            binding.tvFavoriteRating.text = "rating: ${movie.criticsRating ?: "n/a"}"
            Glide.with(binding.ivFavoritePoster.context)
                .load(movie.posterUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.ivFavoritePoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // diff util callback for efficiently managing updates to the list
    class DiffCallback : DiffUtil.ItemCallback<FavoriteMovie>() {
        override fun areItemsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem.documentId == newItem.documentId
        }
        override fun areContentsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem == newItem
        }
    }
}
