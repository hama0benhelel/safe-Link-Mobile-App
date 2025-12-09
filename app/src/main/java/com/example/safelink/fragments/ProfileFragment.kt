package com.example.safelink.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.safelink.LoginActivity
import com.example.safelink.databinding.FragmentProfileBinding
import com.example.safelink.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        val name = sessionManager.getUserName() ?: "Utilisateur"
        val email = sessionManager.getUserEmail() ?: "email@example.com"

        binding.apply {
            tvUserName.text = name
            tvUserEmail.text = email
            tvUserRole.text = "Administrateur"
            tvAlertsResolved.text = "128"
            tvScansPerformed.text = "56"
            tvSecurityScore.text = "94%"
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.btnTheme.setOnClickListener {
            val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            )
            binding.tvThemeMode.text = if (isDark) "Désactivé" else "Activé"
        }

        binding.switchNotifications.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}