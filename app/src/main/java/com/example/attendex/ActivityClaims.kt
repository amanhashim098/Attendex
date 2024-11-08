package com.example.attendex

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.tabs.TabLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityClaims : AppCompatActivity() {
    private lateinit var claimsRecyclerView: RecyclerView
    private lateinit var claimsAdapter: ClaimsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tabLayout: TabLayout
    private var allClaims: List<Claim> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claims)

        firestore = FirebaseFirestore.getInstance()

        claimsRecyclerView = findViewById(R.id.claimsRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)

        setupAdapter()
        setupRecyclerView()
        setupTabLayout()
        fetchAllClaims()
    }

    private fun setupAdapter() {
        claimsAdapter = ClaimsAdapter(
            onItemClick = { claim -> showClaimDetails(claim) },
            onStatusUpdate = { claim, newStatus -> updateClaimStatus(claim, newStatus) }
        )
    }

    private fun setupRecyclerView() {
        claimsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ActivityClaims)
            adapter = claimsAdapter
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterClaims(ClaimStatus.PENDING)
                    1 -> filterClaims(ClaimStatus.APPROVED)
                    2 -> filterClaims(ClaimStatus.DENIED)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showClaimDetails(claim: Claim) {
        val details = buildString {
            append("Name: ${claim.name}\n")
            append("Class: ${claim.className}\n")
            append("Teacher: ${claim.teacher}\n")
            append("Status: ${claim.status}\n")
            append("Timestamp: ${claim.getFormattedTimestamp()}\n\n")

            when (claim) {
                is CoCurricularClaim -> {
                    append("Type: Co-Curricular\n")
                    append("Activity Type: ${claim.activityType}\n")
                    append("Date: ${claim.date}\n")
                    append("Event: ${claim.event}\n")
                    append("Images Uploaded: ${claim.imagesUploaded}\n")
                    append("Periods Missed: ${claim.periodsMissed.joinToString(", ")}\n")
                    append("Reg No: ${claim.regNo}\n")
                }
                is MedicalClaim -> {
                    append("Type: Medical\n")
                    append("Start Date: ${claim.startDate}\n")
                    append("End Date: ${claim.endDate}\n")
                    append("Reason: ${claim.reason}\n")
                    append("Reg No: ${claim.regNo}\n")
                }
                is VisaClaim -> {
                    append("Type: Visa\n")
                    append("Start Date: ${claim.startDate}\n")
                    append("End Date: ${claim.endDate}\n")
                    append("Reason: ${claim.reason}\n")
                    append("Roll Number: ${claim.rollNumber}\n")
                    append("Flight Ticket Path: ${claim.flightTicketPath}\n")
                    append("Other Document Path: ${claim.otherDocumentPath}\n")
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Claim Details")
            .setMessage(details)
            .setPositiveButton("Approve") { dialog, _ ->
                updateClaimStatus(claim, ClaimStatus.APPROVED)
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                updateClaimStatus(claim, ClaimStatus.DENIED)
                dialog.dismiss()
            }
            .setNeutralButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateClaimStatus(claim: Claim, newStatus: String) {
        val collectionName = when (claim) {
            is CoCurricularClaim -> "coCurricularClaims"
            is MedicalClaim -> "medicalClaims"
            is VisaClaim -> "visaClaims"
        }

        firestore.collection(collectionName)
            .document(claim.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show()
                fetchAllClaims()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAllClaims() {
        val claimsList = mutableListOf<Claim>()

        fun fetchClaimsFromCollection(collectionName: String, claimType: ClaimType) {
            firestore.collection(collectionName)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        try {
                            val claim = createClaimFromFirestore(document.id, document.data, claimType)
                            claimsList.add(claim)
                            Log.d("ActivityClaims", "Added claim: ${claim.name}")
                        } catch (e: Exception) {
                            Log.e("ActivityClaims", "Error creating claim from document: ${document.id}", e)
                        }
                    }
                    if (collectionName == "visaClaims") {
                        allClaims = claimsList
                        filterClaims(ClaimStatus.PENDING)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ActivityClaims", "Error fetching $collectionName", e)
                }
        }

        fetchClaimsFromCollection("coCurricularClaims", ClaimType.CO_CURRICULAR)
        fetchClaimsFromCollection("medicalClaims", ClaimType.MEDICAL)
        fetchClaimsFromCollection("visaClaims", ClaimType.VISA)
    }

    private fun filterClaims(status: String) {
        val filteredClaims = allClaims.filter { it.status == status }
        updateUI(filteredClaims)
    }

    private fun updateUI(claims: List<Claim>) {
        if (claims.isEmpty()) {
            claimsRecyclerView.visibility = View.GONE
            findViewById<View>(R.id.emptyStateView)?.visibility = View.VISIBLE
            Toast.makeText(this, "No claims found", Toast.LENGTH_SHORT).show()
        } else {
            claimsRecyclerView.visibility = View.VISIBLE
            findViewById<View>(R.id.emptyStateView)?.visibility = View.GONE
            claimsAdapter.submitList(claims)
        }
    }
}