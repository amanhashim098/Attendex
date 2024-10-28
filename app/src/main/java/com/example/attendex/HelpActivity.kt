package com.example.attendex

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HelpActivity : AppCompatActivity() {
    private val phoneNumber = "9476589016"
    private val emailId = "support@example.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val phoneButton: MaterialButton = findViewById(R.id.phone_button)
        val emailButton: MaterialButton = findViewById(R.id.email_button)

        phoneButton.setOnClickListener {
            openPhoneApp(phoneNumber)
        }

        emailButton.setOnClickListener {
            openEmailApp(emailId)
        }
    }

    private fun openPhoneApp(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun openEmailApp(emailId: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailId")
            putExtra(Intent.EXTRA_SUBJECT, "Help and Support Request")
        }
        startActivity(Intent.createChooser(intent, "Send Email"))
    }
}
