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

class StudentLoginFragment : Fragment() {

    // Commented out for testing purposes
    // private val validRegNo = "2341603"
    // private val validPassword = "12345678"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_login, container, false)

        val regNoInput: EditText = view.findViewById(R.id.studentIdInput)
        val passwordInput: EditText = view.findViewById(R.id.studentPasswordInput)
        val loginButton: Button = view.findViewById(R.id.studentLoginButton)

        loginButton.setOnClickListener {
            val enteredRegNo = regNoInput.text.toString()
            val enteredPassword = passwordInput.text.toString()

            // Commenting out validation to allow access for testing
            // if (enteredRegNo == validRegNo && enteredPassword == validPassword) {

            // Successful login, redirect to ProfileActivity
            val intent = Intent(requireContext(), ProfileActivity::class.java).apply {
                putExtra("studentName", "Aman Hashim")
                putExtra("className", "3BCA B")
                putExtra("attendancePercentage", "92%")
                putExtra("aggregatePercentage", "73%")
                putExtra("totalHours", "321")
                putExtra("hoursMissed", "21")
            }
            startActivity(intent)

            activity?.finish() // Optionally finish the current activity

            // } else {
            //     // Show error message
            //     Toast.makeText(activity, "Invalid registration number or password", Toast.LENGTH_SHORT).show()
            // }
        }

        return view
    }
}
