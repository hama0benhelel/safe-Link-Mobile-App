package com.example.safelink.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safelink.adapters.AlertsAdapter
import com.example.safelink.api.RetrofitClient
import com.example.safelink.databinding.FragmentAlertsBinding
import com.example.safelink.models.Alert
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertsAdapter: AlertsAdapter

    private var currentStatus: String? = null
    private var currentSeverity: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChipFilters()
        loadAlerts()
    }

    private fun setupRecyclerView() {
        alertsAdapter = AlertsAdapter { alert ->
            showAlertDetails(alert)
        }
        binding.rvAlerts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alertsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChipFilters() {
        binding.chipAll.setOnClickListener {
            currentStatus = null
            currentSeverity = null
            updateChipSelection()
            loadAlerts()
        }

        binding.chipOpen.setOnClickListener {
            currentStatus = "open"
            currentSeverity = null
            updateChipSelection()
            loadAlerts()
        }

        binding.chipCritical.setOnClickListener {
            currentSeverity = "critical"
            currentStatus = null
            updateChipSelection()
            loadAlerts()
        }

        binding.chipHigh.setOnClickListener {
            currentSeverity = "high"
            currentStatus = null
            updateChipSelection()
            loadAlerts()
        }
    }

    private fun updateChipSelection() {
        binding.chipAll.isChecked = (currentStatus == null && currentSeverity == null)
        binding.chipOpen.isChecked = (currentStatus == "open")
        binding.chipCritical.isChecked = (currentSeverity == "critical")
        binding.chipHigh.isChecked = (currentSeverity == "high")
    }

    private fun loadAlerts() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAlerts(
                    status = currentStatus,
                    severity = currentSeverity
                )

                if (response.isSuccessful) {
                    val alerts = response.body()?.alerts ?: emptyList()
                    alertsAdapter.submitList(alerts)

                    showEmptyState(alerts.isEmpty())

                    Log.d("AlertsFragment", "✅ ${alerts.size} alertes chargées")
                } else {
                    Log.e("AlertsFragment", "❌ Erreur API: ${response.code()} - ${response.message()}")
                    showError("Erreur du serveur (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e("AlertsFragment", "❌ Erreur réseau ou parsing", e)
                showError("Pas de connexion réseau")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Hide RV while loading
        if (isLoading) {
            binding.rvAlerts.visibility = View.GONE
            binding.tvEmpty?.visibility = View.GONE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAlerts.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlertDetails(alert: Alert) {
        // TODO: Ouvrir un BottomSheet ou naviguer vers AlertDetailFragment
        Toast.makeText(context, "Détails: ${alert.attack_type}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}