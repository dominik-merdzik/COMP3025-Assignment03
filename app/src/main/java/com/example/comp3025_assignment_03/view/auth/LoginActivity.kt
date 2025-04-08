package com.example.comp3025_assignment_03.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.comp3025_assignment_03.databinding.ActivityLoginBinding
import com.example.comp3025_assignment_03.view.search.MovieSearchActivity
import com.example.comp3025_assignment_03.viewmodel.MovieViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var movieViewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth = Firebase.auth

        // check the current user
        if (auth.currentUser != null) {
            navigateToMovieList()
            return // skipping login UI if already authenticated
        }

        // initialize ViewModel... ensure ViewModel handles Auth state
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // setup button listeners
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {

        // declaring login fields
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)

        // sign in using Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithEmail:success")
                    navigateToMovieList()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    // if login successful switch activity
    private fun navigateToMovieList() {
        val intent = Intent(this, MovieSearchActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // loading visual
    private fun setLoading(isLoading: Boolean) {
        binding.pbLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoToRegister.isEnabled = !isLoading
    }
}