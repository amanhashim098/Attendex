package com.example.attendex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.button.MaterialButton

class ProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize buttons
        val medicalButton: MaterialButton = findViewById(R.id.applyMedicalClaim)
        val coCurricularButton: MaterialButton = findViewById(R.id.applyCoCurricularClaim)
        val otherClaimButton: MaterialButton = findViewById(R.id.applyOtherClaim)

        // Set click listeners for buttons
        medicalButton.setOnClickListener {
            val intent = Intent(this, MedicalActivity::class.java)
            startActivity(intent)
        }

        coCurricularButton.setOnClickListener {
            val intent = Intent(this, CoCurricularActivity::class.java)
            startActivity(intent)
        }

        otherClaimButton.setOnClickListener {
            val intent = Intent(this, VisaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("ProfileActivity", "Menu item selected: ${item.title}")
        return super.onOptionsItemSelected(item)
    }
}
