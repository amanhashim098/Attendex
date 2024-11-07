package com.example.attendex

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class TeacherLoginFragment : Fragment(R.layout.fragment_teacher_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val teacherIdInput = view.findViewById<EditText>(R.id.teacherIdInput)
        val teacherPasswordInput = view.findViewById<EditText>(R.id.teacherPasswordInput)
        val loginButton = view.findViewById<Button>(R.id.teacherLoginButton)

        loginButton.setOnClickListener {
            val teacherId = teacherIdInput.text.toString()
            val password = teacherPasswordInput.text.toString()

            if (teacherId.isNotEmpty() && password.isNotEmpty()) {
                // Show a success message
                Toast.makeText(requireContext(), "Teacher Login Successful", Toast.LENGTH_SHORT).show()

                // Create an Intent to navigate to TeacherProfileActivity
                val intent = Intent(requireContext(), TeacherProfileActivity::class.java)
                intent.putExtra("TEACHER_ID", teacherId)  // Pass teacher ID to the profile activity
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
