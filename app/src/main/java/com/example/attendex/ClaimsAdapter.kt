package com.example.attendex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ClaimsAdapter(
    private val onItemClick: (Claim) -> Unit,
    private val onStatusUpdate: (Claim, String) -> Unit
) : ListAdapter<Claim, ClaimsAdapter.ClaimViewHolder>(ClaimDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaimViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_card, parent, false)
        return ClaimViewHolder(view, onItemClick, onStatusUpdate)
    }

    override fun onBindViewHolder(holder: ClaimViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ClaimViewHolder(
        itemView: View,
        private val onItemClick: (Claim) -> Unit,
        private val onStatusUpdate: (Claim, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val studentName: TextView = itemView.findViewById(R.id.studentName)
        private val claimTypeView: TextView = itemView.findViewById(R.id.claimType)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.actionButton)
        private val accentBar: View = itemView.findViewById(R.id.accentBar)

        fun bind(claim: Claim) {
            studentName.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            claimTypeView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            dateText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))

            studentName.text = claim.name
            claimTypeView.text = when (claim.claimType) {
                ClaimType.CO_CURRICULAR -> "Co-Curricular"
                ClaimType.MEDICAL -> "Medical"
                ClaimType.VISA -> "Visa"
            }
            dateText.text = when (claim) {
                is CoCurricularClaim -> claim.date
                is MedicalClaim -> claim.startDate
                is VisaClaim -> claim.startDate
            }

            val accentColor = when (claim.status) {
                ClaimStatus.APPROVED -> ClaimColors.APPROVED
                ClaimStatus.DENIED -> ClaimColors.DENIED
                else -> ClaimColors.PENDING
            }
            accentBar.setBackgroundResource(accentColor)

            when (claim.status) {
                ClaimStatus.PENDING -> {
                    actionButton.text = "Review"
                    actionButton.isEnabled = true
                    actionButton.setOnClickListener { onItemClick(claim) }
                }
                else -> {
                    actionButton.text = claim.status
                    actionButton.isEnabled = false
                }
            }

            itemView.setOnClickListener { onItemClick(claim) }
        }
    }

    private class ClaimDiffCallback : DiffUtil.ItemCallback<Claim>() {
        override fun areItemsTheSame(oldItem: Claim, newItem: Claim): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Claim, newItem: Claim): Boolean {
            return oldItem == newItem
        }
    }
}