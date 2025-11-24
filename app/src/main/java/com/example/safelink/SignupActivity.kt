package com.example.safelink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.safelink.api.RetrofitClient
import com.example.safelink.databinding.ActivitySignupBinding
import com.example.safelink.models.AuthRequest
import com.example.safelink.models.ErrorResponse
import com.example.safelink.models.SignupRequest
import com.example.safelink.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val apiService = RetrofitClient.apiService
    private lateinit var sharedPref: SharedPreferencesHelper
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPreferencesHelper(this)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val termsAccepted = binding.termsCheckbox.isChecked

            if (validateInputs(fullName, email, password, termsAccepted)) {
                performSignup(fullName, email, password)
            }
        }

        binding.loginLinkText.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        termsAccepted: Boolean
    ): Boolean {
        var isValid = true

        if (fullName.isEmpty()) {
            binding.fullNameEditText.error = "Le nom complet est requis"
            isValid = false
        } else {
            binding.fullNameEditText.error = null
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = "L'email est requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Format d'email invalide"
            isValid = false
        } else {
            binding.emailEditText.error = null
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordEditText.error = "Le mot de passe doit contenir au moins 6 caractères"
            isValid = false
        } else {
            binding.passwordEditText.error = null
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Veuillez accepter les conditions d'utilisation", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun performSignup(fullName: String, email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val signupRequest = SignupRequest(fullName, email, password)
                val response = apiService.signup(signupRequest)

                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    if (signupResponse != null) {
                        // Supprimer la vérification de 'success' si elle n'existe pas
                        handleSignupSuccess(signupResponse, email, password)
                    } else {
                        handleSignupError("Réponse vide du serveur")
                    }
                } else {
                    handleHttpError(response)
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleSignupSuccess(
        signupResponse: com.example.safelink.models.SignupResponse,
        email: String,
        password: String
    ) {
        Log.d("SIGNUP_SUCCESS", "✅ Compte créé: ${signupResponse.message}")
        showSuccessMessage("Compte créé avec succès! Connexion automatique...")
        performLoginAfterSignup(email, password)
    }

    private fun performLoginAfterSignup(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val authRequest = AuthRequest(email, password)
                val response = apiService.login(authRequest)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Supprimer la vérification de 'success' si elle n'existe pas
                        authResponse.token?.let { sharedPref.saveAuthToken(it) }
                        authResponse.user?.let { user ->
                            sharedPref.saveUserInfo(user.id, user.name)
                        }
                        showSuccessMessage("Bienvenue!")
                        navigateToMainActivity()
                    } else {
                        navigateToLogin()
                    }
                } else {
                    navigateToLogin()
                }
            } catch (e: Exception) {
                navigateToLogin()
            }
        }
    }

    private fun handleHttpError(response: retrofit2.Response<*>) {
        try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

            val errorMessage = when {
                !errorResponse.message.isNullOrEmpty() -> errorResponse.message
                !errorResponse.error.isNullOrEmpty() -> errorResponse.error
                else -> "Erreur ${response.code()}: ${response.message()}"
            }

            showErrorMessage(errorMessage)
            Log.e("SignupActivity", "HTTP Error ${response.code()}: $errorMessage")

        } catch (e: Exception) {
            val fallbackMessage = when (response.code()) {
                400 -> "Données d'inscription invalides"
                409 -> "Un compte avec cet email existe déjà"
                500 -> "Erreur interne du serveur"
                else -> "Erreur ${response.code()}: ${response.message()}"
            }
            showErrorMessage(fallbackMessage)
        }
    }

    private fun handleException(e: Exception) {
        val errorMessage = when (e) {
            is IOException -> "Erreur de connexion réseau"
            is HttpException -> "Erreur de communication avec le serveur"
            is JsonSyntaxException -> "Erreur de format de données"
            else -> "Erreur inattendue: ${e.message ?: "Veuillez réessayer"}"
        }

        showErrorMessage(errorMessage)
        Log.e("SignupActivity", "Error: ${e.message}")
    }

    private fun handleSignupError(message: String) {
        showErrorMessage(message)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.signupButton.isEnabled = !isLoading
        binding.signupButton.text = if (isLoading) "Création..." else "Créer un compte"
        // Si vous avez une ProgressBar, décommentez la ligne suivante
        // binding.progressBar.isVisible = isLoading
    }

    private fun showSuccessMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showErrorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Échec: $message", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}