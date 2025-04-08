package com.example.comp3025_assignment_03.repository

import android.util.Log
import com.example.comp3025_assignment_03.model.FavoriteMovie
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FavoriteMovieRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // helper function to get the current user's ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * adds a new favorite movie document to Firestore under the current user...
     * returns true on success, false on failure (user not logged in)
     */
    suspend fun addFavoriteMovie(movie: FavoriteMovie): Boolean {
        val userId = getCurrentUserId() ?: run {
            Log.w("FavoriteMovieRepo", "cannot add favorite: No user logged in")
            return false // ensure user is logged in
        }
        // ensure the movie object has the userId set before saving
        movie.userId = userId

        return try {
            // adding the movie to the users 'favorites' sub-collection
            // Path: ~/users/{userId}/favorites/{newMovieId}
            db.collection("users").document(userId).collection("favorites")
                .add(movie) // Firestore generates the document ID
                .await() // await the operation to complete
            Log.d("FavoriteMovieRepo", "favorite movie added successfully")
            true // indicate success
        } catch (e: Exception) {
            Log.e("FavoriteMovieRepo", "error adding favorite movie", e)
            false // indicate failure
        }
    }

    /**
     * Fetches the current user's favorite movies from Firestore...
     * returns a list of FavoriteMovie objects or null on error/no user
     */
    suspend fun getFavoriteMoviesOnce(): List<FavoriteMovie>? {
        val userId = getCurrentUserId() ?: run {
            Log.w("FavoriteMovieRepo", "cannot get favorites: No user logged in")
            return null // needs user logged in
        }

        return try {
            val snapshot = db.collection("users").document(userId).collection("favorites")
                .get() // get documents once
                .await()

            // convert each document snapshot to a FavoriteMovie object
            val movies = snapshot.documents.mapNotNull { document ->
                try {
                    // Use .toObject<FavoriteMovie>() which leverages the @DocumentId
                    document.toObject<FavoriteMovie>()
                } catch(e: Exception) {
                    Log.e("FavoriteMovieRepo", "error converting document ${document.id}", e)
                    null // skip documents that fail to convert
                }
            }
            Log.d("FavoriteMovieRepo", "fetched ${movies.size} favorite movies.")
            movies
        } catch (e: Exception) {
            Log.e("FavoriteMovieRepo", "error fetching favorite movies", e)
            null
        }
    }

    /**
     * Deletes a specific favorite movie document from Firestore...
     * requires the unique document ID of the movie to delete
     */
    suspend fun deleteFavoriteMovie(documentId: String): Boolean {
        val userId = getCurrentUserId() ?: run {
            Log.w("FavoriteMovieRepo", "cannot delete favorite: no user logged in")
            return false
        }

        if (documentId.isBlank()) {
            Log.w("FavoriteMovieRepo", "cannot delete favorite: documentId is blank")
            return false
        }

        return try {
            db.collection("users").document(userId)
                .collection("favorites").document(documentId)
                .delete()
                .await()
            Log.d("FavoriteMovieRepo", "favorite movie deleted successfully: $documentId")
            true
        } catch (e: Exception) {
            Log.e("FavoriteMovieRepo", "error deleting favorite movie: $documentId", e)
            false
        }
    }

    /**
     * fetching a single favorite movie by its Firestore document ID...
     * returns the FavoriteMovie object
     */
    suspend fun getFavoriteMovieById(documentId: String): FavoriteMovie? {
        val userId = getCurrentUserId() ?: run {
            Log.w("FavoriteMovieRepo", "cannot get favorite by ID: no user logged in")
            return null
        }

        if (documentId.isBlank()) {
            Log.w("FavoriteMovieRepo", "cannot get favorite by ID: documentId is blank")
            return null
        }

        return try {
            val docSnapshot = db.collection("users").document(userId)
                .collection("favorites").document(documentId)
                .get()
                .await()

            docSnapshot.toObject<FavoriteMovie>()
        } catch (e: Exception) {
            Log.e("FavoriteMovieRepo", "Error fetching favorite by ID: $documentId", e)
            null
        }
    }

    /**
     * updating an existing favorite movie document in Firestore...
     * requires the movie object containing the updated data AND its documentId
     */
    suspend fun updateFavoriteMovie(movie: FavoriteMovie): Boolean {
        val userId = getCurrentUserId() ?: run {
            Log.w("FavoriteMovieRepo", "cannot update favorite: no user logged in")
            return false
        }
        val docId = movie.documentId ?: run {
            Log.w("FavoriteMovieRepo", "cannot update favorite: documentId is missing")
            return false
        }
        // ensure the userId field matches the current user
        movie.userId = userId

        return try {
            db.collection("users").document(userId)
                .collection("favorites").document(docId)
                .set(movie) // use set to overwrite the document with new data
                .await()
            Log.d("FavoriteMovieRepo", "Favorite movie updated successfully: $docId")
            true
        } catch (e: Exception) {
            Log.e("FavoriteMovieRepo", "Error updating favorite movie: $docId", e)
            false
        }
    }


}