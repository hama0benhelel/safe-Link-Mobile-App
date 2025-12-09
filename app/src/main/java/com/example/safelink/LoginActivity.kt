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
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val apiService = RetrofitClient.apiService
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifier si l'utilisateur est déjà connecté
        if (SafeLinkApplication.sessionManager.isLoggedIn()) {
            navigateToMainActivity()
            return
        }

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
        // Sauvegarder avec SessionManager
        SafeLinkApplication.sessionManager.saveAuthToken(authResponse.token)
        SafeLinkApplication.sessionManager.saveUserData(
            userId = authResponse.user.id,
            name = authResponse.user.name,
            email = authResponse.user.email
        )

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
        binding.progressBar.isVisible = isLoading

        binding.loginButton.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.signUpText.isEnabled = !isLoading

        binding.loginButton.text = if (isLoading) "Connexion..." else "Se connecter"
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

    private fun showForgotPasswordDialog() {
        Toast.makeText(this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToMainActivity() {
        try {
            Log.d("LOGIN_NAV", "🚀 Début navigation vers MainActivity")

            // Vérifier que les données sont bien sauvegardées
            val token = SafeLinkApplication.sessionManager.getToken()
            val userId = SafeLinkApplication.sessionManager.getUserId()
            val userName = SafeLinkApplication.sessionManager.getUserName()
            val userEmail = SafeLinkApplication.sessionManager.getUserEmail()

            Log.d("LOGIN_NAV", "📦 Token: ${token?.take(20)}...")
            Log.d("LOGIN_NAV", "📦 UserId: $userId")
            Log.d("LOGIN_NAV", "📦 UserName: $userName")
            Log.d("LOGIN_NAV", "📦 UserEmail: $userEmail")

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            Log.d("LOGIN_NAV", "✅ Intent créé, démarrage...")
            startActivity(intent)

            Log.d("LOGIN_NAV", "✅ startActivity appelé")
            finish()

            Log.d("LOGIN_NAV", "✅ finish() appelé")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e("LOGIN_NAV", "💥 ERREUR: ${e.message}", e)
            Toast.makeText(this, "Erreur navigation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "LoginActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFECYCLE", "LoginActivity paused")
    }
}