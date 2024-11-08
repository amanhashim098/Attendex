package com.example.attendex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var nameText: TextView
    private lateinit var studentId: TextView
    private lateinit var classInfo: TextView
    private lateinit var missedHours: TextView
    private lateinit var claimedHours: TextView
    private lateinit var totalHours: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var regNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        regNo = intent.getStringExtra("regNo") ?: ""
        if (regNo.isEmpty()) {
            Toast.makeText(this, "No RegNo found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupButtons()
        setupBottomNavigation()
        fetchUserData(regNo)
    }

    private fun initializeViews() {
        nameText = findViewById(R.id.nameText)
        studentId = findViewById(R.id.studentId)
        classInfo = findViewById(R.id.classInfo)
        missedHours = findViewById(R.id.missedHours)
        claimedHours = findViewById(R.id.claimedHours)
        totalHours = findViewById(R.id.totalHours)
        profileImageView = findViewById(R.id.profileImage)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.applyMedicalClaim).setOnClickListener {
            startActivity(Intent(this, MedicalActivity::class.java).putExtra("regNo", regNo))
        }
        findViewById<MaterialButton>(R.id.applyCoCurricularClaim).setOnClickListener {
            startActivity(Intent(this, CoCurricularActivity::class.java).putExtra("regNo", regNo))
        }
        findViewById<MaterialButton>(R.id.applyOtherClaim).setOnClickListener {
            startActivity(Intent(this, VisaActivity::class.java).putExtra("regNo", regNo))
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    // Implement proper logout logic
                    logout()
                    true
                }
                R.id.nav_profile -> {
                    // We're already in ProfileActivity
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@ProfileActivity, SettingsActivity::class.java).putExtra("regNo", regNo))
                    true
                }
                else -> false
            }
        }
        // Set the profile item as selected
        bottomNavigation.selectedItemId = R.id.nav_profile
    }

    private fun logout() {
        // Perform any necessary logout operations (e.g., clear user data, tokens, etc.)
        // For example:
        // clearUserData()
        // clearTokens()

        // Create an intent to start MainActivity
        val intent = Intent(this@ProfileActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Finish all activities and start MainActivity
        finishAffinity()
        startActivity(intent)
    }

    private fun fetchUserData(regNo: String) {
        db.collection("Students").document(regNo).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val className = document.getString("class") ?: "N/A"
                    val totHours = document.getLong("total_hours") ?: 0
                    val hoursMissed = document.getLong("hours_miss") ?: 0
                    val hoursClaimed = document.getLong("hours_claim") ?: 0
                    val imagePath = document.getString("photo")

                    nameText.text = "$name!"
                    studentId.text = regNo
                    classInfo.text = className
                    totalHours.text = totHours.toString()
                    missedHours.text = hoursMissed.toString()
                    claimedHours.text = hoursClaimed.toString()

                    if (!imagePath.isNullOrEmpty()) {
                        loadImageFromStorage(imagePath)
                    } else {
                        Toast.makeText(this, "No photo found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Error fetching user data", exception)
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImageFromStorage(gsPath: String) {
        val storageRef = storage.getReferenceFromUrl(gsPath)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(profileImageView)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}