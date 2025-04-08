package com.example.comp3025_assignment_03.model

import com.google.firebase.firestore.DocumentId // mapping the document ID

// data class for storing favorite movie details in Firestore
data class FavoriteMovie(

    // @DocumentId to automatically map the Firestore document ID to this field
    @DocumentId var documentId: String? = null,

    var userId: String? = null,
    var title: String? = null,
    var year: String? = null,
    var imdbID: String? = null,
    var posterUrl: String? = null,
    var studio: String? = null,
    var criticsRating: String? = null,
    var plot: String? = null

) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}