package com.example.attendex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.ImageView
import androidx.core.view.GravityCompat

class ProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up the hamburger icon (ActionBarDrawerToggle)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Retrieve data from Intent
        val studentName = intent.getStringExtra("studentName") ?: "N/A"
        val className = intent.getStringExtra("className") ?: "N/A"
        val attendancePercentage = intent.getStringExtra("attendancePercentage") ?: "N/A"
        val aggregatePercentage = intent.getStringExtra("aggregatePercentage") ?: "N/A"
        val totalHours = intent.getStringExtra("totalHours") ?: "N/A"
        val hoursMissed = intent.getStringExtra("hoursMissed") ?: "N/A"

        // Set the data in the respective views
        findViewById<TextView>(R.id.valueName).text = studentName
        findViewById<TextView>(R.id.valueClass).text = className
        findViewById<TextView>(R.id.valueAttendancePercentage).text = attendancePercentage
        findViewById<TextView>(R.id.valueAggregatePercentage).text = aggregatePercentage
        findViewById<TextView>(R.id.valueTotalHours).text = totalHours
        findViewById<TextView>(R.id.valueHoursMissed).text = hoursMissed

        // Handle navigation menu item clicks
        val navView: NavigationView = findViewById(R.id.navigation_view)
        navView.itemIconTintList = null
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> { /* Handle Profile click */ }
                R.id.nav_settings -> { /* Handle Settings click */ }
                R.id.nav_logout -> { /* Handle Logout click */ }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Display the hamburger icon without a toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // Handle click on hamburger icon to open the drawer
        val hamburgerIcon: ImageView = findViewById(R.id.hamburger_icon)
        hamburgerIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    // Ensure the hamburger icon works when clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            Log.d("ProfileActivity", "Hamburger icon clicked")
            true
        } else super.onOptionsItemSelected(item)
    }

    // Close the drawer when back button is pressed
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}