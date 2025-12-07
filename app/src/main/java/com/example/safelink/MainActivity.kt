package com.example.safelink

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safelink.databinding.ActivityMainBinding
import com.example.safelink.utils.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: SharedPreferencesHelper
    private lateinit var alertsAdapter: AlertsAdapter

    // Données mock pour les alertes
    private val mockAlerts = listOf(
        Alert("1", "192.168.1.10", "DDoS Attack", "critical", "active", "2 min"),
        Alert("2", "10.0.0.15", "Port Scanning", "high", "active", "5 min"),
        Alert("3", "172.16.0.20", "Brute Force", "medium", "mitigated", "10 min"),
        Alert("4", "192.168.1.25", "Malware Detected", "critical", "active", "1 min"),
        Alert("5", "10.0.0.30", "Suspicious Activity", "low", "ignored", "15 min")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPreferencesHelper(this)
        setupUI()
        setupClickListeners()
        loadData()
        startSecurityMonitoring()
    }

    private fun setupUI() {
        // Afficher le nom de l'utilisateur
        val (_, userName) = sharedPref.getUserInfo()
        binding.welcomeText.text = "Bienvenue, $userName"

        // Configurer RecyclerView
        binding.alertsRecyclerView.layoutManager = LinearLayoutManager(this)
        alertsAdapter = AlertsAdapter(mockAlerts) { alert ->
            showAlertDetails(alert)
        }
        binding.alertsRecyclerView.adapter = alertsAdapter

        // Configurer la barre de progression
        binding.securityProgress.progress = 85
    }

    private fun setupClickListeners() {
        // Bouton de déconnexion
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // Bouton notifications
        binding.notificationButton.setOnClickListener {
            showNotifications()
        }

        // Bouton voir toutes les alertes
        binding.viewAllAlertsButton.setOnClickListener {
            navigateToAllAlerts()
        }

        // Carte des logs
        binding.viewLogsCard.setOnClickListener {
            navigateToLogs()
        }

        // Actions rapides
        binding.root.findViewById<com.google.android.material.card.MaterialCardView>(R.id.viewLogsCard).setOnClickListener {
            navigateToLogs()
        }
    }

    private fun loadData() {
        // Mettre à jour les compteurs
        updateStatsCounters()

        // Charger les alertes
        if (mockAlerts.isEmpty()) {
            showEmptyState()
        } else {
            showAlertsList()
            alertsAdapter.updateAlerts(mockAlerts)
        }
    }

    private fun updateStatsCounters() {
        val criticalCount = mockAlerts.count { it.severity == "critical" }
        val highCount = mockAlerts.count { it.severity == "high" }
        val totalCount = mockAlerts.size

        binding.criticalCountText.text = criticalCount.toString()
        binding.highCountText.text = highCount.toString()
        binding.totalCountText.text = totalCount.toString()

        // Mettre à jour le niveau de sécurité
        updateSecurityLevel(criticalCount)
    }

    private fun updateSecurityLevel(criticalCount: Int) {
        val securityLevel = when (criticalCount) {
            0 -> {
                binding.securityLevelText.text = "Élevé"
                binding.securityLevelText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                binding.securityProgress.setIndicatorColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                95
            }
            in 1..2 -> {
                binding.securityLevelText.text = "Moyen"
                binding.securityLevelText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                binding.securityProgress.setIndicatorColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                75
            }
            else -> {
                binding.securityLevelText.text = "Faible"
                binding.securityLevelText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                binding.securityProgress.setIndicatorColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                45
            }
        }
        binding.securityProgress.progress = securityLevel
    }

    private fun showAlertsList() {
        binding.alertsRecyclerView.visibility = android.view.View.VISIBLE
        binding.emptyAlertsLayout.visibility = android.view.View.GONE
    }

    private fun showEmptyState() {
        binding.alertsRecyclerView.visibility = android.view.View.GONE
        binding.emptyAlertsLayout.visibility = android.view.View.VISIBLE
    }

    private fun showAlertDetails(alert: Alert) {
        Toast.makeText(this, "Détails: ${alert.attackType} - ${alert.srcIp}", Toast.LENGTH_SHORT).show()

        // TODO: Naviguer vers l'écran de détails
        // val intent = Intent(this, AlertDetailActivity::class.java)
        // intent.putExtra("ALERT_ID", alert.id)
        // startActivity(intent)
    }

    private fun navigateToAllAlerts() {
        Toast.makeText(this, "Liste complète des alertes", Toast.LENGTH_SHORT).show()
        // TODO: Naviguer vers la liste complète
    }

    private fun navigateToLogs() {
        Toast.makeText(this, "Journal des logs réseau", Toast.LENGTH_SHORT).show()
        // TODO: Naviguer vers les logs
    }

    private fun showNotifications() {
        Toast.makeText(this, "Notifications de sécurité", Toast.LENGTH_SHORT).show()
        // TODO: Afficher les notifications
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Déconnexion") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun performLogout() {
        sharedPref.clearAuthData()
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startSecurityMonitoring() {
        // Simulation de monitoring en temps réel
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(30000) // Mise à jour toutes les 30 secondes
                updateLiveData()
            }
        }
    }

    private fun updateLiveData() {
        // Simulation de mise à jour des données en temps réel
        val random = java.util.Random()
        val newCriticalCount = mockAlerts.count { it.severity == "critical" } + random.nextInt(2)

        // Mettre à jour l'UI
        binding.criticalCountText.text = newCriticalCount.toString()
        updateSecurityLevel(newCriticalCount)

        Log.d("SECURITY_MONITOR", "Données mises à jour - Alertes critiques: $newCriticalCount")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN_ACTIVITY", "Activity reprise - Rechargement des données")
        loadData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Empêcher le retour vers le login sans déconnexion
        moveTaskToBack(true)
    }
}

