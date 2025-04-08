package com.example.comp3025_assignment_03.view.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.comp3025_assignment_03.R
import com.example.comp3025_assignment_03.databinding.ActivityMovieSearchBinding
import com.example.comp3025_assignment_03.view.auth.LoginActivity
import com.example.comp3025_assignment_03.view.favorites.FavoritesFragment
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MovieSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieSearchBinding
    private lateinit var auth: FirebaseAuth
    // get viewmodel scoped to this activity; fragments will share this instance
    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // setup bottom navigation listener for switching between fragments
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_search -> {
                    selectedFragment = SearchFragment()
                    // keep search bar visible in search mode
                    binding.searchBarLayout.visibility = View.VISIBLE
                }
                R.id.navigation_favorites -> {
                    selectedFragment = FavoritesFragment()
                    // hide search bar when viewing favorites
                    binding.searchBarLayout.visibility = View.GONE
                }
                R.id.navigation_logout -> {
                    handleLogout()
                    return@setOnItemSelectedListener false // don't mark logout as selected
                }
            }

            selectedFragment?.let { fragment ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_container, fragment)
                    .commit()
            }
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_search
            // will trigger the listener above to load SearchFragment
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                // trigger the search in the shared ViewModel
                viewModel.searchMovies(query)
            }
            binding.etSearch.clearFocus()
        }

    }

    private fun handleLogout() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}