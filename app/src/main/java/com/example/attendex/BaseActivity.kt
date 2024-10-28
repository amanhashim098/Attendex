package com.example.attendex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.attendex.R

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)  // Assuming this layout contains the AppBar

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Handle the back button behavior
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
