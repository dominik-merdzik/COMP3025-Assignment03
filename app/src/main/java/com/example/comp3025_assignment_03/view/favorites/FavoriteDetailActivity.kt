package com.example.comp3025_assignment_03.view.favorites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.comp3025_assignment_03.R
import com.example.comp3025_assignment_03.databinding.ActivityFavoriteDetailBinding
import com.example.comp3025_assignment_03.model.FavoriteMovie
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel

class FavoriteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteDetailBinding
    private lateinit var viewModel: MovieViewModel
    private var currentFavoriteMovie: FavoriteMovie? = null
    private var favoriteMovieId: String? = null

    companion object {
        // use a consistent key for passing the id via intent
        const val EXTRA_FAVORITE_MOVIE_ID = "EXTRA_FAVORITE_MOVIE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // retrieve the favorite movie id from the intent
        favoriteMovieId = intent.getStringExtra(EXTRA_FAVORITE_MOVIE_ID)

        if (favoriteMovieId == null) {
            Toast.makeText(this, "error:movie ID missing", Toast.LENGTH_LONG).show()
            finish() // close activity if the id is missing
            return
        }

        observeViewModel() // set up observers for live data changes
        setupButtonClickListeners() // configure button click listeners

        // fetch the details for this favorite movie
        viewModel.fetchFavoriteMovieById(favoriteMovieId!!)
    }

    // button listners
    private fun setupButtonClickListeners() {
        binding.btnDetailBack.setOnClickListener { finish() }
        binding.btnDetailDelete.setOnClickListener {
            handleDelete()
        }
        binding.btnDetailUpdate.setOnClickListener {
            handleUpdate()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedFavoriteMovie.observe(this) { movie ->
            // ensure the loaded movie corresponds to the requested id
            if (movie != null && movie.documentId == favoriteMovieId) {
                currentFavoriteMovie = movie
                populateUI(movie) // update the ui with movie details
            } else if (movie == null && !viewModel.isLoadingFavorites.value!!) {
                // handle case where the movie wasn't found after loading finishes
                Toast.makeText(this, "could not load movie details", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessageFavorites.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "error: $error", Toast.LENGTH_LONG).show()
            }
        }
        // observe saving or update errors and re-enable buttons if needed
        viewModel.saveFavoriteError.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "update error: $error", Toast.LENGTH_LONG).show()
                setButtonsEnabled(true) // re-enable buttons on error
            }
        }
    }

    private fun populateUI(movie: FavoriteMovie) {
        binding.tvDetailTitle.text = movie.title ?: "N/A"
        binding.tvDetailYear.text = "Year: ${movie.year ?: "N/A"}"
        binding.tvDetailStudio.text = "Studio: ${movie.studio ?: "N/A"}"
        binding.tvDetailRating.text = "Rating: ${movie.criticsRating ?: "N/A"}"
        binding.etDetailDescription.setText(movie.plot ?: "")
        Glide.with(this)
            .load(movie.posterUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(binding.ivDetailPoster)
    }

    private fun handleDelete() {
        if (favoriteMovieId == null) return
        setButtonsEnabled(false) // disable buttons while deleting
        viewModel.deleteFavorite(favoriteMovieId!!)
        Toast.makeText(this, "favorite deleted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleUpdate() {
        if (currentFavoriteMovie == null || favoriteMovieId == null) {
            Toast.makeText(this, "cannot update: movie data not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        setButtonsEnabled(false) // disable buttons while updating

        val updatedDescription = binding.etDetailDescription.text.toString().trim()

        // create a copy of the current movie updating only the description as per assignment requirements
        val movieToUpdate = currentFavoriteMovie!!.copy(
            plot = updatedDescription
        )

        viewModel.updateFavorite(movieToUpdate)
        Toast.makeText(this, "Updating description...", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        binding.btnDetailUpdate.isEnabled = isEnabled
        binding.btnDetailDelete.isEnabled = isEnabled
        binding.btnDetailBack.isEnabled = isEnabled
    }
}
