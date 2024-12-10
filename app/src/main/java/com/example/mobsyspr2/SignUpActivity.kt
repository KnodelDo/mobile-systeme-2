package com.example.mobsyspr2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobsyspr2.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (checkAllField()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(baseContext, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                        } else {
                            Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "This is required field"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Check email format"
            return false
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.textInputLayoutPassword.error = "This is required field"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        if (binding.etPassword.text.toString().length < 6) {
            binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        if (binding.etConfirmPassword.text.toString().isEmpty()) {
            binding.textInputLayoutConfirmPassword.error = "This is required field"
            binding.textInputLayoutConfirmPassword.errorIconDrawable = null
            return false
        }
        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            binding.textInputLayoutPassword.error = "Password do not match"
            return false
        }
        return true
    }
}