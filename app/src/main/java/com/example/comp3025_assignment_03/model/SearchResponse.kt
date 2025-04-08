package com.example.comp3025_assignment_03.model

// SearchResponse data class is used to represent the response from the OMDB API when searching for movies
data class SearchResponse(
    val search: List<MovieSearchResult>?,
    val totalResults: String?,
    val response: String?,
    val error: String?
)
