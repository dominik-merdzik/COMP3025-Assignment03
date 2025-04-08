package com.example.comp3025_assignment_03.view.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.comp3025_assignment_03.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase auth
        auth = Firebase.auth

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }
        binding.btnCancelRegister.setOnClickListener {
            finish() // close this activity and return to login activity
        }
    }

    private fun handleRegistration() {

        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // create user using firebase auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // sign in success: update ui with the signed in user's information
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    // val user = auth.currentUser // you could potentially send a verification email here
                    Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                    // navigate back to login screen as per requirement
                    finish() // close register activity

                } else {
                    // if sign in fails, display a message to the user
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.btnCancelRegister.isEnabled = !isLoading
    }
}
