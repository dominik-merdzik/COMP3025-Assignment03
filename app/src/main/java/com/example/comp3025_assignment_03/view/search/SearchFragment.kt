package com.example.comp3025_assignment_03.view.search

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comp3025_assignment_03.databinding.FragmentSearchBinding
import com.example.comp3025_assignment_03.view.details.MovieDetailActivity
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var movieAdapter: MovieAdapter // Use the adapter for OMDb results

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // initialize the adapter for search results (MovieAdapter)
        movieAdapter = MovieAdapter { movieSearchResult ->
            // handle click on a search result item -> navigate to MovieDetailActivity
            val intent = Intent(requireActivity(), MovieDetailActivity::class.java)
            intent.putExtra("imdbID", movieSearchResult.imdbID)
            startActivity(intent)
        }

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }
    }

    private fun observeViewModel() {
        // observe the OMDb search results LiveData
        viewModel.searchResults.observe(viewLifecycleOwner) { movies ->
            // submit the list to the adapter
            movieAdapter.submitList(movies)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }
}