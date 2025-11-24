package com.example.safelink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.safelink.api.RetrofitClient
import com.example.safelink.databinding.ActivityLoginBinding
import com.example.safelink.models.AuthRequest
import com.example.safelink.models.ErrorResponse
import com.example.safelink.models.SignupRequest
import com.example.safelink.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val apiService = RetrofitClient.apiService
    private lateinit var sharedPref: SharedPreferencesHelper
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPreferencesHelper(this)

        // Afficher l'URL utilisée
        Log.d("CONFIG", "🎯 URL du serveur: ${RetrofitClient.getBaseUrl()}")

        setupClickListeners()
    }



    private fun setupClickListeners() {
        // Bouton de connexion principal
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Bouton création compte
        binding.signUpText.setOnClickListener {
            navigateToSignUp()
        }

        // Bouton mot de passe oublié
        binding.forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Réinitialiser les erreurs
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "L'email est requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Format d'email invalide"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Le mot de passe doit contenir au moins 6 caractères"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("LOGIN_ATTEMPT", "🔐 Tentative de connexion: $email")

                val authRequest = AuthRequest(email, password)
                val response = apiService.login(authRequest)

                Log.d("LOGIN_RESPONSE", "📡 Réponse: ${response.code()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        handleLoginSuccess(authResponse)
                    } else {
                        handleLoginError("Réponse vide du serveur")
                    }
                } else {
                    handleHttpError(response)
                }
            } catch (e: Exception) {
                Log.e("LOGIN_ERROR", "💥 Erreur: ${e.javaClass.simpleName} - ${e.message}")
                handleException(e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleLoginSuccess(authResponse: com.example.safelink.models.AuthResponse) {
        sharedPref.saveAuthToken(authResponse.token)
        sharedPref.saveUserInfo(authResponse.user.id, authResponse.user.name)

        Log.d("LOGIN_SUCCESS", "✅ Utilisateur connecté: ${authResponse.user.name}")
        showSuccessMessage("Connexion réussie! Bienvenue ${authResponse.user.name}")
        navigateToMainActivity()
    }

    private fun handleHttpError(response: retrofit2.Response<*>) {
        try {
            val errorBody = response.errorBody()?.string()
            Log.d("HTTP_ERROR", "Body erreur: $errorBody")

            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

            val errorMessage = when {
                !errorResponse.message.isNullOrEmpty() -> errorResponse.message
                !errorResponse.error.isNullOrEmpty() -> errorResponse.error
                else -> "Erreur ${response.code()}: ${response.message()}"
            }

            showErrorMessage(errorMessage)

        } catch (e: Exception) {
            val fallbackMessage = when (response.code()) {
                400 -> "Requête invalide - Vérifiez vos données"
                401 -> "Email ou mot de passe incorrect"
                403 -> "Accès refusé au serveur"
                404 -> "Service non trouvé"
                409 -> "Conflit de données"
                500 -> "Erreur interne du serveur"
                502 -> "Bad Gateway"
                503 -> "Service indisponible"
                else -> "Erreur ${response.code()}: ${response.message()}"
            }
            showErrorMessage(fallbackMessage)
        }
    }

    private fun handleException(e: Exception) {
        val errorMessage = when (e) {
            is IOException -> {
                Log.e("NETWORK_ERROR", "Problème réseau")
                "Erreur de connexion réseau. Vérifiez votre internet."
            }
            is HttpException -> {
                Log.e("HTTP_ERROR", "Erreur HTTP")
                "Erreur de communication avec le serveur"
            }
            is JsonSyntaxException -> {
                Log.e("JSON_ERROR", "Erreur parsing JSON")
                "Erreur de format de données reçues"
            }
            is java.net.SocketTimeoutException -> {
                Log.e("TIMEOUT_ERROR", "Timeout connexion")
                "Timeout - Le serveur met trop de temps à répondre"
            }
            is java.net.UnknownHostException -> {
                Log.e("HOST_ERROR", "Host inconnu")
                "Serveur inaccessible - Vérifiez l'URL"
            }
            else -> {
                Log.e("UNKNOWN_ERROR", "Erreur inconnue: ${e.message}")
                "Erreur inattendue: ${e.message ?: "Veuillez réessayer"}"
            }
        }
        showErrorMessage(errorMessage)
    }

    private fun handleLoginError(message: String) {
        showErrorMessage(message)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.text = if (isLoading) "Connexion..." else "Se connecter"
        binding.progressBar.isVisible = isLoading
        binding.signUpText.isEnabled = !isLoading
        binding.forgotPasswordText.isEnabled = !isLoading

        // Désactiver les champs pendant le chargement
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
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

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showForgotPasswordDialog() {
        Toast.makeText(this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        // Optionnel: animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Fermer LoginActivity pour empêcher le retour
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // Méthode pour tester la création d'utilisateur (optionnel)
    private fun createTestUser() {
        lifecycleScope.launch {
            Log.d("USER_TEST", "👤 Création utilisateur test...")

            val testUser = SignupRequest(
                name = "Test User",
                email = "test@test.com",
                password = "test123"
            )

            try {
                val response = apiService.signup(testUser)

                when {
                    response.isSuccessful -> {
                        Log.d("USER_TEST", "✅ Utilisateur créé: ${response.body()?.user_id}")
                        showToast("✅ Utilisateur test créé!")
                    }
                    response.code() == 409 -> {
                        Log.d("USER_TEST", "ℹ️ Utilisateur existe déjà")
                        showToast("ℹ️ Utilisateur test existe déjà")
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e("USER_TEST", "❌ Erreur création: ${response.code()} - $errorBody")
                        showToast("❌ Erreur création utilisateur")
                    }
                }
            } catch (e: Exception) {
                Log.e("USER_TEST", "💥 Erreur: ${e.message}")
                showToast("❌ Erreur création: ${e.message}")
            }
        }
    }

    // Gestion du cycle de vie
    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "LoginActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFECYCLE", "LoginActivity paused")
    }
}