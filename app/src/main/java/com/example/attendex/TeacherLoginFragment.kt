package com.example.attendex

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
                Toast.makeText(requireContext(), "Teacher Login Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
