package com.example.attendex

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class TeacherProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_teacher_profile)

        // Find the "Pending Claims" button by ID
        val pendingClaimsButton = findViewById<MaterialButton>(R.id.pendingClaimsButton)

        // Set an OnClickListener to navigate to ActivityClaims
        pendingClaimsButton.setOnClickListener {
            // Intent to navigate to ActivityClaims
            val intent = Intent(this, ActivityClaims::class.java)
            startActivity(intent)
        }
    }
}
