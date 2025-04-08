package com.example.comp3025_assignment_03.repository

import android.util.Log
import com.example.comp3025_assignment_03.BuildConfig
import com.example.comp3025_assignment_03.model.MovieDetail
import com.example.comp3025_assignment_03.model.MovieSearchResult
import com.example.comp3025_assignment_03.model.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MovieRepository {

    // initiate baseUrl and key for OMDB
    private val apiKey = BuildConfig.OMDB_API_KEY
    private val baseUrl = "https://www.omdbapi.com/"

    // helper function to safely get a string from JSONObject
    private fun JSONObject.optStringOrNull(key: String): String? {
        // returns null if key doesn't exist or maps to JSONObject.NULL
        return if (has(key) && !isNull(key)) getString(key) else null
    }
    private fun JSONObject.optStringEmpty(key: String): String {
        return if (has(key) && !isNull(key)) getString(key) else ""
    }

    // function to search for movies by query
    suspend fun searchMovies(query: String): SearchResponse? {
        return withContext(Dispatchers.IO) {
            fetchAndParseJson(query = query, imdbID = null) as? SearchResponse
        }
    }

    // function to get movie details by imdbID
    suspend fun getMovieDetails(imdbID: String): MovieDetail? {
        return withContext(Dispatchers.IO) {
            fetchAndParseJson(query = null, imdbID = imdbID) as? MovieDetail
        }
    }

    // function for fetching and parsing data
    private fun fetchAndParseJson(query: String?, imdbID: String?): Any? {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        val jsonResponseString: String?

        try {
            // construct URL based on if its a search or detail request
            val urlString = if (query != null) {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                "$baseUrl?apikey=$apiKey&s=$encodedQuery"
            } else if (imdbID != null) {
                "$baseUrl?apikey=$apiKey&i=$imdbID"
            } else {
                Log.e("MovieRepository", "either query or imdbID must be provided")
                return null
            }

            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000 // 10 seconds timeout
            connection.readTimeout = 10000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))
                jsonResponseString = reader.use { it.readText() } // read entire response
            } else {
                Log.e("MovieRepository", "error Response Code: $responseCode for URL: $urlString")
                return null // return null on non-OK response
            }

        } catch (e: Exception) {
            Log.e("MovieRepository", "error during network operation", e)
            return null // return null on network error
        } finally {
            // close resources
            reader?.close()
            connection?.disconnect()
        }

        // proceed with parsing if the response string is not null (should- never be null)
        if (jsonResponseString != null) {
            try {
                val jsonObject = JSONObject(jsonResponseString)

                // check for OMDB API error response
                if (jsonObject.optString("Response") == "False") {
                    Log.e("MovieRepository", "OMDB API error: ${jsonObject.optString("error")}")
                    return null
                }

                // parse based on whether it was a search or detail request
                return if (query != null) {
                    parseSearchResponse(jsonObject) // search results
                } else {
                    parseMovieDetail(jsonObject) // movie details
                }

            } catch (e: JSONException) {
                Log.e("MovieRepository", "error parsing JSON response", e)
                return null // return null on JSON parsing error
            }
        } else {
            return null // return null if jsonResponseString is null
        }
    }

    // manual parser for SearchResponse
    private fun parseSearchResponse(jsonObject: JSONObject): SearchResponse? {
        try {
            val searchResults = mutableListOf<MovieSearchResult>()
            val searchArray: JSONArray? = jsonObject.optJSONArray("Search")

            searchArray?.let { // only proceed if searchArray exists
                for (i in 0 until it.length()) {
                    val movieJson = it.getJSONObject(i)
                    val movie = MovieSearchResult(
                        title = movieJson.optStringEmpty("Title"),
                        year = movieJson.optStringEmpty("Year"),
                        imdbID = movieJson.optStringEmpty("imdbID"),
                        type = movieJson.optStringEmpty("Type"),
                        posterUrl = movieJson.optStringEmpty("Poster")
                    )
                    searchResults.add(movie)
                }
            }

            return SearchResponse(
                search = searchResults.takeIf { it.isNotEmpty() }, // return null if list is empty
                totalResults = jsonObject.optStringOrNull("totalResults"),
                response = jsonObject.optStringOrNull("Response"), // hoping for "true" here
                error = null // error should be null if Response was "true"
            ) //

        } catch (e: JSONException) {
            Log.e("MovieRepository", "error parsing SearchResponse JSON", e)
            return null
        }
    }

    // parser for MovieDetail
    private fun parseMovieDetail(jsonObject: JSONObject): MovieDetail? {
        try {
            return MovieDetail(
                title = jsonObject.optStringEmpty("Title"),
                year = jsonObject.optStringEmpty("Year"),
                rated = jsonObject.optStringEmpty("Rated"),
                released = jsonObject.optStringEmpty("Released"),
                runtime = jsonObject.optStringEmpty("Runtime"),
                genre = jsonObject.optStringEmpty("Genre"),
                director = jsonObject.optStringEmpty("Director"),
                writer = jsonObject.optStringEmpty("Writer"),
                actors = jsonObject.optStringEmpty("Actors"),
                plot = jsonObject.optStringEmpty("Plot"),
                language = jsonObject.optStringEmpty("Language"),
                country = jsonObject.optStringEmpty("Country"),
                awards = jsonObject.optStringEmpty("Awards"),
                posterUrl = jsonObject.optStringEmpty("Poster"),
                imdbRating = jsonObject.optStringEmpty("imdbRating")
            )
        } catch (e: JSONException) {
            Log.e("MovieRepository", "error parsing MovieDetail JSON", e)
            return null
        }
    }
}