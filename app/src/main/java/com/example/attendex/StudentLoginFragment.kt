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
import com.google.firebase.firestore.FirebaseFirestore

class StudentLoginFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

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

            // Validate input fields
            if (enteredRegNo.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the login function
            loginUser(enteredRegNo, enteredPassword)
        }

        return view
    }

    private fun loginUser(regNo: String, password: String) {
        try {
            // Convert the input to Long (matching Firestore's 'number' type)
            val regNoLong = regNo.toLong()
            val passwordLong = password.toLong()

            // Display entered regno and password for testing
            Toast.makeText(
                activity,
                "Entered RegNo: $regNoLong, Password: $passwordLong",
                Toast.LENGTH_SHORT
            ).show()

            // Query Firestore with the numeric 'regno'
            db.collection("students")
                .whereEqualTo("regno", regNoLong)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(
                            activity,
                            "No student found with RegNo: $regNoLong",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        for (document in documents) {
                            val storedPassword = document.getLong("pass")
                            if (storedPassword == passwordLong) {
                                // Successful login, redirect to ProfileActivity
                                val intent = Intent(activity, ProfileActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                            } else {
                                // Invalid password
                                Toast.makeText(
                                    activity,
                                    "Invalid password for RegNo: $regNoLong",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: NumberFormatException) {
            // Handle invalid input
            Toast.makeText(
                activity,
                "Invalid input. Enter numeric RegNo and Password.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

