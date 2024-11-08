package com.example.attendex

import com.google.firebase.Timestamp

data class Student(
    val id: String,
    val name: String,
    val className: String,
    val regNo: String
)

// Note: The following claim classes are now in Claim.kt, so we'll remove them from Student.kt
// to avoid redeclaration errors. Instead, we'll add a Student class that can be used
// for other student-related functionalities.

// If you need to use the claim classes, import them from the Claim.kt file:
// import com.example.attendex.CoCurricularClaim
// import com.example.attendex.MedicalClaim
// import com.example.attendex.VisaClaim