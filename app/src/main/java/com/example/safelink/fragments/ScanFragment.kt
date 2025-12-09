package com.example.safelink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.safelink.databinding.FragmentScanBinding

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private var isScanning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScanControls()
    }

    private fun setupScanControls() {
        binding.btnStartScan.setOnClickListener {
            if (!isScanning) {
                startRealTimeScan()
            }
        }

        binding.btnStopScan.setOnClickListener {
            if (isScanning) {
                stopRealTimeScan()
            }
        }
    }

    private fun startRealTimeScan() {
        isScanning = true
        binding.tvScanStatus.text = "Scanning network..."
        binding.btnStartScan.isEnabled = false
        binding.btnStopScan.isEnabled = true
    }

    private fun stopRealTimeScan() {
        isScanning = false
        binding.tvScanStatus.text = "Scan stopped"
        binding.btnStartScan.isEnabled = true
        binding.btnStopScan.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isScanning) {
            stopRealTimeScan()
        }
        _binding = null
    }
}