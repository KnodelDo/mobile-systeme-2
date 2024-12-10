package com.example.mobsyspr2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobsyspr2.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = Firebase.auth

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (checkAllField()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                val uid = currentUser.uid
                                if (uid == "jLTcvJk78ch8yEuFOqRdOivpySs1") {
                                    Toast.makeText(this, "Willkommen Admin!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, AdminBoard::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Login fehlgeschlagen: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "E-Mail ist erforderlich"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Ungültiges E-Mail-Format"
            return false
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.textInputLayoutPassword.error = "Passwort ist erforderlich"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        if (binding.etPassword.text.toString().length < 6) {
            binding.textInputLayoutPassword.error = "Passwort muss mindestens 6 Zeichen lang sein"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        return true
    }
}
