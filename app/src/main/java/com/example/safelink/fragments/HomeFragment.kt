package com.example.safelink.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safelink.MainActivity
import com.example.safelink.R
import com.example.safelink.SafeLinkApplication
import com.example.safelink.adapters.AlertsAdapter
import com.example.safelink.databinding.FragmentHomeBinding
import com.example.safelink.models.Alert
import kotlinx.coroutines.*
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertsAdapter: AlertsAdapter
    private var statsAnimationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "🏠 onCreateView DÉBUT")
        try {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            Log.d("HomeFragment", "✅ Binding créé")
            return binding.root
        } catch (e: Exception) {
            Log.e("HomeFragment", "💥 Erreur onCreateView: ${e.message}", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "🏠 onViewCreated DÉBUT")

        try {
            Log.d("HomeFragment", "1️⃣ initAdapter...")
            initAdapter()

            Log.d("HomeFragment", "2️⃣ setupUI...")
            setupUI()

            Log.d("HomeFragment", "3️⃣ setupCards...")
            setupCards()

            Log.d("HomeFragment", "4️⃣ setupRecyclerView...")
            setupRecyclerView()

            Log.d("HomeFragment", "5️⃣ loadData...")
            loadData()

            Log.d("HomeFragment", "6️⃣ setupClickListeners...")
            setupClickListeners()

            Log.d("HomeFragment", "7️⃣ startStatsAnimation...")
            startStatsAnimation()

            Log.d("HomeFragment", "✅ HomeFragment initialisé avec succès")
        } catch (e: Exception) {
            Log.e("HomeFragment", "💥💥💥 CRASH dans onViewCreated 💥💥💥")
            Log.e("HomeFragment", "Message: ${e.message}", e)
        }
    }
    private fun initAdapter() {
        alertsAdapter = AlertsAdapter { alert ->
            try {
                (requireActivity() as MainActivity).selectItem(R.id.nav_alerts)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Erreur navigation: ${e.message}")
            }
        }
    }

    private fun setupUI() {
        try {
            val userName = SafeLinkApplication.sessionManager.getUserName() ?: "Admin"
            binding.tvWelcome.text = "Bonjour, $userName"
            binding.tvDate.text = getCurrentDate()

            binding.tvSystemStatus.apply {
                text = "Sécurisé"
                try {
                    setBackgroundResource(R.drawable.bg_status_green)
                } catch (e: Exception) {
                    Log.w("HomeFragment", "Drawable bg_status_green non trouvé")
                }
                setTextColor(resources.getColor(R.color.white, null))
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur setupUI: ${e.message}")
        }
    }

    private fun getCurrentDate(): String {
        return try {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRENCH)
            val year = calendar.get(Calendar.YEAR)
            "$day $month $year"
        } catch (e: Exception) {
            "Date non disponible"
        }
    }

    private fun setupCards() {
        try {
            listOf(binding.cardStats, binding.cardQuickActions, binding.cardRecentAlerts).forEach { card ->
                card.setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> v.translationZ = 12f
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> v.translationZ = 0f
                    }
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur setupCards: ${e.message}")
        }
    }

    private fun startStatsAnimation() {
        try {
            statsAnimationJob?.cancel()
            statsAnimationJob = CoroutineScope(Dispatchers.Main).launch {
                launch { animateCounter(binding.tvActiveAlerts, 0, 12) }
                launch { animateCounter(binding.tvSuccessfulScans, 0, 156) }
                launch { animateCounter(binding.tvThreatsBlocked, 0, 2400) }
                launch { animateCounter(binding.tvResponseTime, 0, 45) }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur animation stats: ${e.message}")
        }
    }

    private suspend fun animateCounter(textView: android.widget.TextView, start: Int, end: Int) {
        try {
            val duration = 1200L
            val steps = 50
            val increment = (end - start).coerceAtLeast(1) / steps.toFloat()

            var current = start
            repeat(steps) {
                current += increment.toInt()
                textView.text = current.coerceAtMost(end).toString()
                delay(duration / steps)
            }
            textView.text = end.toString()
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur animateCounter: ${e.message}")
            textView.text = end.toString()
        }
    }

    private fun setupRecyclerView() {
        try {
            binding.rvRecentAlerts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = alertsAdapter
                setHasFixedSize(true)
            }

            val recentAlerts = listOf(
                Alert(
                    _id = "1",
                    src_ip = "192.168.1.100",
                    attack_type = "DDoS Attack",
                    severity = "critical",
                    status = "open",
                    timestamp = "2024-01-20T10:30:00Z"
                ),
                Alert(
                    _id = "2",
                    src_ip = "10.0.0.15",
                    attack_type = "Port Scan",
                    severity = "high",
                    status = "open",
                    timestamp = "2024-01-20T09:15:00Z"
                ),
                Alert(
                    _id = "3",
                    src_ip = "172.16.0.5",
                    attack_type = "Suspicious Login",
                    severity = "medium",
                    status = "mitigated",
                    timestamp = "2024-01-20T08:45:00Z"
                )
            )

            alertsAdapter.submitList(recentAlerts)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur setupRecyclerView: ${e.message}")
        }
    }

    private fun loadData() {
        try {
            binding.progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                delay(500)

                binding.progressBar.visibility = View.GONE
                binding.content.visibility = View.VISIBLE

                (activity as? MainActivity)?.updateAlertBadge(3)
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur loadData: ${e.message}")
            binding.progressBar.visibility = View.GONE
            binding.content.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnQuickScan.setOnClickListener {
                (requireActivity() as MainActivity).selectItem(R.id.nav_scan)
            }

            binding.btnViewAlerts.setOnClickListener {
                (requireActivity() as MainActivity).selectItem(R.id.nav_alerts)
            }

            binding.tvViewAll.setOnClickListener {
                (requireActivity() as MainActivity).selectItem(R.id.nav_alerts)
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Erreur setupClickListeners: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "🔴 onDestroyView")
        statsAnimationJob?.cancel()
        _binding = null
    }
}