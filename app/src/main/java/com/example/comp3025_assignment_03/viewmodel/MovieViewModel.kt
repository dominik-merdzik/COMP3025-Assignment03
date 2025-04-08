package com.example.comp3025_assignment_03.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comp3025_assignment_03.model.MovieDetail
import com.example.comp3025_assignment_03.model.MovieSearchResult
import com.example.comp3025_assignment_03.repository.MovieRepository
import kotlinx.coroutines.launch
import com.example.comp3025_assignment_03.model.FavoriteMovie
import com.example.comp3025_assignment_03.repository.FavoriteMovieRepository

class MovieViewModel : ViewModel() {

    private val repository = MovieRepository()
    // repository for Firestore favorites
    private val favoriteRepository = FavoriteMovieRepository()

    private val _searchResults = MutableLiveData<List<MovieSearchResult>?>()
    val searchResults: MutableLiveData<List<MovieSearchResult>?> get() = _searchResults

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> get() = _movieDetail

    // --- LiveData for favorite movies ---
    private val _favoriteMovies = MutableLiveData<List<FavoriteMovie>>()
    val favoriteMovies: LiveData<List<FavoriteMovie>> get() = _favoriteMovies

    private val _isLoadingFavorites = MutableLiveData<Boolean>()
    val isLoadingFavorites: LiveData<Boolean> get() = _isLoadingFavorites

    val _errorMessageFavorites = MutableLiveData<String?>()
    val errorMessageFavorites: LiveData<String?> get() = _errorMessageFavorites

    // LiveData for single selected/editing Favorite
    private val _selectedFavoriteMovie = MutableLiveData<FavoriteMovie?>()
    val selectedFavoriteMovie: LiveData<FavoriteMovie?> get() = _selectedFavoriteMovie

    // add/edit Specific Loading/Error
    private val _isSavingFavorite = MutableLiveData<Boolean>()

    val _saveFavoriteError = MutableLiveData<String?>()
    val saveFavoriteError: LiveData<String?> get() = _saveFavoriteError


    // search for movies by query string
    fun searchMovies(query: String) {
        viewModelScope.launch {
            repository.searchMovies(query)?.let { response ->
                _searchResults.value = response.search
            }
        }
    }

    // fetch movie details by imdbID
    fun fetchMovieDetails(imdbID: String) {
        viewModelScope.launch {
            repository.getMovieDetails(imdbID)?.let {
                _movieDetail.value = it
            }
        }
    }

    // fetching movie by id from Firestore
    fun fetchFavoriteMovieById(documentId: String) {
        _isLoadingFavorites.value = true // reuse loading state or use a specific one
        _selectedFavoriteMovie.value = null // clear previous
        viewModelScope.launch {
            val result = favoriteRepository.getFavoriteMovieById(documentId)
            _selectedFavoriteMovie.value = result // post result
            _isLoadingFavorites.value = false
            if (result == null) {
                _errorMessageFavorites.value = "could not load movie details for editing"
            }
        }
    }

    // updates a movie in favorites within Firestore
    fun updateFavorite(movie: FavoriteMovie) {
        _isSavingFavorite.value = true
        _saveFavoriteError.value = null
        viewModelScope.launch {
            val success = favoriteRepository.updateFavoriteMovie(movie)
            if (!success) {
                _saveFavoriteError.value = "failed to update favorite"
            } else {
                // Optionally trigger a refresh of the main list after update
                // fetchFavoriteMovies()
                // Or update the item in the existing LiveData list
                val currentList = _favoriteMovies.value?.toMutableList()
                val index = currentList?.indexOfFirst { it.documentId == movie.documentId }
                if (index != null && index != -1) {
                    currentList[index] = movie
                    _favoriteMovies.value = currentList!!
                }
            }
            _isSavingFavorite.value = false
        }
    }

    // adds a movie to favorites in Firestore
    fun addFavorite(movie: FavoriteMovie) {
        _isSavingFavorite.value = true
        _saveFavoriteError.value = null
        viewModelScope.launch {
            val success = favoriteRepository.addFavoriteMovie(movie)
            if (!success) {
                _saveFavoriteError.value = "failed to add favorite"
            }
            _isSavingFavorite.value = false
        }
    }

    fun fetchFavoriteMovies() {
        _isLoadingFavorites.value = true
        viewModelScope.launch {
            val result = favoriteRepository.getFavoriteMoviesOnce()
            if (result != null) {
                _favoriteMovies.value = result!! // post the list
            } else {
                // Handle error case
                _favoriteMovies.value = emptyList()
                _errorMessageFavorites.value = "failed to load favorites"
            }
            _isLoadingFavorites.value = false
        }
    }
    fun deleteFavorite(documentId: String) {
        viewModelScope.launch {
            val success = favoriteRepository.deleteFavoriteMovie(documentId)
            if (success) {
                // remove the movie from the current LiveData list for immediate UI update
                val currentList = _favoriteMovies.value?.toMutableList() ?: mutableListOf()
                currentList.removeAll { it.documentId == documentId }
                _favoriteMovies.value = currentList
            } else {
                _errorMessageFavorites.value = "failed to delete favorite"
            }
        }
    }

}
