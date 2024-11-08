package com.example.attendex

// Import necessary Android and Firebase libraries
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    // Firestore and Storage instances
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Variables to store TextViews and ImageView
    private lateinit var nameText: TextView
    private lateinit var studentId: TextView
    private lateinit var classInfo: TextView
    private lateinit var missedHours: TextView
    private lateinit var claimedHours: TextView
    private lateinit var totalHours: TextView
    private lateinit var profileImageView: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Get the regNo from Intent
        val regNo = intent.getStringExtra("regNo")

        // Check if regNo is passed, if not show error and finish activity
        if (regNo.isNullOrEmpty()) {
            Toast.makeText(this, "No RegNo found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize TextViews and ImageView with correct `findViewById`
        nameText = findViewById(R.id.nameText)
        studentId = findViewById(R.id.studentId)
        classInfo = findViewById(R.id.classInfo)
        missedHours = findViewById(R.id.missedHours)
        claimedHours = findViewById(R.id.claimedHours)
        totalHours = findViewById(R.id.totalHours)
        profileImageView = findViewById(R.id.profileImage)

        // Initialize buttons
        val medicalButton: MaterialButton = findViewById(R.id.applyMedicalClaim)
        val coCurricularButton: MaterialButton = findViewById(R.id.applyCoCurricularClaim)
        val otherClaimButton: MaterialButton = findViewById(R.id.applyOtherClaim)

        // Set click listeners for buttons
        medicalButton.setOnClickListener {
            startActivity(Intent(this, MedicalActivity::class.java))
        }
        coCurricularButton.setOnClickListener {
            startActivity(Intent(this, CoCurricularActivity::class.java))
        }
        otherClaimButton.setOnClickListener {
            startActivity(Intent(this, VisaActivity::class.java))
        }

        // Fetch user data from Firestore
        fetchUserData(regNo)
    }

    private fun fetchUserData(regNo: String) {
        db.collection("Students").document(regNo).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Extract data from Firestore document
                    val name = document.getString("name") ?: "Unknown"
                    val className = document.getString("class") ?: "N/A"
                    val totHours = document.getLong("total_hours") ?: 0
                    val hoursMissed = document.getLong("hours_miss") ?: 0
                    val hoursClaimed = document.getLong("hours_claim") ?: 0
                    val imagePath = document.getString("photo") // Get the image path

                    // Set data to TextViews
                    nameText.text = "$name!"
                    studentId.text = "$regNo"
                    classInfo.text = "$className"
                    totalHours.text = "$totHours"
                    missedHours.text = "$hoursMissed"
                    claimedHours.text = "$hoursClaimed"

                    // Load the profile image if available
                    if (!imagePath.isNullOrEmpty()) {
                        Log.d("ProfileActivity", "Image Path: $imagePath") // Log the image path
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
        // Get the reference for the image from Firebase Storage
        val storageRef = storage.getReferenceFromUrl(gsPath)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // Load the image using Glide into the ImageView
                Glide.with(this).load(uri).into(profileImageView)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("ProfileActivity", "Menu item selected: ${item.title}")
        return super.onOptionsItemSelected(item)
    }
}
