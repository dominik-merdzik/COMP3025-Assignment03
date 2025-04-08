package com.example.comp3025_assignment_03.view.details

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.comp3025_assignment_03.databinding.ActivityMovieDetailBinding
import com.example.comp3025_assignment_03.model.FavoriteMovie
import com.example.comp3025_assignment_03.model.MovieDetail
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var movieViewModel: MovieViewModel
    private var currentMovieDetail: MovieDetail? = null // stores the fetched omdb movie details
    private var currentImdbID: String? = null // stores the imdbID passed to this activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // retrieve the imdbID from the intent and fetch movie details if available
        currentImdbID = intent.getStringExtra("imdbID")
        currentImdbID?.let { imdbID ->
            movieViewModel.fetchMovieDetails(imdbID)
        }

        observeViewModel() // set up observers for live data changes

        // set up the back button to close this activity and return to the search screen
        binding.btnBack.setOnClickListener {
            finish()
        }

        // set up listener for adding the current movie to favorites
        binding.btnAddToFavorites.setOnClickListener {
            addCurrentMovieToFavorites()
        }
    }

    private fun observeViewModel() {
        // observe movie detail live data to update the ui when data is received
        movieViewModel.movieDetail.observe(this) { movie ->
            currentMovieDetail = movie // update current movie details
            if (movie != null) {
                // update ui with movie properties
                binding.tvTitle.text = movie.title
                binding.tvYear.text = "Year: ${movie.year}"
                binding.tvRated.text = "Rated: ${movie.rated}"
                binding.tvReleased.text = "Released: ${movie.released}"
                binding.tvRuntime.text = "Runtime: ${movie.runtime}"
                binding.tvGenre.text = "Genre: ${movie.genre}"
                binding.tvDirector.text = "Director: ${movie.director}"
                binding.tvWriter.text = "Writer: ${movie.writer}"
                binding.tvActors.text = "Actors: ${movie.actors}"
                binding.tvPlot.text = movie.plot
                // load poster image with glide using placeholders for loading and errors
                Glide.with(this)
                    .load(movie.posterUrl)
                    .placeholder(com.example.comp3025_assignment_03.R.drawable.ic_launcher_background)
                    .error(com.example.comp3025_assignment_03.R.drawable.ic_launcher_foreground)
                    .into(binding.ivPoster)

                // enable add-to-favorites button now that movie details are loaded
                binding.btnAddToFavorites.isEnabled = true
            } else {
                // disable add-to-favorites button if movie details are unavailable
                binding.btnAddToFavorites.isEnabled = false
            }
        }
        // observe any errors during favorite saving and inform the user
        movieViewModel.saveFavoriteError.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "error saving: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addCurrentMovieToFavorites() {
        // check that movie details and imdbID are loaded before proceeding
        if (currentMovieDetail == null || currentImdbID == null) {
            Toast.makeText(this, "movie details not loaded yet.", Toast.LENGTH_SHORT).show()
            return
        }

        // map movie details from omdb to favorite movie model for firestore
        val favoriteMovie = FavoriteMovie(
            // documentId will be set automatically by firestore
            // userId will be determined by the repository
            title = currentMovieDetail!!.title,
            year = currentMovieDetail!!.year,
            imdbID = currentImdbID,
            posterUrl = currentMovieDetail!!.posterUrl,
            plot = currentMovieDetail!!.plot,
            criticsRating = currentMovieDetail!!.imdbRating,
        )

        // add favorite movie by calling view model method
        movieViewModel.addFavorite(favoriteMovie)

        // notify the user and update the favorites button to prevent duplicate entries
        Toast.makeText(this, "${favoriteMovie.title} added to favorites!", Toast.LENGTH_SHORT).show()
        binding.btnAddToFavorites.isEnabled = false
        binding.btnAddToFavorites.text = "Added!"
    }
}
