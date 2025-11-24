package com.example.safelink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.safelink.databinding.ActivityMainBinding
import com.example.safelink.utils.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPreferencesHelper(this)

        // Vérifier si l'utilisateur est connecté
        if (!sharedPref.isLoggedIn()) {
            Log.w("MainActivity", "⚠️ Utilisateur non connecté, redirection vers Login")
            navigateToLogin()
            return
        }

        Log.d("MainActivity", "✅ MainActivity démarrée - Utilisateur connecté")
        setupUI()
        setupClickListeners()
        logUserInfo()
    }

    private fun setupUI() {
        val (userId, userName) = sharedPref.getUserInfo()

        // Mettre à jour l'interface utilisateur
        binding.welcomeText.text = "Bienvenue, ${userName ?: "Utilisateur"}!"
        binding.userIdText.text = "ID: ${userId ?: "N/A"}"

        // Afficher les premières lettres du nom pour un avatar
        val userInitials = getUserInitials(userName)
        binding.userInitialsText.text = userInitials
    }

    private fun getUserInitials(userName: String?): String {
        return if (!userName.isNullOrEmpty()) {
            userName.split(" ")
                .take(2)
                .joinToString("") { it.firstOrNull()?.toString() ?: "" }
                .uppercase()
        } else {
            "U"
        }
    }

    private fun setupClickListeners() {
//        binding.logoutButton.setOnClickListener {
//            performLogout()
//        }

        binding.profileButton.setOnClickListener {
            showProfile()
        }

//        binding.settingsButton.setOnClickListener {
//            showSettings()
//        }

        binding.refreshButton.setOnClickListener {
            refreshUserData()
        }
    }

    private fun performLogout() {
        Log.d("MainActivity", "🚪 Tentative de déconnexion")

        // Afficher une confirmation avant de déconnecter
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui") { dialog, which ->
                sharedPref.clearAuthData()
                Log.d("MainActivity", "✅ Utilisateur déconnecté")
                Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showProfile() {
        Log.d("MainActivity", "👤 Affichage du profil")

        val (userId, userName) = sharedPref.getUserInfo()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profil Utilisateur")
            .setMessage(
                """
                📋 Informations du compte:

                👤 Nom: ${userName ?: "Non disponible"}
                🆔 ID: ${userId ?: "Non disponible"}
                🔐 Statut: Connecté

                Cette fonctionnalité sera bientôt disponible!
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSettings() {
        Log.d("MainActivity", "⚙️ Affichage des paramètres")
        Toast.makeText(this, "Paramètres - Fonctionnalité à venir", Toast.LENGTH_SHORT).show()

        // TODO: Implémenter l'écran des paramètres
        // val intent = Intent(this, SettingsActivity::class.java)
        // startActivity(intent)
    }

    private fun refreshUserData() {
        Log.d("MainActivity", "🔄 Actualisation des données")

        // Simuler un chargement
        binding.refreshButton.isEnabled = false
        binding.progressBar.isVisible = true

        // Réactualiser l'interface
        setupUI()

        // Simuler un délai de chargement
        binding.root.postDelayed({
            binding.refreshButton.isEnabled = true
            binding.progressBar.isVisible = false
            Toast.makeText(this, "Données actualisées", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    private fun logUserInfo() {
        val (userId, userName) = sharedPref.getUserInfo()
        Log.d("MainActivity", "📊 Informations utilisateur:")
        Log.d("MainActivity", "   👤 Nom: $userName")
        Log.d("MainActivity", "   🆔 ID: $userId")
        Log.d("MainActivity", "   🔐 Token présent: ${!sharedPref.getAuthToken().isNullOrEmpty()}")
    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "🔀 Navigation vers LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Demander confirmation pour quitter l'application
        AlertDialog.Builder(this)
            .setTitle("Quitter l'application")
            .setMessage("Voulez-vous vraiment quitter l'application ?")
            .setPositiveButton("Oui") { dialog, which ->
                finishAffinity() // Ferme toute l'application
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "🔄 MainActivity reprise")

        // Vérifier à nouveau la connexion au cas où
        if (!sharedPref.isLoggedIn()) {
            navigateToLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "🔚 MainActivity détruite")
    }
}