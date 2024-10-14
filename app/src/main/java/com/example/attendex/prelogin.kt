package com.example.attendex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup

class prelogin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prelogin)

        val toggleUserType: MaterialButtonToggleGroup = findViewById(R.id.toggleUserType)

        // Load the StudentLoginFragment initially
        loadFragment(StudentLoginFragment())

        // Handle selection change with animation
        toggleUserType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonStudent -> loadFragment(StudentLoginFragment())
                    R.id.buttonTeacher -> loadFragment(TeacherLoginFragment())
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.slide_out_right
            )
            .replace(R.id.container, fragment)
            .commit()
    }
}
