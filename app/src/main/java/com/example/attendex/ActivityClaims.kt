package com.example.attendex

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.tabs.TabLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bumptech.glide.Glide
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

class ActivityClaims : AppCompatActivity() {
    private lateinit var claimsRecyclerView: RecyclerView
    private lateinit var claimsAdapter: ClaimsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var tabLayout: TabLayout
    private var allClaims: List<Claim> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claims)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_claim_details, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.claimImageView)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.claimDetailsTextView)

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
                    append("Hours Claimed: ${claim.hoursClaimed}\n")
                    append("Hours Missed: ${claim.hoursMissed}\n")
                    append("Pass: ${claim.pass}\n")
                    append("Total Hours: ${claim.totalHours}\n")

                    loadCoCurricularImages(claim.id, imageView)
                }
                is MedicalClaim -> {
                    append("Type: Medical\n")
                    append("Start Date: ${claim.startDate}\n")
                    append("End Date: ${claim.endDate}\n")
                    append("Reason: ${claim.reason}\n")
                    append("Reg No: ${claim.regNo}\n")

                    loadMedicalImage(claim.id, imageView)
                }
                is VisaClaim -> {
                    append("Type: Visa\n")
                    append("Start Date: ${claim.startDate}\n")
                    append("End Date: ${claim.endDate}\n")
                    append("Reason: ${claim.reason}\n")
                    append("Roll Number: ${claim.rollNumber}\n")

                    loadVisaImage(claim, imageView)
                }
            }
        }

        detailsTextView.text = details

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
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

    private fun loadCoCurricularImages(claimId: String, imageView: ImageView) {
        val ccImageRef = storage.reference
            .child("coCurricularClaims")
            .child("${claimId}_cc.jpg")

        ccImageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(imageView)
            imageView.visibility = View.VISIBLE

            imageView.setOnClickListener {
                showFullSizeImage(uri.toString())
            }
        }.addOnFailureListener { exception ->
            val odImageRef = storage.reference
                .child("coCurricularClaims")
                .child("${claimId}_od.jpg")

            odImageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(imageView)
                imageView.visibility = View.VISIBLE

                imageView.setOnClickListener {
                    showFullSizeImage(uri.toString())
                }
            }.addOnFailureListener { innerException ->
                Log.e("ActivityClaims", "Error loading both CC and OD images", innerException)
                imageView.visibility = View.GONE
                Toast.makeText(this, "No images found for this claim", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMedicalImage(claimId: String, imageView: ImageView) {
        storage.reference.child("medicalClaims").listAll()
            .addOnSuccessListener { result ->
                val matchingFolder = result.prefixes.find { it.name == claimId }
                if (matchingFolder != null) {
                    matchingFolder.listAll().addOnSuccessListener { folderContents ->
                        if (folderContents.items.isNotEmpty()) {
                            folderContents.items.first().downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(this)
                                    .load(uri)
                                    .into(imageView)
                                imageView.visibility = View.VISIBLE

                                imageView.setOnClickListener {
                                    showFullSizeImage(uri.toString())
                                }
                            }
                        } else {
                            imageView.visibility = View.GONE
                            Toast.makeText(this, "No images found in claim folder", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    imageView.visibility = View.GONE
                    Toast.makeText(this, "No folder found for this claim", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ActivityClaims", "Error listing medical claim folders", exception)
                imageView.visibility = View.GONE
                Toast.makeText(this, "Failed to load medical claim image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadVisaImage(claim: VisaClaim, imageView: ImageView) {
        if (claim.flightTicketPath.isNotEmpty()) {
            val flightTicketRef = storage.reference.child(claim.flightTicketPath)
            flightTicketRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(imageView)
                imageView.visibility = View.VISIBLE

                imageView.setOnClickListener {
                    showFullSizeImage(uri.toString())
                }
            }.addOnFailureListener { exception ->
                if (claim.otherDocumentPath.isNotEmpty()) {
                    val otherDocRef = storage.reference.child(claim.otherDocumentPath)
                    otherDocRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(imageView)
                        imageView.visibility = View.VISIBLE

                        imageView.setOnClickListener {
                            showFullSizeImage(uri.toString())
                        }
                    }.addOnFailureListener { innerException ->
                        Log.e("ActivityClaims", "Error loading both visa documents", innerException)
                        imageView.visibility = View.GONE
                        Toast.makeText(this, "No images found for this claim", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ActivityClaims", "Error loading visa flight ticket", exception)
                    imageView.visibility = View.GONE
                    Toast.makeText(this, "Failed to load flight ticket", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            imageView.visibility = View.GONE
            Toast.makeText(this, "No flight ticket attached", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFullSizeImage(imageUrl: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_full_size_image, null)
        val fullSizeImageView = dialogView.findViewById<ImageView>(R.id.fullSizeImageView)

        Glide.with(this)
            .load(imageUrl)
            .into(fullSizeImageView)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
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