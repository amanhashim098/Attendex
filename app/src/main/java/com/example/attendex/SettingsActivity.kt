package com.example.attendex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var profileImage: ShapeableImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var editProfileButton: MaterialButton
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var privacySecurityButton: MaterialButton
    private lateinit var darkModeSwitch: SwitchMaterial
    private lateinit var languageButton: MaterialButton
    private lateinit var pushNotificationsSwitch: SwitchMaterial
    private lateinit var emailNotificationsSwitch: SwitchMaterial
    private lateinit var termsOfServiceButton: MaterialButton
    private lateinit var privacyPolicyButton: MaterialButton
    private lateinit var versionInfo: TextView
    private lateinit var logoutButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var regNo: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Get the regNo from Intent
        regNo = intent.getStringExtra("regNo") ?: ""
        if (regNo.isEmpty()) {
            Toast.makeText(this, "No RegNo found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        initializeViews()

        // Fetch user data from Firestore
        fetchUserData(regNo)

        // Set up click listeners
        setupClickListeners()

        setupBottomNavigation()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        editProfileButton = findViewById(R.id.editProfileButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        privacySecurityButton = findViewById(R.id.privacySecurityButton)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        languageButton = findViewById(R.id.languageButton)
        pushNotificationsSwitch = findViewById(R.id.pushNotificationsSwitch)
        emailNotificationsSwitch = findViewById(R.id.emailNotificationsSwitch)
        termsOfServiceButton = findViewById(R.id.termsOfServiceButton)
        privacyPolicyButton = findViewById(R.id.privacyPolicyButton)
        versionInfo = findViewById(R.id.versionInfo)
        logoutButton = findViewById(R.id.logoutButton)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun fetchUserData(regNo: String) {
        db.collection("Students").document(regNo).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Extract data from Firestore document
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "amanhashim@christuniversity.in"
                    val imagePath = document.getString("photo")
                    val darkModeEnabled = document.getBoolean("darkModeEnabled") ?: false
                    val pushNotificationsEnabled = document.getBoolean("pushNotificationsEnabled") ?: true
                    val emailNotificationsEnabled = document.getBoolean("emailNotificationsEnabled") ?: true

                    // Set data to views
                    profileName.text = name
                    profileEmail.text = email
                    darkModeSwitch.isChecked = darkModeEnabled
                    pushNotificationsSwitch.isChecked = pushNotificationsEnabled
                    emailNotificationsSwitch.isChecked = emailNotificationsEnabled

                    // Load the profile image if available
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
                Log.e("SettingsActivity", "Error fetching user data", exception)
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImageFromStorage(gsPath: String) {
        val storageRef = storage.getReferenceFromUrl(gsPath)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(profileImage)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load image: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("regNo", regNo))
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }
        // Set the profile item as selected
        bottomNavigation.selectedItemId = R.id.nav_settings
    }

    private fun logout() {
        val intent = Intent(this@SettingsActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        finishAffinity()
        startActivity(intent)
    }

    private fun setupClickListeners() {
        editProfileButton.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        changePasswordButton.setOnClickListener {
            Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
        }

        privacySecurityButton.setOnClickListener {
            Toast.makeText(this, "Privacy and Security clicked", Toast.LENGTH_SHORT).show()
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Dark Mode: $isChecked", Toast.LENGTH_SHORT).show()
        }

        languageButton.setOnClickListener {
            Toast.makeText(this, "Language Selection clicked", Toast.LENGTH_SHORT).show()
        }

        pushNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Push Notifications: $isChecked", Toast.LENGTH_SHORT).show()
        }

        emailNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Email Notifications: $isChecked", Toast.LENGTH_SHORT).show()
        }

        termsOfServiceButton.setOnClickListener {
            Toast.makeText(this, "Terms of Service clicked", Toast.LENGTH_SHORT).show()
        }

        privacyPolicyButton.setOnClickListener {
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}