private fun ProgressBar.setIndicatorColor(color: Int) {}

// Modèle de données amélioré
data class Alert(
    val id: String,
    val srcIp: String,
    val attackType: String,
    val severity: String,
    val status: String,
    val timeAgo: String
)

// Adapter moderne pour RecyclerView
class AlertsAdapter(
    private var alerts: List<Alert>,
    private val onAlertClick: (Alert) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val severityIndicator: android.view.View = view.findViewById(R.id.severityIndicator)
        val alertTitleText: android.widget.TextView = view.findViewById(R.id.alertTitleText)
        val alertIpText: android.widget.TextView = view.findViewById(R.id.alertIpText)
        val alertStatusText: android.widget.TextView = view.findViewById(R.id.alertStatusText)
        val alertTimeText: android.widget.TextView = view.findViewById(R.id.alertTimeText)
        val quickActionButton: com.google.android.material.button.MaterialButton = view.findViewById(R.id.quickActionButton)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): AlertViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]

        holder.alertTitleText.text = alert.attackType
        holder.alertIpText.text = "${alert.srcIp} → ${getTargetPort(alert.attackType)}"
        holder.alertStatusText.text = alert.status.uppercase()
        holder.alertTimeText.text = alert.timeAgo

        // Couleur selon la sévérité
        val (severityColor, statusColor) = getAlertColors(alert.severity, alert.status)
        holder.severityIndicator.setBackgroundColor(severityColor)
        holder.alertStatusText.setBackgroundColor(statusColor)

        // Configurer le bouton d'action rapide
        holder.quickActionButton.setOnClickListener {
//            handleQuickAction(alert)
        }

        holder.itemView.setOnClickListener {
            onAlertClick(alert)
        }
    }

    private fun getAlertColors(severity: String, status: String): Pair<Int, Int> {
        val severityColor = when (severity) {
            "critical" -> Color.parseColor("#E53935")
            "high" -> Color.parseColor("#FF9800")
            "medium" -> Color.parseColor("#FFC107")
            else -> Color.parseColor("#4CAF50")
        }

        val statusColor = when (status) {
            "active" -> Color.parseColor("#E53935")
            "mitigated" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#FF9800")
        }

        return Pair(severityColor, statusColor)
    }

    private fun getTargetPort(attackType: String): String {
        return when (attackType) {
            "DDoS Attack" -> "Port 80"
            "Port Scanning" -> "Multiple Ports"
            "Brute Force" -> "Port 22"
            "Malware Detected" -> "Port 443"
            else -> "Various"
        }
    }

//    private fun handleQuickAction(alert: Alert) {
//        when (alert.status) {
//            "active" -> {
//                // Action: Bloquer l'IP
//                Toast.makeText(holder.itemView.context, "IP ${alert.srcIp} bloquée", Toast.LENGTH_SHORT).show()
//            }
//            "mitigated" -> {
//                // Action: Voir les détails
//                Toast.makeText(holder.itemView.context, "Détails de l'attaque", Toast.LENGTH_SHORT).show()
//            }
//            else -> {
//                // Action: Ignorer
//                Toast.makeText(holder.itemView.context, "Alerte ignorée", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    override fun getItemCount() = alerts.size

    fun updateAlerts(newAlerts: List<Alert>) {
        this.alerts = newAlerts
        notifyDataSetChanged()
    }
}