package com.example.safelink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safelink.R
import com.example.safelink.databinding.ItemAlertBinding
import com.example.safelink.models.Alert

class AlertsAdapter(private val onItemClick: (Alert) -> Unit) :
    ListAdapter<Alert, AlertsAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = getItem(position)
        holder.bind(alert)
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(alert: Alert) {
            binding.apply {
                tvAttackType.text = alert.attack_type.replace("_", " ").uppercase()
                tvSourceIp.text = "De : ${alert.src_ip}"
                tvTimestamp.text = formatTimestamp(alert.timestamp)

                tvSeverity.text = alert.severity.uppercase()

                // Couleur du badge de sévérité
                val severityColor = when (alert.severity.lowercase()) {
                    "critical" -> R.color.red
                    "high" -> R.color.orange
                    "medium" -> R.color.yellow
                    else -> R.color.green
                }
                tvSeverity.setBackgroundColor(root.context.getColor(severityColor))

                // Icône de statut
                val statusIcon = when (alert.status.lowercase()) {
                    "open" -> R.drawable.ic_status_open
                    "mitigated" -> R.drawable.ic_status_mitigated
                    else -> R.drawable.ic_status_ignored
                }
                ivStatus.setImageResource(statusIcon)
            }
        }

        // Optionnel : joli format d'heure
        private fun formatTimestamp(timestamp: String): String {
            // Si tu as une date ISO, tu peux la parser, sinon :
            return "Il y a quelques instants"
        }
        }

class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem == newItem
    }
}
    }