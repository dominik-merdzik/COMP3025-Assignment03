package com.example.comp3025_assignment_03.view.favorites

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comp3025_assignment_03.databinding.FragmentFavoritesBinding
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    // use activityViewModels to share the view model with the activity and other fragments
    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var favoriteAdapter: FavoriteMovieAdapter // adapter for displaying favorite movies

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView() // setup recycler view for favorites list
        observeViewModel() // set up live data observers for updating UI
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchFavoriteMovies() // fetch updated favorites when fragment resumes
    }

    private fun setupRecyclerView() {
        // initialize adapter with a lambda to handle item clicks
        favoriteAdapter = FavoriteMovieAdapter { clickedMovie ->
            // navigate to detail screen using the clicked movie's document id
            navigateToFavoriteDetail(clickedMovie.documentId)
        }
        binding.rvFavoriteMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }

    private fun observeViewModel() {
        // use viewLifecycleOwner for observing live data in fragments
        val owner: LifecycleOwner = viewLifecycleOwner

        viewModel.favoriteMovies.observe(owner) { movies ->
            favoriteAdapter.submitList(movies) // update adapter's data
            binding.tvErrorFavorites.visibility = View.GONE // hide error message on new data
        }
        viewModel.isLoadingFavorites.observe(owner) { isLoading ->
            binding.pbLoadingFavorites.visibility = if (isLoading) View.VISIBLE else View.GONE
            // hide recycler view when loading to provide clear feedback to the user
            binding.rvFavoriteMovies.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }
        viewModel.errorMessageFavorites.observe(owner) { error ->
            if (error != null && binding.pbLoadingFavorites.visibility == View.GONE) {
                binding.tvErrorFavorites.visibility = View.VISIBLE
                binding.tvErrorFavorites.text = error
                binding.rvFavoriteMovies.visibility = View.INVISIBLE // hide list on error
            } else {
                binding.tvErrorFavorites.visibility = View.GONE
                // ensure recycler view is visible if there's no error and not loading
                if (binding.pbLoadingFavorites.visibility == View.GONE) {
                    binding.rvFavoriteMovies.visibility = View.VISIBLE
                }
            }
        }
    }

    // function to navigate to the favorite detail screen
    private fun navigateToFavoriteDetail(movieId: String?) {
        if (movieId == null) {
            Toast.makeText(requireContext(), "error: cannot view details without id", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(requireActivity(), FavoriteDetailActivity::class.java)
        intent.putExtra(FavoriteDetailActivity.EXTRA_FAVORITE_MOVIE_ID, movieId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // clear binding reference to prevent memory leaks
    }
}
