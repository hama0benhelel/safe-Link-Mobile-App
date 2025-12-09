package com.example.safelink

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.safelink.databinding.ActivityMainBinding
import com.example.safelink.fragments.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null
    private var currentSelectedItemId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "════════════════════════════════")
        Log.d("MainActivity", "🎬 DÉBUT onCreate")
        Log.d("MainActivity", "════════════════════════════════")

        try {
            // Vérifier SessionManager
            Log.d("MainActivity", "1️⃣ Vérification SessionManager...")
            val token = SafeLinkApplication.sessionManager.getToken()
            val userId = SafeLinkApplication.sessionManager.getUserId()
            val userName = SafeLinkApplication.sessionManager.getUserName()
            val userEmail = SafeLinkApplication.sessionManager.getUserEmail()

            Log.d("MainActivity", "   Token: ${token?.take(20) ?: "NULL"}")
            Log.d("MainActivity", "   UserId: ${userId ?: "NULL"}")
            Log.d("MainActivity", "   UserName: ${userName ?: "NULL"}")
            Log.d("MainActivity", "   UserEmail: ${userEmail ?: "NULL"}")

            if (!SafeLinkApplication.sessionManager.isLoggedIn()) {
                Log.d("MainActivity", "❌ Non connecté, redirection")
                navigateToLogin()
                return
            }

            Log.d("MainActivity", "2️⃣ Inflation du layout...")
            binding = ActivityMainBinding.inflate(layoutInflater)

            Log.d("MainActivity", "3️⃣ setContentView...")
            setContentView(binding.root)

            Log.d("MainActivity", "4️⃣ Setup Toolbar...")


            Log.d("MainActivity", "5️⃣ Setup BottomNavigation...")
            setupBottomNavigation()

            Log.d("MainActivity", "6️⃣ Setup FAB...")
            setupFab()

            if (savedInstanceState == null) {
                Log.d("MainActivity", "7️⃣ Chargement HomeFragment...")
                navigateToFragment(HomeFragment())
            }

            Log.d("MainActivity", "✅ onCreate terminé avec succès")

        } catch (e: Exception) {
            Log.e("MainActivity", "💥💥💥 CRASH dans onCreate 💥💥💥")
            Log.e("MainActivity", "Type: ${e.javaClass.simpleName}")
            Log.e("MainActivity", "Message: ${e.message}")
            Log.e("MainActivity", "StackTrace:", e)

            // Essayer de nettoyer et rediriger
            try {
                SafeLinkApplication.sessionManager.clearSession()
                navigateToLogin()
            } catch (e2: Exception) {
                Log.e("MainActivity", "Impossible de rediriger: ${e2.message}")
                finish()
            }
        }
    }



    private fun setupBottomNavigation() {
        try {
            // Set initial state
            setSelectedItem(R.id.nav_home)

            // Set click listeners
            binding.navHome.setOnClickListener { selectItem(R.id.nav_home) }
            binding.navAlerts.setOnClickListener { selectItem(R.id.nav_alerts) }
            binding.navScan.setOnClickListener { selectItem(R.id.nav_scan) }
            binding.navProfile.setOnClickListener { selectItem(R.id.nav_profile) }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur bottom nav: ${e.message}")
        }
    }

    fun selectItem(itemId: Int) {
        if (currentSelectedItemId == itemId) return

        try {
            // Animate icon colors and indicators
            animateIconColorChange(currentSelectedItemId, itemId)
            animateIndicatorChange(currentSelectedItemId, itemId)

            // Navigate to fragment
            when (itemId) {
                R.id.nav_home -> {
                    Log.d("MainActivity", "Navigation vers HomeFragment")
                    navigateToFragment(HomeFragment())
                }
                R.id.nav_alerts -> {
                    Log.d("MainActivity", "Navigation vers AlertsFragment")
                    navigateToFragment(AlertsFragment())
                }
                R.id.nav_scan -> {
                    Log.d("MainActivity", "Navigation vers ScanFragment")
                    navigateToFragment(ScanFragment())
                }
                R.id.nav_profile -> {
                    Log.d("MainActivity", "Navigation vers ProfileFragment")
                    navigateToFragment(ProfileFragment())
                }
            }

            currentSelectedItemId = itemId
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors de la sélection: ${e.message}", e)
        }
    }

    private fun setSelectedItem(itemId: Int) {
        try {
            // Reset all indicators
            binding.indicatorHome.visibility = View.GONE
            binding.indicatorAlerts.visibility = View.GONE
            binding.indicatorScan.visibility = View.GONE
            binding.indicatorProfile.visibility = View.GONE

            // Reset all icon colors
            val secondaryColor = ContextCompat.getColor(this, R.color.text_secondary)
            binding.icHome.setColorFilter(secondaryColor)
            binding.icAlerts.setColorFilter(secondaryColor)
            binding.icScan.setColorFilter(secondaryColor)
            binding.icProfile.setColorFilter(secondaryColor)

            // Set selected item
            val primaryColor = ContextCompat.getColor(this, R.color.primary_blue)
            when (itemId) {
                R.id.nav_home -> {
                    binding.indicatorHome.visibility = View.VISIBLE
                    binding.icHome.setColorFilter(primaryColor)
                }
                R.id.nav_alerts -> {
                    binding.indicatorAlerts.visibility = View.VISIBLE
                    binding.icAlerts.setColorFilter(primaryColor)
                }
                R.id.nav_scan -> {
                    binding.indicatorScan.visibility = View.VISIBLE
                    binding.icScan.setColorFilter(primaryColor)
                }
                R.id.nav_profile -> {
                    binding.indicatorProfile.visibility = View.VISIBLE
                    binding.icProfile.setColorFilter(primaryColor)
                }
            }

            currentSelectedItemId = itemId
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur setSelectedItem: ${e.message}")
        }
    }

    private fun animateIconColorChange(fromItemId: Int, toItemId: Int) {
        try {
            val fromIcon = when (fromItemId) {
                R.id.nav_home -> binding.icHome
                R.id.nav_alerts -> binding.icAlerts
                R.id.nav_scan -> binding.icScan
                R.id.nav_profile -> binding.icProfile
                else -> null
            }

            val toIcon = when (toItemId) {
                R.id.nav_home -> binding.icHome
                R.id.nav_alerts -> binding.icAlerts
                R.id.nav_scan -> binding.icScan
                R.id.nav_profile -> binding.icProfile
                else -> null
            }

            fromIcon?.animate()?.alpha(0.5f)?.duration = 150
            toIcon?.animate()?.alpha(1f)?.duration = 150
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur animation icon: ${e.message}")
        }
    }

    private fun animateIndicatorChange(fromItemId: Int, toItemId: Int) {
        try {
            val fromIndicator = when (fromItemId) {
                R.id.nav_home -> binding.indicatorHome
                R.id.nav_alerts -> binding.indicatorAlerts
                R.id.nav_scan -> binding.indicatorScan
                R.id.nav_profile -> binding.indicatorProfile
                else -> null
            }

            val toIndicator = when (toItemId) {
                R.id.nav_home -> binding.indicatorHome
                R.id.nav_alerts -> binding.indicatorAlerts
                R.id.nav_scan -> binding.indicatorScan
                R.id.nav_profile -> binding.indicatorProfile
                else -> null
            }

            fromIndicator?.apply {
                animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction {
                        visibility = View.GONE
                        scaleX = 1f
                        scaleY = 1f
                    }
                    .start()
            }

            toIndicator?.apply {
                visibility = View.VISIBLE
                scaleX = 0f
                scaleY = 0f
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur animation indicator: ${e.message}")
        }
    }

    private fun setupFab() {
        try {
            binding.fabQuickScan.apply {
                setOnClickListener {
                    selectItem(R.id.nav_scan)

                    animate().scaleX(0.8f).scaleY(0.8f).setDuration(150)
                        .withEndAction {
                            animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                        }.start()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur FAB: ${e.message}")
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        try {
            if (currentFragment?.javaClass == fragment.javaClass) {
                Log.d("MainActivity", "Fragment déjà affiché, ignorer")
                return
            }

            Log.d("MainActivity", "📱 Remplacement du fragment: ${fragment.javaClass.simpleName}")

            val transaction = supportFragmentManager.beginTransaction()

            // Animation seulement si les ressources existent
            try {
                transaction.setCustomAnimations(
                    R.anim.slide_in_up,
                    R.anim.slide_out_down,
                    R.anim.slide_in_down,
                    R.anim.slide_out_up
                )
            } catch (e: Exception) {
                Log.w("MainActivity", "Animations non disponibles, continuer sans animation")
            }

            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()

            currentFragment = fragment

            Log.d("MainActivity", "✅ Fragment chargé avec succès")
        } catch (e: Exception) {
            Log.e("MainActivity", "💥 Erreur lors du chargement du fragment: ${e.message}", e)
        }
    }

    fun updateAlertBadge(count: Int) {
        try {
            if (count > 0) {
                binding.badgeAlerts.text = if (count > 9) "9+" else count.toString()
                binding.badgeAlerts.visibility = View.VISIBLE
            } else {
                binding.badgeAlerts.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur badge: ${e.message}")
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur navigation login: ${e.message}")
            finish()
        }
    }

    fun logout() {
        Log.d("MainActivity", "🚪 Déconnexion utilisateur")
        SafeLinkApplication.sessionManager.clearSession()
        navigateToLogin()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            if (currentSelectedItemId == R.id.nav_home) {
                finish()
            } else {
                selectItem(R.id.nav_home)
            }
        }
    }
}