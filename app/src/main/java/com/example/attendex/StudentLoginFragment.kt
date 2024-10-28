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
        val db = FirebaseFirestore.getInstance()

        try {
            val regNoLong = regNo.toLong() // Convert regno to Long

            db.collection("Students").document(regNoLong.toString())
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Fetch the stored password from Firestore
                        val storedPassword = document.getLong("pass")

                        // Compare the stored password with the entered one
                        if (storedPassword != null && storedPassword == password.toLong()) {
                            // Successful login, redirect to ProfileActivity with the regNo
                            val intent = Intent(activity, ProfileActivity::class.java)
                            intent.putExtra("regNo", regNoLong.toString()) // Pass regno
                            startActivity(intent)
                            activity?.finish() // Optionally finish current activity
                        } else {
                            Toast.makeText(activity, "Invalid password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            activity,
                            "No student found with this RegNo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: NumberFormatException) {
            Toast.makeText(
                activity,
                "Invalid input. Enter numeric RegNo and Password.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

