package com.example.comp3025_assignment_03.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.comp3025_assignment_03.databinding.ItemMovieBinding
import com.example.comp3025_assignment_03.model.MovieSearchResult

// list adapter that observes data changes and updates the recycler view accordingly
class MovieAdapter(private val onItemClick: (MovieSearchResult) -> Unit) :
    ListAdapter<MovieSearchResult, MovieAdapter.MovieViewHolder>(DiffCallback()) {

    // view holder for a movie item in the list
    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieSearchResult) {
            binding.tvTitle.text = movie.title
            binding.tvYear.text = movie.year
            binding.tvType.text = movie.type
            binding.root.setOnClickListener { onItemClick(movie) }
        }
    }

    // create new view holders
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    // replace the contents of a view
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // diff util callback for updating the list
    class DiffCallback : DiffUtil.ItemCallback<MovieSearchResult>() {
        override fun areItemsTheSame(oldItem: MovieSearchResult, newItem: MovieSearchResult): Boolean =
            oldItem.imdbID == newItem.imdbID

        override fun areContentsTheSame(oldItem: MovieSearchResult, newItem: MovieSearchResult): Boolean =
            oldItem == newItem
    }
}
